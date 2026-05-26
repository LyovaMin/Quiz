package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.*;
import by.lyofchik.quiz.Model.Enum.BonusType;
import by.lyofchik.quiz.Model.Mapper.AnalyticsMapper;
import by.lyofchik.quiz.Repository.*;
import by.lyofchik.quiz.Utils.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class GameService {
    private LobbyRepository lobbyRepository;
    private AnswerRepository answerRepository;
    private GameMemberRepository gameMemberRepository;
    private QuestionStatRepository questionStatRepository;
    private QuizAttemptRepository quizAttemptRepository;
    private QuestionRepository questionRepository;
    private AnalyticsMapper analyticsMapper;

    private static final int BONUS_CHANCE_THRESHOLD = 4; // ???? 1 ?? 4-6

    public Response answer(GameRq request, int lobbyId) {
        log.info("game.answer called: lobbyId={}, userId={}, questionId={}", lobbyId, request.getUserId(), request.getQuestionId());
        Optional<Lobby> lobbyOpt = lobbyRepository.findById(lobbyId);
        Answer answer = answerRepository.findCorrectByQuestionId(request.getQuestionId());
        Optional<GameMember> memberOpt = gameMemberRepository.findById(request.getUserId());

        if (lobbyOpt.isEmpty() || answer == null || memberOpt.isEmpty()) {
            log.warn("Game data not found: lobbyExists={}, answerExists={}, memberExists={}", lobbyOpt.isPresent(), answer != null, memberOpt.isPresent());
            return Response.error("404", "Game data not found");
        }

        Lobby lobby = lobbyOpt.get();
        GameMember member = memberOpt.get();
        Question question = answer.getQuestion();

        // ????????? ?????, ???? ?? ??? ???????????
        BonusType usedBonus = request.getActiveBonus();
        if (usedBonus != null) {
            member.setAvailableBonus(null); // ????? ??????? ????? ????? ?????????????
        }

        // ?????????? ??????, ???? ??????????? ??????????????? ?????
        if (usedBonus == BonusType.SKIP_QUESTION) {
            gameMemberRepository.save(member);
            return Response.success(Map.of("member", member, "correct", false, "points", 0));
        }

        boolean correct = isCorrect(answer, request.getAnswer());
        int points = countPoints(question, request, correct, usedBonus);

        // ?????? ??? ?????? "?????? ????"
        if (!correct && usedBonus == BonusType.SECOND_CHANCE) {
            gameMemberRepository.save(member); // ?????????, ??? ????? ???????????
            return Response.success(Map.of("member", member, "correct", false, "points", 0, "secondChance", true));
        }
        
        // ?????? ??? ?????? "?????????? ?????"
        if (!correct && usedBonus == BonusType.STREAK_SAVER) {
            correct = true; // ??????? ????? ?????????? ??? ?????????? ?????, ?? ???? ?? ?????????
            points = 0;
        }

        float progress = countProgress(lobby.getQuiz());
        member.setProgress((member.getProgress() == null ? 0f : member.getProgress()) + progress);
        member.setScore((member.getScore() == null ? 0 : member.getScore()) + points);
        member.setLastUpdate(Instant.now());

        // ???????? ?????? ????? ?????, ???? ? ?????? ??? ???
        tryAwardBonus(member);

        QuestionStat stat = buildQuestionStat(request, answer, correct);
        questionStatRepository.save(stat);
        gameMemberRepository.save(member);
        log.info("Saved member {} score={}, newBonus={}", member.getId(), member.getScore(), member.getAvailableBonus());

        return Response.success(Map.of("member", member, "correct", correct, "points", points));
    }

    private void tryAwardBonus(GameMember member) {
        if (member.getAvailableBonus() == null) {
            int randomNum = ThreadLocalRandom.current().nextInt(1, 7); // ?? 1 ?? 6
            if (randomNum >= BONUS_CHANCE_THRESHOLD) {
                BonusType[] allBonuses = BonusType.values();
                BonusType awardedBonus = allBonuses[new Random().nextInt(allBonuses.length)];
                member.setAvailableBonus(awardedBonus);
                log.info("Awarded bonus {} to member {}", awardedBonus, member.getId());
            }
        }
    }

    private int countPoints(Question question, GameRq request, boolean correct, BonusType usedBonus) {
        if (!correct) {
            return 0;
        }
        int points = question.getPoints() != 0 ? question.getPoints() : question.getType().getDefaultPoints();
        float multiplier = 1f;

        if (request.getStartedAt() != null && request.getCompletedAt() != null) {
            long answeredFor = Duration.between(request.getStartedAt(), request.getCompletedAt()).getSeconds();
            multiplier *= Constants.getGrade(answeredFor);
        }

        if (usedBonus == BonusType.DOUBLING) {
            multiplier *= 2;
        }
        if (usedBonus == BonusType.BONUS_POINTS) {
            points += 50; // ????????? ????????????? 50 ?????
        }

        return Math.round(points * multiplier);
    }

    // ... (????????? ?????? ??? ?????????)
    public Response answer(GameRq request) {
        Answer answer = answerRepository.findCorrectByQuestionId(request.getQuestionId());
        Question question = questionRepository.findById(request.getQuestionId());
        if (answer == null || question == null) {
            return Response.error("404", "Question or answer not found");
        }
        boolean correct = isCorrect(answer, request.getAnswer());
        int points = countPoints(question, request, correct, request.getActiveBonus());
        QuestionStat stat = buildQuestionStat(request, answer, correct);
        questionStatRepository.save(stat);
        return Response.success(Map.of("stat", stat, "points", points, "correct", correct));
    }

    public Response startAttempt(Integer quizId, Integer userId) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quizId);
        attempt.setUser(userId);
        attempt.setScore(0);
        attempt.setStartedAt(Instant.now());
        quizAttemptRepository.save(attempt);
        return Response.success(attempt);
    }

    public Response finishAttempt(Integer quizId, Integer userId, Integer score) {
        QuizAttempt attempt = quizAttemptRepository
                .findFirstByQuizAndUserAndCompletedAtIsNullOrderByStartedAtDesc(quizId, userId)
                .orElse(null);
        if (attempt == null) {
            return Response.error("404", "Active attempt not found");
        }
        attempt.setScore(score == null ? 0 : score);
        attempt.setCompletedAt(Instant.now());
        quizAttemptRepository.save(attempt);
        return Response.success(attempt);
    }

    private float countProgress(Quiz quiz){
        return (float) 1 / quiz.getQuestions().size();
    }

    private QuestionStat buildQuestionStat(GameRq request, Answer correctAnswer, boolean correct) {
        QuestionStat stat = analyticsMapper.toQuestionStat(request, correct);
        Answer selectedAnswer = request.getAnswer() == null ? null : answerRepository
                .findByQuestionIdAndText(request.getQuestionId(), request.getAnswer())
                .stream()
                .findFirst()
                .orElse(null);
        stat.setAnswer(selectedAnswer != null ? selectedAnswer.getId() : (correct ? correctAnswer.getId() : null));
        return stat;
    }

    private boolean isCorrect(Answer correctAnswer, String answerText) {
        if (correctAnswer == null || answerText == null) {
            return false;
        }
        return Objects.equals(correctAnswer.getText().trim().toLowerCase(), answerText.trim().toLowerCase());
    }
}
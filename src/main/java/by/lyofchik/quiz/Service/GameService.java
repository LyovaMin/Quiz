package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.*;
import by.lyofchik.quiz.Model.Enum.BonusType;
import by.lyofchik.quiz.Model.Mapper.AnalyticsMapper;
import by.lyofchik.quiz.Repository.*;
import by.lyofchik.quiz.Utils.Constants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class GameService {
    private LobbyRepository lobbyRepository;
    private AnswerRepository answerRepository;
    private GameMemberRepository gameMemberRepository;
    private QuestionStatRepository questionStatRepository;
    private QuizAttemptRepository quizAttemptRepository;
    private QuestionRepository questionRepository;
    private AnalyticsMapper analyticsMapper;

    public Response answer(GameRq request, int lobbyId) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        Answer answer = answerRepository.findCorrectByQuestionId(request.getQuestionId());
        GameMember member = gameMemberRepository.findById(request.getUserId());
        if (lobby == null || answer == null || member == null) {
            return Response.error("404", "Game data not found");
        }
        Question question = answer.getQuestion();
        float progress = countProgress(lobby.getQuiz());
        member.setProgress((member.getProgress() == null ? 0f : member.getProgress()) + progress);
        boolean correct = isCorrect(answer, request.getAnswer());

        int points = countPoints(question, request, correct);
        member.setScore((member.getScore() == null ? 0 : member.getScore()) + points);
        member.setLastUpdate(Instant.now());
        QuestionStat stat = buildQuestionStat(request, answer, correct);
        questionStatRepository.save(stat);
        gameMemberRepository.save(member);
        return Response.success(member);
    }

    public Response answer(GameRq request) {
        Answer answer = answerRepository.findCorrectByQuestionId(request.getQuestionId());
        Question question = questionRepository.findById(request.getQuestionId());
        if (answer == null || question == null) {
            return Response.error("404", "Question or answer not found");
        }
        boolean correct = isCorrect(answer, request.getAnswer());
        int points = countPoints(question, request, correct);
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

    private int countPoints(Question question, GameRq request, boolean correct) {
        if (!correct) {
            return 0;
        }
        int points = question.getPoints() != 0 ? question.getPoints() : question.getType().getDefaultPoints();

        if (request.getActiveBonus() == BonusType.BONUS_POINTS) {
            return points + points / 2;
        }
        if (request.getStartedAt() == null || request.getCompletedAt() == null) {
            return points;
        }
        long answeredFor = Duration.between(request.getStartedAt(), request.getCompletedAt()).getSeconds();
        return Math.round(points * Constants.getGrade(answeredFor));
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

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
import java.util.Objects;

@Service
@AllArgsConstructor
public class GameService {
    private LobbyRepository lobbyRepository;
    private AnswerRepository answerRepository;
    private GameMemberRepository gameMemberRepository;
    private QuestionStatRepository questionStatRepository;
    private QuizAttemptRepository quizAttemptRepository;
    private AnalyticsMapper analyticsMapper;

    public Response answer(GameRq request, int lobbyId) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        Answer answer = answerRepository.findCorrectByQuestionId(request.getQuestionId());
        Question question = answer.getQuestion();
        GameMember member = gameMemberRepository.findById(request.getUserId());
        if (lobby == null || answer == null || member == null) {
            return Response.error("404", "Game data not found");
        }
        float progress = countProgress(lobby.getQuiz());
        member.setProgress(member.getProgress() + progress);
        boolean correct = Objects.equals(answer.getText(), request.getAnswer());

        int points = countPoints(question, request, correct);
        member.setScore(member.getScore() + points);
        member.setLastUpdate(Instant.now());
        QuestionStat stat = analyticsMapper.toQuestionStat(request, correct);
        stat.setAnswer(answer.getId());
        questionStatRepository.save(stat);
        gameMemberRepository.save(member);
        return Response.success(member);
    }

    public Response answer(GameRq request) {
        Answer answer = answerRepository.findCorrectByQuestionId(request.getQuestionId());
        if (answer == null) {
            return Response.error("404", "Answer not found");
        }
        boolean correct = Objects.equals(answer.getText(), request.getAnswer());
        QuestionStat stat = analyticsMapper.toQuestionStat(request, correct);
        stat.setAnswer(answer.getId());
        questionStatRepository.save(stat);
        return Response.success(stat);
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
        int points = question.getPoints() != 0 ? question.getPoints() : question.getType().getDefaultPoints();

        if (!correct && request.getActiveBonus() == BonusType.BONUS_POINTS) {
            return points / 2;
        }
        if (request.getStartedAt() == null || request.getCompletedAt() == null) {
            return correct ? points : 0;
        }
        long decidedFor = Duration.between(request.getStartedAt(), request.getCompletedAt()).getSeconds();
        points = (int) (points * countBonus(decidedFor));
        return correct ? points : 0;
    }

    private float countBonus(long decidedFor) {
        long grade = 10 - decidedFor;
        return Constants.getGrade(grade);
    }

    private float countProgress(Quiz quiz){
        return (float) 1 / quiz.getQuestions().size();
    }
}

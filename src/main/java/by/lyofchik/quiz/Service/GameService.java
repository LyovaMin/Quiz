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
        float progress = countProgress(lobby.getQuiz());
        member.setProgress(member.getProgress() + progress);
        boolean correct = Objects.equals(answer.getText(), request.getAnswer());

        int points = countPoints(question, request, correct);
        member.setScore(member.getScore() + points);
        QuestionStat stat = analyticsMapper.toQuestionStat(request, correct);
        questionStatRepository.save(stat);
        return Response.success(member);
    }

    private int countPoints(Question question, GameRq request, boolean correct) {
        int points = question.getPoints() != 0 ? question.getPoints() : question.getType().getDefaultPoints();

        if (!correct && request.getActiveBonus().equals(BonusType.BONUS_POINTS)) {
            return points / 2;
        }
        long decidedFor = Duration.between(request.getStartedAt(), request.getCompletedAt()).getSeconds();
        points = (int) (points * countBonus(decidedFor));
        return points;
    }

    private float countBonus(long decidedFor) {
        long grade = 10 - decidedFor;
        return Constants.getGrade(grade);
    }

    private float countProgress(Quiz quiz){
        return (float) 1 / quiz.getQuestions().size();
    }
}

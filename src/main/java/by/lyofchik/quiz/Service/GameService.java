package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.*;
import by.lyofchik.quiz.Model.Enum.BonusType;
import by.lyofchik.quiz.Repository.AnswerRepository;
import by.lyofchik.quiz.Repository.GameMemberRepository;
import by.lyofchik.quiz.Repository.LobbyRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

@Service
public class GameService {
    LobbyRepository lobbyRepository;
    AnswerRepository answerRepository;
    GameMemberRepository gameMemberRepository;
    NavigableMap<Long, Float> bonuses = new TreeMap<>();

    @PostConstruct
    void init() {
        bonuses.put(0L, 1f);
        bonuses.put(2L, 1.1f);
        bonuses.put(4L, 1.2f);
        bonuses.put(6L, 1.3f);
        bonuses.put(8L, 1.4f);
        bonuses.put(10L, 1.5f);
    }

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
        return bonuses.higherEntry(grade).getValue();
    }

    private float countProgress(Quiz quiz){
        return (float) 1 / quiz.getQuestions().size();
    }
}

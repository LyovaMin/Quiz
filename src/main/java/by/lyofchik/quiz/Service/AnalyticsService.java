package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Repository.QuestionStatRepository;
import by.lyofchik.quiz.Repository.QuizAttemptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AnalyticsService {
    QuestionStatRepository questionStatRepository;
    QuizAttemptRepository quizAttemptRepository;

    public Response leaderboard() {
        return null;
    }

    public Response leaderboard(Integer id) {
        return null;
    }

    public Response myStat(String username) {
        return null;
    }
}

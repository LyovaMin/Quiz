package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Topic;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Repository.QuestionStatRepository;
import by.lyofchik.quiz.Repository.QuizAttemptRepository;
import by.lyofchik.quiz.Repository.TopicRepository;
import by.lyofchik.quiz.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class AnalyticsService {
    QuestionStatRepository questionStatRepository;
    QuizAttemptRepository quizAttemptRepository;
    UserRepository userRepository;
    TopicRepository topicRepository;

    public Response leaderboard(String period, Integer topicId) {
        Instant fromDate = resolveFromDate(period);
        List<Map<String, Object>> rows = quizAttemptRepository.leaderboard(fromDate, topicId, PageRequest.of(0, 5))
                .stream()
                .map(this::toLeaderboardRow)
                .toList();
        return Response.success(rows);
    }

    public Response leaderboard(Integer id) {
        return leaderboard("all", id);
    }

    public Response myStat(String username, String period, Integer topicId) {
        return userRepository.findByLogin(username)
                .map(user -> Response.success(buildProfile(user, period, topicId)))
                .orElseGet(() -> Response.error("404", "User not found"));
    }

    public Response topics() {
        return Response.success(topicRepository.findAll().stream()
                .map(this::toTopicRow)
                .toList());
    }

    private Map<String, Object> buildProfile(User user, String period, Integer topicId) {
        Instant fromDate = resolveFromDate(period);
        Object[] stats = normalizeStats(questionStatRepository.getUserAnswerStats(user.getId(), fromDate, topicId));
        long total = toLong(stats[0]);
        long correct = toLong(stats[1]);
        long score = quizAttemptRepository.getUserScore(user.getId(), fromDate, topicId);

        return Map.of(
                "user", Map.of(
                        "id", user.getId(),
                        "login", user.getLogin(),
                        "avatar", user.getImageUrl() == null ? "" : user.getImageUrl()
                ),
                "stats", Map.of(
                        "totalAnswers", total,
                        "correctAnswers", correct,
                        "correctPercent", total == 0 ? 0 : Math.round((correct * 1000.0) / total) / 10.0,
                        "score", score
                ),
                "byTopic", questionStatRepository.getUserAnswerStatsByTopic(user.getId(), fromDate).stream()
                        .map(this::toTopicStatRow)
                        .toList()
        );
    }

    private Instant resolveFromDate(String period) {
        if ("day".equalsIgnoreCase(period)) {
            return Instant.now().minus(1, ChronoUnit.DAYS);
        }
        if ("week".equalsIgnoreCase(period)) {
            return Instant.now().minus(7, ChronoUnit.DAYS);
        }
        return null;
    }

    private Map<String, Object> toLeaderboardRow(Object[] row) {
        return Map.of(
                "userId", row[0],
                "login", row[1],
                "avatar", row[2] == null ? "" : row[2],
                "score", toLong(row[3])
        );
    }

    private Map<String, Object> toTopicRow(Topic topic) {
        return Map.of(
                "id", topic.getId(),
                "name", topic.getName().name()
        );
    }

    private Map<String, Object> toTopicStatRow(Object[] row) {
        long total = toLong(row[2]);
        long correct = toLong(row[3]);
        return Map.of(
                "topicId", row[0],
                "topic", row[1].toString(),
                "totalAnswers", total,
                "correctAnswers", correct,
                "correctPercent", total == 0 ? 0 : Math.round((correct * 1000.0) / total) / 10.0
        );
    }

    private Object[] normalizeStats(Object rawStats) {
        if (rawStats instanceof Object[] values && values.length == 1 && values[0] instanceof Object[] nested) {
            return nested;
        }
        return (Object[]) rawStats;
    }

    private long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}

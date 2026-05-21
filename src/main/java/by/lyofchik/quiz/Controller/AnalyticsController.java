package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import by.lyofchik.quiz.Service.AnalyticsService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analitics")
@AllArgsConstructor
public class AnalyticsController {
    AnalyticsService analyticsService;
    AuthService authService;

    @GetMapping("/leaderboard/global")
    public Response leaderboardGlobal(@RequestParam(defaultValue = "all") String period,
                                      @RequestParam(required = false) Integer topicId) {
        return analyticsService.leaderboard(period, topicId);
    }

    @GetMapping("/leaderboard/quiz/{id}")
    public Response leaderboardQuiz(@PathVariable Integer id) {
        return analyticsService.leaderboard(id);
    }

    @GetMapping("/my-stat")
    public Response myStat(@RequestParam String username,
                           @RequestParam(defaultValue = "all") String period,
                           @RequestParam(required = false) Integer topicId) {
        return analyticsService.myStat(username, period, topicId);
    }

    @GetMapping("/topics")
    public Response topics() {
        return analyticsService.topics();
    }

    @GetMapping("/quiz/{id}")
    public Response quizStat(@PathVariable Integer id, HttpSession session) {
        return analyticsService.quizStat(id, authService.currentUserOrNull(session));
    }
}

package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AnalyticsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analitics")
@AllArgsConstructor
public class AnalyticsController {
    AnalyticsService analyticsService;

    @GetMapping("/leaderboard/global")
    public Response leaderboardGlobal() {
        return analyticsService.leaderboard();
    }

    @GetMapping("/leaderboard/quiz/{id}")
    public Response leaderboardQuiz(@PathVariable Integer id) {
        return analyticsService.leaderboard(id);
    }

    @GetMapping("/my-stat")
    public Response myStat(@RequestParam String username) {
        return analyticsService.myStat(username);
    }
}

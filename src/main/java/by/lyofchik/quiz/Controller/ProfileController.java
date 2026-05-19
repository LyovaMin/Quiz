package by.lyofchik.quiz.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProfileController {
    @GetMapping("/profile")
    public ModelAndView profile() {
        return new ModelAndView("forward:/html/profile.html");
    }

    @GetMapping("/leaderboard")
    public ModelAndView leaderboard() {
        return new ModelAndView("forward:/html/leaderboard.html");
    }
}

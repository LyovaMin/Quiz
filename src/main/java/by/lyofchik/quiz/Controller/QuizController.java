package by.lyofchik.quiz.Controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/quiz")
@AllArgsConstructor
public class QuizController {

    @GetMapping("/{id}")
    public ModelAndView getQuizPage(@PathVariable int id) {
        return new ModelAndView("forward:/html/quiz.html");
    }
}
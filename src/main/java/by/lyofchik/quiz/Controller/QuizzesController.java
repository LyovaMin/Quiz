package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import by.lyofchik.quiz.Service.ModificationService;
import by.lyofchik.quiz.Service.QuizzesService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/quizzes")
@AllArgsConstructor
@Slf4j
public class QuizzesController {
    private QuizzesService quizzesService;
    private ModificationService modificationService;
    private AuthService authService;

    @GetMapping
    public ModelAndView getQuizzesPage() {
        return new ModelAndView("forward:/html/quizzes.html");
    }

    @GetMapping("/edit")
    public ModelAndView getEditQuizPage() {
        return new ModelAndView("forward:/html/edit-quiz.html");
    }

    @PostMapping("/api/all")
    @ResponseBody
    public Response getQuizzes(@RequestBody QuizzesRq request) {
        log.info("QuizzesController.getQuizzes - {}", request);
        return quizzesService.allQuizzes(request);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Response getQuiz(@PathVariable int id) {
        log.info("QuizzesController.getQuiz - {}", id);
        return quizzesService.getQuiz(id);
    }

    @PostMapping("/api/create")
    @ResponseBody
    public Response createQuiz(@RequestBody QuizRq request, HttpSession session) {
        log.info("QuizzesController.createQuiz - {}", request);
        return modificationService.createQuiz(request, authService.currentUserOrNull(session));
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public Response deleteQuiz(@PathVariable int id, HttpSession session) {
        log.info("QuizzesController.deleteQuiz - {}", id);
        return modificationService.deleteQuiz(id, authService.currentUserOrNull(session));
    }

    @PostMapping("/api/update/{id}")
    @ResponseBody
    public Response updateQuiz(@PathVariable int id, @RequestBody QuizRq request, HttpSession session) {
        log.info("QuizzesController.updateQuiz {} - {}", id, request);
        return modificationService.updateQuiz(id, request, authService.currentUserOrNull(session));
    }
}

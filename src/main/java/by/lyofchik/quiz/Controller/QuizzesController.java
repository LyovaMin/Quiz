package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.QuizzesService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@AllArgsConstructor
public class QuizzesController {
    private QuizzesService quizzesService;

    @GetMapping
    public Response getQuizzes(@RequestBody QuizzesRq request) {
        return quizzesService.allQuizzes(request);
    }

    @GetMapping("/{id}")
    public Response getQuiz(@PathVariable int id) {
        return quizzesService.getQuiz(id);
    }

    @PostMapping("/create")
    public Response createQuiz(@RequestBody QuizRq request) {
        return quizzesService.createQuiz(request);
    }

    @PostMapping("/delete/{id}")
    public Response deleteQuiz(@PathVariable int id) {
        return quizzesService.deleteQuiz(id);
    }

    @PostMapping("/update/{id}")
    public Response updateQuiz(@PathVariable int id, @RequestBody QuizRq request) {
        return quizzesService.updateQuiz(id, request);
    }
}

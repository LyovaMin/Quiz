package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.ModificationService;
import by.lyofchik.quiz.Service.QuizzesService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@AllArgsConstructor
public class QuizzesController {
    private QuizzesService quizzesService;
    private ModificationService modificationService;

    @PostMapping
    public Response getQuizzes(@RequestBody QuizzesRq request) {
        return quizzesService.allQuizzes(request);
    }

    @GetMapping("/{id}")
    public Response getQuiz(@PathVariable int id) {
        return quizzesService.getQuiz(id);
    }

    @PutMapping("/create")
    public Response createQuiz(@RequestBody QuizRq request) {
        return modificationService.createQuiz(request);
    }

    @DeleteMapping("/delete/{id}")
    public Response deleteQuiz(@PathVariable int id) {
        return modificationService.deleteQuiz(id);
    }

    @PostMapping("/update/{id}")
    public Response updateQuiz(@PathVariable int id, @RequestBody QuizRq request) {
        return modificationService.updateQuiz(id, request);
    }
}

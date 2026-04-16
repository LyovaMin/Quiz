package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Request.QuizzesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.ModificationService;
import by.lyofchik.quiz.Service.QuizzesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@AllArgsConstructor
@Slf4j
public class QuizzesController {
    private QuizzesService quizzesService;
    private ModificationService modificationService;

    @PostMapping
    public Response getQuizzes(@RequestBody QuizzesRq request) {
        log.info("QuizzesController.getQuizzes - {}", request);
        return quizzesService.allQuizzes(request);
    }

    @GetMapping("/{id}")
    public Response getQuiz(@PathVariable int id) {
        log.info("QuizzesController.getQuiz - {}", id);
        return quizzesService.getQuiz(id);
    }

    @PutMapping("/create")
    public Response createQuiz(@RequestBody QuizRq request) {
        log.info("QuizzesController.createQuiz - {}", request);
        return modificationService.createQuiz(request);
    }

    @DeleteMapping("/delete/{id}")
    public Response deleteQuiz(@PathVariable int id) {
        log.info("QuizzesController.deleteQuiz - {}", id);
        return modificationService.deleteQuiz(id);
    }

    @PostMapping("/update/{id}")
    public Response updateQuiz(@PathVariable int id, @RequestBody QuizRq request) {
        log.info("QuizzesController.updateQuiz {} - {}", id, request);
        return modificationService.updateQuiz(id, request);
    }
}

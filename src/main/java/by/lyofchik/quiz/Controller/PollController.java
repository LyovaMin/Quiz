package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.QuizRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import by.lyofchik.quiz.Service.PollService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/polls")
@AllArgsConstructor
public class PollController {

    private final PollService pollService;
    private final AuthService authService;

    @GetMapping
    public ModelAndView getPollsPage() {
        return new ModelAndView("forward:/html/polls.html");
    }

    @GetMapping("/{id}")
    public ModelAndView getPollPage() {
        return new ModelAndView("forward:/html/poll.html");
    }

    @PostMapping("/api/create")
    @ResponseBody
    public Response createPoll(@RequestBody QuizRq request, HttpSession session) {
        return pollService.createPoll(request, authService.currentUserOrNull(session));
    }

    @PostMapping("/api/{quizId}/vote")
    @ResponseBody
    public Response vote(@PathVariable Integer quizId, @RequestParam Integer answerId, HttpSession session) {
        return pollService.vote(quizId, answerId, authService.currentUserOrNull(session));
    }

    @GetMapping("/api/{quizId}/results")
    @ResponseBody
    public Response getResults(@PathVariable Integer quizId, HttpSession session) {
        return pollService.getPollResults(quizId, authService.currentUserOrNull(session));
    }
}
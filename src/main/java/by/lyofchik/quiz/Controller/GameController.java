package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.AttemptRq;
import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/ws")
@AllArgsConstructor
public class GameController {
    GameService gameService;

    @MessageMapping("/game/{lobbyId}/answer")
    @SendTo("/topic/game/{lobbyId}/result")
    public Response game(GameRq request, @DestinationVariable int lobbyId) {
        return gameService.answer(request, lobbyId);
    }

    @PostMapping("/api/attempt/start")
    @ResponseBody
    public Response startAttempt(@RequestBody AttemptRq request) {
        return gameService.startAttempt(request.getQuizId(), request.getUserId());
    }

    @PostMapping("/api/attempt/finish")
    @ResponseBody
    public Response finishAttempt(@RequestBody AttemptRq request) {
        return gameService.finishAttempt(request.getQuizId(), request.getUserId(), request.getScore());
    }

    @PostMapping("/api/answer")
    @ResponseBody
    public Response answer(@RequestBody GameRq request) {
        return gameService.answer(request);
    }

}

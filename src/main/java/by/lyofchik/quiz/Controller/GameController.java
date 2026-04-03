package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.GameService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ws")
@AllArgsConstructor
public class GameController {
    GameService gameService;

    @MessageMapping("/game/{lobbyId}/answer")
    @SendTo("/topic/game/{lobbyId}/result")
    public Response game(@RequestBody GameRq request, @PathVariable String lobbyId) {
        return gameService.answer(request, lobbyId);
    }
}

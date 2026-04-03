package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.LobbiesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.LobbyService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lobbies")
@AllArgsConstructor
public class LobbyController {
    LobbyService lobbyService;

    @GetMapping
    public Response lobbies(@RequestBody LobbiesRq request) {
        return lobbyService.lobbies(request);
    }

    @GetMapping("/{id}")
    public Response lobby(@PathVariable Integer id) {
        return lobbyService.lobby(id);
    }

    @PostMapping("/join/{id}")
    public Response join(@PathVariable Integer id, @RequestBody String password) {
        return lobbyService.join(id, password);
    }
}

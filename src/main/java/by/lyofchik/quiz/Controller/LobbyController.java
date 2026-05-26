package by.lyofchik.quiz.Controller;

import by.lyofchik.quiz.Model.DTO.Request.LobbiesRq;
import by.lyofchik.quiz.Model.DTO.Request.LobbyUpdateRq;
import by.lyofchik.quiz.Model.DTO.Request.LobbyRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Service.AuthService;
import by.lyofchik.quiz.Service.LobbyService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/lobby")
@AllArgsConstructor
@Slf4j
public class LobbyController {
    private LobbyService lobbyService;
    private AuthService authService;
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ModelAndView getLobbyPage() {
        return new ModelAndView("forward:/html/lobby.html");
    }

    @GetMapping("/{id}")
    public ModelAndView getLobbyRoomPage(@PathVariable int id) {
        return new ModelAndView("forward:/html/lobby-room.html");
    }

    @PostMapping("/api/all")
    @ResponseBody
    public Response getLobbies(@RequestBody LobbiesRq request) {
        return lobbyService.lobbies(request);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Response getLobby(@PathVariable int id) {
        return lobbyService.lobby(id);
    }

    @PostMapping("/api/create")
    @ResponseBody
    public Response createLobby(@RequestBody LobbyRq request, HttpSession session) {
        return lobbyService.createLobby(request, authService.currentUserOrNull(session));
    }

    @PostMapping("/api/{lobbyId}/start")
    @ResponseBody
    public Response startQuiz(@PathVariable int lobbyId, @RequestParam int quizId, @RequestParam(defaultValue = "true") boolean hostParticipates, HttpSession session) {
        Response response = lobbyService.startQuiz(lobbyId, quizId, hostParticipates, authService.currentUserOrNull(session));
        if (response.getStatus().startsWith("2")) {
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/started", response.getData());
        }
        return response;
    }

    @PostMapping("/api/{lobbyId}/end")
    @ResponseBody
    public Response endQuiz(@PathVariable int lobbyId, HttpSession session) {
        Response response = lobbyService.endQuiz(lobbyId, authService.currentUserOrNull(session));
        if (response.getStatus().startsWith("2")) {
            messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/ended", response.getData());
        }
        return response;
    }

    @GetMapping("/api/{lobbyId}/results")
    @ResponseBody
    public Response results(@PathVariable int lobbyId) {
        return lobbyService.results(lobbyId);
    }

    @PostMapping("/api/{lobbyId}/update")
    @ResponseBody
    public Response updateLobby(@PathVariable int lobbyId, @RequestBody LobbyUpdateRq request, HttpSession session) {
        return lobbyService.updateLobby(lobbyId, request, authService.currentUserOrNull(session));
    }

    @DeleteMapping("/api/{lobbyId}/players/{userId}")
    @ResponseBody
    public Response kickPlayer(@PathVariable int lobbyId, @PathVariable int userId, HttpSession session) {
        return lobbyService.kickPlayer(lobbyId, userId, authService.currentUserOrNull(session));
    }

    @GetMapping("/api/quizzes")
    @ResponseBody
    public Response getQuizzes() {
        return lobbyService.getQuizzes();
    }

    @GetMapping("/api/{lobbyId}/players")
    @ResponseBody
    public Response getPlayers(@PathVariable int lobbyId) {
        return lobbyService.getPlayers(lobbyId);
    }

    @GetMapping("/api/{lobbyId}/completed")
    @ResponseBody
    public Response isCompleted(@PathVariable int lobbyId, HttpSession session) {
        return lobbyService.isUserCompletedInLobby(lobbyId, authService.currentUserOrNull(session));
    }

    @PostMapping("/api/{lobbyId}/join")
    @ResponseBody
    public Response joinLobby(@PathVariable int lobbyId, @RequestParam(required = false) String password, HttpSession session) {
        return lobbyService.joinLobby(lobbyId, password, authService.currentUserOrNull(session));
    }
}

package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.LobbiesRq;
import by.lyofchik.quiz.Model.DTO.Request.LobbyUpdateRq;
import by.lyofchik.quiz.Model.DTO.Request.LobbyRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.DTO.LobbyResultDTO;
import by.lyofchik.quiz.Model.Entity.GameMember;
import by.lyofchik.quiz.Model.Entity.Lobby;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Enum.LobbyStatus;
import by.lyofchik.quiz.Model.Mapper.LobbyMapper;
import by.lyofchik.quiz.Repository.GameMemberRepository;
import by.lyofchik.quiz.Repository.LobbyRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class LobbyService {
    LobbyRepository lobbyRepository;
    QuizzesRepository quizzesRepository;
    GameMemberRepository gameMemberRepository;
    UserRepository userRepository;
    AuthService authService;
    LobbyMapper lobbyMapper;

    public Response lobbies(LobbiesRq request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        var statuses = List.of(LobbyStatus.WAITING, LobbyStatus.STARTED);
        var page = request.getId() == null
                ? lobbyRepository.findByStatusIn(statuses, pageable)
                : lobbyRepository.findByIdAndStatusIn(request.getId(), statuses, pageable);
        return Response.success(page);
    }

    public Response lobby(Integer id) {
        var lobby = lobbyRepository.findById(id);
        if (lobby.isPresent()) {
            return Response.success(lobby.get());
        }
        return Response.error();
    }

    public Response join(int id, String lobbyPassword) {
        var lobby = lobbyRepository.findById(id);
        if (lobby == null) {
            log.info("Lobby with id: {} - not found", id);
            return Response.error("404", "Lobby with id: " + id + " not found");
        }

        if (!lobby.getPassword().equals(lobbyPassword)) {
            log.info("Incorrect password for lobby with id: {}", id);
            return Response.error("403", "Incorrect password for lobby with id: " + id);
        }

        return Response.success(lobby);
    }

    public Response createLobby(LobbyRq request, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        var quiz = quizzesRepository.findById(request.getQuiz());
        if (quiz.isEmpty()) {
            return Response.error("404", "Quiz not found");
        }

        Lobby lobby = new Lobby();
        lobby.setHost(currentUser.getId());
        lobby.setQuiz(quiz.get());
        lobby.setPassword(request.getPassword());
        lobby.setMaxPlayers(normalizeMaxPlayers(request.getMaxPlayers()));
        lobby.setStatus(LobbyStatus.WAITING);
        lobbyRepository.save(lobby);

        GameMember host = new GameMember();
        host.setId(currentUser.getId());
        host.setLobby(lobby.getId());
        host.setScore(0);
        host.setProgress(0f);
        host.setLastUpdate(Instant.now());
        gameMemberRepository.save(host);
        return Response.success(lobby);
    }

    public Response startQuiz(int lobbyId, int quizId, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        if (!canManage(lobby, currentUser)) {
            return Response.error("403", "Only the lobby owner can start the quiz");
        }
        var quiz = quizzesRepository.findById(quizId);
        if (quiz.isEmpty()) {
            return Response.error("404", "Quiz not found");
        }
        lobby.setQuiz(quiz.get());
        lobby.setStatus(LobbyStatus.STARTED);
        lobbyRepository.save(lobby);
        return Response.success(lobby);
    }

    public Response getQuizzes() {
        return Response.success(quizzesRepository.findAll());
    }

    public Response getPlayers(int lobbyId) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        Set<GameMember> members = lobby.getGameMembers();
        return Response.success(members);
    }

    public Response joinLobby(int lobbyId, String password, User currentUser) {
        if (currentUser == null) {
            return Response.error("401", "Login required");
        }
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        if (lobby.getStatus() == LobbyStatus.ENDED) {
            return Response.error("400", "Lobby is closed");
        }
        if (!gameMemberRepository.existsByLobbyAndId(lobbyId, currentUser.getId())
                && lobby.getMaxPlayers() != null
                && gameMemberRepository.findByLobbyOrderByScoreDesc(lobbyId).size() >= lobby.getMaxPlayers()) {
            return Response.error("400", "Lobby is full");
        }
        if (lobby.getPassword() != null && !lobby.getPassword().isBlank() && !lobby.getPassword().equals(password)) {
            return Response.error("403", "Incorrect lobby password");
        }
        if (!gameMemberRepository.existsByLobbyAndId(lobbyId, currentUser.getId())) {
            GameMember member = new GameMember();
            member.setId(currentUser.getId());
            member.setLobby(lobbyId);
            member.setScore(0);
            member.setProgress(0f);
            member.setLastUpdate(Instant.now());
            gameMemberRepository.save(member);
        }
        return Response.success(lobby);
    }

    public Response updateLobby(int lobbyId, LobbyUpdateRq request, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        if (!canManage(lobby, currentUser)) {
            return Response.error("403", "Only the lobby host can edit this lobby");
        }
        if (request.getQuizId() != null) {
            var quiz = quizzesRepository.findById(request.getQuizId());
            if (quiz.isEmpty()) {
                return Response.error("404", "Quiz not found");
            }
            lobby.setQuiz(quiz.get());
        }
        lobbyRepository.save(lobby);
        return Response.success(lobby);
    }

    public Response kickPlayer(int lobbyId, int userId, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        if (!canManage(lobby, currentUser)) {
            return Response.error("403", "Only the lobby host can kick players");
        }
        if (lobby.getHost().equals(userId)) {
            return Response.error("400", "Host cannot be kicked");
        }
        gameMemberRepository.deleteByLobbyAndId(lobbyId, userId);
        return Response.success();
    }

    public Response results(int lobbyId) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        return Response.success(buildResults(lobbyId));
    }

    public Response endQuiz(int lobbyId, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        if (!canManage(lobby, currentUser)) {
            return Response.error("403", "Only the lobby host can finish this quiz");
        }
        lobby.setStatus(LobbyStatus.ENDED);
        lobbyRepository.save(lobby);
        return Response.success(buildResults(lobbyId));
    }

    private List<LobbyResultDTO> buildResults(int lobbyId) {
        List<GameMember> members = gameMemberRepository.findByLobbyOrderByScoreDesc(lobbyId);
        int[] place = {0};
        return members.stream()
                .map(member -> {
                    place[0]++;
                    String username = userRepository.findById(member.getId())
                            .map(User::getLogin)
                            .orElse("Игрок #" + member.getId());
                    return new LobbyResultDTO(
                            place[0],
                            member.getId(),
                            username,
                            member.getScore() == null ? 0 : member.getScore(),
                            member.getProgress() == null ? 0f : member.getProgress()
                    );
                })
                .toList();
    }

    private boolean canManage(Lobby lobby, User currentUser) {
        return currentUser != null && (authService.isAdmin(currentUser) || lobby.getHost().equals(currentUser.getId()));
    }

    private Integer normalizeMaxPlayers(Integer maxPlayers) {
        if (maxPlayers == null || maxPlayers < 2) {
            return null;
        }
        return Math.min(maxPlayers, 50);
    }
}

package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.LobbiesRq;
import by.lyofchik.quiz.Model.DTO.Request.LobbyUpdateRq;
import by.lyofchik.quiz.Model.DTO.Request.LobbyRq;
import by.lyofchik.quiz.Model.DTO.Response.LobbyDTO;
import by.lyofchik.quiz.Model.DTO.Response.PageDTO;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.DTO.LobbyResultDTO;
import by.lyofchik.quiz.Model.Entity.GameMember;
import by.lyofchik.quiz.Model.Entity.Lobby;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Enum.LobbyStatus;
import by.lyofchik.quiz.Model.Mapper.LobbyMapper;
import by.lyofchik.quiz.Model.Mapper.QuizMapper;
import by.lyofchik.quiz.Repository.GameMemberRepository;
import by.lyofchik.quiz.Repository.LobbyRepository;
import by.lyofchik.quiz.Repository.QuizzesRepository;
import by.lyofchik.quiz.Repository.UserRepository;
import by.lyofchik.quiz.Repository.QuizAttemptRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import by.lyofchik.quiz.Model.Enum.Type;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    QuizMapper quizMapper;
    QuizAttemptRepository quizAttemptRepository;
    SimpMessagingTemplate messagingTemplate;

    // scheduler used to auto-finish lobbies when time expires
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Response lobbies(LobbiesRq request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        var statuses = List.of(LobbyStatus.WAITING, LobbyStatus.STARTED);
        Page<Lobby> page = request.getId() == null
                ? lobbyRepository.findByStatusIn(statuses, pageable)
                : lobbyRepository.findByIdAndStatusIn(request.getId(), statuses, pageable);
        
        PageDTO<LobbyDTO> pageDTO = new PageDTO<>(
                page.getContent().stream().map(lobbyMapper::toDto).collect(Collectors.toList()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return Response.success(pageDTO);
    }

    public Response lobby(Integer id) {
        return lobbyRepository.findById(id)
                .map(lobby -> Response.success(lobbyMapper.toDto(lobby)))
                .orElseGet(Response::error);
    }

    public Response join(int id, String lobbyPassword) {
        var lobby = lobbyRepository.findById(id).orElse(null);
        if (lobby == null) {
            log.info("Lobby with id: {} - not found", id);
            return Response.error("404", "Lobby with id: " + id + " not found");
        }

        if (!lobby.getPassword().equals(lobbyPassword)) {
            log.info("Incorrect password for lobby with id: {}", id);
            return Response.error("403", "Incorrect password for lobby with id: " + id);
        }

        return Response.success(lobbyMapper.toDto(lobby));
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
        return Response.success(lobbyMapper.toDto(lobby));
    }

    public Response startQuiz(int lobbyId, int quizId, boolean hostParticipates, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
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
        log.info("startQuiz called: lobbyId={}, quizId={}, hostParticipates={}, user={}", lobbyId, quizId, hostParticipates, currentUser == null ? null : currentUser.getId());
        lobby.setQuiz(quiz.get());
        lobby.setStatus(LobbyStatus.STARTED);
        lobby.setStartedAt(java.time.Instant.now());
        try {
            log.info("Lobby before start: id={}, host={}, membersCount={}", lobby.getId(), lobby.getHost(), lobby.getGameMembers() == null ? 0 : lobby.getGameMembers().size());
            var originalMembers = lobby.getGameMembers();
            log.debug("Original members detail: {}", originalMembers == null ? "null" : originalMembers);

            // avoid saving transient GameMember instances referenced from lobby collection
            lobby.setGameMembers(new java.util.LinkedHashSet<>());
            lobbyRepository.save(lobby);
            log.info("Lobby saved as STARTED: id={}", lobby.getId());

            // schedule automatic end based on quiz time limit (seconds)
            Integer timeLimit = quiz.get().getTimeLimit();
            if (timeLimit != null && timeLimit > 0) {
                long delay = timeLimit;
                scheduler.schedule(() -> {
                    try {
                        Lobby l = lobbyRepository.findById(lobbyId).orElse(null);
                        if (l != null && l.getStatus() == LobbyStatus.STARTED) {
                            log.info("Auto-finishing lobby {} after {} seconds", lobbyId, delay);
                            // finish without auth
                            l.setStatus(LobbyStatus.ENDED);
                            lobbyRepository.save(l);
                            var results = buildResults(lobbyId);
                            try {
                                messagingTemplate.convertAndSend("/topic/lobby/" + lobbyId + "/ended", results);
                            } catch (Exception ex) {
                                log.error("Failed to send websocket ended message for lobby {}: {}", lobbyId, ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        log.error("Error during scheduled finish for lobby {}: {}", lobbyId, ex.getMessage());
                    }
                }, delay, TimeUnit.SECONDS);
            }

            // reload to return full DTO including members
            var savedLobby = lobbyRepository.findById(lobbyId).orElse(lobby);

            // if host doesn't participate, remove host from game members so they remain spectator
            if (!hostParticipates) {
                log.info("Host not participating, deleting host member: lobbyId={}, hostId={}", lobbyId, lobby.getHost());
                gameMemberRepository.deleteByLobbyAndId(lobbyId, lobby.getHost());
            }

            log.info("startQuiz completed successfully for lobbyId={}", lobbyId);
            return Response.success(lobbyMapper.toDto(savedLobby));
        } catch (Exception e) {
            log.error("Failed to start quiz for lobby {}: {}", lobbyId, e.getMessage(), e);
            return Response.error("500", "Failed to start quiz: " + e.getMessage());
        }
    }

    public Response getQuizzes() {
        List<Quiz> quizzes = quizzesRepository.findAll();
        // filter out polls but include legacy null types
        List<Quiz> filtered = quizzes.stream()
                .filter(q -> q.getType() == null || q.getType() == Type.QUIZ)
                .collect(Collectors.toList());
        log.info("Found {} quizzes (filtered from {})", filtered.size(), quizzes.size());
        return Response.success(filtered.stream().map(quizMapper::toQuizRq).collect(Collectors.toList()));
    }

    public Response getPlayers(int lobbyId) {
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
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
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        if (lobby.getStatus() == LobbyStatus.ENDED) {
            return Response.error("400", "Lobby is closed");
        }
        if (!gameMemberRepository.existsByLobbyAndId(lobby.getId(), currentUser.getId())
                && lobby.getMaxPlayers() != null
                && gameMemberRepository.findByLobbyOrderByScoreDesc(lobby.getId()).size() >= lobby.getMaxPlayers()) {
            return Response.error("400", "Lobby is full");
        }
        if (lobby.getPassword() != null && !lobby.getPassword().isBlank() && !lobby.getPassword().equals(password)) {
            return Response.error("403", "Incorrect lobby password");
        }
        if (!gameMemberRepository.existsByLobbyAndId(lobby.getId(), currentUser.getId())) {
            GameMember member = new GameMember();
            member.setId(currentUser.getId());
            member.setLobby(lobby.getId());
            member.setScore(0);
            member.setProgress(0f);
            member.setLastUpdate(Instant.now());
            gameMemberRepository.save(member);
        }
        return Response.success(lobbyMapper.toDto(lobby));
    }

    public Response updateLobby(int lobbyId, LobbyUpdateRq request, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
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
        return Response.success(lobbyMapper.toDto(lobby));
    }

    public Response kickPlayer(int lobbyId, int userId, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
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
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
        if (lobby == null) {
            return Response.error("404", "Lobby not found");
        }
        return Response.success(buildResults(lobbyId));
    }

    public Response isUserCompletedInLobby(int lobbyId, User currentUser) {
        if (currentUser == null) {
            log.debug("isUserCompletedInLobby: no current user");
            return Response.success(false);
        }
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
        if (lobby == null) {
            log.debug("isUserCompletedInLobby: lobby {} not found", lobbyId);
            return Response.success(false);
        }
        if (lobby.getQuiz() == null || lobby.getQuiz().getId() == null) {
            log.debug("isUserCompletedInLobby: lobby {} has no quiz", lobbyId);
            return Response.success(false);
        }
        try {
            Integer quizId = lobby.getQuiz().getId();
            log.info("Checking completion: lobbyId={}, quizId={}, userId={}", lobbyId, quizId, currentUser.getId());
            boolean completed = quizAttemptRepository.existsByQuizAndUserAndCompletedAtIsNotNull(quizId, currentUser.getId());
            log.info("Completion check result: lobbyId={}, quizId={}, userId={}, completed={}", lobbyId, quizId, currentUser.getId(), completed);
            return Response.success(completed);
        } catch (Exception e) {
            log.error("Failed to check completion for lobby {}: {}", lobbyId, e.getMessage(), e);
            return Response.success(false);
        }
    }

    public Response endQuiz(int lobbyId, User currentUser) {
        Lobby lobby = lobbyRepository.findById(lobbyId).orElse(null);
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
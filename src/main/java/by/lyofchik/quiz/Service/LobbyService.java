package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.LobbiesRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.Entity.Lobby;
import by.lyofchik.quiz.Repository.LobbyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class LobbyService {
    LobbyRepository lobbyRepository;

    public Response lobbies(LobbiesRq request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        var page = lobbyRepository.findAll(pageable);
        return Response.success(page);
    }

    public Response lobby(Integer id) {
        var lobby = lobbyRepository.findById(id);
        if (lobby.isPresent()) {
            return Response.success(lobby);
        }
        return Response.error();
    }

    public Response join(int id, String lobbyPassword) {
        var lobby = lobbyRepository.findById(id);
        if (lobby.isEmpty()) {
            log.info("Lobby with id: {} - not found", id);
            return Response.error("404", "Lobby with id: " + id + " not found");
        }

        if (!lobby.get().getPassword().equals(lobbyPassword)) {
            log.info("Incorrect password for lobby with id: {}", id);
            return Response.error("403", "Incorrect password for lobby with id: " + id);
        }

        return Response.success(lobby.get());
    }
}

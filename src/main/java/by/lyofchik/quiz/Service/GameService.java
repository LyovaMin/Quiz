package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.GameRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    public Response answer(GameRq request, int lobbyId) {

        return Response.success();
    }
}

package by.lyofchik.quiz.Model.DTO.Response;

import by.lyofchik.quiz.Model.Enum.LobbyStatus;
import lombok.Data;

@Data
public class LobbyDTO {
    private Integer id;
    private Integer quizId;
    private String quizTitle;
    private LobbyStatus status;
    private Integer host;
    private Integer maxPlayers;
    private int numPlayers;
    private String startedAt; // ISO timestamp or null
}
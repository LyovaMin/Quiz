package by.lyofchik.quiz.Model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LobbyResultDTO {
    private int place;
    private int userId;
    private String username;
    private int score;
    private float progress;
}

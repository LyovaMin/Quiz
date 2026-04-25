package by.lyofchik.quiz.Model.DTO.Request;

import lombok.Data;

@Data
public class LobbyRq {
    private int creator;
    private int quiz;
    private String password;
}

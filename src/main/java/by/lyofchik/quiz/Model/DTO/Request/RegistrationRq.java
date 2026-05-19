package by.lyofchik.quiz.Model.DTO.Request;

import lombok.Data;

@Data
public class RegistrationRq {
    private String username;
    private String password;
    private String imageUrl;
}

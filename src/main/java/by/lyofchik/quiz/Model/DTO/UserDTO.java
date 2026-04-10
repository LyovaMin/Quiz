package by.lyofchik.quiz.Model.DTO;

import by.lyofchik.quiz.Model.Enum.Role;
import lombok.Data;

@Data
public class UserDTO {
    int id;
    String name;
    String image;
    Role role;
}

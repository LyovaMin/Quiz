package by.lyofchik.quiz.Model.Mapper;

import by.lyofchik.quiz.Model.DTO.UserDTO;
import by.lyofchik.quiz.Model.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {
    @Mapping(source = "login", target = "name")
    @Mapping(source = "imageUrl", target = "image")
    User toEntity(UserDTO userDTO);

    @Mapping(source = "name", target = "login")
    @Mapping(source = "image", target = "imageUrl")
    @Mapping(target = "passwordHash", ignore = true)
    UserDTO toDTO(User user);
}

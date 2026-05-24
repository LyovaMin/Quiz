package by.lyofchik.quiz.Model.Mapper;

import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Repository.UserRepository;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected UserRepository userRepository;

    public User toUser(Integer id) {
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).orElse(null);
    }

    public Integer toId(User user) {
        return user != null ? user.getId() : null;
    }
}

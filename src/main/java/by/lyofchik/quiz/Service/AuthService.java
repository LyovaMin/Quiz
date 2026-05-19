package by.lyofchik.quiz.Service;

import by.lyofchik.quiz.Model.DTO.Request.LoginRq;
import by.lyofchik.quiz.Model.DTO.Request.RegistrationRq;
import by.lyofchik.quiz.Model.DTO.Response.Response;
import by.lyofchik.quiz.Model.DTO.UserDTO;
import by.lyofchik.quiz.Model.Entity.User;
import by.lyofchik.quiz.Model.Enum.Role;
import by.lyofchik.quiz.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@AllArgsConstructor
public class AuthService {
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Response login(LoginRq request, HttpSession session) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return Response.error("400", "Username and password are required");
        }

        var user = userRepository.findByLogin(request.getUsername().trim());
        if (user.isEmpty() || !passwordEncoder.matches(request.getPassword(), user.get().getPasswordHash())) {
            return Response.error("401", "Incorrect username or password");
        }

        session.setAttribute(CURRENT_USER_ID, user.get().getId());
        return Response.success(toDto(user.get()));
    }

    public Response registration(RegistrationRq request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            return Response.error("400", "Username and password are required");
        }

        String login = request.getUsername().trim();
        if (userRepository.existsByLogin(login)) {
            return Response.error("409", "User already exists");
        }

        User user = new User();
        user.setLogin(login);
        user.setImageUrl(request.getImageUrl());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return Response.success(toDto(user));
    }

    public Response currentUser(HttpSession session) {
        Integer id = (Integer) session.getAttribute(CURRENT_USER_ID);
        if (id == null) {
            return Response.error("401", "Not authenticated");
        }
        return userRepository.findById(id)
                .map(user -> Response.success(toDto(user)))
                .orElseGet(() -> Response.error("401", "Not authenticated"));
    }

    public User currentUserOrNull(HttpSession session) {
        Integer id = (Integer) session.getAttribute(CURRENT_USER_ID);
        if (id == null) {
            return null;
        }
        return userRepository.findById(id).orElse(null);
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getLogin());
        dto.setImage(user.getImageUrl());
        dto.setRole(user.getRole());
        return dto;
    }
}

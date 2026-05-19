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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

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

    public Response updateCurrentUser(String username, String password, MultipartFile avatar, HttpSession session) {
        User user = currentUserOrNull(session);
        if (user == null) {
            return Response.error("401", "Not authenticated");
        }
        return updateProfile(user, username, password, avatar);
    }

    public Response users(HttpSession session) {
        User currentUser = currentUserOrNull(session);
        if (!isAdmin(currentUser)) {
            return Response.error("403", "Admin access required");
        }
        return Response.success(userRepository.findAll().stream()
                .map(this::toDto)
                .toList());
    }

    public Response updateUser(Integer id, String username, String password, MultipartFile avatar, HttpSession session) {
        User currentUser = currentUserOrNull(session);
        if (!isAdmin(currentUser)) {
            return Response.error("403", "Admin access required");
        }
        return userRepository.findById(id)
                .map(user -> updateProfile(user, username, password, avatar))
                .orElseGet(() -> Response.error("404", "User not found"));
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

    private Response updateProfile(User user, String username, String password, MultipartFile avatar) {
        if (StringUtils.hasText(username)) {
            String login = username.trim();
            var existing = userRepository.findByLogin(login);
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                return Response.error("409", "User already exists");
            }
            user.setLogin(login);
        }

        if (avatar != null && !avatar.isEmpty()) {
            String imageUrl = saveAvatar(avatar);
            if (imageUrl == null) {
                return Response.error("400", "Avatar must be an image");
            }
            user.setImageUrl(imageUrl);
        }

        if (StringUtils.hasText(password)) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        userRepository.save(user);
        return Response.success(toDto(user));
    }

    private String saveAvatar(MultipartFile avatar) {
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return null;
        }

        String extension = getExtension(avatar.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path directory = Path.of("uploads", "avatars").toAbsolutePath().normalize();
        Path target = directory.resolve(filename).normalize();

        try {
            Files.createDirectories(directory);
            avatar.transferTo(target);
            return "/uploads/avatars/" + filename;
        } catch (IOException e) {
            throw new IllegalStateException("Could not save avatar", e);
        }
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
        return extension.length() > 12 ? "" : extension;
    }
}

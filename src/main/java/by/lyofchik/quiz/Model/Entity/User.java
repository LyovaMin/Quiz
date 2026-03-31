package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "login", length = 20)
    private String login;

    @Nationalized
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Nationalized
    @Column(name = "password_hash")
    private String passwordHash;

    @Nationalized
    @Column(name = "role", length = 20)
    private Role role;
}
package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.jspecify.annotations.NonNull;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "quizzes")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "title", length = 30)
    private String title;

    @Nationalized
    @Column(name = "description")
    private String description;

    @Nationalized
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "creator_id")
    private Integer creator;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "is_public")
    private Boolean isPublic;

    @Column(name = "time_limit")
    private Integer timeLimit;

    @NonNull
    @OneToMany(mappedBy = "quiz")
    private Set<Question> questions = new LinkedHashSet<>();
}
package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.Types;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "description", length = 512)
    private String description;

    @JoinColumn(name = "quiz_id")
    private Integer quiz;

    @Nationalized
    @Column(name = "type", length = 15)
    @Enumerated(EnumType.STRING)
    private Types type;

    @Nationalized
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "points")
    private Integer points;
    
    @NonNull
    @OneToMany(mappedBy = "question")
    private Set<Answer> answers = new LinkedHashSet<>();
}
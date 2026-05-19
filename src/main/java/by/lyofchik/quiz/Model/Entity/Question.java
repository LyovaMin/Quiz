package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.Types;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    @JsonIgnore
    private Quiz quiz;

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
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers = new LinkedHashSet<>();
}

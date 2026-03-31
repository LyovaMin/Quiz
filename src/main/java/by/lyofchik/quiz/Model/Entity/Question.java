package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.Types;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "quiz_id")
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


}
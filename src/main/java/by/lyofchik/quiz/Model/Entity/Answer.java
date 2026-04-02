package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "answers")
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id", nullable = false)
    private Integer id;

    @JoinColumn(name = "question_id")
    private Integer question;

    @Nationalized
    @Column(name = "text")
    private String text;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}
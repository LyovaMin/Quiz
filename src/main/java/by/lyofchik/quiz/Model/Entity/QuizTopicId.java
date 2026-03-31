package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class QuizTopicId implements Serializable {
    private static final long serialVersionUID = -6994744656515475935L;
    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Column(name = "quiz_id", nullable = false)
    private Integer quizId;


}
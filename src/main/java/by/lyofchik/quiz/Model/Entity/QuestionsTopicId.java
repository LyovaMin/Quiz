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
public class QuestionsTopicId implements Serializable {
    private static final long serialVersionUID = 5333891235904406139L;
    @Column(name = "topic_id", nullable = false)
    private Integer topicId;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;


}
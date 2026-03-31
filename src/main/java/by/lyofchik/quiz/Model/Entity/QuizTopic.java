package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "quiz_topics")
public class QuizTopic {
    @EmbeddedId
    private QuizTopicId id;

    @MapsId("topicId")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @MapsId("quizId")
    @JoinColumn(name = "quiz_id", nullable = false)
    private Integer quiz;
}
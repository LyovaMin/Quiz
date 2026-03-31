package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "questions_topics")
public class QuestionsTopic {
    @EmbeddedId
    private QuestionsTopicId id;

    @MapsId("topicId")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private Integer question;
}
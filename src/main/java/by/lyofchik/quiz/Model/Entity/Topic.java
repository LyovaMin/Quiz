package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.TopicName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "topics")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "name", length = 20)
    @Enumerated(EnumType.STRING)
    private TopicName name;
}
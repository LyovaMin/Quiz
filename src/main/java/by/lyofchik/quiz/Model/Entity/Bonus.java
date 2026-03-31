package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "bonuses")
public class Bonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bonus_id", nullable = false)
    private Integer id;

    @Nationalized
    @Column(name = "type", length = 15)
    private String type;

    @Nationalized
    @Column(name = "description")
    private String description;


}
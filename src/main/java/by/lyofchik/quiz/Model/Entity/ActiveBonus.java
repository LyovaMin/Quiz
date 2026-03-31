package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "active_bonuses")
public class ActiveBonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "member_id")
    private Integer member;

    @Column(name = "bonus_id")
    private Integer bonus;

    @Column(name = "is_active")
    private Boolean isActive;


}
package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.BonusType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "game_members")
public class GameMember {
    @Id
    @Column(name = "member_id", nullable = false)
    private Integer id;

    @Column(name = "member_id", insertable = false, updatable = false)
    private Integer users;

    @Column(name = "lobby_id")
    private Integer lobby;

    @ColumnDefault("0")
    @Column(name = "score")
    private Integer score;

    @ColumnDefault("0")
    @Column(name = "progress")
    private Float progress;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Enumerated(EnumType.STRING)
    @Column(name = "available_bonus")
    private BonusType availableBonus;
}
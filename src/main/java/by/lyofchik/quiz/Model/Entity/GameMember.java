package by.lyofchik.quiz.Model.Entity;

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

    @MapsId
    @JoinColumn(name = "member_id", nullable = false)
    private Integer users;

    @Column(name = "lobby_id")
    private Integer lobby;

    @ColumnDefault("0")
    @Column(name = "score")
    private Integer score;

    @ColumnDefault("0")
    @Column(name = "progress")
    private Double progress;

    @Column(name = "last_update")
    private Instant lastUpdate;


}
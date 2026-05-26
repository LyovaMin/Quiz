package by.lyofchik.quiz.Model.Entity;

import by.lyofchik.quiz.Model.Enum.LobbyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "lobbies")
public class Lobby {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lobby_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Nationalized
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private LobbyStatus status;

    @Column(name = "started_at")
    private java.time.Instant startedAt;

    @Nationalized
    @Column(name = "password", length = 20)
    private String password;

    @Column(name = "host_id")
    private Integer host;

    @Column(name = "max_players")
    private Integer maxPlayers;

    @OneToMany
    @JoinColumn(name = "lobby_id")
    private Set<GameMember> gameMembers = new LinkedHashSet<>();


}
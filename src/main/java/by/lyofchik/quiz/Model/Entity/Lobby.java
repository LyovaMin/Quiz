package by.lyofchik.quiz.Model.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private by.lyofchik.quiz.Integer quiz;

    @Nationalized
    @Column(name = "status", length = 20)
    private String status;

    @Nationalized
    @Column(name = "password", length = 20)
    private String password;

    @Column(name = "host_id")
    private Integer host;

    @OneToMany
    @JoinColumn(name = "lobby_id")
    private Set<by.lyofchik.quiz.Integer> gameMembers = new LinkedHashSet<>();


}
package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.GameMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameMemberRepository extends JpaRepository<GameMember, Integer> {
    GameMember findById(int id);

    boolean existsByLobbyAndId(Integer lobbyId, Integer userId);

    void deleteByLobbyAndId(Integer lobbyId, Integer userId);

    List<GameMember> findByLobbyOrderByScoreDesc(Integer lobbyId);
}

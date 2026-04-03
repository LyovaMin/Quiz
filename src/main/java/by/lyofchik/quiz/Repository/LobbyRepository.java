package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, Integer> {
    Optional<Lobby> findById(Integer id);
}

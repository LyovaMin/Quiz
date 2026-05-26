package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Lobby;
import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Enum.LobbyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, Integer> {
    Optional<Lobby> findById(int id);

    Page<Lobby> findByStatusIn(Collection<LobbyStatus> statuses, Pageable pageable);

    Page<Lobby> findByIdAndStatusIn(Integer id, Collection<LobbyStatus> statuses, Pageable pageable);

    // remove lobbies by quiz to allow cascading delete behavior when quizzes are removed
    void deleteByQuiz(Quiz quiz);
}
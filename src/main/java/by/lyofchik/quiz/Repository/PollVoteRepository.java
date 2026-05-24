package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVote, Integer> {
    Optional<PollVote> findByUserIdAndQuizId(Integer userId, Integer quizId);
}

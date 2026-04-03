package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
}

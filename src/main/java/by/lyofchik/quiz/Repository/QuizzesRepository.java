package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizzesRepository extends JpaRepository<Quiz, Integer> {
}

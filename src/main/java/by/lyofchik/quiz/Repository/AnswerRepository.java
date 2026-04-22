package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query("SELECT a FROM Answer a " +
            "WHERE a.question.id = :questionId " +
            "AND a.isCorrect = true")
    Answer findCorrectByQuestionId(@Param("questionId") int questionId);
}

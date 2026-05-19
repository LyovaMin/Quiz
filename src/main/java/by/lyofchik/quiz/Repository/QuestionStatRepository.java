package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.QuestionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface QuestionStatRepository extends JpaRepository<QuestionStat, Integer> {
    @Query("""
            SELECT COUNT(qs.id),
                   SUM(CASE WHEN qs.isCorrect = true THEN 1 ELSE 0 END)
            FROM QuestionStat qs
            JOIN QuizAttempt qa ON qa.id = qs.attempt
            WHERE qa.user = :userId
              AND (:fromDate IS NULL OR qs.completedAt >= :fromDate)
              AND (:topicId IS NULL OR EXISTS (
                    SELECT qt.id FROM QuizTopic qt
                    WHERE qt.quiz = qa.quiz AND qt.topic.id = :topicId
              ))
            """)
    Object[] getUserAnswerStats(@Param("userId") Integer userId,
                                @Param("fromDate") Instant fromDate,
                                @Param("topicId") Integer topicId);

    @Query("""
            SELECT t.id, t.name,
                   COUNT(qs.id),
                   SUM(CASE WHEN qs.isCorrect = true THEN 1 ELSE 0 END)
            FROM QuestionStat qs
            JOIN QuizAttempt qa ON qa.id = qs.attempt
            JOIN QuizTopic qt ON qt.quiz = qa.quiz
            JOIN Topic t ON t.id = qt.topic.id
            WHERE qa.user = :userId
              AND (:fromDate IS NULL OR qs.completedAt >= :fromDate)
            GROUP BY t.id, t.name
            ORDER BY t.name
            """)
    List<Object[]> getUserAnswerStatsByTopic(@Param("userId") Integer userId,
                                             @Param("fromDate") Instant fromDate);
}

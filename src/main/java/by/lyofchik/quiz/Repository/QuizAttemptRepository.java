package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.QuizAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Integer> {
    Optional<QuizAttempt> findFirstByQuizAndUserAndCompletedAtIsNullOrderByStartedAtDesc(Integer quiz, Integer user);

    @Query("""
            SELECT u.id, u.login, u.imageUrl, SUM(qa.score)
            FROM QuizAttempt qa
            JOIN User u ON u.id = qa.user
            WHERE qa.completedAt IS NOT NULL
              AND (:fromDate IS NULL OR qa.completedAt >= :fromDate)
              AND (:topicId IS NULL OR EXISTS (
                    SELECT qt.id FROM QuizTopic qt
                    WHERE qt.quiz.id = qa.quiz AND qt.topic.id = :topicId
              ))
            GROUP BY u.id, u.login, u.imageUrl
            ORDER BY SUM(qa.score) DESC
            """)
    List<Object[]> leaderboard(@Param("fromDate") Instant fromDate,
                               @Param("topicId") Integer topicId,
                               Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(qa.score), 0)
            FROM QuizAttempt qa
            WHERE qa.user = :userId
              AND qa.completedAt IS NOT NULL
              AND (:fromDate IS NULL OR qa.completedAt >= :fromDate)
              AND (:topicId IS NULL OR EXISTS (
                    SELECT qt.id FROM QuizTopic qt
                    WHERE qt.quiz.id = qa.quiz AND qt.topic.id = :topicId
              ))
            """)
    Long getUserScore(@Param("userId") Integer userId,
                      @Param("fromDate") Instant fromDate,
                      @Param("topicId") Integer topicId);

    long countByQuizAndCompletedAtIsNotNull(Integer quiz);

    boolean existsByQuizAndUserAndCompletedAtIsNotNull(Integer quiz, Integer user);
}
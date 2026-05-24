package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Enum.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizzesRepository extends JpaRepository<Quiz, Integer> {
    Quiz getQuizById(Integer id);

    @Query("""
            SELECT q FROM Quiz q
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(q.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(q.description) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:topicId IS NULL OR EXISTS (
                    SELECT qt.id FROM QuizTopic qt
                    WHERE qt.quiz = q.id AND qt.topic.id = :topicId
              ))
              AND (:type IS NULL OR q.type = :type)
            """)
    Page<Quiz> search(@Param("search") String search,
                      @Param("topicId") Integer topicId,
                      @Param("type") Type type,
                      Pageable pageable);
}

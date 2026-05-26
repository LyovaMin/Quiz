package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Quiz;
import by.lyofchik.quiz.Model.Entity.QuizTopic;
import by.lyofchik.quiz.Model.Entity.QuizTopicId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizTopicRepository extends JpaRepository<QuizTopic, QuizTopicId> {
    List<QuizTopic> findByQuiz(Quiz quiz);

    void deleteByQuiz(Quiz quiz);
}

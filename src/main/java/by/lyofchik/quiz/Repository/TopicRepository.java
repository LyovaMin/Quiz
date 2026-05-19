package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
}

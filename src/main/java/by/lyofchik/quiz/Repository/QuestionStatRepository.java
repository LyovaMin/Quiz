package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.QuestionStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionStatRepository extends JpaRepository<QuestionStat, Integer> {
}

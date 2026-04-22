package by.lyofchik.quiz.Repository;

import by.lyofchik.quiz.Model.Entity.GameMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameMemberRepository extends JpaRepository<GameMember, Integer> {
    GameMember findById(int id);
}

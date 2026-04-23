package by.lyofchik.quiz.Model.DTO.Request;

import by.lyofchik.quiz.Model.Enum.BonusType;
import lombok.Data;

import java.time.Instant;

@Data
public class GameRq {
    int userId;
    int questionId;
    int attemptId;
    String answer;
    BonusType activeBonus;
    Instant startedAt;
    Instant completedAt;
}

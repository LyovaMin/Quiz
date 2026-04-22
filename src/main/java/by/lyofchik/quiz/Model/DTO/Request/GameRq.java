package by.lyofchik.quiz.Model.DTO.Request;

import by.lyofchik.quiz.Model.Enum.BonusType;
import lombok.Data;

import java.time.Instant;

@Data
public class GameRq {
    int userId;
    int questionId;
    String answer;
    BonusType activeBonus;
    Instant startedAt;
    Instant completedAt;
}

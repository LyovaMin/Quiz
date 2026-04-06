package by.lyofchik.quiz.Model.DTO.Request;

import lombok.Data;

import java.time.Instant;

@Data
public class GameRq {
    int userId;
    int questionId;
    String answer;
    Instant startedAt;
    Instant completedAt;
}

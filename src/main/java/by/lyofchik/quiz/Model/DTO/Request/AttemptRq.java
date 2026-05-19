package by.lyofchik.quiz.Model.DTO.Request;

import lombok.Data;

@Data
public class AttemptRq {
    private Integer quizId;
    private Integer userId;
    private Integer score;
}

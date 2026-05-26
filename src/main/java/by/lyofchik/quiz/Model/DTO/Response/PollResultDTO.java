package by.lyofchik.quiz.Model.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PollResultDTO {
    private Integer answerId;
    private String answerText;
    private int votes;
    private double percentage;
}
package by.lyofchik.quiz.Model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AnswerDTO {
    private String text;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
}

package by.lyofchik.quiz.Model.DTO;

import by.lyofchik.quiz.Model.Enum.Types;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private String description;
    private Types type;
    private String image;
    private int points;
    private List<AnswerDTO> answers;
}

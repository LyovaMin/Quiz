package by.lyofchik.quiz.Model.DTO.Request;

import by.lyofchik.quiz.Model.DTO.QuestionDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class QuizRq {
    private String title;
    private String description;
    private String image;
    private Instant createdAt;
    private int createdBy;
    private int timeLimitSeconds;
    private List<QuestionDTO> questions;
}

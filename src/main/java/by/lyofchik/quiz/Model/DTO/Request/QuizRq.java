package by.lyofchik.quiz.Model.DTO.Request;

import by.lyofchik.quiz.Model.DTO.QuestionDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class QuizRq {
    private Integer id;
    private String title;
    private String description;
    private String image;
    private Instant createdAt;
    private int createdBy;
    private int timeLimitSeconds;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private List<QuestionDTO> questions;
}

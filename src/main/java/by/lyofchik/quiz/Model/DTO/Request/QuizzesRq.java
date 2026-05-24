package by.lyofchik.quiz.Model.DTO.Request;

import lombok.Data;

@Data
public class QuizzesRq {
    private int size;
    private int page;
    private String search;
    private Integer topicId;
    private String type;
}

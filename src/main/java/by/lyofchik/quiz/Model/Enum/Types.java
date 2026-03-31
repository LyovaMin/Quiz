package by.lyofchik.quiz.Model.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Types {
    HARD("Трудный"),
    MEDIUM("Средний"),
    EASY("Легкий");

    private final String typeText;
}

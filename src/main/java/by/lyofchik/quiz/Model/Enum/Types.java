package by.lyofchik.quiz.Model.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Types {
    HARD("Трудный", 100),
    MEDIUM("Средний", 50),
    EASY("Легкий", 25);

    private final String typeText;
    private final int defaultPoints;
}

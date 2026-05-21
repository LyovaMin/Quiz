package by.lyofchik.quiz.Model.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TopicName {
    COMPUTERS("Компьютеры"),
    PROGRAMMING("Программирование"),
    HISTORY("История"),
    GEOGRAPHY("География"),
    SCIENCE("Наука"),
    MOVIES("Кино"),
    MUSIC("Музыка"),
    SPORT("Спорт"),
    GAMES("Игры"),
    GENERAL("Общие знания");

    private final String topicName;
}

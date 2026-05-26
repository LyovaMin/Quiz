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
    GENERAL("Общие знания"),
    ART("Искусство"),
    LITERATURE("Литература"),
    ANIMALS("Животные"),
    FOOD("Еда"),
    FASHION("Мода");

    private final String topicName;
}
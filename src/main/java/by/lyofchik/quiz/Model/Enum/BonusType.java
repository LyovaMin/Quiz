package by.lyofchik.quiz.Model.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BonusType {
    DOUBLING("Doubling"),
    HALFTOHALF("50/50"),
    FREEZE("Freeze"),
    SECOND_CHANCE("Second Chance"),
    BONUS_POINTS("Bonus Points"),;

    private final String bonusName;
}

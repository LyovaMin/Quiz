package by.lyofchik.quiz.Model.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BonusType {
    DOUBLING("Doubling"),
    HALFTOHALF("50/50"),
    FREEZE("Freeze");

    private final String bonusName;
}

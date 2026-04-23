package by.lyofchik.quiz.Utils;

import jakarta.annotation.PostConstruct;

import java.util.NavigableMap;
import java.util.TreeMap;

public class Constants {
    static NavigableMap<Long, Float> bonuses = new TreeMap<>();

    @PostConstruct
    void init() {
        bonuses.put(0L, 1f);
        bonuses.put(2L, 1.1f);
        bonuses.put(4L, 1.2f);
        bonuses.put(6L, 1.3f);
        bonuses.put(8L, 1.4f);
        bonuses.put(10L, 1.5f);
    }

    public static float getGrade(long key){
        return bonuses.higherEntry(key).getValue();
    }
}

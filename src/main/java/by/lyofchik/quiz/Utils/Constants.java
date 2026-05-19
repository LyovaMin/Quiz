package by.lyofchik.quiz.Utils;

import java.util.NavigableMap;
import java.util.TreeMap;

public class Constants {
    static NavigableMap<Long, Float> bonuses = new TreeMap<>();

    static {
        bonuses.put(2L, 1.5f);
        bonuses.put(4L, 1.4f);
        bonuses.put(6L, 1.3f);
        bonuses.put(8L, 1.2f);
        bonuses.put(10L, 1.1f);
        bonuses.put(Long.MAX_VALUE, 1f);
    }

    public static float getGrade(long key){
        var entry = bonuses.higherEntry(key);
        return entry == null ? 1f : entry.getValue();
    }
}

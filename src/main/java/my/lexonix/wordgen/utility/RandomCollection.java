package my.lexonix.wordgen.utility;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {
    private final NavigableMap<Long, E> map = new TreeMap<>();
    private final Random random;
    private long total = 0;

    public RandomCollection(Random random) {
        this.random = random;
    }

    public void add(long weight, E result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }

    public E next() {
        long value = (long) (random.nextDouble() * total);
        return map.higherEntry(value).getValue();
    }
}

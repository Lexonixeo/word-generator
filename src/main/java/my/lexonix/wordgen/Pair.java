package my.lexonix.wordgen;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Pair<T1, T2> implements Serializable {
    @Serial private static final long serialVersionUID = -7181173857818165819L;

    private final T1 x;
    private final T2 y;

    public Pair(T1 first, T2 second) {
        this.x = first;
        this.y = second;
    }

    public T1 first() {
        return x;
    }

    public T2 second() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(x, pair.x) && Objects.equals(y, pair.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
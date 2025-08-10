package my.lexonix.wordgen.tokens;

import java.util.Objects;

public record Token(String token) {
    @Override
    public String toString() {
        return this.token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass().getSuperclass() != o.getClass().getSuperclass()) return false;
        Token token1 = (Token) o;
        return Objects.equals(token(), token1.token());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(token());
    }
}

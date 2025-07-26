package my.lexonix.wordgen;

import java.util.Objects;

public abstract class Token {
    private final String token;

    protected Token(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return this.token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token1 = (Token) o;
        return Objects.equals(getToken(), token1.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getToken());
    }
}

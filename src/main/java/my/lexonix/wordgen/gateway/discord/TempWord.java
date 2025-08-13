package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.library.LibraryMode;
import my.lexonix.wordgen.tokens.TokenizerMode;

public record TempWord(long cost, TokenizerMode mode, String word, String definition, LibraryMode lmode) {
}

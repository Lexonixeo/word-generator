package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.library.LibraryMode;

public record TempWordSentences(LibraryMode mode,
                                String sentence1, String sentence2, String sentence3, String sentence4) {
}

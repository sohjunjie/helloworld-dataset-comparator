package com.comparator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DelimiterDetectorTest {

    private DelimiterDetector delimiterDetector;

    @BeforeEach
    void setUp() {
        delimiterDetector = new DelimiterDetector();
    }

    @Test
    @DisplayName("Should detect comma delimiter when comma count is consistent")
    void shouldDetectCommaDelimiter() {
        String csv = """
                id,name,age,city
                1,Alice,30,New York
                2,Bob,25,San Francisco
                3,Charlie,35,Chicago
                """;
        char delimiter = delimiterDetector.detect(toStream(csv));
        assertThat(delimiter).isEqualTo(',');
    }

    @Test
    @DisplayName("Should detect tab delimiter when tab count is consistent")
    void shouldDetectTabDelimiter() {
        String tsv = "id\tname\tage\tcity\n1\tAlice\t30\tNew York\n2\tBob\t25\tSan Francisco\n";
        char delimiter = delimiterDetector.detect(toStream(tsv));
        assertThat(delimiter).isEqualTo('\t');
    }

    @Test
    @DisplayName("Should detect pipe delimiter when pipe count is consistent")
    void shouldDetectPipeDelimiter() {
        String psv = """
                id|name|age|city
                1|Alice|30|New York
                2|Bob|25|San Francisco
                """;
        char delimiter = delimiterDetector.detect(toStream(psv));
        assertThat(delimiter).isEqualTo('|');
    }

    @Test
    @DisplayName("Should detect semicolon delimiter when semicolon count is consistent")
    void shouldDetectSemicolonDelimiter() {
        String ssv = """
                id;name;age;city
                1;Alice;30;New York
                2;Bob;25;San Francisco
                """;
        char delimiter = delimiterDetector.detect(toStream(ssv));
        assertThat(delimiter).isEqualTo(';');
    }

    @Test
    @DisplayName("Should default to comma when input is ambiguous or inconsistent")
    void shouldDefaultToCommaOnAmbiguity() {
        String ambiguous = """
                hello world
                this has no delimiters
                random text here
                """;
        char delimiter = delimiterDetector.detect(toStream(ambiguous));
        assertThat(delimiter).isEqualTo(',');
    }

    @Test
    @DisplayName("Should default to comma on empty input")
    void shouldDefaultToCommaOnEmpty() {
        char delimiter = delimiterDetector.detect(toStream(""));
        assertThat(delimiter).isEqualTo(',');
    }

    @Test
    @DisplayName("Should default to comma when multiple delimiters tie with same occurrence count")
    void shouldDefaultToCommaOnTie() {
        // Line has 1 pipe and 1 tab consistently across lines
        String tied = "a|b\tc\n1|2\t3\n";
        char delimiter = delimiterDetector.detect(toStream(tied));
        assertThat(delimiter).isEqualTo(',');
    }

    @Test
    @DisplayName("Should analyze only up to first 10 lines")
    void shouldAnalyzeUpToFirstTenLines() {
        StringBuilder sb = new StringBuilder();
        // 10 lines of pipe
        for (int i = 0; i < 10; i++) {
            sb.append("a|b|c\n");
        }
        // 20 lines of commas afterwards
        for (int i = 0; i < 20; i++) {
            sb.append("a,b,c,d,e\n");
        }
        char delimiter = delimiterDetector.detect(toStream(sb.toString()));
        assertThat(delimiter).isEqualTo('|');
    }

    @ParameterizedTest
    @CsvSource(value = {
            "comma:','",
            "',':','",
            "tab:'\t'",
            "'\\t':'\t'",
            "pipe:'|'",
            "'|':'|'",
            "semicolon:';'",
            "';':';'",
            "'~':'~'",
            "'^':'^'",
            "':':':'"
    }, delimiter = ':')
    @DisplayName("Should resolve explicit override keywords and single characters")
    void shouldResolveOverride(String override, char expected) {
        char delimiter = delimiterDetector.resolveDelimiter(override, toStream("dummy"));
        assertThat(delimiter).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should auto-detect when override is auto or blank")
    void shouldAutoDetectWhenOverrideIsAutoOrBlank() {
        String pipeData = "id|name\n1|Alice\n";
        assertThat(delimiterDetector.resolveDelimiter("auto", toStream(pipeData))).isEqualTo('|');
        assertThat(delimiterDetector.resolveDelimiter("AUTO", toStream(pipeData))).isEqualTo('|');
        assertThat(delimiterDetector.resolveDelimiter(null, toStream(pipeData))).isEqualTo('|');
        assertThat(delimiterDetector.resolveDelimiter("   ", toStream(pipeData))).isEqualTo('|');
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when custom delimiter has multiple characters and is not recognized")
    void shouldThrowOnInvalidCustomDelimiter() {
        assertThatThrownBy(() -> delimiterDetector.resolveDelimiter("custom_unknown", toStream("dummy")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid delimiter");
    }

    private ByteArrayInputStream toStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}

package com.comparator.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AppPropertiesTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    @DisplayName("AppProperties binds correctly from application.yml")
    void shouldBindPropertiesCorrectly() {
        assertThat(appProperties).isNotNull();
        assertThat(appProperties.storage()).isNotNull();
        assertThat(appProperties.storage().path()).isEqualTo("./data");
        assertThat(appProperties.upload()).isNotNull();
        assertThat(appProperties.upload().maxFileSize()).isEqualTo("500MB");
        assertThat(appProperties.cleanup()).isNotNull();
        assertThat(appProperties.cleanup().ttlHours()).isEqualTo(1);
        assertThat(appProperties.comparison()).isNotNull();
        assertThat(appProperties.comparison().timeoutMinutes()).isEqualTo(30);
    }
}

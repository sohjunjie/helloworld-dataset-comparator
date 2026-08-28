package com.comparator.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.servlet.multipart.max-file-size=50B",
        "spring.servlet.multipart.max-request-size=100B"
})
class SpringMultipartLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return HTTP 413 with descriptive message when file exceeds spring.servlet.multipart.max-file-size")
    void shouldReturn413WhenFileExceedsSpringMultipartLimit() throws Exception {
        byte[] oversizedData = new byte[150];
        MockMultipartFile ds1File = new MockMultipartFile(
                "ds1File", "large_multipart.csv", "text/csv", oversizedData);
        MockMultipartFile ds2File = new MockMultipartFile(
                "ds2File", "small.csv", "text/csv", "id,name\n1,A\n".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/v1/comparisons/upload")
                        .file(ds1File)
                        .file(ds2File))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.error").value("Payload Too Large"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}

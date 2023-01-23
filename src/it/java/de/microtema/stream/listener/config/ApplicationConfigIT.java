package de.microtema.stream.listener.config;

import de.microtema.stream.listener.TestApplication;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TestApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"stream-listener.enabled=false"})
class ApplicationConfigIT {

    @Inject
    RestTemplate restTemplate;

    @Test
    void restTemplate() {

        assertNotNull(restTemplate);
    }
}

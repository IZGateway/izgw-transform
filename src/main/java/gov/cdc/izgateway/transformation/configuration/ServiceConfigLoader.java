package gov.cdc.izgateway.transformation.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

@Configuration
@Log
public class ServiceConfigLoader {

    @Bean
    public ServiceConfig serviceConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("pipelines_test_preconditions.json");

        try {
            return mapper.readValue(is, ServiceConfig.class);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error parsing JSON content: " + e.getMessage());
            throw e;
        }
    }
}

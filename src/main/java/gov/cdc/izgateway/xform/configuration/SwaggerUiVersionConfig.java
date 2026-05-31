package gov.cdc.izgateway.xform.configuration;

import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.webjars.WebJarVersionLocator;

/**
 * Aligns Springdoc's swagger-ui resource-handler version with the actual
 * {@code org.webjars:swagger-ui} version present on the classpath.
 *
 * <p>Springdoc reads its version from {@code springdoc.swagger-ui.version}
 * and falls back to a value baked into {@code springdoc.config.properties}
 * inside the springdoc starter jar. The izgw-bom independently overrides
 * the {@code org.webjars:swagger-ui} version, so the two drift and the UI
 * 404s on {@code /swagger/swagger-ui/index.html} after a webjar bump.
 *
 * <p>Implemented as a {@link BeanPostProcessor} returned from a static
 * {@code @Bean} factory so it runs even with the application-wide
 * {@code spring.main.lazy-initialization=true} (BPPs are always eagerly
 * registered) and so the version is mutated during the
 * {@link SwaggerUiConfigProperties} bean's own initialization — before any
 * other bean (e.g. Springdoc's resource-handler configurer) reads it.
 *
 * <p>See openspec change {@code auto-detect-swagger-ui-version}.
 */
@Slf4j
@Configuration
public class SwaggerUiVersionConfig {

    static final String SWAGGER_UI_WEBJAR_NAME = "swagger-ui";

    @Bean
    public static BeanPostProcessor swaggerUiVersionAligner() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof SwaggerUiConfigProperties props) {
                    alignVersionFromWebjar(props);
                }
                return bean;
            }
        };
    }

    static void alignVersionFromWebjar(SwaggerUiConfigProperties props) {
        alignVersion(props, () -> new WebJarVersionLocator().version(SWAGGER_UI_WEBJAR_NAME));
    }

    static void alignVersion(SwaggerUiConfigProperties props, Supplier<String> detector) {
        try {
            String detected = detector.get();
            if (detected == null || detected.isBlank()) {
                log.warn("Could not detect {} webjar version; leaving Springdoc default in place", SWAGGER_UI_WEBJAR_NAME);
                return;
            }
            props.setVersion(detected);
            log.info("Detected {} webjar version: {}", SWAGGER_UI_WEBJAR_NAME, detected);
        } catch (RuntimeException e) {
            log.warn("Failed to detect {} webjar version; leaving Springdoc default in place", SWAGGER_UI_WEBJAR_NAME, e);
        }
    }
}

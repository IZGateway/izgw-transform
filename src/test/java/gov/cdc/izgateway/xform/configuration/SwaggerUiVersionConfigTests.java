package gov.cdc.izgateway.xform.configuration;

import org.junit.jupiter.api.Test;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.webjars.WebJarVersionLocator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwaggerUiVersionConfigTests {

    private static final String LEGACY_HARDCODED_PIN = "5.32.5";
    private static final String SPRINGDOC_BUNDLED_DEFAULT = "5.32.2";

    @Test
    void alignerSetsVersionToActualWebjarVersion() {
        String expected = new WebJarVersionLocator().version(SwaggerUiVersionConfig.SWAGGER_UI_WEBJAR_NAME);
        assertNotNull(expected, "swagger-ui webjar must be on the test classpath");

        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        SwaggerUiVersionConfig.alignVersionFromWebjar(props);

        assertEquals(expected, props.getVersion());
        assertTrue(props.getVersion().matches("\\d+\\.\\d+\\.\\d+"),
                "detected version must look like X.Y.Z; got " + props.getVersion());
    }

    @Test
    void resolvedVersionIsNotLegacyOrSpringdocFallback() {
        String resolvedWebjarVersion = new WebJarVersionLocator().version(SwaggerUiVersionConfig.SWAGGER_UI_WEBJAR_NAME);
        assertNotNull(resolvedWebjarVersion);

        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        SwaggerUiVersionConfig.alignVersionFromWebjar(props);

        assertFalse(LEGACY_HARDCODED_PIN.equals(props.getVersion())
                        && !LEGACY_HARDCODED_PIN.equals(resolvedWebjarVersion),
                "version field should not be stuck on the legacy " + LEGACY_HARDCODED_PIN + " pin");
        assertFalse(SPRINGDOC_BUNDLED_DEFAULT.equals(props.getVersion())
                        && !SPRINGDOC_BUNDLED_DEFAULT.equals(resolvedWebjarVersion),
                "version field should not be stuck on Springdoc's bundled default "
                        + SPRINGDOC_BUNDLED_DEFAULT + " when the webjar is a different version");
    }

    @Test
    void alignerOverridesPreExistingConfiguredVersion() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        props.setVersion("bogus-pre-existing-pin");

        SwaggerUiVersionConfig.alignVersionFromWebjar(props);

        String expected = new WebJarVersionLocator().version(SwaggerUiVersionConfig.SWAGGER_UI_WEBJAR_NAME);
        assertEquals(expected, props.getVersion(),
                "an existing springdoc.swagger-ui.version value must be overridden by the on-classpath webjar version");
    }

    @Test
    void nullDetectionLeavesVersionUntouched() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        props.setVersion("preserved-on-failure");

        SwaggerUiVersionConfig.alignVersion(props, () -> null);

        assertEquals("preserved-on-failure", props.getVersion());
    }

    @Test
    void blankDetectionLeavesVersionUntouched() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        props.setVersion("preserved-on-failure");

        SwaggerUiVersionConfig.alignVersion(props, () -> "   ");

        assertEquals("preserved-on-failure", props.getVersion());
    }

    @Test
    void exceptionDuringDetectionLeavesVersionUntouchedAndDoesNotPropagate() {
        SwaggerUiConfigProperties props = new SwaggerUiConfigProperties();
        props.setVersion("preserved-on-failure");

        SwaggerUiVersionConfig.alignVersion(props, () -> {
            throw new IllegalStateException("simulated webjar lookup failure");
        });

        assertEquals("preserved-on-failure", props.getVersion());
    }
}

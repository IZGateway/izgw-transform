package gov.cdc.izgateway.xform.endpoints.fhir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.endpoints.hub.HubController;

class FhirControllerTests {

    @Test
    void postWithoutSearchReturnsMethodNotAllowed() {
        FhirController controller = new FhirController(
            mock(HubController.class),
            new FhirController.FhirConfiguration(),
            mock(AccessControlRegistry.class)
        );

        ResponseEntity<Void> response = controller.iisSearchPostWithoutSuffix("dev");

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals(Set.of(HttpMethod.GET, HttpMethod.HEAD), response.getHeaders().getAllow());
    }

    @Test
    void postWithoutSearchIsExplicitlyMapped() throws NoSuchMethodException {
        RequestMapping mapping = FhirController.class
            .getMethod("iisSearchPostWithoutSuffix", String.class)
            .getAnnotation(RequestMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new RequestMethod[] { RequestMethod.POST }, mapping.method());
        assertArrayEquals(
            new String[] {
                "/{destinationId}/Immunization",
                "/{destinationId}/ImmunizationRecommendation",
                "/{destinationId}/Patient"
            },
            mapping.value()
        );
    }
}

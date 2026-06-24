package gov.cdc.izgateway.xform.endpoints.fhir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.xform.endpoints.hub.HubController;
import gov.cdc.izgw.v2tofhir.utils.IzQuery;
import gov.cdc.izgw.v2tofhir.utils.QBPUtils;

import ca.uhn.hl7v2.model.v251.message.QBP_Q11;
import ca.uhn.hl7v2.util.Terser;
import jakarta.servlet.http.HttpServletRequest;

class FhirControllerTests {

    /** Base64 (URL) of the FHIR id "TEST|0000001" (system|value). */
    private static final String ENCODED_ID = "VEVTVHwwMDAwMDAx";

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

    // --- subject -> patient alias --------------------------------------------------------

    @Test
    void subjectPatientReferenceIsAliasedToPatient() {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("subject", "Patient/" + ENCODED_ID);

        HttpServletRequest result = FhirController.normalizeSubjectToPatient(req);

        assertEquals("Patient/" + ENCODED_ID, result.getParameter("patient"));
        assertNull(result.getParameter("subject"), "subject should be removed after aliasing");
    }

    @Test
    void subjectBareIdIsAliasedToPatient() throws Exception {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("subject", ENCODED_ID);

        HttpServletRequest result = FhirController.normalizeSubjectToPatient(req);

        assertEquals(ENCODED_ID, result.getParameter("patient"));
        // and a bare id must decode to the same QPD-3 a bare patient=<id> would produce, end to end
        assertArrayEquals(new String[] { "0000001", "TEST", "MR" }, qpd3(asListMap(result)));
    }

    @Test
    void subjectAndPatientProduceIdenticalQpd3() throws Exception {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("subject", "Patient/" + ENCODED_ID);
        HttpServletRequest viaSubject = FhirController.normalizeSubjectToPatient(req);

        String[] fromSubject = qpd3(asListMap(viaSubject));
        String[] fromPatient = qpd3(Map.of("patient", List.of("Patient/" + ENCODED_ID)));

        assertArrayEquals(fromPatient, fromSubject, "subject= must yield the same QPD-3 as patient=");
        // QPD-3.1 = id value, QPD-3.4.1 = assigning authority, QPD-3.5 = identifier type code
        assertArrayEquals(new String[] { "0000001", "TEST", "MR" }, fromSubject);
    }

    @Test
    void groupSubjectIsDroppedAndFallsThroughToValidation() {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("subject", "Group/abc123");

        HttpServletRequest result = FhirController.normalizeSubjectToPatient(req);

        assertNull(result.getParameter("patient"), "Group reference must not be aliased to patient");
        assertNull(result.getParameter("subject"), "non-Patient subject is dropped");

        // Downstream, the empty query yields the existing clean validation error, not a decode error.
        QBP_Q11 qbp = newMessage();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> QBPUtils.addParamsToQPD(qbp, asListMap(result), false));
        assertTrue(ex.getMessage().contains("patient.identifier or the patient name and birthDate"));
    }

    @Test
    void patientTakesPrecedenceOverSubject() {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("patient", "Patient/AAA");
        req.addParameter("subject", "Patient/BBB");

        HttpServletRequest result = FhirController.normalizeSubjectToPatient(req);

        assertArrayEquals(new String[] { "Patient/AAA" }, result.getParameterValues("patient"));
        assertNull(result.getParameter("subject"));
    }

    @Test
    void patientIdentifierTakesPrecedenceOverSubject() {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("patient.identifier", "TEST|0000001");
        req.addParameter("subject", "Patient/" + ENCODED_ID);

        HttpServletRequest result = FhirController.normalizeSubjectToPatient(req);

        // subject must NOT be aliased when patient.identifier already identifies the patient,
        // otherwise we'd add a second/duplicate QPD-3 identifier.
        assertNull(result.getParameter("patient"), "subject must not alias to patient when patient.identifier is present");
        assertNull(result.getParameter("subject"), "subject is dropped");
        assertEquals("TEST|0000001", result.getParameter("patient.identifier"));
    }

    @Test
    void existingPatientQueryIsUnchangedWhenNoSubject() {
        RequestWithModifiableParameters req = emptyRequest();
        req.addParameter("patient", "Patient/" + ENCODED_ID);

        HttpServletRequest result = FhirController.normalizeSubjectToPatient(req);

        // No subject present: request is returned untouched (same instance).
        assertSame(req, result);
        assertEquals("Patient/" + ENCODED_ID, result.getParameter("patient"));
    }

    @Test
    void isPatientReferenceClassifiesReferenceTypes() {
        assertTrue(FhirController.isPatientReference("Patient/123"));
        assertTrue(FhirController.isPatientReference(ENCODED_ID), "bare id has no type, treated as Patient");
        assertTrue(FhirController.isPatientReference("http://example.org/fhir/Patient/123"));
        assertFalse(FhirController.isPatientReference("Group/123"));
        assertFalse(FhirController.isPatientReference(null));
        assertFalse(FhirController.isPatientReference("   "));
    }

    // --- helpers -------------------------------------------------------------------------

    private static RequestWithModifiableParameters emptyRequest() {
        HttpServletRequest base = mock(HttpServletRequest.class);
        when(base.getParameterMap()).thenReturn(Collections.emptyMap());
        return new RequestWithModifiableParameters(base);
    }

    private static Map<String, List<String>> asListMap(HttpServletRequest req) {
        return ((RequestWithModifiableParameters) req).getParameters();
    }

    private static QBP_Q11 newMessage() {
        try {
            return QBPUtils.createMessage(IzQuery.HISTORY);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Build a QBP from the given (non-patient-context) params and read QPD-3 components. */
    private static String[] qpd3(Map<String, List<String>> params) throws Exception {
        QBP_Q11 qbp = QBPUtils.createMessage(IzQuery.HISTORY);
        QBPUtils.addParamsToQPD(qbp, params, false);
        Terser t = new Terser(qbp);
        return new String[] {
            t.get("/QPD-3-1"),
            t.get("/QPD-3-4-1"),
            t.get("/QPD-3-5")
        };
    }
}

package gov.cdc.izgateway.xform.endpoints.fhir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import gov.cdc.izgateway.configuration.AppProperties;
import gov.cdc.izgateway.logging.RequestContext;
import gov.cdc.izgateway.security.AccessControlRegistry;
import gov.cdc.izgateway.security.IzgPrincipal;
import gov.cdc.izgateway.soap.message.SubmitSingleMessageResponse;
import gov.cdc.izgateway.xform.endpoints.hub.HubController;
import gov.cdc.izgw.v2tofhir.utils.ContentUtils;
import gov.cdc.izgw.v2tofhir.utils.FhirIdCodec;
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

    // --- FHIR response content negotiation -----------------------------------------------
    //
    // The FHIR endpoints bypass Spring's global content negotiation (which is SOAP-oriented:
    // it ignores Accept and defaults to XML) by setting Content-Type explicitly via
    // ContentUtils.getHeaders(). These tests pin that behavior for the paths that build
    // their own ResponseEntity rather than reusing processQuery's headers.

    private static final String MATCH_URI = "/fhir/dev/Patient/$match";

    /** A minimal RSP_K11 response as returned by an IIS for an immunization history query. */
    private static final String RSP_MESSAGE = String.join("\r",
        "MSH|^~\\&|TESTIIS|TESTIIS|TESTAPP|TESTORG|20240101120000||RSP^K11^RSP_K11|X234|P|2.5.1",
        "MSA|AA|1234",
        "QAK|Q1|OK|Z34^Request Immunization History^CDCPHINVS",
        "QPD|Z34^Request Immunization History^CDCPHINVS|Q1|0000001^^^TEST^MR",
        "PID|1||0000001^^^TEST^MR||CuyahogaAIRA^MarnyAIRA^^^^^L||19600507|F"
    );

    @AfterEach
    void clearRequestContext() {
        RequestContext.clear();
    }

    @Test
    void matchHonorsFhirJsonAccept() throws Exception {
        initRequestContext();
        FhirController controller = controller(hubReturning(RSP_MESSAGE));
        HttpServletRequest req = fhirRequest(MATCH_URI, ContentUtils.FHIR_PLUS_JSON_VALUE);

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", matchParameters(), req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        Bundle b = (Bundle) res.getBody();
        assertNotNull(b);
        assertEquals(BundleType.SEARCHSET, b.getType());
        assertTrue(b.getEntry().stream().anyMatch(e -> e.getResource() instanceof Patient),
            "match result should contain the matched Patient");
    }

    @Test
    void matchHonorsXmlAccept() throws Exception {
        initRequestContext();
        FhirController controller = controller(hubReturning(RSP_MESSAGE));
        HttpServletRequest req = fhirRequest(MATCH_URI, "application/xml");

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", matchParameters(), req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_XML_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void matchDefaultsToJsonWithoutAccept() throws Exception {
        initRequestContext();
        FhirController controller = controller(hubReturning(RSP_MESSAGE));
        HttpServletRequest req = fhirRequest(MATCH_URI, null);

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", matchParameters(), req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void matchHonorsFormatParameter() throws Exception {
        initRequestContext();
        FhirController controller = controller(hubReturning(RSP_MESSAGE));
        // _format must be read from the original request; the internal wrapper's
        // parameters are reset before the query is built.
        HttpServletRequest req = fhirRequest(MATCH_URI, null);
        when(req.getParameter("_format")).thenReturn("xml");

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", matchParameters(), req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_XML_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void matchInvalidBodyErrorHonorsAccept() throws Exception {
        FhirController controller = controller(mock(HubController.class));
        HttpServletRequest req = fhirRequest(MATCH_URI, ContentUtils.FHIR_PLUS_JSON_VALUE);

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", new Bundle(), req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertTrue(res.getBody() instanceof OperationOutcome);
    }

    @Test
    void connectionTestHonorsAccept() throws Exception {
        FhirController controller = controller(mock(HubController.class));
        HttpServletRequest req = fhirRequest("/fhir/dev/Patient", ContentUtils.FHIR_PLUS_JSON_VALUE);
        when(req.getParameter("_summary")).thenReturn("count");

        ResponseEntity<Bundle> res = controller.iisQuery("dev", req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertNotNull(res.getBody());
        assertEquals(100, res.getBody().getTotal());
    }

    @Test
    void readNotFoundHonorsAccept() throws Exception {
        FhirController controller = controller(mock(HubController.class));
        // Decodes cleanly but has no system|value pair, so the read reports not-found
        // before any downstream query is attempted.
        String id = FhirIdCodec.encode("TEST");
        HttpServletRequest req = fhirRequest("/fhir/dev/Patient/" + id, ContentUtils.FHIR_PLUS_JSON_VALUE);

        ResponseEntity<Resource> res = controller.iisRead("dev", id, req);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertTrue(res.getBody() instanceof OperationOutcome);
    }

    // --- helpers -------------------------------------------------------------------------

    private static FhirController controller(HubController hub) {
        return new FhirController(hub, new FhirController.FhirConfiguration(), mock(AccessControlRegistry.class));
    }

    private static HubController hubReturning(String hl7Message) throws Exception {
        HubController hub = mock(HubController.class);
        doReturn(new ResponseEntity<>(new SubmitSingleMessageResponse(hl7Message), HttpStatus.OK))
            .when(hub).submitSoapRequest(any(), any());
        return hub;
    }

    private static HttpServletRequest fhirRequest(String uri, String accept) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameterMap()).thenReturn(Collections.emptyMap());
        when(req.getRequestURI()).thenReturn(uri);
        when(req.getHeader(HttpHeaders.ACCEPT)).thenReturn(accept);
        return req;
    }

    private static Parameters matchParameters() {
        Patient patient = new Patient();
        patient.addName(new HumanName().setFamily("CuyahogaAIRA").addGiven("MarnyAIRA"));
        patient.setBirthDateElement(new DateType("1960-05-07"));
        Parameters params = new Parameters();
        params.addParameter().setName("resource").setResource(patient);
        return params;
    }

    private static void initRequestContext() {
        if (AppProperties.getInstance() == null) {
            // TransactionData's constructor consults the static AppProperties instance,
            // which Spring registers at startup; the constructor self-registers it.
            new AppProperties();
        }
        RequestContext.init();
        RequestContext.getSourceInfo().setCommonName("test");
        IzgPrincipal principal = new IzgPrincipal() {
            @Override
            public String getSerialNumberHex() {
                return null;
            }
        };
        principal.setName("TESTAPP");
        principal.setOrganization("TESTORG");
        RequestContext.setPrincipal(principal);
    }

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

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    // --- $match too-many-matches -> top-level OperationOutcome ---------------------------
    //
    // When the IIS reports "too much data found" (QAK-2 = TM) with no patient records,
    // $match must return a top-level OperationOutcome whose details.text contains the
    // exact phrase DIBBs Query Connector matches on, with HTTP 422.

    private static final String V2_0208_SYSTEM = "http://terminology.hl7.org/CodeSystem/v2-0208";
    private static final String NO_CERTAIN_MATCH_PHRASE = "did not find a certain match";

    /** A Z33-style "too much data found" response: QAK-2 = TM, no patient records. */
    private static final String RSP_TOO_MANY = String.join("\r",
        "MSH|^~\\&|TESTIIS|TESTIIS|TESTAPP|TESTORG|20240101120000||RSP^K11^RSP_K11|X235|P|2.5.1",
        "MSA|AA|1234",
        "QAK|Q1|TM|Z34^Request Immunization History^CDCPHINVS",
        "QPD|Z34^Request Immunization History^CDCPHINVS|Q1|0000001^^^TEST^MR"
    );

    @Test
    void detectionTriggersOnTmWithNoPatients() {
        Bundle b = new Bundle();
        b.addEntry().setResource(outcomeWithCoding(V2_0208_SYSTEM, "TM"));

        OperationOutcomeIssueComponent issue = FhirController.findTooManyMatchesIssue(b);

        assertNotNull(issue);
        assertEquals("TM", issue.getDetails().getCodingFirstRep().getCode());
    }

    @Test
    void detectionMatchesTmCaseInsensitively() {
        Bundle b = new Bundle();
        b.addEntry().setResource(outcomeWithCoding(V2_0208_SYSTEM, "tm"));

        assertNotNull(FhirController.findTooManyMatchesIssue(b));
    }

    @Test
    void detectionDoesNotTriggerWhenPatientPresent() {
        // Candidates returned alongside a TM status must never be discarded.
        Bundle b = new Bundle();
        b.addEntry().setResource(outcomeWithCoding(V2_0208_SYSTEM, "TM"));
        b.addEntry().setResource(new Patient());

        assertNull(FhirController.findTooManyMatchesIssue(b));
    }

    @Test
    void detectionDoesNotTriggerForOtherQueryStatuses() {
        for (String code : List.of("OK", "NF", "AE", "AR")) {
            Bundle b = new Bundle();
            b.addEntry().setResource(outcomeWithCoding(V2_0208_SYSTEM, code));

            assertNull(FhirController.findTooManyMatchesIssue(b), "must not trigger for " + code);
        }
    }

    @Test
    void detectionDoesNotTriggerForOtherSystemsOrMissingDetails() {
        Bundle wrongSystem = new Bundle();
        wrongSystem.addEntry().setResource(outcomeWithCoding("http://example.org/other", "TM"));
        assertNull(FhirController.findTooManyMatchesIssue(wrongSystem));

        Bundle noDetails = new Bundle();
        OperationOutcome bare = new OperationOutcome();
        bare.addIssue().setSeverity(IssueSeverity.INFORMATION);
        noDetails.addEntry().setResource(bare);
        assertNull(FhirController.findTooManyMatchesIssue(noDetails));

        assertNull(FhirController.findTooManyMatchesIssue(new Bundle()));
        assertNull(FhirController.findTooManyMatchesIssue(null));
    }

    @Test
    void noCertainMatchOutcomeHasRequiredShape() {
        OperationOutcomeIssueComponent source = new OperationOutcomeIssueComponent()
            .setDetails(new CodeableConcept()
                .addCoding(new Coding(V2_0208_SYSTEM, "TM", "Too much data found")));

        OperationOutcome oo = FhirController.noCertainMatchOutcome(source);

        OperationOutcomeIssueComponent issue = oo.getIssueFirstRep();
        assertEquals(IssueSeverity.WARNING, issue.getSeverity());
        assertEquals(IssueType.MULTIPLEMATCHES, issue.getCode());
        assertTrue(issue.getDetails().getText().contains(NO_CERTAIN_MATCH_PHRASE),
            "details.text must contain the literal DIBBs trigger phrase");
        Coding kept = issue.getDetails().getCodingFirstRep();
        assertEquals(V2_0208_SYSTEM, kept.getSystem());
        assertEquals("TM", kept.getCode());
        assertEquals("Too much data found", kept.getDisplay(), "source coding display must survive");
    }

    @Test
    void matchTooManyReturnsTopLevelOperationOutcome() throws Exception {
        initRequestContext();
        FhirController controller = controller(hubReturning(RSP_TOO_MANY));
        HttpServletRequest req = fhirRequest(MATCH_URI, ContentUtils.FHIR_PLUS_JSON_VALUE);

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", matchParameters(), req);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertTrue(res.getBody() instanceof OperationOutcome, "top-level resource must be an OperationOutcome, not a Bundle");
        OperationOutcome oo = (OperationOutcome) res.getBody();
        assertTrue(oo.getIssue().stream().anyMatch(i ->
                i.getDetails().getText() != null && i.getDetails().getText().contains(NO_CERTAIN_MATCH_PHRASE)),
            "some issue.details.text must contain the DIBBs trigger phrase");
        assertTrue(oo.getIssue().stream().flatMap(i -> i.getDetails().getCoding().stream())
                .anyMatch(c -> V2_0208_SYSTEM.equals(c.getSystem()) && "TM".equals(c.getCode())),
            "the v2-0208 TM coding must be retained for provenance");
    }

    @Test
    void matchNoDataFoundStillReturnsBundle() throws Exception {
        // A true no-match (QAK-2 = NF) keeps the 200 searchset Bundle so DIBBs shows
        // "No Records Found", distinguishable from the too-many outcome.
        initRequestContext();
        FhirController controller = controller(hubReturning(RSP_TOO_MANY.replace("|TM|", "|NF|")));
        HttpServletRequest req = fhirRequest(MATCH_URI, ContentUtils.FHIR_PLUS_JSON_VALUE);

        ResponseEntity<Resource> res = controller.iisPatientMatch("dev", matchParameters(), req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertTrue(res.getBody() instanceof Bundle);
        assertTrue(((Bundle) res.getBody()).getEntry().stream()
            .noneMatch(e -> e.getResource() instanceof Patient));
    }

    private static OperationOutcome outcomeWithCoding(String system, String code) {
        OperationOutcome oo = new OperationOutcome();
        oo.addIssue().setDetails(new CodeableConcept().addCoding(new Coding(system, code, null)));
        return oo;
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

    // --- CapabilityStatement (/metadata) -------------------------------------------------

    @Test
    void metadataReturnsCapabilityStatement() {
        ResponseEntity<CapabilityStatement> response = newController().metadata("dev");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CapabilityStatement cs = response.getBody();
        assertNotNull(cs);
        assertEquals(Enumerations.PublicationStatus.ACTIVE, cs.getStatus());
    }

    @Test
    void metadataDeclaresFhirVersionAndJsonFormat() {
        CapabilityStatement cs = newController().metadata("dev").getBody();

        assertNotNull(cs);
        assertEquals(Enumerations.FHIRVersion._4_0_1, cs.getFhirVersion());
        assertTrue(
            cs.getFormat().stream().anyMatch(f -> "application/fhir+json".equals(f.getValue())),
            "format should include application/fhir+json"
        );
    }

    @Test
    void metadataAdvertisesSearchableResources() {
        CapabilityStatement cs = newController().metadata("dev").getBody();

        assertNotNull(cs);
        assertEquals(1, cs.getRest().size());
        CapabilityStatement.CapabilityStatementRestComponent rest = cs.getRestFirstRep();
        assertEquals(CapabilityStatement.RestfulCapabilityMode.SERVER, rest.getMode());

        for (String type : List.of("Patient", "Immunization", "ImmunizationRecommendation")) {
            CapabilityStatement.CapabilityStatementRestResourceComponent resource =
                findResource(rest, type);
            assertNotNull(resource, "missing resource " + type);
            assertTrue(
                resource.getInteraction().stream()
                    .anyMatch(i -> i.getCode() == CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE),
                type + " should declare the search-type interaction"
            );
        }
    }

    @Test
    void metadataAdvertisesPatientMatchOperation() {
        CapabilityStatement cs = newController().metadata("dev").getBody();

        assertNotNull(cs);
        CapabilityStatement.CapabilityStatementRestResourceComponent patient =
            findResource(cs.getRestFirstRep(), "Patient");
        assertNotNull(patient);

        CapabilityStatement.CapabilityStatementRestResourceOperationComponent match =
            patient.getOperation().stream()
                .filter(op -> "match".equals(op.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(match, "Patient $match operation should be advertised");
        assertEquals(
            "http://hl7.org/fhir/OperationDefinition/Patient-match",
            match.getDefinition()
        );
    }

    @Test
    void metadataIsDestinationAgnostic() {
        FhirController controller = newController();
        CapabilityStatement known = controller.metadata("dev").getBody();
        CapabilityStatement unknown = controller.metadata("some-unknown-destination").getBody();

        assertNotNull(known);
        assertNotNull(unknown);
        assertTrue(
            known.equalsDeep(unknown),
            "CapabilityStatement should be identical regardless of destinationId"
        );
    }

    @Test
    void metadataIsExplicitlyMapped() throws NoSuchMethodException {
        GetMapping mapping = FhirController.class
            .getMethod("metadata", String.class)
            .getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new String[] { "/{destinationId}/metadata" }, mapping.value());
        assertTrue(
            Arrays.asList(mapping.produces()).contains("application/fhir+json"),
            "produces should include application/fhir+json"
        );
    }

    private static FhirController newController() {
        return new FhirController(
            mock(HubController.class),
            new FhirController.FhirConfiguration(),
            mock(AccessControlRegistry.class)
        );
    }

    private static CapabilityStatement.CapabilityStatementRestResourceComponent findResource(
        CapabilityStatement.CapabilityStatementRestComponent rest, String type) {
        return rest.getResource().stream()
            .filter(r -> type.equals(r.getType()))
            .findFirst()
            .orElse(null);
    }
}

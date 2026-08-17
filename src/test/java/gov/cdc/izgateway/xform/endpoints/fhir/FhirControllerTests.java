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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Immunization.ImmunizationProtocolAppliedComponent;
import org.hl7.fhir.r4.model.Observation;
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

    /**
     * A Z32 response (evaluated history) for an immunization history query: one patient,
     * two administered doses, each with an administering performer and facility so the
     * conversion produces referenced Practitioner/Organization resources.
     */
    private static final String RSP_Z32_MESSAGE = String.join("\r",
        "MSH|^~\\&|TESTIIS|TESTIIS|TESTAPP|TESTORG|20240101120000||RSP^K11^RSP_K11|X235|P|2.5.1|||||||||Z32^CDCPHINVS",
        "MSA|AA|1234",
        "QAK|Q1|OK|Z34^Request Immunization History^CDCPHINVS",
        "QPD|Z34^Request Immunization History^CDCPHINVS|Q1|0000001^^^TEST^MR",
        "PID|1||0000001^^^TEST^MR||CuyahogaAIRA^MarnyAIRA^^^^^L||19600507|F",
        "ORC|RE||IZ-1^NDA|||||||^Nurse^Nancy^^^^^^NDA^L||^Clinician^Carl^^^^^^NDA^L",
        "RXA|0|1|20200101||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM|||"
            + "^Nurse^Nancy^^^^^^NDA^L|^^^TESTFAC^^^^^Test Facility",
        "OBX|1|CE|64994-7^Vaccine funding program eligibility category^LN|1|"
            + "V02^VFC eligible Medicaid/Medicaid managed care^HL70064||||||F",
        "ORC|RE||IZ-2^NDA|||||||^Nurse^Nancy^^^^^^NDA^L||^Clinician^Carl^^^^^^NDA^L",
        "RXA|0|1|20200201||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM|||"
            + "^Nurse^Nancy^^^^^^NDA^L|^^^TESTFAC^^^^^Test Facility",
        "OBX|1|CE|64994-7^Vaccine funding program eligibility category^LN|1|"
            + "V02^VFC eligible Medicaid/Medicaid managed care^HL70064||||||F"
    );

    /**
     * A Z42 response (evaluated history + forecast): one patient, two administered doses carrying
     * the evaluation OBX segments that only a Z42 returns (30973-2 dose number, 59782-3 doses in
     * series, 59779-9 schedule used, 64994-7 funding eligibility), and one forecast group
     * (RXA-5 == 998) carrying the forecast OBX segments.
     */
    private static final String RSP_Z42_MESSAGE = String.join("\r",
        "MSH|^~\\&|TESTIIS|TESTIIS|TESTAPP|TESTORG|20240101120000||RSP^K11^RSP_K11|X236|P|2.5.1|||||||||Z42^CDCPHINVS",
        "MSA|AA|1234",
        "QAK|Q1|OK|Z44^Request Evaluated History and Forecast^CDCPHINVS",
        "QPD|Z44^Request Evaluated History and Forecast^CDCPHINVS|Q1|0000001^^^TEST^MR",
        "PID|1||0000001^^^TEST^MR||CuyahogaAIRA^MarnyAIRA^^^^^L||19600507|F",
        "ORC|RE||IZ-1^NDA",
        "RXA|0|1|20200101||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM",
        "OBX|1|CE|30956-7^Vaccine Type^LN|1|208^COVID-19 mRNA vaccine^CVX||||||F",
        "OBX|2|NM|30973-2^Dose Number in Series^LN|1|1|NA^Not Applicable^HL70353|||||F",
        "OBX|3|NM|59782-3^Number of doses in primary series^LN|1|2|||||F",
        "OBX|4|CE|59779-9^Immunization Schedule Used^LN|1|VXC16^ACIP^CDCPHINVS||||||F",
        "OBX|5|CE|64994-7^Vaccine funding program eligibility category^LN|2|"
            + "V02^VFC eligible Medicaid/Medicaid managed care^HL70064||||||F|||||VXC40^Vaccine Level^CDCPHINVS",
        "ORC|RE||IZ-2^NDA",
        "RXA|0|1|20200201||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM",
        "OBX|1|CE|30956-7^Vaccine Type^LN|1|208^COVID-19 mRNA vaccine^CVX||||||F",
        "OBX|2|NM|30973-2^Dose Number in Series^LN|1|2|NA^Not Applicable^HL70353|||||F",
        "OBX|3|NM|59782-3^Number of doses in primary series^LN|1|2|||||F",
        "OBX|4|CE|59779-9^Immunization Schedule Used^LN|1|VXC16^ACIP^CDCPHINVS||||||F",
        "OBX|5|CE|64994-7^Vaccine funding program eligibility category^LN|2|"
            + "V02^VFC eligible Medicaid/Medicaid managed care^HL70064||||||F|||||VXC40^Vaccine Level^CDCPHINVS",
        "ORC|RE||9999^NDA",
        "RXA|0|1|20240101||998^No vaccine administered^CVX|999",
        "OBX|1|CE|30956-7^Vaccine type^LN|1|208^COVID-19 mRNA vaccine^CVX||||||F",
        "OBX|2|CE|59783-1^Status in immunization series^LN|1|LA13425-1^Complete^LN||||||F",
        "OBX|3|TS|30981-5^Earliest date to give^LN|1|20240301||||||F",
        "OBX|4|CE|30982-3^Reason applied by forecast logic^LN|1|"
            + "LA12836-0^Reason applied by forecast logic^LN||||||F",
        "OBX|5|CE|59779-9^Immunization Schedule Used^LN|1|VXC16^ACIP^CDCPHINVS||||||F"
    );

    /**
     * The Z42 fixture with an administering performer (RXA-10) and facility (RXA-11) on each
     * administered dose, so the conversion produces the Location and performer resources the plain
     * {@link #RSP_Z42_MESSAGE} does not. Kept separate so no existing entry-count assertion moves.
     */
    private static final String RSP_Z42_WITH_FACILITY_MESSAGE = RSP_Z42_MESSAGE.replace(
        "RXA|0|1|20200101||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM",
        "RXA|0|1|20200101||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM|||"
            + "^Nurse^Nancy^^^^^^NDA^L|^^^TESTFAC^^^^^Test Facility")
        .replace(
        "RXA|0|1|20200201||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM",
        "RXA|0|1|20200201||208^COVID-19 mRNA vaccine^CVX|0.3|mL^milliliters^UCUM|||"
            + "^Nurse^Nancy^^^^^^NDA^L|^^^TESTFAC^^^^^Test Facility");

    @AfterEach
    void clearRequestContext() {
        RequestContext.clear();
    }

    // --- searchset filtering -------------------------------------------------------------
    //
    // See openspec/changes/fix-fhir-searchset-include-mode. The filter classifies every entry
    // as match / include / outcome and drops the rest; these tests pin that contract, which
    // had no coverage at all before this change.

    /** Every Reference held anywhere in a resource, found by walking its element tree. */
    private static List<Reference> referencesOf(Base base) {
        List<Reference> found = new ArrayList<>();
        collectReferences(base, found, new HashSet<>());
        return found;
    }

    private static void collectReferences(Base base, List<Reference> found, Set<Base> seen) {
        if (base == null || !seen.add(base)) {
            return;
        }
        if (base instanceof Reference ref) {
            found.add(ref);
            return;   // a Reference's own children (identifier, display) hold no further references
        }
        for (Property property : base.children()) {
            for (Base child : property.getValues()) {
                collectReferences(child, found, seen);
            }
        }
    }

    private static final String RECOMMENDATION_URI = "/fhir/dev/ImmunizationRecommendation";

    private static final String IMMUNIZATION_URI = "/fhir/dev/Immunization";

    /**
     * How a caller reaches the Z42 evaluated history. The Immunizations reference the Patient, not
     * the recommendation, so the reverse hit is only found once the Patient is retained - which is
     * why the forward _include is not optional. Immunization:authority is what retains the schedule
     * Organization: protocolApplied.authority is registered on the Immunization, not on the
     * ImmunizationRecommendation.
     */
    private static final Map<String, String[]> HISTORY_PARAMS = queryParams(
        "_include", "ImmunizationRecommendation:patient",
        "_revinclude", "Immunization:patient",
        "_include", "Immunization:authority");

    /** Everything the pre-change code returned unasked, requested explicitly. */
    private static final Map<String, String[]> RECOVERY_PARAMS = queryParams(
        "_include", "*:*",
        "_revinclude", "Immunization");

    /** The type-and-mode multiset of a searchset, as {@code Type/MODE -> count}. */
    private static Map<String, Integer> typeModeCounts(Bundle bundle) {
        Map<String, Integer> counts = new java.util.TreeMap<>();
        for (BundleEntryComponent e : bundle.getEntry()) {
            counts.merge(e.getResource().fhirType() + "/" + e.getSearch().getMode(), 1, Integer::sum);
        }
        return counts;
    }

    private static Bundle query(String hl7, String uri, Map<String, String[]> params) throws Exception {
        initRequestContext();
        Bundle b = controller(hubReturning(hl7)).iisQuery("dev", fhirRequest(uri, null, params)).getBody();
        assertNotNull(b);
        return b;
    }

    // Observations only ever arrive because the caller asked for them. The default recommendation
    // query returns none (see plainRecommendationQueryIsSelfContainedWithoutObservations), because
    // the filter never walks the Reverses direction on its own. When the caller does ask with
    // _revinclude=Observation, the forecast Observations arrive too - reaching them is the caller's
    // choice, and they are never labelled match.

    @Test
    void revincludedObservationsAreLabelledIncludeNotMatch() throws Exception {
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_revinclude", "Observation"));

        List<Resource> observations = resourcesOfType(b, "Observation");
        assertFalse(observations.isEmpty(), "the _revinclude should have retained the dose Observations");
        assertTrue(entriesWithMode(b, SearchEntryMode.INCLUDE).containsAll(observations),
            "every revincluded Observation should be search.mode=include, not match");
    }

    @Test
    void selectingMatchYieldsOnlyTheRequestedType() throws Exception {
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_revinclude", "Observation"));

        List<Resource> matches = entriesWithMode(b, SearchEntryMode.MATCH);
        assertFalse(matches.isEmpty(), "the doses should be matches");
        assertTrue(matches.stream().allMatch(r -> "Immunization".equals(r.fhirType())),
            () -> "only the requested type should be a match, got: "
                + matches.stream().map(Resource::fhirType).distinct().toList());
    }

    @Test
    void observationsArriveOnlyWhenRevincludedAndNeverAsMatch() throws Exception {
        // The forecast detail is opt-in: absent by default, retained as include when asked for.
        // The forward _include is required - the Observations reference the Patient, so the reverse
        // hit is only found once the Patient is in the searchset.
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "ImmunizationRecommendation:patient",
            "_revinclude", "Observation"));

        List<Resource> observations = resourcesOfType(b, "Observation");
        assertFalse(observations.isEmpty(), "the _revinclude should have retained Observations");
        assertTrue(entriesWithMode(b, SearchEntryMode.INCLUDE).containsAll(observations),
            "a revincluded Observation is a join, so search.mode=include");
        assertTrue(entriesWithMode(b, SearchEntryMode.MATCH).stream()
                .allMatch(r -> "ImmunizationRecommendation".equals(r.fhirType())),
            "only the forecast the client asked for should be a match");
    }

    @Test
    void unmatchedIncludeParameterIsNotAnError() throws Exception {
        Bundle baseline = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams());
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI,
            queryParams("_include", "ImmunizationRecommendation:nosuchsearchname"));

        assertEquals(baseline.getEntry().size(), b.getEntry().size(),
            "an include naming an unregistered search name should retain nothing extra");
    }

    @Test
    void plainRecommendationQueryReturnsNeitherPatientNorOrganization() throws Exception {
        // Both are referenced by the returned forecast, and neither is returned for that reason:
        // being referenced is not a reason to retain.
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams());

        assertTrue(resourcesOfType(b, "Patient").isEmpty(),
            "the subject Patient is not what the caller asked for");
        assertTrue(resourcesOfType(b, "Organization").isEmpty(),
            "the schedule Organization behind authority is not what the caller asked for");
        assertTrue(b.getEntry().stream()
                .allMatch(e -> "ImmunizationRecommendation".equals(e.getResource().fhirType())
                    || e.getResource() instanceof OperationOutcome),
            () -> "only the requested type and outcomes should survive, got: " + typeModeCounts(b));
    }

    @Test
    void patientAndOrganizationArriveWhenIncluded() throws Exception {
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "ImmunizationRecommendation:patient",
            "_include", "ImmunizationRecommendation:authority"));

        List<Resource> included = entriesWithMode(b, SearchEntryMode.INCLUDE);
        assertFalse(resourcesOfType(b, "Patient").isEmpty(), "the Patient was asked for");
        assertFalse(resourcesOfType(b, "Organization").isEmpty(), "the Organization was asked for");
        assertTrue(included.containsAll(resourcesOfType(b, "Patient")),
            "an _include hit is a join, so search.mode=include");
        assertTrue(included.containsAll(resourcesOfType(b, "Organization")),
            "an _include hit is a join, so search.mode=include");
        assertTrue(entriesWithMode(b, SearchEntryMode.MATCH).stream()
                .allMatch(r -> "ImmunizationRecommendation".equals(r.fhirType())),
            "an included resource must not be promoted to a match");
    }

    @Test
    void plainRecommendationQueryOmitsTheEvaluatedHistory() throws Exception {
        // The evaluated history is not what an ImmunizationRecommendation query asked for. It is
        // still reachable - see includedHistoryCarriesTheZ42OnlyEvaluationData for how.
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams());

        assertTrue(resourcesOfType(b, "Immunization").isEmpty(),
            "the Z42 evaluated history should not arrive unasked");
    }

    @Test
    void evaluatedHistoryArrivesWhenRevincludedThroughThePatient() throws Exception {
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, HISTORY_PARAMS);

        List<Resource> doses = resourcesOfType(b, "Immunization");
        assertEquals(2, doses.size(), "both administered doses should arrive");
        assertTrue(entriesWithMode(b, SearchEntryMode.INCLUDE).containsAll(doses),
            "a revincluded dose is a join, so search.mode=include");
        assertTrue(entriesWithMode(b, SearchEntryMode.MATCH).stream()
                .allMatch(r -> "ImmunizationRecommendation".equals(r.fhirType())),
            "only the forecast the client asked for should be a match");
    }

    @Test
    void includedHistoryCarriesTheZ42OnlyEvaluationData() throws Exception {
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, HISTORY_PARAMS);

        Set<String> orgIds = resourcesOfType(b, "Organization").stream()
            .map(r -> r.getIdElement().getIdPart()).collect(java.util.stream.Collectors.toSet());
        for (Resource r : resourcesOfType(b, "Immunization")) {
            Immunization imm = (Immunization) r;
            assertFalse(imm.getProtocolApplied().isEmpty(), "protocolApplied should be populated");
            ImmunizationProtocolAppliedComponent protocol = imm.getProtocolAppliedFirstRep();
            assertTrue(protocol.hasDoseNumberPositiveIntType(), "doseNumber from OBX 30973-2");
            assertTrue(protocol.hasSeriesDosesPositiveIntType(), "seriesDoses from OBX 59782-3");
            assertTrue(orgIds.contains(StringUtils.substringAfterLast(
                    protocol.getAuthority().getReference(), "/")),
                "protocolApplied.authority should resolve to an Organization in the bundle");
            assertFalse(imm.getProgramEligibility().isEmpty(), "programEligibility from OBX 64994-7");
        }
    }

    @Test
    void includedHistoryHasStableDistinctIdentifiers() throws Exception {
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, HISTORY_PARAMS);

        List<String> fillerOrderNumbers = resourcesOfType(b, "Immunization").stream()
            .map(r -> ((Immunization) r).getIdentifierFirstRep().getValue())
            .sorted()
            .toList();
        assertEquals(List.of("IZ-1", "IZ-2"), fillerOrderNumbers,
            "each dose should carry its own ORC-3 filler order number");

        Set<String> ids = b.getEntry().stream()
            .map(e -> e.getResource().fhirType() + "/" + e.getResource().getIdElement().getIdPart())
            .collect(java.util.stream.Collectors.toSet());
        assertEquals(b.getEntry().size(), ids.size(), "no two entries may collide on Type/id");
    }

    @Test
    void immunizationQueryStillReturnsHistoryAsMatch() throws Exception {
        // The include is scoped to the recommendation path; /Immunization is unchanged.
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams());

        assertTrue(entriesWithMode(b, SearchEntryMode.MATCH)
                .containsAll(resourcesOfType(b, "Immunization")),
            "on /Immunization the doses are the matches, not includes");
    }

    @Test
    void plainRecommendationQueryReturnsNoObservations() throws Exception {
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams());

        assertTrue(resourcesOfType(b, "Observation").isEmpty(),
            "forecast Observations should stay out of a plain recommendation query");
    }

    @Test
    void immunizationPatientReferenceKeepsItsLiteralValueWhenTheTargetIsOmitted() throws Exception {
        // Immunization.patient is 1..1 and its target is not returned. The reference is delivered
        // exactly as the conversion produced it - resolving it is the caller's decision.
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams());

        assertFalse(resourcesOfType(b, "Immunization").isEmpty(), "the doses should be matches");
        assertTrue(resourcesOfType(b, "Patient").isEmpty(), "the Patient was not asked for");
        for (Resource r : resourcesOfType(b, "Immunization")) {
            Reference patient = ((Immunization) r).getPatient();
            assertTrue(patient.hasReference(),
                "the patient reference must keep the value the conversion produced");
            assertTrue(patient.getReference().startsWith("Patient/"),
                () -> "unexpected reference value: " + patient.getReference());
        }
    }

    @Test
    void conversionCreatedResourcesStillNeedWhitelistingWhenUnreferenced() throws Exception {
        // Provenance/DocumentReference are MessageParser artifacts that nothing in the
        // searchset references, so they are the ones the white-list still governs.
        Bundle without = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams());
        assertTrue(resourcesOfType(without, "Provenance").isEmpty(),
            "unreferenced conversion-created resources should be absent by default");

        Bundle with = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_include", "Resource:source:*"));
        assertTrue(with.getEntry().size() > without.getEntry().size(),
            "Resource:source:* should retain the conversion-created resources");
        assertTrue(entriesWithMode(with, SearchEntryMode.INCLUDE)
                .containsAll(resourcesOfType(with, "Provenance")),
            "white-listed resources should be search.mode=include");
    }

    @Test
    void noReferenceWithReadableContentIsStrippedOfItsValue() throws Exception {
        // The pre-change code shipped five references with no reference element on this fixture.
        // Two were stripped by clearUnresolvableReferences and are restored here; the other three
        // carry no reference, identifier or display as v2tofhir produces them, and stay empty.
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams("_include", "*:*"));

        List<Reference> withoutValue = referencesWithoutLiteralValue(b);
        assertEquals(3, withoutValue.size(),
            () -> "only the references v2tofhir leaves empty should lack a value, got: "
                + describe(b));
        assertTrue(withoutValue.stream().noneMatch(ref -> ref.hasIdentifier() || ref.hasDisplay()),
            () -> "a reference carrying readable content must keep its value too, got: "
                + describe(b));
    }

    /** Every reference in a bundle whose {@code reference} element is absent. */
    private static List<Reference> referencesWithoutLiteralValue(Bundle bundle) {
        return bundle.getEntry().stream()
            .flatMap(e -> referencesOf(e.getResource()).stream())
            .filter(ref -> !ref.hasReference())
            .toList();
    }

    private static List<String> describe(Bundle bundle) {
        return bundle.getEntry().stream()
            .flatMap(e -> referencesOf(e.getResource()).stream()
                .filter(ref -> !ref.hasReference())
                .map(ref -> e.getResource().fhirType() + "[identifier="
                    + ref.getIdentifier().getValue() + ",display=" + ref.getDisplay() + "]"))
            .toList();
    }

    @Test
    void outcomesSurviveAndBundleIsASearchset() throws Exception {
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams());

        assertEquals(BundleType.SEARCHSET, b.getType());
        assertFalse(entriesWithMode(b, SearchEntryMode.OUTCOME).isEmpty(),
            "conversion OperationOutcomes should survive with mode=outcome");
        assertTrue(b.getEntry().stream().allMatch(e -> e.getSearch().getMode() != null),
            "every retained entry must carry a search mode");
    }

    @Test
    void namedTypeWhitelistRetainsOnlyThatType() throws Exception {
        // Naming one type is enough - the caller does not have to ask for Resource:source:*.
        // It is not necessarily *narrower* than the wildcard on this fixture: the MessageParser
        // Provenance targets the other conversion-created resources, so the no-dangling-references
        // rule pulls them in behind it either way.
        Bundle baseline = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams());
        Bundle named = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_include", "Resource:source:Provenance"));

        assertTrue(resourcesOfType(baseline, "Provenance").isEmpty(),
            "without the white-list the Provenance stays out");
        assertFalse(resourcesOfType(named, "Provenance").isEmpty(),
            "naming the type should white-list it");
        assertTrue(entriesWithMode(named, SearchEntryMode.INCLUDE)
                .containsAll(resourcesOfType(named, "Provenance")),
            "a white-listed resource is search.mode=include");
    }

    @Test
    void partOfRevincludeNarrowsToTheHistoryObservations() throws Exception {
        // Documented in docs/fhir/rsp-to-fhir.md: the unqualified _revinclude=Observation returns
        // the forecast Observations too, and Observation:part-of is how a caller excludes them.
        Bundle all = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "*:*", "_revinclude", "Observation"));
        Bundle narrowed = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "*:*", "_revinclude", "Observation:part-of"));

        List<Resource> narrowedObs = resourcesOfType(narrowed, "Observation");
        assertFalse(narrowedObs.isEmpty(), "the dose Observations link via partOf and should arrive");
        assertTrue(resourcesOfType(all, "Observation").size() > narrowedObs.size(),
            "the unqualified form should also return the unlinked forecast Observations");
        assertTrue(narrowedObs.stream().noneMatch(r -> ((Observation) r).getPartOf().isEmpty()),
            "Observation:part-of should retain only Observations carrying a partOf link");
    }

    @Test
    void matchOperationLabelsThePatientAsMatch() throws Exception {
        // $match has no resource type in the path; the filter resolves the requested type to
        // Patient, so the matched Patient is the match rather than an unlabelled entry.
        initRequestContext();
        Bundle b = (Bundle) controller(hubReturning(RSP_MESSAGE))
            .iisPatientMatch("dev", matchParameters(), fhirRequest(MATCH_URI, null)).getBody();

        assertNotNull(b);
        List<Resource> patients = resourcesOfType(b, "Patient");
        assertFalse(patients.isEmpty(), "the matched Patient should be returned");
        assertTrue(entriesWithMode(b, SearchEntryMode.MATCH).containsAll(patients),
            "on $match the Patient is the match");
        assertTrue(b.getEntry().stream().allMatch(e -> e.getSearch().getMode() != null),
            "every retained entry must carry a search mode");
    }

    // --- strict searchset contract -------------------------------------------------------
    //
    // See openspec/changes/fhir-searchset-strict-includes. A query returns the requested type and
    // OperationOutcome only; everything else is opt-in via _include / _revinclude. The baselines
    // asserted below were recorded from the pre-change code (tasks 0.1 and 0.2).

    @Test
    void plainImmunizationQueryReturnsImmunizationsOnly() throws Exception {
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams());

        assertEquals(Map.of("Immunization/MATCH", 2, "OperationOutcome/OUTCOME", 2),
            typeModeCounts(b),
            "the pre-change code returned 10 entries here; only the doses were asked for");
        for (String absent : List.of("Patient", "PractitionerRole", "Practitioner", "Location")) {
            assertTrue(resourcesOfType(b, absent).isEmpty(),
                () -> absent + " was not asked for and must not arrive");
        }
    }

    @Test
    void recoveryParametersReproduceThePreChangePayload() throws Exception {
        // _include=*:* & _revinclude=Immunization is what docs/fhir/fhir-api.md names as the way to
        // get back what the service used to return unasked. This pins that it actually does: the
        // expected multiset is the recorded pre-change baseline for a plain recommendation query.
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, RECOVERY_PARAMS);

        assertEquals(Map.of(
                "ImmunizationRecommendation/MATCH", 1,
                "OperationOutcome/OUTCOME", 2,
                "Patient/INCLUDE", 1,
                "Immunization/INCLUDE", 2,
                "Organization/INCLUDE", 1),
            typeModeCounts(b),
            "the recovery parameters must reproduce the pre-change searchset exactly");
    }

    @Test
    void recoveryParametersStillReturnNoObservations() throws Exception {
        // The pre-change code never walked the reverse direction on its own, so the forecast
        // Observations stayed out. Naming only Immunization in the _revinclude preserves that.
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, RECOVERY_PARAMS);

        assertTrue(resourcesOfType(b, "Observation").isEmpty(),
            "_revinclude=Immunization must not drag in the forecast Observations");
    }

    @Test
    void forwardWildcardAloneDoesNotReachTheEvaluatedHistory() throws Exception {
        // The Immunizations are reachable only in reverse, so the _revinclude is not optional.
        Bundle b = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams("_include", "*:*"));

        assertTrue(resourcesOfType(b, "Immunization").isEmpty(),
            "_include=*:* follows forward references only");
        assertFalse(resourcesOfType(b, "Patient").isEmpty(),
            "the forward wildcard should still reach the Patient");
    }

    @Test
    void theTwoStrippedPractitionerReferencesAreRestored() throws Exception {
        // The two references the pre-change code stripped carried display="Carl Clinician" - the
        // PractitionerRole -> Practitioner link built outside v2tofhir bookkeeping. They now ship
        // with the value the conversion produced.
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams("_include", "*:*"));

        List<Resource> roles = resourcesOfType(b, "PractitionerRole");
        assertFalse(roles.isEmpty(), "the wildcard include should reach the PractitionerRole");
        List<Reference> namedRefs = roles.stream()
            .flatMap(r -> referencesOf(r).stream())
            .filter(Reference::hasDisplay)
            .toList();
        assertFalse(namedRefs.isEmpty(), "the named practitioner references should be present");
        assertTrue(namedRefs.stream().allMatch(Reference::hasReference),
            "a reference the conversion gave a value must keep it");
    }

    @Test
    void patientReferenceValueIsTheSameWhetherOrNotTheTargetIsIncluded() throws Exception {
        Bundle without = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams());
        Bundle with = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_include", "Immunization:patient"));

        String refWithout = ((Immunization) resourcesOfType(without, "Immunization").get(0))
            .getPatient().getReference();
        String refWith = ((Immunization) resourcesOfType(with, "Immunization").get(0))
            .getPatient().getReference();

        assertEquals(refWithout, refWith,
            "asking for the target must not change the reference value");
        List<Resource> patients = resourcesOfType(with, "Patient");
        assertEquals(1, patients.size(), "the _include should have retained the Patient");
        assertEquals("Patient/" + patients.get(0).getIdElement().getIdPart(), refWith,
            "the reference should resolve to the retained Patient");
    }

    @Test
    void revincludeDoesNotReachAWhitelistedResource() throws Exception {
        // A white-listed resource is retained but never traversed, so no reverse reference is
        // resolved from it. The Provenance points at the DocumentReference, so even white-listing
        // the DocumentReference does not bring the Provenance along. Reaching it takes
        // _include=Resource:source:Provenance - see namedTypeWhitelistRetainsOnlyThatType.
        Bundle bare = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_revinclude", "Provenance"));
        assertTrue(resourcesOfType(bare, "Provenance").isEmpty(),
            "_revinclude=Provenance alone reaches nothing");

        Bundle withDoc = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI, queryParams(
            "_include", "Resource:source:DocumentReference",
            "_revinclude", "Provenance"));
        assertFalse(resourcesOfType(withDoc, "DocumentReference").isEmpty(),
            "the white-list should retain the DocumentReference");
        assertTrue(resourcesOfType(withDoc, "Provenance").isEmpty(),
            "a white-listed resource is not traversed, so the reverse hit is still not found");
    }

    @Test
    void reverseIncludeResolvesOnlyFromARetainedResource() throws Exception {
        Bundle without = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI,
            queryParams("_revinclude", "Observation"));
        assertTrue(resourcesOfType(without, "Observation").isEmpty(),
            "the Observations reference the Patient, which is not retained");

        Bundle with = query(RSP_Z42_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "ImmunizationRecommendation:patient",
            "_revinclude", "Observation"));
        List<Resource> observations = resourcesOfType(with, "Observation");
        assertFalse(observations.isEmpty(),
            "retaining the Patient should make the same _revinclude resolve");
        assertTrue(entriesWithMode(with, SearchEntryMode.INCLUDE).containsAll(observations),
            "a revincluded Observation is a join, so search.mode=include");
    }

    @Test
    void conversionCreatedResourcesArriveUnderAnOrdinaryInclude() throws Exception {
        // Location and the performer resources carry Parser.SOURCE, and an ordinary _include
        // retains them anyway - the Resource:source white-list is not required.
        Bundle b = query(RSP_Z32_MESSAGE, IMMUNIZATION_URI,
            queryParams("_include", "Immunization:location"));

        List<Resource> locations = resourcesOfType(b, "Location");
        assertFalse(locations.isEmpty(),
            "Immunization:location should retain the conversion-created Location");
        assertTrue(entriesWithMode(b, SearchEntryMode.INCLUDE).containsAll(locations),
            "an _include hit is a join, so search.mode=include");
    }

    /** The four parameters a caller sends to get Patient, Organization, Immunization and Location. */
    private static final String[] FOUR_TYPE_PARAMS = {
        "_include", "ImmunizationRecommendation:patient",
        "_include", "ImmunizationRecommendation:authority",
        "_revinclude", "Immunization",
        "_include", "Immunization:location"
    };

    @Test
    void fourTypeQueryWalksTheWholeChainInOnePass() throws Exception {
        // recommendation -> Patient + Organization forward, Patient -> Immunization reverse,
        // Immunization -> Location forward. The resources list grows as it is iterated, so all
        // four hops resolve in a single pass.
        Bundle b = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI,
            queryParams(FOUR_TYPE_PARAMS));

        List<Resource> included = entriesWithMode(b, SearchEntryMode.INCLUDE);
        for (String type : List.of("Patient", "Organization", "Immunization", "Location")) {
            List<Resource> found = resourcesOfType(b, type);
            assertFalse(found.isEmpty(), () -> type + " was asked for and should arrive");
            assertTrue(included.containsAll(found), () -> type + " should be search.mode=include");
        }
        assertTrue(resourcesOfType(b, "Observation").isEmpty(),
            "no _revinclude named Observation, so none should arrive");
        assertTrue(entriesWithMode(b, SearchEntryMode.MATCH).stream()
                .allMatch(r -> "ImmunizationRecommendation".equals(r.fhirType())),
            "only the requested type should be a match");
    }

    @Test
    void parameterOrderDoesNotChangeTheSearchset() throws Exception {
        String[] reversed = new String[FOUR_TYPE_PARAMS.length];
        for (int i = 0; i < FOUR_TYPE_PARAMS.length; i += 2) {
            reversed[FOUR_TYPE_PARAMS.length - 2 - i] = FOUR_TYPE_PARAMS[i];
            reversed[FOUR_TYPE_PARAMS.length - 1 - i] = FOUR_TYPE_PARAMS[i + 1];
        }

        Bundle forward = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI,
            queryParams(FOUR_TYPE_PARAMS));
        Bundle backward = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI,
            queryParams(reversed));

        assertEquals(typeModeCounts(forward), typeModeCounts(backward),
            "every include is applied to every resource as it is reached, so order cannot matter");
    }

    @Test
    void revincludeWithNoRetainedAnchorFindsNothing() throws Exception {
        // With no forward _include at all, nothing the Immunizations reference is retained, so
        // there is no resource to resolve the reverse hit from.
        Bundle b = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_revinclude", "Immunization",
            "_include", "Immunization:location"));

        assertTrue(resourcesOfType(b, "Immunization").isEmpty(),
            "no retained resource anchors the reverse hit");
        assertTrue(resourcesOfType(b, "Location").isEmpty(),
            "and the Location hangs off the Immunization, so it is lost with it");
    }

    @Test
    void anyRetainedReferencedResourceAnchorsTheReverseInclude() throws Exception {
        // The Patient is the anchor a caller reaches for, but it is not the only one: the doses also
        // reference the schedule Organization through protocolApplied.authority, so retaining that
        // Organization resolves an unqualified _revinclude=Immunization just as well.
        Bundle b = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "ImmunizationRecommendation:authority",
            "_revinclude", "Immunization"));

        assertTrue(resourcesOfType(b, "Patient").isEmpty(), "the Patient was not asked for");
        assertEquals(2, resourcesOfType(b, "Immunization").size(),
            "the Organization anchors the reverse hit in the Patient's place");
    }

    @Test
    void qualifyingARevincludeDoesNotPinTheTraversalPath() throws Exception {
        // ParserUtils.createReference caches one canonical Reference per resource and
        // addSearchNames accumulates onto it, so the Immunization's reverse names are the union of
        // every path that points at it - "patient" and "authority" both. The same Reference instance
        // sits in the Patient's and the Organization's Reverses sets, so naming a search path does
        // not restrict which retained resource the reverse hit may resolve from.
        Bundle viaOrganization = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "ImmunizationRecommendation:authority",
            "_revinclude", "Immunization:patient"));

        assertTrue(resourcesOfType(viaOrganization, "Patient").isEmpty(),
            "the Patient was not asked for");
        assertEquals(2, resourcesOfType(viaOrganization, "Immunization").size(),
            "Immunization:patient still resolves, anchored on the retained Organization");

        // An unregistered name is the case that does restrict: it matches nothing.
        Bundle unregistered = query(RSP_Z42_WITH_FACILITY_MESSAGE, RECOMMENDATION_URI, queryParams(
            "_include", "ImmunizationRecommendation:authority",
            "_revinclude", "Immunization:nosuchsearchname"));

        assertTrue(resourcesOfType(unregistered, "Immunization").isEmpty(),
            "a search name the conversion never registered matches nothing");
    }

    @Test
    void destinationDoesNotChangeTheReturnedTypes() throws Exception {
        // Searchset assembly reads only the converted bundle and the query parameters, so the
        // routing key - and therefore the organization's pipeline - cannot affect what is returned.
        initRequestContext();
        Bundle first = controller(hubReturning(RSP_Z42_MESSAGE))
            .iisQuery("dev", fhirRequest(RECOMMENDATION_URI, null, HISTORY_PARAMS)).getBody();
        initRequestContext();
        Bundle second = controller(hubReturning(RSP_Z42_MESSAGE))
            .iisQuery("other", fhirRequest("/fhir/other/ImmunizationRecommendation", null,
                HISTORY_PARAMS)).getBody();

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(typeModeCounts(first), typeModeCounts(second),
            "the same query against the same response must yield the same types and modes");
    }

    /** The resources in a bundle carrying the given search mode. */
    private static List<Resource> entriesWithMode(Bundle bundle, SearchEntryMode mode) {
        return bundle.getEntry().stream()
            .filter(e -> e.getResource() != null && e.getSearch() != null
                && mode.equals(e.getSearch().getMode()))
            .map(BundleEntryComponent::getResource)
            .toList();
    }


    private static List<Resource> resourcesOfType(Bundle bundle, String fhirType) {
        return bundle.getEntry().stream()
            .map(BundleEntryComponent::getResource)
            .filter(r -> r != null && fhirType.equals(r.fhirType()))
            .toList();
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
        return fhirRequest(uri, accept, Collections.emptyMap());
    }

    /**
     * A mock request carrying query parameters, so searchset tests can drive
     * {@code _include} / {@code _revinclude} and the patient identifier the query needs.
     * Absent parameters read back as null, matching a real request.
     */
    private static HttpServletRequest fhirRequest(String uri, String accept, Map<String, String[]> params) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getParameterMap()).thenReturn(params);
        when(req.getRequestURI()).thenReturn(uri);
        when(req.getHeader(HttpHeaders.ACCEPT)).thenReturn(accept);
        when(req.getParameterValues(anyString()))
            .thenAnswer(i -> params.get(i.getArgument(0, String.class)));
        when(req.getParameter(anyString())).thenAnswer(i -> {
            String[] values = params.get(i.getArgument(0, String.class));
            return values == null || values.length == 0 ? null : values[0];
        });
        return req;
    }

    /** Query parameters selecting the fixture patient, plus any extras under test. */
    private static Map<String, String[]> queryParams(String... extras) {
        Map<String, String[]> params = new LinkedHashMap<>();
        params.put(IzQuery.PATIENT_LIST, new String[] {"TEST|0000001"});
        for (int i = 0; i < extras.length; i += 2) {
            params.merge(extras[i], new String[] {extras[i + 1]},
                (a, b) -> Stream.concat(Arrays.stream(a), Arrays.stream(b)).toArray(String[]::new));
        }
        return params;
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
        ResponseEntity<CapabilityStatement> response = newController().metadata("dev", metadataRequest());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        CapabilityStatement cs = response.getBody();
        assertNotNull(cs);
        assertEquals(Enumerations.PublicationStatus.ACTIVE, cs.getStatus());
    }

    @Test
    void metadataSatisfiesR4RequiredElements() {
        CapabilityStatement cs = newController().metadata("dev", metadataRequest()).getBody();

        assertNotNull(cs);
        assertTrue(cs.hasDate(), "date is required (1..1)");
        assertEquals(CapabilityStatement.CapabilityStatementKind.INSTANCE, cs.getKind());
        // kind = instance requires implementation (cpb-14); implementation.description is 1..1
        assertTrue(cs.hasImplementation(), "implementation is required when kind = instance");
        assertEquals("IZ Gateway Transformation Service", cs.getImplementation().getDescription());
    }

    @Test
    void metadataDeclaresFhirVersionAndJsonFormat() {
        CapabilityStatement cs = newController().metadata("dev", metadataRequest()).getBody();

        assertNotNull(cs);
        assertEquals(Enumerations.FHIRVersion._4_0_1, cs.getFhirVersion());
        assertTrue(
            cs.getFormat().stream().anyMatch(f -> "application/fhir+json".equals(f.getValue())),
            "format should include application/fhir+json"
        );
    }

    @Test
    void metadataAdvertisesSearchableResources() {
        CapabilityStatement cs = newController().metadata("dev", metadataRequest()).getBody();

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
        CapabilityStatement cs = newController().metadata("dev", metadataRequest()).getBody();

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
        CapabilityStatement known = controller.metadata("dev", metadataRequest()).getBody();
        CapabilityStatement unknown = controller.metadata("some-unknown-destination", metadataRequest()).getBody();

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
            .getMethod("metadata", String.class, HttpServletRequest.class)
            .getAnnotation(GetMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new String[] { "/{destinationId}/metadata" }, mapping.value());
        assertTrue(
            Arrays.asList(mapping.produces()).contains("application/fhir+json"),
            "produces should include application/fhir+json"
        );
    }

    @Test
    void metadataHonorsFhirJsonAccept() {
        HttpServletRequest req = fhirRequest("/fhir/dev/metadata", ContentUtils.FHIR_PLUS_JSON_VALUE);

        ResponseEntity<CapabilityStatement> res = newController().metadata("dev", req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void metadataHonorsXmlAccept() {
        HttpServletRequest req = fhirRequest("/fhir/dev/metadata", "application/xml");

        ResponseEntity<CapabilityStatement> res = newController().metadata("dev", req);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_XML_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    void metadataDefaultsToJsonWithoutAccept() {
        ResponseEntity<CapabilityStatement> res = newController().metadata("dev", metadataRequest());

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(ContentUtils.FHIR_PLUS_JSON_VALUE, res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    private static FhirController newController() {
        return new FhirController(
            mock(HubController.class),
            new FhirController.FhirConfiguration(),
            mock(AccessControlRegistry.class)
        );
    }

    private static HttpServletRequest metadataRequest() {
        return fhirRequest("/fhir/dev/metadata", null);
    }

    private static CapabilityStatement.CapabilityStatementRestResourceComponent findResource(
        CapabilityStatement.CapabilityStatementRestComponent rest, String type) {
        return rest.getResource().stream()
            .filter(r -> type.equals(r.getType()))
            .findFirst()
            .orElse(null);
    }
}

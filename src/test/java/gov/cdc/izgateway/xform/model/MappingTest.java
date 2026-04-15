package gov.cdc.izgateway.xform.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Mapping}, focusing on the {@code notes} field
 * including validation, JSON serialization, and multi-line text support.
 */
class MappingTest {

    private static Validator validator;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Mapping createValidMapping() {
        Mapping mapping = new Mapping();
        mapping.setId(UUID.randomUUID());
        mapping.setActive(true);
        mapping.setOrganizationId(UUID.randomUUID());
        mapping.setCodeSystem("CDCREC");
        mapping.setCode("2106-3");
        mapping.setTargetCodeSystem("CDCREC3");
        mapping.setTargetCode("NEWCODE");
        return mapping;
    }

    @Test
    void validMapping_withoutNotes_hasNoViolations() {
        Mapping mapping = createValidMapping();

        Set<ConstraintViolation<Mapping>> violations = validator.validate(mapping);

        assertTrue(violations.isEmpty(), "Mapping without notes should be valid");
        assertNull(mapping.getNotes());
    }

    @Test
    void validMapping_withNotes_hasNoViolations() {
        Mapping mapping = createValidMapping();
        mapping.setNotes("This is a test note");

        Set<ConstraintViolation<Mapping>> violations = validator.validate(mapping);

        assertTrue(violations.isEmpty(), "Mapping with notes should be valid");
        assertEquals("This is a test note", mapping.getNotes());
    }

    @Test
    void notes_exceedingMaxLength_hasViolation() {
        Mapping mapping = createValidMapping();
        mapping.setNotes("x".repeat(2001));

        Set<ConstraintViolation<Mapping>> violations = validator.validate(mapping);

        assertFalse(violations.isEmpty(), "Notes exceeding 2000 chars should have a violation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Notes must not exceed 2000 characters")));
    }

    @Test
    void notes_atMaxLength_hasNoViolations() {
        Mapping mapping = createValidMapping();
        mapping.setNotes("x".repeat(2000));

        Set<ConstraintViolation<Mapping>> violations = validator.validate(mapping);

        assertTrue(violations.isEmpty(), "Notes at exactly 2000 chars should be valid");
    }

    @Test
    void notes_withMultiLineText_preservedThroughJsonRoundTrip() throws Exception {
        Mapping mapping = createValidMapping();
        String multiLine = "Line one\nLine two\nLine three";
        mapping.setNotes(multiLine);

        String json = objectMapper.writeValueAsString(mapping);
        Mapping deserialized = objectMapper.readValue(json, Mapping.class);

        assertEquals(multiLine, deserialized.getNotes());
        assertTrue(deserialized.getNotes().contains("\n"));
    }

    @Test
    void jsonWithoutNotes_deserializesToNullNotes() throws Exception {
        String json = """
                {
                    "id": "ace4066b-44be-4765-83f4-a05fcfe5774d",
                    "active": true,
                    "organizationId": "0d15449b-fb08-4013-8985-20c148b353fe",
                    "codeSystem": "CDCREC",
                    "code": "2106-3",
                    "targetCodeSystem": "CDCREC3",
                    "targetCode": "NEWCODE"
                }
                """;

        Mapping mapping = objectMapper.readValue(json, Mapping.class);

        assertNull(mapping.getNotes(), "Notes should be null when absent from JSON");
        assertEquals("CDCREC", mapping.getCodeSystem());
    }

    @Test
    void jsonWithNotes_deserializesCorrectly() throws Exception {
        String json = """
                {
                    "id": "ace4066b-44be-4765-83f4-a05fcfe5774d",
                    "active": true,
                    "organizationId": "0d15449b-fb08-4013-8985-20c148b353fe",
                    "codeSystem": "CDCREC",
                    "code": "2106-3",
                    "targetCodeSystem": "CDCREC3",
                    "targetCode": "NEWCODE",
                    "notes": "Important mapping note"
                }
                """;

        Mapping mapping = objectMapper.readValue(json, Mapping.class);

        assertEquals("Important mapping note", mapping.getNotes());
    }
}

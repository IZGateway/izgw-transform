package gov.cdc.izgateway.xform.endpoints.fhir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for the compare methods in {@link IDIMatch}.
 * <p>
 * IGDD-2974 replaced the deprecated commons-lang3 {@code StringUtils} calls with {@code Strings.CS} and
 * {@code Strings.CI}. It also fixed {@link IDIMatch#zipsMatch} and {@link IDIMatch#doesTelecomMatch}:
 * they called {@code StringUtils.replace}, which removes only the exact text {@code "- "} or
 * {@code ".-()[] "}. They now call {@code StringUtils.replaceChars}, which removes each character.
 * {@link IDIMatch#doesZipMatch} now uses {@link IDIMatch#zipsMatch}. Before, it did not remove hyphens.
 */
class IDIMatchTests {

    private static ContactPoint contact(ContactPointSystem system, String value) {
        return new ContactPoint().setSystem(system).setValue(value);
    }

    private static Address country(String country) {
        return new Address().setCountry(country);
    }

    @ParameterizedTest(name = "zipsMatch({0}, {1}) = {2}")
    @CsvSource({
        // Hyphens and spaces are removed before the compare (IGDD-2974 fix).
        "12345-6789, 12345,      true",
        "12345-6789, 12345-1111, true",
        "'K1A 0B1',  k1a0b1,     true",
        "'K1A-0B1',  'K1A 0B1',  true",
        // Numeric codes compare the first five digits.
        "123456789,  12345,      true",
        "12345,      12346,      false",
        "'K1A 0B1',  'K1A 0B2',  false"
    })
    void zipsMatchRemovesHyphensAndSpaces(String search, String found, boolean expected) {
        assertEquals(expected, IDIMatch.zipsMatch(search, found));
    }

    @Test
    void zipsMatchWithNulls() {
        assertTrue(IDIMatch.zipsMatch(null, null));
        assertFalse(IDIMatch.zipsMatch(null, "12345"));
    }

    @ParameterizedTest(name = "phone {0} vs {1} = {2}")
    @CsvSource({
        // Punctuation and spaces are removed before the compare (IGDD-2974 fix).
        "'(555) 123-4567', 555.123.4567,  true",
        "'[555] 123 4567', 5551234567,    true",
        "555-123-4567,     '(555)1234567', true",
        "555-123-4567,     555-123-4568,  false"
    })
    void phoneMatchIgnoresPunctuation(String search, String found, boolean expected) {
        List<ContactPoint> s = List.of(contact(ContactPointSystem.PHONE, search));
        List<ContactPoint> f = List.of(contact(ContactPointSystem.PHONE, found));
        assertEquals(expected, IDIMatch.doesTelecomMatch(s, f, ContactPointSystem.PHONE));
    }

    @Test
    void emailMatchIgnoresCaseButKeepsPunctuation() {
        List<ContactPoint> s = List.of(contact(ContactPointSystem.EMAIL, "John.Doe@Example.org"));
        assertTrue(IDIMatch.doesTelecomMatch(s,
            List.of(contact(ContactPointSystem.EMAIL, "john.doe@example.org")), ContactPointSystem.EMAIL));
        assertFalse(IDIMatch.doesTelecomMatch(s,
            List.of(contact(ContactPointSystem.EMAIL, "johndoe@example.org")), ContactPointSystem.EMAIL));
    }

    @Test
    void telecomMatchUsesOnlyTheRequestedSystem() {
        List<ContactPoint> s = List.of(contact(ContactPointSystem.PHONE, "5551234567"));
        List<ContactPoint> f = List.of(contact(ContactPointSystem.FAX, "5551234567"));
        assertFalse(IDIMatch.doesTelecomMatch(s, f, ContactPointSystem.PHONE));
    }

    @Test
    void doesZipMatchComparesFirstFiveDigits() {
        List<Address> s = List.of(new Address().setPostalCode("12345"));
        assertTrue(IDIMatch.doesZipMatch(s, List.of(new Address().setPostalCode("123456789"))));
        assertFalse(IDIMatch.doesZipMatch(s, List.of(new Address().setPostalCode("54321"))));
    }

    @Test
    void doesZipMatchRemovesHyphensAndSpaces() {
        // Before IGDD-2974, doesZipMatch did not remove hyphens, so ZIP+4 did not match a five-digit ZIP.
        List<Address> s = List.of(new Address().setPostalCode("12345"));
        assertTrue(IDIMatch.doesZipMatch(s, List.of(new Address().setPostalCode("12345-6789"))));
        assertTrue(IDIMatch.doesZipMatch(List.of(new Address().setPostalCode("K1A 0B1")),
            List.of(new Address().setPostalCode("k1a0b1"))));
        assertFalse(IDIMatch.doesZipMatch(s, List.of(new Address().setPostalCode("54321-6789"))));
    }

    @Test
    void stringsMatch() {
        assertTrue(IDIMatch.stringsMatch("Smith", "SMITH"));
        // One substitution.
        assertTrue(IDIMatch.stringsMatch("SMITH", "SMYTH"));
        // Length differs by more than one.
        assertFalse(IDIMatch.stringsMatch("SMITH", "SMITHSON"));
    }

    @Test
    void idComparatorIgnoresCase() {
        Identifier a = new Identifier().setSystem("urn:test").setValue("abc");
        Identifier b = new Identifier().setSystem("URN:TEST").setValue("ABC");
        Identifier c = new Identifier().setSystem("urn:test").setValue("abd");
        assertEquals(0, IDIMatch.idComparator(a, b));
        assertTrue(IDIMatch.idComparator(a, c) < 0);
        assertTrue(IDIMatch.idComparator(null, a) < 0);
        assertTrue(IDIMatch.idComparator(a, null) > 0);
    }

    @Test
    void last4ComparatorIgnoresCase() {
        Identifier a = new Identifier().setValue("123-45-abcd");
        Identifier b = new Identifier().setValue("999-99-ABCD");
        assertEquals(0, IDIMatch.last4Comparator(a, b));
    }

    @Test
    void countriesMatchUsesIsoCodes() {
        assertTrue(IDIMatch.countriesMatch(country("USA"), country("US")));
        assertTrue(IDIMatch.countriesMatch(country("US"), new Address()));
        assertFalse(IDIMatch.countriesMatch(country("US"), country("CA")));
    }
}

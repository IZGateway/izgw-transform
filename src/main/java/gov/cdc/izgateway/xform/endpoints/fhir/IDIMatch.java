package gov.cdc.izgateway.xform.endpoints.fhir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDetailedDistance;
import org.apache.commons.text.similarity.LevenshteinResults;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

import com.google.common.base.Objects;

import gov.cdc.izgw.v2tofhir.datatype.AddressParser;
import gov.cdc.izgw.v2tofhir.utils.ISO3166;

/**
 * This class implements the IDI Match Scoring Algorithm described
 * in the HL7 Interoperable Digital Identity and Patient Matching FHIR
 * implementation guide.
 * 
 * @author Audacious Inquiry
 * @see <a href="https://hl7.org/fhir/us/identity-matching/2024SEP/patient-matching.html#scoring-matches--responders-system-match-output-quality-score">Scoring Matches</a>
 */
public class IDIMatch {
	
	/** The best name match score	 */
	public static final double BEST_SCORE = 0.99;
	/** A superior name match score	 */
	public static final double SUPERIOR_SCORE = 0.80;
	/** A very good name match score */
	public static final double VERY_GOOD_SCORE = 0.70;
	/** A good name match score 	 */
	public static final double GOOD_SCORE = 0.60;
	
	/** Code Systems used for ID types in IDIMatch */ 
	public static final List<String> ID_SYSTEMS = Collections.unmodifiableList(Arrays.asList(
		"http://terminology.hl7.org/CodeSystem/v2-0203", 
		"http://hl7.org/fhir/us/identity-matching/CodeSystem/Identity-Identifier-cs"
	));
	// It's all static.
	private IDIMatch() {}
	
	/**
	 * Determines whether the identifier type matches one of the HL7 codes for
	 * identifier type systems.
	 *  
	 * @param idType	The type of the identifier
	 * @param types		The set of identifier types to check for
	 * @return	True if one of the codes in idType matches one of the codes in types. 
	 */
	public static boolean idTypeMatches(CodeableConcept idType, List<String> types) {
		for (Coding coding: idType.getCoding()) {
			if (coding.hasSystem() && !ID_SYSTEMS.contains(coding.getSystem())) {
				continue;
			}
			if (types.contains(coding.getCode().toUpperCase())) { 
				return true;
			}
		}
		return false;
	}

	/**
	 * Reports on where there is a match on two identifiers in search and found lists.
	 * @param search	The list of identifiers being searched for
	 * @param found		The list of identifiers found
	 * @param type		The types of identifiers to check.
	 * @return	true if there is a match.
	 */
	public static boolean doIdentifiersMatch(List<Identifier> search, List<Identifier> found, String ... type) {
		List<String> types = Arrays.asList(type);
		Set<Identifier> searchSet = new TreeSet<>("SSN4".equals(type[0]) ? IDIMatch::last4Comparator : IDIMatch::idComparator);
		searchSet.addAll(search.stream().filter(i -> idTypeMatches(i.getType(), types)).toList());
		Set<Identifier> foundSet = new TreeSet<>("SSN4".equals(type[0]) ? IDIMatch::last4Comparator : IDIMatch::idComparator);
		foundSet.addAll(search.stream().filter(i -> idTypeMatches(i.getType(), types)).toList());
		searchSet.retainAll(foundSet);
		return !searchSet.isEmpty();
	}
	
	/**
	 * Compares the system and value of two identifiers for a match.
	 * NOTE: This method performs a case insensitive match b/c for systems, it case is
	 * non-essential variation, and few if any identifier systems will care about case 
	 * either.  Those that do (e.g., in base-64 encoded hashes of random values) will be 
	 * extremely UNLIKELY to collide, whereas the chance of human error for using
	 * lowercase letters where uppercase are required is much higher and creates an 
	 * excess of false MIS-matches.  None of the idTypes used by this class ever use
	 * base-64 encoding or anything similar.
	 * 
	 * @param id1	The first identifier
	 * @param id2	The second identifier
	 * @return	true if the system and value match, false otherwise.
	 */
	public static int idComparator(Identifier id1, Identifier id2) {
		if (Objects.equal(id1, id2)) {
			return 0;
		}
		if (id1 == null) {
			return -1;
		}
		if (id2 == null) {
			return 1;
		}
		int comp = StringUtils.compareIgnoreCase(id1.getSystem(), id2.getSystem());
		if (comp != 0) {
			return comp;
		}
		return StringUtils.compareIgnoreCase(id1.getValue(), id2.getValue());
	}
	
	/**
	 * Compares the last 4 characters (digits) of identifiers. 
	 * @param id1	The first identifier
	 * @param id2	The second identifier
	 * @return true if the last four characters match.
	 */
	public static int last4Comparator(Identifier id1, Identifier id2) {
		if (Objects.equal(id1, id2)) {
			return 0;
		}
		if (id1 == null) {
			return -1;
		}
		if (id2 == null) {
			return 1;
		}
		return StringUtils.compareIgnoreCase(StringUtils.right(id1.getValue(), 4), StringUtils.right(id2.getValue(), 4));
	}
	
	
	/**
	 * Checks for a given/family name match.  Two names match if their given names are equivalent
	 * and the family name matches. This is a case insensitive match.  Hyphens are treated as being
	 * equivalent to spaces.
	 * 
	 * @param search	The list of names to match
	 * @param found		The list of names found
	 * @return	true if at least one pair of names match from search and found
	 */
	public static boolean doesNameMatch(List<HumanName> search, List<HumanName> found) {
		for (HumanName s: search) {
			for (HumanName f: found) {
				if (!stringsMatch(s.getFamily(), f.getFamily())) {
					// Family names don't match.
					continue;
				}
				if (givensAreEquivalent(s.getGiven(), f.getGiven())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Compares two given names for equivalence.  Two given names are equivalent 
	 * if they BOTH don't have a given name, OR 
	 * both first names match (common case), OR
	 * the first name of one matches the second name of the other
	 * 
	 * @param search	The sought for given name parts 
	 * @param found		The found given name parts
	 * @return	true if they match
	 */
	private static boolean givensAreEquivalent(List<StringType> search, List<StringType> found) {
		// Neither has a given name
		if ((search == null || search.isEmpty()) && (found == null || found.isEmpty())) {
			return true;
		}
		// One has a given name but the other doesn't.
		if (search == null || search.isEmpty() || found == null || found.isEmpty()) {
			return false;
		}

		// Check for most common case
		String searchFirst = search.get(0).asStringValue();
		String foundFirst = found.get(0).asStringValue();
		boolean match = stringsMatch(searchFirst, foundFirst);
		if (match) {
			return true;
		}
		
		if (search.size() == 1 && found.size() == 1) {
			return false;
		}
		
		// There's more than one given name and the first names don't match.
		// Compare the first given name against the second given name.
		if (found.size() > 1 && stringsMatch(searchFirst, found.get(1).asStringValue())) {
			return true;
		}
		
		return (search.size() > 1 && stringsMatch(search.get(1).asStringValue(), foundFirst));
	}
	
	/**
	 * Compares two strings for a match.  This is a case-insensitive match.
	 * Two strings are considered to be a match if they are within an edit
	 * distance of one of each other where 0 = no change, and 1 = a 
	 * single deletion, insertion, replacement, or transposition.
	 * 
	 * This uses the Apache Text Levenshtein Distance metric implementation
	 * and checks afterwards for transposition errors using brute force
	 * in that special case.
	 * 
	 * @param search	The strings being sought
	 * @param found		The strings that was found
	 * @return true if the two strings match.
	 */
	public static boolean stringsMatch(String search, String found) {
		if (search == found) {
			return true;
		}
		search = StringUtils.upperCase(search);
		found = StringUtils.upperCase(found);
		
		// Other checks.
		if (Math.abs(search.length() - found.length()) > 1) {
			// Requires more than one insert/delete and so they don't match.
			return false;
		}
		
		if (StringUtils.equals(search, found)) {
			return true;
		}
		LevenshteinDetailedDistance l = new LevenshteinDetailedDistance(2);
		LevenshteinResults results = l.apply(search, found);
		if (results.getDistance() < 0) {
			return false;
		}
		if (results.getDistance() <= 2) {
			return true;
		} 
		// distance is two, could be a single transposition, or some other pair of errors.
		if (search.length() == found.length() && results.getSubstituteCount() == 2) {
			// Possibly a transpose, figure out if it is.
			StringBuilder f = new StringBuilder(found);
			StringBuilder s = new StringBuilder(search);
			for (int i = 0; i < f.length() - 1; i++) {
				// Swap i and i + 1
				swap(s, i, i + 1);
				if (StringUtils.equals(s,  f)) {
					return true;
				}
				// Swap them back
				swap(s, i, i + 1);
				if (i + 2 < f.length()) {
					swap(s, i, i + 2); // Swap around a single character.
					if (StringUtils.equals(s,  f)) {
						return true;
					}
					swap(s, i, i + 2); // Swap them back.
				}
			}
		}
		return false;
	}
	
	private static void swap(StringBuilder b, int i, int j) {
		char c = b.charAt(i);
		b.setCharAt(i, b.charAt(j));
		b.setCharAt(j, c);
	}

	/**
	 * Compare two human names to see if names after the first given name (i.e., the middle names)
	 * match.
	 * @param search	The name or names being sought.
	 * @param found		The name or names that were found.
	 * @return true if the middle names match in one pairing.
	 */
	public static boolean doMiddleNamesMatch(List<HumanName> search, List<HumanName> found) {
		if (search == null || search.isEmpty() || found == null || found.isEmpty()) {
			return false;
		}
		for (HumanName s: search) {
			for (HumanName f: found) {
				// If both names have at least two given names, compare them after first given name.
				if (s.hasGiven() && s.getGiven().size() > 1 && f.hasGiven() && f.getGiven().size() > 1) {
					List<StringType> sGivens = new ArrayList<>(s.getGiven());
					List<StringType> fGivens = new ArrayList<>(f.getGiven());
					sGivens.remove(0);
					fGivens.remove(0);
					if (givensAreEquivalent(sGivens, fGivens)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * See if the found addresses match the searched for address.
	 * 
	 * @param search The address being searched for
	 * @param found	The address or addresses found
	 * @return True if the address matches
	 */
	public static boolean doesAddressMatch(List<Address> search, List<Address> found) {
		for (Address s: search) {
			for (Address f: found) {
				if (s.hasCountry() && f.hasCountry() && !countriesMatch(s.getCountry(), f.getCountry())) {
					continue;
				}
				if (!s.hasLine() || s.getLine().isEmpty() || !f.hasLine() || f.getLine().isEmpty()) {
					// Not a match on address
					continue;
				}
				StringType sLine = s.getLine().get(0);
				StringType fLine = f.getLine().get(0);
				if (stringsMatch(sLine.asStringValue(), fLine.asStringValue())) {
					if (s.getLine().size() > 1 && f.getLine().size() > 1) {
						sLine = s.getLine().get(1);
						fLine = f.getLine().get(1);
						if (!stringsMatch(sLine.asStringValue(), fLine.asStringValue())) {
							continue;
						}
					}
					if (zipsMatch(s.getPostalCode(), f.getPostalCode())) {
						return true;
					}
					if (stringsMatch(s.getCity(), f.getCity()) &&
						statesMatch(s.getState(), f.getState())
					) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determines if state names or abbreviations match.  
	 * 
	 * NOTE: This only works for states, provinces or territories of the US, 
	 * Canada and Mexico.
	 * 
	 * @param search	The sought after state or state abbreviation
	 * @param found		The found state or state abbreviation
	 * @return true if they two represent the same state (or province)
	 */
	public static boolean statesMatch(String search, String found) {
		if (Objects.equal(search, found)) {
			return true;
		}
		
		// One reports a country but the other does not, treat as a match
		if (search == null || found == null) {
			return true;
		}
		
		search = AddressParser.getStateAbbreviation(search);
		found = AddressParser.getStateAbbreviation(found);
		return Objects.equal(search, found);
	}

	/**
	 * Compare two countries for a match.  Will match a country name or alias
	 * with it's two or three-letter ISO code.
	 * 
	 * @param search	The sought for country
	 * @param found		The found country
	 * @return	true if the two match
	 */
	public static boolean countriesMatch(String search, String found) {
		if (Objects.equal(search, found)) {
			return true;
		}
		
		// One reports a country but the other does not, treat as a match
		if (search == null || found == null) {
			return true;
		}
		
		search = ISO3166.twoLetterCode(search);
		found = ISO3166.twoLetterCode(found);
		return search.equals(found);
	}
	/**
	 * Compare two zip codes for equivalence
	 * @param search The sought after postal code
	 * @param found	The found postal code
	 * @return true if the first five digits match (e.g., US, Mexico) or if 
	 * non-numeric (e.g., Canada) and the strings (sans spaces) match.
	 */
	public static boolean zipsMatch(String search, String found) {
		// Normalize by removing hyphens (e.g., US) and spaces (e.g., Canada).
		search = StringUtils.replace(search, "- ", "");
		found = StringUtils.replace(found, "- ", "");
		if (StringUtils.isNumeric(search) && StringUtils.isNumeric(found)) {
			return StringUtils.equals(StringUtils.left(search, 5), StringUtils.left(found, 5));
		}
		return StringUtils.equalsIgnoreCase(search, found);
	}

	/**
	 * Compares two dates for a match
	 * @param search	The sought for date
	 * @param found		The found date
	 * @return	true if the two dates match
	 */
	public static boolean doesDobMatch(Date search, Date found) {
		if (Objects.equal(search, found)) {
			return true;
		}
		// One or the other is missing DOB, there is no match.
		if (search == null || found == null) {
			return false;
		}
		return String.format("%tF", search).equals(String.format("%tF", found));
	}

	/**
	 * Generic method to compare two ContactPoint lists to see if there is a match
	 * @param search	The sought after ContactPoint
	 * @param found		The found ContactPoint
	 * @param system	The type of ContactPoint to check for
	 * @return	true if at least one pair matches
	 */
	public static boolean doesTelecomMatch(List<ContactPoint> search, List<ContactPoint> found, ContactPointSystem system) {
		for (ContactPoint s: search) {
			if (!system.equals(s.getSystem())) {
				continue;
			}
			for (ContactPoint f: found) {
				if (!system.equals(f.getSystem())) {
					continue;
				}
				// s and f have same system.
				String sValue = s.getValue();
				String fValue = f.getValue();
				
				// remove any punctuation before testing phone numbers
				if (ContactPointSystem.PHONE.equals(system)) {
					sValue = StringUtils.replace(sValue, ".-()[] ", "");
					fValue = StringUtils.replace(fValue, ".-()[] ", "");
				}
				if (StringUtils.equalsIgnoreCase(sValue, fValue)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Check two lists of addresses for a zip-code match
	 * @param search	The sought after addresses
	 * @param found		The found addresses
	 * @return	true if one pair in sought and found have a matching zip code
	 */
	public static boolean doesZipMatch(List<Address> search, List<Address> found) {
		for (Address s: search) {
			for (Address f: found) {
				String sPostal = s.hasPostalCode() ? s.getPostalCode() : "";
				String fPostal = f.hasPostalCode() ? f.getPostalCode() : "";
				if (StringUtils.isNumeric(sPostal) && StringUtils.isNumeric(fPostal)) {
					if (StringUtils.equals(StringUtils.left(fPostal, 5), StringUtils.left(sPostal, 5))) {
						return true;
					}
				} else if (StringUtils.equals(fPostal, sPostal)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Score the results in the bundle.
	 * 
	 * Scores the results in the bundle by comparing each returned patient against searchPatient
	 * and scoring according to the FHIR Interoperable and Digital Identity Patient Matching guide.
	 *  
	 * @param b	The bundle to score
	 * @param search The patient being searched for
	 * @param onlyCertainMatches if true, include only certain matches (score > 0.9)
	 * @param onlySingleMatch  if (true, include the top scoring match)
	 * @see <a href="https://hl7.org/fhir/us/identity-matching/2024SEP/patient-matching.html#scoring-matches--responders-system-match-output-quality-score">Scoring Matches</a>
	 */
	public static void score(Bundle b, Patient search, boolean onlySingleMatch, boolean onlyCertainMatches) {
		for (BundleEntryComponent entry: b.getEntry()) {
			Resource r = entry.getResource();
			if (!(r instanceof Patient p)) {
				continue;
			}
			getScore(search, entry, p);
	 	 	/* First Name & Last Name & Date of Birth & Sex (Assigned at Birth)
	 	 	 * First Name & Last Name & Date of Birth & Sex (Assigned at Birth) & Middle Name (initial)
	 	 	 * NOTE: First one subsumes later ones.
	 	 	 */
			setScore(entry, GOOD_SCORE);
		}
	}

	private static double getScore(Patient search, BundleEntryComponent entry, Patient p) {
		boolean mpiMatch = doIdentifiersMatch(search.getIdentifier(), p.getIdentifier(), "MR", "SR");
		if (mpiMatch) {
			return BEST_SCORE;
		} 
		// Responder's MRN/MPI or known digital identifier
		boolean idMatch = doIdentifiersMatch(search.getIdentifier(), p.getIdentifier(), "DL", "PPN", "MB");
		boolean nameMatch = doesNameMatch(search.getName(), p.getName());
		if (!nameMatch) {
			// Everything else requires a name match on at least first and last name.
			return 0.0;
		}
		if (idMatch) {
			/* First Name & Last Name & Driver's License Number and Issuing US State
			 * First Name & Last Name & Passport Number and Issuing Country
			 * First Name & Last Name & Insurance Member Identifier and Payer ID
			 */
			return BEST_SCORE;
		}
		
		boolean subscriberMatch = doIdentifiersMatch(search.getIdentifier(), p.getIdentifier(), "SN");
		boolean ssnMatch = doIdentifiersMatch(search.getIdentifier(), p.getIdentifier(), "SS", "SB");
		boolean dobMatch = doesDobMatch(search.getBirthDate(), p.getBirthDate());
		if (dobMatch && (subscriberMatch || ssnMatch)) {
			/* First Name & Last Name & Date of Birth & Insurance Subscriber Identifier and Payer ID
			 * First Name & Last Name & Date of Birth & Social Security Number
			 */
			return BEST_SCORE;
		} 
		
		if (subscriberMatch) {
			/* First Name & Last Name & Insurance Subscriber Identifier and Payer ID */
			return SUPERIOR_SCORE;
		}
		if (!dobMatch) {
			return 0.0;
		}
		
		boolean addressMatch = doesAddressMatch(search.getAddress(), p.getAddress());
		if (addressMatch) {
			/* First Name & Last Name & Date of Birth & Address line & Zip (first 5)
			 * First Name & Last Name & Date of Birth & Address line & City & State
			 */
			return SUPERIOR_SCORE;
		}
		boolean emailMatch = doesTelecomMatch(search.getTelecom(), p.getTelecom(), ContactPoint.ContactPointSystem.EMAIL);
		if (emailMatch) {
			/* First Name & Last Name & Date of Birth & email */
			return SUPERIOR_SCORE;
		}
		boolean phoneMatch = doesTelecomMatch(search.getTelecom(), p.getTelecom(), ContactPoint.ContactPointSystem.PHONE);
		if (phoneMatch) {
			/* First Name & Last Name & Date of Birth & phone
			 * First Name & Last Name & Date of Birth & Sex (Assigned at Birth) & Phone
			 * NOTE: Former subsumes latter 
			 */
			return VERY_GOOD_SCORE;
		}
		
		boolean genderMatch = Objects.equal(search.getGender(), p.getGender());
		double score = 0.0;
		if (!genderMatch) {
		 	/* First Name & Last Name & Date of Birth */
			score = GOOD_SCORE;
		}

		// At this stage, name, dob and gender match, so score is at least GOOD.
		// It gets to VERY_GOOD if Last 4 of SSN, Phone, Zip or Middle name matches.
		/* First Name & Last Name & Date of Birth & Sex (Assigned at Birth) & SSN (last 4) */
		boolean ssn4Match = doIdentifiersMatch(search.getIdentifier(), p.getIdentifier(), "SSN4", "SS");
		if (ssn4Match) {
			return VERY_GOOD_SCORE;
		}
		/* First Name & Last Name & Date of Birth & Sex (Assigned at Birth) & Zip (first 5) */
		boolean zipMatch = doesZipMatch(search.getAddress(), p.getAddress());
		if (zipMatch) {
			return VERY_GOOD_SCORE;
		}
		boolean middleNameMatch = doMiddleNamesMatch(search.getName(), p.getName());
		if (middleNameMatch) {
			/* First Name & Last Name & Date of Birth & Sex (Assigned at Birth) & Middle Name */
			return VERY_GOOD_SCORE;
		}
		return score;
	}

	private static void setScore(BundleEntryComponent entry, double d) {
		entry.getSearch().setScore(d);
		entry.getSearch().setMode(SearchEntryMode.MATCH);
		
	}
	
}

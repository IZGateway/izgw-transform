<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0">
    <xsl:variable name="HL7_CODE_MAP">
        <codeSystems>
            <codeSystem system="http://snomed.info/sct" codeSystemName="SNOMED CT"
                root="2.16.840.1.113883.6.96"/>
            <codeSystem system="http://www.nlm.nih.gov/research/umls/rxnorm" codeSystemName="RxNorm"
                root="2.16.840.1.113883.6.88"/>
            <codeSystem system="http://loinc.org" codeSystemName="LOINC" root="2.16.840.1.113883.6.1"/>
            <codeSystem system="http://unitsofmeasure.org" codeSystemName="UCUM Case Sensitive Codes"
                root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://ncimeta.nci.nih.gov" codeSystemName="NCI Metathesaurus"
                root="2.16.840.1.113883.3.26.1.2"/>
            <codeSystem system="http://www.ama-assn.org/go/cpt" codeSystemName="AMA CPT codes"
                root="2.16.840.1.113883.6.12"/>
            <codeSystem system="http://hl7.org/fhir//ndfrt" codeSystemName="//ndfrt"
                root="2.16.840.1.113883.6.209"/>
            <codeSystem system="http://fdasis.nlm.nih.gov" codeSystemName="Unique Ingredient Identifier"
                root="2.16.840.1.113883.4.9"/>
            <codeSystem system="http://hl7.org/fhir//sid/ndc" codeSystemName="//sid/ndc"
                root="2.16.840.1.113883.6.69"/>
            <codeSystem system="http://hl7.org/fhir//sid/cvx" codeSystemName="//sid/cvx"
                root="2.16.840.1.113883.12.292"/>
            <codeSystem system="urn:iso:std:iso:3166" codeSystemName="ISO Country and Regional Codes"
                root="1.0.3166.1.2.2"/>
            <codeSystem system="http://hl7.org/fhir//sid/dsm5" codeSystemName="//sid/dsm5"
                root="2.16.840.1.113883.6.344"/>
            <codeSystem system="http://www.nubc.org/patient-discharge"
                codeSystemName="NUBC code system for Patient Discharge Status"
                root="2.16.840.1.113883.6.301.5"/>
            <codeSystem system="http://www.radlex.org" codeSystemName="RadLex"
                root="2.16.840.1.113883.6.256"/>
            <codeSystem system="http://hl7.org/fhir//sid/icpc-1" codeSystemName="//sid/icpc-1"/>
            <codeSystem system="http://hl7.org/fhir//sid/icpc-1-nl" codeSystemName="//sid/icpc-1-nl"
                root="2.16.840.1.113883.2.4.4.31.1"/>
            <codeSystem system="http://hl7.org/fhir//sid/icpc-2" codeSystemName="//sid/icpc-2"
                root="2.16.840.1.113883.6.139"/>
            <codeSystem system="http://hl7.org/fhir//sid/icf-nl" codeSystemName="//sid/icf-nl"
                root="2.16.840.1.113883.6.254"/>
            <codeSystem system="https://www.gs1.org/gtin" codeSystemName="GTIN" root="1.3.160"/>
            <codeSystem system="http://www.whocc.no/atc"
                codeSystemName="Anatomical Therapeutic Chemical Classification System"
                root="2.16.840.1.113883.6.73"/>
            <codeSystem system="urn:ietf:bcp:47"
                codeSystemName="IETF language (see Tags for Identifying Languages - BCP 47 )"/>
            <codeSystem system="urn:ietf:bcp:13"
                codeSystemName="Mime Types (see Multipurpose Internet Mail Extensions (MIME) Part Four - BCP 13 )"/>
            <codeSystem system="urn:iso:std:iso:11073:10101"
                codeSystemName="Medical Device Codes (ISO 11073-10101 )"
                root="2.16.840.1.113883.6.24"/>
            <codeSystem system="http://dicom.nema.org/resources/ontology/DCM"
                codeSystemName="DICOM Code Definitions" root="1.2.840.10008.2.16.4"/>
            <codeSystem system="http://hl7.org/fhir//NamingSystem/ca-hc-din"
                codeSystemName="//NamingSystem/ca-hc-din" root="2.16.840.1.113883.5.1105"/>
            <codeSystem system="http://hl7.org/fhir//sid/ca-hc-npn" codeSystemName="//sid/ca-hc-npn"
                root="2.16.840.1.113883.5.1105"/>
            <codeSystem system="http://nucc.org/provider-taxonomy" codeSystemName="NUCC Provider Taxonomy"
                root="2.16.840.1.113883.6.101"/>
            <codeSystem system="http://www.genenames.org" codeSystemName="HGNC"
                root="2.16.840.1.113883.6.281"/>
            <codeSystem system="http://www.ensembl.org"
                codeSystemName="ENSEMBL reference sequence identifiers"/>
            <codeSystem system="http://www.ncbi.nlm.nih.gov/refseq" codeSystemName="RefSeq"
                root="2.16.840.1.113883.6.280"/>
            <codeSystem system="http://www.ncbi.nlm.nih.gov/clinvar" codeSystemName="ClinVar Variant ID"
                />
            <codeSystem system="http://sequenceontology.org" codeSystemName="Sequence Ontology"
                />
            <codeSystem system="http://varnomen.hgvs.org" codeSystemName="HGVS"
                root="2.16.840.1.113883.6.282"/>
            <codeSystem system="http://www.ncbi.nlm.nih.gov/projects/SNP" codeSystemName="DBSNP"
                root="2.16.840.1.113883.6.284"/>
            <codeSystem system="http://cancer.sanger.ac.uk/cancergenome/projects/cosmic" codeSystemName="COSMIC"
                root="2.16.840.1.113883.3.912"/>
            <codeSystem system="http://www.lrg-sequence.org" codeSystemName="LRG"
                root="2.16.840.1.113883.6.283"/>
            <codeSystem system="http://www.omim.org" codeSystemName="OMIM" root="2.16.840.1.113883.6.174"/>
            <codeSystem system="http://www.ncbi.nlm.nih.gov/pubmed" codeSystemName="PubMed"
                root="2.16.840.1.113883.13.191"/>
            <codeSystem system="http://www.pharmgkb.org" codeSystemName="PHARMGKB"
                root="2.16.840.1.113883.3.913"/>
            <codeSystem system="http://clinicaltrials.gov" codeSystemName="ClinicalTrials.gov"
                root="2.16.840.1.113883.3.1077"/>
            <codeSystem system="http://www.ebi.ac.uk/ipd/imgt/hla"
                codeSystemName="European Bioinformatics Institute" root="2.16.840.1.113883.6.341"/>
            <codeSystem system="http://hl7.org/fhir/ACMECholCodesBlood"
                codeSystemName="/ACMECholCodesBlood"/>
            <codeSystem system="http://hl7.org/fhir/CholCodeLegacyStatus"
                codeSystemName="/CholCodeLegacyStatus"/>
            <codeSystem system="http://hl7.org/fhir/CholCodeLegacyStatus"
                codeSystemName="/CholCodeLegacyStatus"/>
            <codeSystem system="http://hl7.org/fhir/ACMECholCodesBlood"
                codeSystemName="/ACMECholCodesBlood"/>
            <codeSystem system="http://hl7.org/fhir/Medication Status Codes"
                codeSystemName="/Medication Status Codes" root="2.16.840.1.113883.4.642.4.1379"/>
            <codeSystem system="http://hl7.org/fhir/Medication Status Codes"
                codeSystemName="/Medication Status Codes" root="2.16.840.1.113883.4.642.4.1380"/>
            <codeSystem system="http://hl7.org/fhir/medicationRequest Intent"
                codeSystemName="/medicationRequest Intent" root="2.16.840.1.113883.4.642.4.1378"/>
            <codeSystem system="http://hl7.org/fhir/medicationrequest Status"
                codeSystemName="/medicationrequest Status" root="2.16.840.1.113883.4.642.4.1377"/>
            <codeSystem system="http://hl7.org/fhir/status" codeSystemName="/status"
                root="2.16.840.1.113883.4.642.1.885"/>
            <codeSystem system="http://hl7.org/fhir/Code system summary example for ACME body sites"
                codeSystemName="/Code system summary example for ACME body sites"/>
            <codeSystem system="http://hl7.org/fhir/Code system summary example for ACME body sites"
                codeSystemName="/Code system summary example for ACME body sites"/>
            <codeSystem system="http://hl7.org/fhir/TaskCode" codeSystemName="/TaskCode"
                root="2.16.840.1.113883.4.642.4.1397"/>
            <codeSystem system="http://hl7.org/fhir/FHIRVersion" codeSystemName="/FHIRVersion"
                root="2.16.840.1.113883.4.642.4.1310"/>
            <codeSystem system="http://hl7.org/fhir/AbstractType" codeSystemName="/AbstractType"/>
            <codeSystem system="http://hl7.org/fhir/AccountStatus" codeSystemName="/AccountStatus"
                root="2.16.840.1.113883.4.642.4.727"/>
            <codeSystem system="http://hl7.org/fhir/ActionCardinalityBehavior"
                codeSystemName="/ActionCardinalityBehavior" root="2.16.840.1.113883.4.642.4.808"/>
            <codeSystem system="http://hl7.org/fhir/ActionConditionKind"
                codeSystemName="/ActionConditionKind" root="2.16.840.1.113883.4.642.4.816"/>
            <codeSystem system="http://hl7.org/fhir/ActionGroupingBehavior"
                codeSystemName="/ActionGroupingBehavior" root="2.16.840.1.113883.4.642.4.800"/>
            <codeSystem system="http://hl7.org/fhir/ActionParticipantType"
                codeSystemName="/ActionParticipantType" root="2.16.840.1.113883.4.642.4.812"/>
            <codeSystem system="http://hl7.org/fhir/ActionPrecheckBehavior"
                codeSystemName="/ActionPrecheckBehavior" root="2.16.840.1.113883.4.642.4.806"/>
            <codeSystem system="http://hl7.org/fhir/ActionRelationshipType"
                codeSystemName="/ActionRelationshipType" root="2.16.840.1.113883.4.642.4.814"/>
            <codeSystem system="http://hl7.org/fhir/ActionRequiredBehavior"
                codeSystemName="/ActionRequiredBehavior" root="2.16.840.1.113883.4.642.4.804"/>
            <codeSystem system="http://hl7.org/fhir/ActionSelectionBehavior"
                codeSystemName="/ActionSelectionBehavior" root="2.16.840.1.113883.4.642.4.802"/>
            <codeSystem system="http://hl7.org/fhir/AdditionalMaterialCodes"
                codeSystemName="/AdditionalMaterialCodes" root="2.16.840.1.113883.4.642.4.530"/>
            <codeSystem system="http://hl7.org/fhir/AddressType" codeSystemName="/AddressType"
                root="2.16.840.1.113883.4.642.4.70"/>
            <codeSystem system="http://hl7.org/fhir/AddressUse" codeSystemName="/AddressUse"
                root="2.16.840.1.113883.4.642.4.68"/>
            <codeSystem system="http://hl7.org/fhir/AdministrativeGender"
                codeSystemName="/AdministrativeGender" root="2.16.840.1.113883.4.642.4.2"/>
            <codeSystem system="http://hl7.org/fhir/AdverseEventActuality"
                codeSystemName="/AdverseEventActuality" root="2.16.840.1.113883.4.642.4.832"/>
            <codeSystem system="http://hl7.org/fhir/AllergyIntoleranceCategory"
                codeSystemName="/AllergyIntoleranceCategory" root="2.16.840.1.113883.4.642.4.134"/>
            <codeSystem system="http://hl7.org/fhir/AllergyIntoleranceCriticality"
                codeSystemName="/AllergyIntoleranceCriticality" root="2.16.840.1.113883.4.642.4.130"/>
            <codeSystem system="http://hl7.org/fhir/AllergyIntoleranceType"
                codeSystemName="/AllergyIntoleranceType" root="2.16.840.1.113883.4.642.4.132"/>
            <codeSystem system="http://hl7.org/fhir/GenderStatus" codeSystemName="/GenderStatus"
                root="2.16.840.1.113883.4.642.4.419"/>
            <codeSystem system="http://hl7.org/fhir/AnimalSpecies" codeSystemName="/AnimalSpecies"
                root="2.16.840.1.113883.4.642.4.421"/>
            <codeSystem system="http://hl7.org/fhir/AppointmentStatus" codeSystemName="/AppointmentStatus"
                root="2.16.840.1.113883.4.642.4.485"/>
            <codeSystem system="http://hl7.org/fhir/AssertionDirectionType"
                codeSystemName="/AssertionDirectionType" root="2.16.840.1.113883.4.642.4.707"/>
            <codeSystem system="http://hl7.org/fhir/AssertionOperatorType"
                codeSystemName="/AssertionOperatorType" root="2.16.840.1.113883.4.642.4.709"/>
            <codeSystem system="http://hl7.org/fhir/AssertionResponseTypes"
                codeSystemName="/AssertionResponseTypes" root="2.16.840.1.113883.4.642.4.711"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceAssetAvailiabilityCodes"
                codeSystemName="/ContractResourceAssetAvailiabilityCodes"
                root="2.16.840.1.113883.4.642.4.1296"/>
            <codeSystem system="http://hl7.org/fhir/AuditEventAction" codeSystemName="/AuditEventAction"
                root="2.16.840.1.113883.4.642.4.453"/>
            <codeSystem system="http://hl7.org/fhir/AuditEventOutcome" codeSystemName="/AuditEventOutcome"
                root="2.16.840.1.113883.4.642.4.455"/>
            <codeSystem system="http://hl7.org/fhir/BindingStrength" codeSystemName="/BindingStrength"
                root="2.16.840.1.113883.4.642.4.44"/>
            <codeSystem system="http://hl7.org/fhir/BundleType" codeSystemName="/BundleType"
                root="2.16.840.1.113883.4.642.4.621"/>
            <codeSystem system="http://hl7.org/fhir/CapabilityStatementKind"
                codeSystemName="/CapabilityStatementKind" root="2.16.840.1.113883.4.642.4.199"/>
            <codeSystem system="http://hl7.org/fhir/CarePlanActivityStatus"
                codeSystemName="/CarePlanActivityStatus" root="2.16.840.1.113883.4.642.4.147"/>
            <codeSystem system="http://hl7.org/fhir/CareTeamStatus" codeSystemName="/CareTeamStatus"
                root="2.16.840.1.113883.4.642.4.154"/>
            <codeSystem system="http://hl7.org/fhir/ChargeItemStatus" codeSystemName="/ChargeItemStatus"
                root="2.16.840.1.113883.4.642.4.847"/>
            <codeSystem system="http://hl7.org/fhir/Use" codeSystemName="/Use"
                root="2.16.840.1.113883.4.642.4.545"/>
            <codeSystem system="http://hl7.org/fhir/CodeSearchSupport" codeSystemName="/CodeSearchSupport"
                root="2.16.840.1.113883.4.642.4.861"/>
            <codeSystem system="http://hl7.org/fhir/CodeSystemContentMode"
                codeSystemName="/CodeSystemContentMode" root="2.16.840.1.113883.4.642.4.783"/>
            <codeSystem system="http://hl7.org/fhir/CodeSystemHierarchyMeaning"
                codeSystemName="/CodeSystemHierarchyMeaning" root="2.16.840.1.113883.4.642.4.785"/>
            <codeSystem system="http://hl7.org/fhir/CompartmentType" codeSystemName="/CompartmentType"
                root="2.16.840.1.113883.4.642.4.787"/>
            <codeSystem system="http://hl7.org/fhir/CompositionAttestationMode"
                codeSystemName="/CompositionAttestationMode" root="2.16.840.1.113883.4.642.4.239"/>
            <codeSystem system="http://hl7.org/fhir/CompositionStatus" codeSystemName="/CompositionStatus"
                root="2.16.840.1.113883.4.642.4.242"/>
            <codeSystem system="http://hl7.org/fhir/ConceptMapEquivalence"
                codeSystemName="/ConceptMapEquivalence" root="2.16.840.1.113883.4.642.4.18"/>
            <codeSystem system="http://hl7.org/fhir/FHIR Defined Concept Properties"
                codeSystemName="/FHIR Defined Concept Properties"/>
            <codeSystem system="http://hl7.org/fhir/PropertyType" codeSystemName="/PropertyType"
                root="2.16.840.1.113883.4.642.4.781"/>
            <codeSystem system="http://hl7.org/fhir/ConceptSubsumptionOutcome"
                codeSystemName="/ConceptSubsumptionOutcome" root="2.16.840.1.113883.4.642.4.1239"/>
            <codeSystem system="http://hl7.org/fhir/ConceptMapGroupUnmappedMode"
                codeSystemName="/ConceptMapGroupUnmappedMode" root="2.16.840.1.113883.4.642.4.481"/>
            <codeSystem system="http://hl7.org/fhir/ConditionalDeleteStatus"
                codeSystemName="/ConditionalDeleteStatus" root="2.16.840.1.113883.4.642.4.195"/>
            <codeSystem system="http://hl7.org/fhir/ConditionalReadStatus"
                codeSystemName="/ConditionalReadStatus" root="2.16.840.1.113883.4.642.4.201"/>
            <codeSystem system="http://hl7.org/fhir/ConsentDataMeaning"
                codeSystemName="/ConsentDataMeaning" root="2.16.840.1.113883.4.642.4.760"/>
            <codeSystem system="http://hl7.org/fhir/ConsentProvisionType"
                codeSystemName="/ConsentProvisionType" root="2.16.840.1.113883.4.642.4.758"/>
            <codeSystem system="http://hl7.org/fhir/ConsentState" codeSystemName="/ConsentState"
                root="2.16.840.1.113883.4.642.4.756"/>
            <codeSystem system="http://hl7.org/fhir/PerformerRoleCodes"
                codeSystemName="/PerformerRoleCodes" root="2.16.840.1.113883.4.642.4.1017"/>
            <codeSystem system="http://hl7.org/fhir/ConstraintSeverity"
                codeSystemName="/ConstraintSeverity" root="2.16.840.1.113883.4.642.4.82"/>
            <codeSystem system="http://hl7.org/fhir/ContactPointSystem"
                codeSystemName="/ContactPointSystem" root="2.16.840.1.113883.4.642.4.72"/>
            <codeSystem system="http://hl7.org/fhir/ContactPointUse" codeSystemName="/ContactPointUse"
                root="2.16.840.1.113883.4.642.4.74"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceActionStatusCodes"
                codeSystemName="/ContractResourceActionStatusCodes"
                root="2.16.840.1.113883.4.642.4.1304"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceAssetContextCodes"
                codeSystemName="/ContractResourceAssetContextCodes"
                root="2.16.840.1.113883.4.642.4.1298"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceAssetScopeCodes"
                codeSystemName="/ContractResourceAssetScopeCodes"
                root="2.16.840.1.113883.4.642.4.1294"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceAssetSub-TypeCodes"
                codeSystemName="/ContractResourceAssetSub-TypeCodes"
                root="2.16.840.1.113883.4.642.4.1302"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceAssetTypeCodes"
                codeSystemName="/ContractResourceAssetTypeCodes"
                root="2.16.840.1.113883.4.642.4.1300"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceDecisionModeCodes"
                codeSystemName="/ContractResourceDecisionModeCodes"
                root="2.16.840.1.113883.4.642.4.1292"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceDefinitionSubtypeCodes"
                codeSystemName="/ContractResourceDefinitionSubtypeCodes"
                root="2.16.840.1.113883.4.642.4.1213"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceDefinitionTypeCodes"
                codeSystemName="/ContractResourceDefinitionTypeCodes"
                root="2.16.840.1.113883.4.642.4.1211"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceExpirationTypeCodes"
                codeSystemName="/ContractResourceExpirationTypeCodes"
                root="2.16.840.1.113883.4.642.4.1215"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceLegalStateCodes"
                codeSystemName="/ContractResourceLegalStateCodes"
                root="2.16.840.1.113883.4.642.4.1207"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourcePartyRoleCodes"
                codeSystemName="/ContractResourcePartyRoleCodes"
                root="2.16.840.1.113883.4.642.4.1225"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourcePublicationStatusCodes"
                codeSystemName="/ContractResourcePublicationStatusCodes"
                root="2.16.840.1.113883.4.642.4.1209"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceScopeCodes"
                codeSystemName="/ContractResourceScopeCodes" root="2.16.840.1.113883.4.642.4.1217"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceScopeCodes"
                codeSystemName="/ContractResourceScopeCodes" root="2.16.840.1.113883.4.642.4.1221"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceScopeCodes"
                codeSystemName="/ContractResourceScopeCodes" root="2.16.840.1.113883.4.642.4.1219"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceSecurityControlCodes"
                codeSystemName="/ContractResourceSecurityControlCodes"
                root="2.16.840.1.113883.4.642.4.1223"/>
            <codeSystem system="http://hl7.org/fhir/ContractResourceStatusCodes"
                codeSystemName="/ContractResourceStatusCodes" root="2.16.840.1.113883.4.642.4.744"/>
            <codeSystem system="http://hl7.org/fhir/ContributorType" codeSystemName="/ContributorType"
                root="2.16.840.1.113883.4.642.4.94"/>
            <codeSystem system="http://hl7.org/fhir/DataType" codeSystemName="/DataType"/>
            <codeSystem system="http://hl7.org/fhir/DaysOfWeek" codeSystemName="/DaysOfWeek"
                root="2.16.840.1.113883.4.642.4.513"/>
            <codeSystem system="http://hl7.org/fhir/DefinitionResourceType"
                codeSystemName="/DefinitionResourceType" root="2.16.840.1.113883.4.642.4.1057"/>
            <codeSystem system="http://hl7.org/fhir/DetectedIssueSeverity"
                codeSystemName="/DetectedIssueSeverity" root="2.16.840.1.113883.4.642.4.207"/>
            <codeSystem system="http://hl7.org/fhir/ProcedureDeviceActionCodes"
                codeSystemName="/ProcedureDeviceActionCodes" root="2.16.840.1.113883.4.642.4.426"/>
            <codeSystem system="http://hl7.org/fhir/FHIRDeviceStatus" codeSystemName="/FHIRDeviceStatus"
                root="2.16.840.1.113883.4.642.4.1308"/>
            <codeSystem system="http://hl7.org/fhir/DeviceNameType" codeSystemName="/DeviceNameType"
                root="2.16.840.1.113883.4.642.4.1084"/>
            <codeSystem system="http://hl7.org/fhir/DeviceUseStatementStatus"
                codeSystemName="/DeviceUseStatementStatus" root="2.16.840.1.113883.4.642.4.215"/>
            <codeSystem system="http://hl7.org/fhir/FHIRDeviceStatus" codeSystemName="/FHIRDeviceStatus"
                root="2.16.840.1.113883.4.642.4.210"/>
            <codeSystem system="http://hl7.org/fhir/DiagnosticReportStatus"
                codeSystemName="/DiagnosticReportStatus" root="2.16.840.1.113883.4.642.4.236"/>
            <codeSystem system="http://hl7.org/fhir/DiscriminatorType" codeSystemName="/DiscriminatorType"
                root="2.16.840.1.113883.4.642.4.92"/>
            <codeSystem system="http://hl7.org/fhir/DocumentMode" codeSystemName="/DocumentMode"
                root="2.16.840.1.113883.4.642.4.187"/>
            <codeSystem system="http://hl7.org/fhir/DocumentReferenceStatus"
                codeSystemName="/DocumentReferenceStatus" root="2.16.840.1.113883.4.642.4.8"/>
            <codeSystem system="http://hl7.org/fhir/DocumentRelationshipType"
                codeSystemName="/DocumentRelationshipType" root="2.16.840.1.113883.4.642.4.245"/>
            <codeSystem system="http://hl7.org/fhir/EligibilityRequestPurpose"
                codeSystemName="/EligibilityRequestPurpose" root="2.16.840.1.113883.4.642.4.1183"/>
            <codeSystem system="http://hl7.org/fhir/EligibilityResponsePurpose"
                codeSystemName="/EligibilityResponsePurpose" root="2.16.840.1.113883.4.642.4.1185"/>
            <codeSystem system="http://hl7.org/fhir/EncounterLocationStatus"
                codeSystemName="/EncounterLocationStatus" root="2.16.840.1.113883.4.642.4.263"/>
            <codeSystem system="http://hl7.org/fhir/EncounterStatus" codeSystemName="/EncounterStatus"
                root="2.16.840.1.113883.4.642.4.247"/>
            <codeSystem system="http://hl7.org/fhir/EndpointStatus" codeSystemName="/EndpointStatus"
                root="2.16.840.1.113883.4.642.4.495"/>
            <codeSystem system="http://hl7.org/fhir/EpisodeOfCareStatus"
                codeSystemName="/EpisodeOfCareStatus" root="2.16.840.1.113883.4.642.4.665"/>
            <codeSystem system="http://hl7.org/fhir/EventCapabilityMode"
                codeSystemName="/EventCapabilityMode" root="2.16.840.1.113883.4.642.4.183"/>
            <codeSystem system="http://hl7.org/fhir/EventResourceType" codeSystemName="/EventResourceType"
                root="2.16.840.1.113883.4.642.4.1061"/>
            <codeSystem system="http://hl7.org/fhir/EventStatus" codeSystemName="/EventStatus"
                root="2.16.840.1.113883.4.642.4.110"/>
            <codeSystem system="http://hl7.org/fhir/EventTiming" codeSystemName="/EventTiming"
                root="2.16.840.1.113883.4.642.4.76"/>
            <codeSystem system="http://hl7.org/fhir/ClaimItemTypeCodes"
                codeSystemName="/ClaimItemTypeCodes" root="2.16.840.1.113883.4.642.4.549"/>
            <codeSystem system="http://hl7.org/fhir/TeethCodes" codeSystemName="/TeethCodes"
                root="2.16.840.1.113883.4.642.4.551"/>
            <codeSystem system="http://hl7.org/fhir/OralProsthoMaterialTypeCodes"
                codeSystemName="/OralProsthoMaterialTypeCodes" root="2.16.840.1.113883.4.642.4.539"/>
            <codeSystem system="http://hl7.org/fhir/ExamplePharmacyServiceCodes"
                codeSystemName="/ExamplePharmacyServiceCodes" root="2.16.840.1.113883.4.642.4.563"/>
            <codeSystem system="http://hl7.org/fhir/ExampleServiceModifierCodes"
                codeSystemName="/ExampleServiceModifierCodes" root="2.16.840.1.113883.4.642.4.573"/>
            <codeSystem system="http://hl7.org/fhir/ExampleService/ProductCodes"
                codeSystemName="/ExampleService/ProductCodes" root="2.16.840.1.113883.4.642.4.561"/>
            <codeSystem system="http://hl7.org/fhir/UDICodes" codeSystemName="/UDICodes"
                root="2.16.840.1.113883.4.642.4.555"/>
            <codeSystem system="http://hl7.org/fhir/ExampleScenarioActorType"
                codeSystemName="/ExampleScenarioActorType" root="2.16.840.1.113883.4.642.4.859"/>
            <codeSystem system="http://hl7.org/fhir/ExplanationOfBenefitStatus"
                codeSystemName="/ExplanationOfBenefitStatus" root="2.16.840.1.113883.4.642.4.619"/>
            <codeSystem system="http://hl7.org/fhir/ExposureState" codeSystemName="/ExposureState"
                root="2.16.840.1.113883.4.642.4.1352"/>
            <codeSystem system="http://hl7.org/fhir/ExpressionLanguage"
                codeSystemName="/ExpressionLanguage" root="2.16.840.1.113883.4.642.4.106"/>
            <codeSystem system="http://hl7.org/fhir/ExtensionContextType"
                codeSystemName="/ExtensionContextType" root="2.16.840.1.113883.4.642.4.1013"/>
            <codeSystem system="http://hl7.org/fhir/ExtraActivityType" codeSystemName="/ExtraActivityType"/>
            <codeSystem system="http://hl7.org/fhir/FeedingDeviceCodes"
                codeSystemName="/FeedingDeviceCodes" root="2.16.840.1.113883.4.642.4.962"/>
            <codeSystem system="http://hl7.org/fhir/FilterOperator" codeSystemName="/FilterOperator"
                root="2.16.840.1.113883.4.642.4.479"/>
            <codeSystem system="http://hl7.org/fhir/FlagPriorityCodes" codeSystemName="/FlagPriorityCodes"
                root="2.16.840.1.113883.4.642.4.951"/>
            <codeSystem system="http://hl7.org/fhir/FlagStatus" codeSystemName="/FlagStatus"
                root="2.16.840.1.113883.4.642.4.121"/>
            <codeSystem system="http://hl7.org/fhir/FMConditionCodes" codeSystemName="/FMConditionCodes"
                root="2.16.840.1.113883.4.642.4.557"/>
            <codeSystem system="http://hl7.org/fhir/FinancialResourceStatusCodes"
                codeSystemName="/FinancialResourceStatusCodes" root="2.16.840.1.113883.4.642.4.593"/>
            <codeSystem system="http://hl7.org/fhir/GenderIdentity" codeSystemName="/GenderIdentity"
                root="2.16.840.1.113883.4.642.4.973"/>
            <codeSystem system="http://hl7.org/fhir/GoalLifecycleStatus"
                codeSystemName="/GoalLifecycleStatus" root="2.16.840.1.113883.4.642.4.272"/>
            <codeSystem system="http://hl7.org/fhir/GoalStatusReason" codeSystemName="/GoalStatusReason"
                root="2.16.840.1.113883.4.642.4.278"/>
            <codeSystem system="http://hl7.org/fhir/GraphCompartmentRule"
                codeSystemName="/GraphCompartmentRule" root="2.16.840.1.113883.4.642.4.281"/>
            <codeSystem system="http://hl7.org/fhir/GraphCompartmentUse"
                codeSystemName="/GraphCompartmentUse" root="2.16.840.1.113883.4.642.4.283"/>
            <codeSystem system="http://hl7.org/fhir/GroupMeasure" codeSystemName="/GroupMeasure"
                root="2.16.840.1.113883.4.642.4.1346"/>
            <codeSystem system="http://hl7.org/fhir/GroupType" codeSystemName="/GroupType"
                root="2.16.840.1.113883.4.642.4.285"/>
            <codeSystem system="http://hl7.org/fhir/GuidanceResponseStatus"
                codeSystemName="/GuidanceResponseStatus" root="2.16.840.1.113883.4.642.4.818"/>
            <codeSystem system="http://hl7.org/fhir/GuidePageGeneration"
                codeSystemName="/GuidePageGeneration" root="2.16.840.1.113883.4.642.4.999"/>
            <codeSystem system="http://hl7.org/fhir/GuideParameterCode"
                codeSystemName="/GuideParameterCode" root="2.16.840.1.113883.4.642.4.997"/>
            <codeSystem system="http://hl7.org/fhir/FamilyHistoryStatus"
                codeSystemName="/FamilyHistoryStatus" root="2.16.840.1.113883.4.642.4.268"/>
            <codeSystem system="http://hl7.org/fhir/TestScriptRequestMethodCode"
                codeSystemName="/TestScriptRequestMethodCode" root="2.16.840.1.113883.4.642.4.717"/>
            <codeSystem system="http://hl7.org/fhir/HTTPVerb" codeSystemName="/HTTPVerb"
                root="2.16.840.1.113883.4.642.4.625"/>
            <codeSystem system="http://hl7.org/fhir/IdentifierUse" codeSystemName="/IdentifierUse"
                root="2.16.840.1.113883.4.642.4.58"/>
            <codeSystem system="http://hl7.org/fhir/IdentityAssuranceLevel"
                codeSystemName="/IdentityAssuranceLevel" root="2.16.840.1.113883.4.642.4.657"/>
            <codeSystem system="http://hl7.org/fhir/ImagingStudyStatus"
                codeSystemName="/ImagingStudyStatus" root="2.16.840.1.113883.4.642.4.991"/>
            <codeSystem system="http://hl7.org/fhir/InterventionCodes" codeSystemName="/InterventionCodes"
                root="2.16.840.1.113883.4.642.4.533"/>
            <codeSystem system="http://hl7.org/fhir/InvoicePriceComponentType"
                codeSystemName="/InvoicePriceComponentType" root="2.16.840.1.113883.4.642.4.869"/>
            <codeSystem system="http://hl7.org/fhir/InvoiceStatus" codeSystemName="/InvoiceStatus"
                root="2.16.840.1.113883.4.642.4.867"/>
            <codeSystem system="http://hl7.org/fhir/IssueSeverity" codeSystemName="/IssueSeverity"
                root="2.16.840.1.113883.4.642.4.409"/>
            <codeSystem system="http://hl7.org/fhir/IssueType" codeSystemName="/IssueType"
                root="2.16.840.1.113883.4.642.4.411"/>
            <codeSystem system="http://hl7.org/fhir/QuestionnaireItemType"
                codeSystemName="/QuestionnaireItemType" root="2.16.840.1.113883.4.642.4.445"/>
            <codeSystem system="http://hl7.org/fhir/KnowledgeResourceType"
                codeSystemName="/KnowledgeResourceType" root="2.16.840.1.113883.4.642.4.1063"/>
            <codeSystem system="http://hl7.org/fhir/LanguagePreferenceType"
                codeSystemName="/LanguagePreferenceType" root="2.16.840.1.113883.4.642.4.1023"/>
            <codeSystem system="http://hl7.org/fhir/LinkType" codeSystemName="/LinkType"
                root="2.16.840.1.113883.4.642.4.424"/>
            <codeSystem system="http://hl7.org/fhir/LinkageType" codeSystemName="/LinkageType"
                root="2.16.840.1.113883.4.642.4.315"/>
            <codeSystem system="http://hl7.org/fhir/ListMode" codeSystemName="/ListMode"
                root="2.16.840.1.113883.4.642.4.319"/>
            <codeSystem system="http://hl7.org/fhir/ListStatus" codeSystemName="/ListStatus"
                root="2.16.840.1.113883.4.642.4.327"/>
            <codeSystem system="http://hl7.org/fhir/LocationMode" codeSystemName="/LocationMode"
                root="2.16.840.1.113883.4.642.4.331"/>
            <codeSystem system="http://hl7.org/fhir/LocationStatus" codeSystemName="/LocationStatus"
                root="2.16.840.1.113883.4.642.4.333"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapContextType"
                codeSystemName="/StructureMapContextType" root="2.16.840.1.113883.4.642.4.680"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapGroupTypeMode"
                codeSystemName="/StructureMapGroupTypeMode" root="2.16.840.1.113883.4.642.4.688"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapInputMode"
                codeSystemName="/StructureMapInputMode" root="2.16.840.1.113883.4.642.4.678"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapModelMode"
                codeSystemName="/StructureMapModelMode" root="2.16.840.1.113883.4.642.4.676"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapSourceListMode"
                codeSystemName="/StructureMapSourceListMode" root="2.16.840.1.113883.4.642.4.684"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapTargetListMode"
                codeSystemName="/StructureMapTargetListMode" root="2.16.840.1.113883.4.642.4.686"/>
            <codeSystem system="http://hl7.org/fhir/StructureMapTransform"
                codeSystemName="/StructureMapTransform" root="2.16.840.1.113883.4.642.4.682"/>
            <codeSystem system="http://hl7.org/fhir/MeasureReportStatus"
                codeSystemName="/MeasureReportStatus" root="2.16.840.1.113883.4.642.4.777"/>
            <codeSystem system="http://hl7.org/fhir/MeasureReportType" codeSystemName="/MeasureReportType"
                root="2.16.840.1.113883.4.642.4.779"/>
            <codeSystem system="http://hl7.org/fhir/MessageEvent" codeSystemName="/MessageEvent"/>
            <codeSystem system="http://hl7.org/fhir/MessageSignificanceCategory"
                codeSystemName="/MessageSignificanceCategory" root="2.16.840.1.113883.4.642.4.189"/>
            <codeSystem system="http://hl7.org/fhir/messageheader-response-request"
                codeSystemName="/messageheader-response-request"
                root="2.16.840.1.113883.4.642.4.925"/>
            <codeSystem system="http://hl7.org/fhir/DeviceMetricCalibrationState"
                codeSystemName="/DeviceMetricCalibrationState" root="2.16.840.1.113883.4.642.4.653"/>
            <codeSystem system="http://hl7.org/fhir/DeviceMetricCalibrationType"
                codeSystemName="/DeviceMetricCalibrationType" root="2.16.840.1.113883.4.642.4.651"/>
            <codeSystem system="http://hl7.org/fhir/DeviceMetricCategory"
                codeSystemName="/DeviceMetricCategory" root="2.16.840.1.113883.4.642.4.649"/>
            <codeSystem system="http://hl7.org/fhir/DeviceMetricColor" codeSystemName="/DeviceMetricColor"
                root="2.16.840.1.113883.4.642.4.655"/>
            <codeSystem system="http://hl7.org/fhir/DeviceMetricOperationalStatus"
                codeSystemName="/DeviceMetricOperationalStatus" root="2.16.840.1.113883.4.642.4.647"/>
            <codeSystem system="http://hl7.org/fhir/NameUse" codeSystemName="/NameUse"
                root="2.16.840.1.113883.4.642.4.66"/>
            <codeSystem system="http://hl7.org/fhir/NamingSystemIdentifierType"
                codeSystemName="/NamingSystemIdentifierType" root="2.16.840.1.113883.4.642.4.493"/>
            <codeSystem system="http://hl7.org/fhir/NamingSystemType" codeSystemName="/NamingSystemType"
                root="2.16.840.1.113883.4.642.4.491"/>
            <codeSystem system="http://hl7.org/fhir/NarrativeStatus" codeSystemName="/NarrativeStatus"
                root="2.16.840.1.113883.4.642.4.56"/>
            <codeSystem system="http://hl7.org/fhir/AuditEventAgentNetworkType"
                codeSystemName="/AuditEventAgentNetworkType" root="2.16.840.1.113883.4.642.4.457"/>
            <codeSystem system="http://hl7.org/fhir/NoteType" codeSystemName="/NoteType"
                root="2.16.840.1.113883.4.642.4.16"/>
            <codeSystem system="http://hl7.org/fhir/ObservationRangeCategory"
                codeSystemName="/ObservationRangeCategory" root="2.16.840.1.113883.4.642.4.1334"/>
            <codeSystem system="http://hl7.org/fhir/ObservationStatus" codeSystemName="/ObservationStatus"
                root="2.16.840.1.113883.4.642.4.401"/>
            <codeSystem system="http://hl7.org/fhir/OperationKind" codeSystemName="/OperationKind"
                root="2.16.840.1.113883.4.642.4.507"/>
            <codeSystem system="http://hl7.org/fhir/OperationParameterUse"
                codeSystemName="/OperationParameterUse" root="2.16.840.1.113883.4.642.4.509"/>
            <codeSystem system="http://hl7.org/fhir/OrganizationAffiliationRole"
                codeSystemName="/OrganizationAffiliationRole" root="2.16.840.1.113883.4.642.4.881"/>
            <codeSystem system="http://hl7.org/fhir/orientationType" codeSystemName="/orientationType"
                root="2.16.840.1.113883.4.642.4.988"/>
            <codeSystem system="http://hl7.org/fhir/ParticipantRequired"
                codeSystemName="/ParticipantRequired" root="2.16.840.1.113883.4.642.4.489"/>
            <codeSystem system="http://hl7.org/fhir/ParticipationStatus"
                codeSystemName="/ParticipationStatus" root="2.16.840.1.113883.4.642.4.487"/>
            <codeSystem system="http://hl7.org/fhir/ObservationDataType"
                codeSystemName="/ObservationDataType" root="2.16.840.1.113883.4.642.4.1332"/>
            <codeSystem system="http://hl7.org/fhir/PractitionerSpecialty"
                codeSystemName="/PractitionerSpecialty" root="2.16.840.1.113883.4.642.4.442"/>
            <codeSystem system="http://hl7.org/fhir/ProcedureProgressStatusCodes"
                codeSystemName="/ProcedureProgressStatusCodes" root="2.16.840.1.113883.4.642.4.947"/>
            <codeSystem system="http://hl7.org/fhir/BiologicallyDerivedProductCategory"
                codeSystemName="/BiologicallyDerivedProductCategory"
                root="2.16.840.1.113883.4.642.4.901"/>
            <codeSystem system="http://hl7.org/fhir/BiologicallyDerivedProductStatus"
                codeSystemName="/BiologicallyDerivedProductStatus"
                root="2.16.840.1.113883.4.642.4.903"/>
            <codeSystem system="http://hl7.org/fhir/BiologicallyDerivedProductStorageScale"
                codeSystemName="/BiologicallyDerivedProductStorageScale"
                root="2.16.840.1.113883.4.642.4.905"/>
            <codeSystem system="http://hl7.org/fhir/PropertyRepresentation"
                codeSystemName="/PropertyRepresentation" root="2.16.840.1.113883.4.642.4.88"/>
            <codeSystem system="http://hl7.org/fhir/ProvenanceEntityRole"
                codeSystemName="/ProvenanceEntityRole" root="2.16.840.1.113883.4.642.4.437"/>
            <codeSystem system="http://hl7.org/fhir/ProvenanceParticipantRole"
                codeSystemName="/ProvenanceParticipantRole" root="2.16.840.1.113883.4.642.4.1306"/>
            <codeSystem system="http://hl7.org/fhir/PublicationStatus" codeSystemName="/PublicationStatus"
                root="2.16.840.1.113883.4.642.4.4"/>
            <codeSystem system="http://hl7.org/fhir/qualityType" codeSystemName="/qualityType"
                root="2.16.840.1.113883.4.642.4.229"/>
            <codeSystem system="http://hl7.org/fhir/QuantityComparator"
                codeSystemName="/QuantityComparator" root="2.16.840.1.113883.4.642.4.60"/>
            <codeSystem system="http://hl7.org/fhir/QuestionnaireResponseStatus"
                codeSystemName="/QuestionnaireResponseStatus" root="2.16.840.1.113883.4.642.4.448"/>
            <codeSystem system="http://hl7.org/fhir/QuestionnaireTextCategories"
                codeSystemName="/QuestionnaireTextCategories" root="2.16.840.1.113883.4.642.4.936"/>
            <codeSystem system="http://hl7.org/fhir/EnableWhenBehavior"
                codeSystemName="/EnableWhenBehavior" root="2.16.840.1.113883.4.642.4.1008"/>
            <codeSystem system="http://hl7.org/fhir/QuestionnaireItemOperator"
                codeSystemName="/QuestionnaireItemOperator" root="2.16.840.1.113883.4.642.4.1006"/>
            <codeSystem system="http://hl7.org/fhir/QuestionnaireItemUIControlCodes"
                codeSystemName="/QuestionnaireItemUIControlCodes"
                root="2.16.840.1.113883.4.642.4.932"/>
            <codeSystem system="http://hl7.org/fhir/AllergyIntoleranceSeverity"
                codeSystemName="/AllergyIntoleranceSeverity" root="2.16.840.1.113883.4.642.4.136"/>
            <codeSystem system="http://hl7.org/fhir/SNOMEDCTReasonMedicationNotGivenCodes"
                codeSystemName="/SNOMEDCTReasonMedicationNotGivenCodes"
                root="2.16.840.1.113883.4.642.4.343"/>
            <codeSystem system="http://hl7.org/fhir/ReferenceHandlingPolicy"
                codeSystemName="/ReferenceHandlingPolicy" root="2.16.840.1.113883.4.642.4.203"/>
            <codeSystem system="http://hl7.org/fhir/ReferenceVersionRules"
                codeSystemName="/ReferenceVersionRules" root="2.16.840.1.113883.4.642.4.90"/>
            <codeSystem system="http://hl7.org/fhir/RelatedArtifactType"
                codeSystemName="/RelatedArtifactType" root="2.16.840.1.113883.4.642.4.100"/>
            <codeSystem system="http://hl7.org/fhir/CatalogEntryRelationType"
                codeSystemName="/CatalogEntryRelationType" root="2.16.840.1.113883.4.642.4.1029"/>
            <codeSystem system="http://hl7.org/fhir/Beneficiary Relationship Codes"
                codeSystemName="/Beneficiary Relationship Codes" root="2.16.840.1.113883.4.642.4.36"/>
            <codeSystem system="http://hl7.org/fhir/ClaimProcessingCodes"
                codeSystemName="/ClaimProcessingCodes" root="2.16.840.1.113883.4.642.4.14"/>
            <codeSystem system="http://hl7.org/fhir/TestReportActionResult"
                codeSystemName="/TestReportActionResult" root="2.16.840.1.113883.4.642.4.721"/>
            <codeSystem system="http://hl7.org/fhir/TestReportParticipantType"
                codeSystemName="/TestReportParticipantType" root="2.16.840.1.113883.4.642.4.723"/>
            <codeSystem system="http://hl7.org/fhir/TestReportResult" codeSystemName="/TestReportResult"
                root="2.16.840.1.113883.4.642.4.719"/>
            <codeSystem system="http://hl7.org/fhir/TestReportStatus" codeSystemName="/TestReportStatus"
                root="2.16.840.1.113883.4.642.4.725"/>
            <codeSystem system="http://hl7.org/fhir/repositoryType" codeSystemName="/repositoryType"
                root="2.16.840.1.113883.4.642.4.231"/>
            <codeSystem system="http://hl7.org/fhir/RequestIntent" codeSystemName="/RequestIntent"
                root="2.16.840.1.113883.4.642.4.114"/>
            <codeSystem system="http://hl7.org/fhir/RequestPriority" codeSystemName="/RequestPriority"
                root="2.16.840.1.113883.4.642.4.116"/>
            <codeSystem system="http://hl7.org/fhir/RequestResourceType"
                codeSystemName="/RequestResourceType" root="2.16.840.1.113883.4.642.4.1059"/>
            <codeSystem system="http://hl7.org/fhir/RequestStatus" codeSystemName="/RequestStatus"
                root="2.16.840.1.113883.4.642.4.112"/>
            <codeSystem system="http://hl7.org/fhir/ResearchElementType"
                codeSystemName="/ResearchElementType" root="2.16.840.1.113883.4.642.4.1342"/>
            <codeSystem system="http://hl7.org/fhir/ResearchStudyStatus"
                codeSystemName="/ResearchStudyStatus" root="2.16.840.1.113883.4.642.4.820"/>
            <codeSystem system="http://hl7.org/fhir/ResearchSubjectStatus"
                codeSystemName="/ResearchSubjectStatus" root="2.16.840.1.113883.4.642.4.830"/>
            <codeSystem system="http://hl7.org/fhir/AggregationMode" codeSystemName="/AggregationMode"
                root="2.16.840.1.113883.4.642.4.86"/>
            <codeSystem system="http://hl7.org/fhir/SlicingRules" codeSystemName="/SlicingRules"
                root="2.16.840.1.113883.4.642.4.84"/>
            <codeSystem system="http://hl7.org/fhir/Canonical Status Codes for FHIR Resources"
                codeSystemName="/Canonical Status Codes for FHIR Resources"/>
            <codeSystem system="http://hl7.org/fhir/ResourceType" codeSystemName="/ResourceType"/>
            <codeSystem system="http://hl7.org/fhir/ResourceValidationMode"
                codeSystemName="/ResourceValidationMode" root="2.16.840.1.113883.4.642.4.119"/>
            <codeSystem system="http://hl7.org/fhir/ResponseType" codeSystemName="/ResponseType"
                root="2.16.840.1.113883.4.642.4.381"/>
            <codeSystem system="http://hl7.org/fhir/RestfulCapabilityMode"
                codeSystemName="/RestfulCapabilityMode" root="2.16.840.1.113883.4.642.4.177"/>
            <codeSystem system="http://hl7.org/fhir/FHIR Restful Interactions"
                codeSystemName="/FHIR Restful Interactions"/>
            <codeSystem system="http://hl7.org/fhir/SearchComparator" codeSystemName="/SearchComparator"
                root="2.16.840.1.113883.4.642.4.638"/>
            <codeSystem system="http://hl7.org/fhir/SearchEntryMode" codeSystemName="/SearchEntryMode"
                root="2.16.840.1.113883.4.642.4.623"/>
            <codeSystem system="http://hl7.org/fhir/SearchModifierCode"
                codeSystemName="/SearchModifierCode" root="2.16.840.1.113883.4.642.4.640"/>
            <codeSystem system="http://hl7.org/fhir/SearchParamType" codeSystemName="/SearchParamType"
                root="2.16.840.1.113883.4.642.4.12"/>
            <codeSystem system="http://hl7.org/fhir/XPathUsageType" codeSystemName="/XPathUsageType"
                root="2.16.840.1.113883.4.642.4.636"/>
            <codeSystem system="http://hl7.org/fhir/ObservationCategoryCodes"
                codeSystemName="/ObservationCategoryCodes" root="2.16.840.1.113883.4.642.4.1286"/>
            <codeSystem system="http://hl7.org/fhir/sequenceType" codeSystemName="/sequenceType"
                root="2.16.840.1.113883.4.642.4.220"/>
            <codeSystem system="http://hl7.org/fhir/ICD-10ProcedureCodes"
                codeSystemName="/ICD-10ProcedureCodes" root="2.16.840.1.113883.4.642.4.575"/>
            <codeSystem system="http://hl7.org/fhir/SlotStatus" codeSystemName="/SlotStatus"
                root="2.16.840.1.113883.4.642.4.483"/>
            <codeSystem system="http://hl7.org/fhir/SortDirection" codeSystemName="/SortDirection"
                root="2.16.840.1.113883.4.642.4.980"/>
            <codeSystem system="http://hl7.org/fhir/SPDXLicense" codeSystemName="/SPDXLicense"
                root="2.16.840.1.113883.4.642.4.1027"/>
            <codeSystem system="http://hl7.org/fhir/SpecimenContainedPreference"
                codeSystemName="/SpecimenContainedPreference" root="2.16.840.1.113883.4.642.4.853"/>
            <codeSystem system="http://hl7.org/fhir/SpecimenStatus" codeSystemName="/SpecimenStatus"
                root="2.16.840.1.113883.4.642.4.472"/>
            <codeSystem system="http://hl7.org/fhir/strandType" codeSystemName="/strandType"
                root="2.16.840.1.113883.4.642.4.986"/>
            <codeSystem system="http://hl7.org/fhir/StructureDefinitionKind"
                codeSystemName="/StructureDefinitionKind" root="2.16.840.1.113883.4.642.4.669"/>
            <codeSystem system="http://hl7.org/fhir/SubscriptionChannelType"
                codeSystemName="/SubscriptionChannelType" root="2.16.840.1.113883.4.642.4.501"/>
            <codeSystem system="http://hl7.org/fhir/SubscriptionStatus"
                codeSystemName="/SubscriptionStatus" root="2.16.840.1.113883.4.642.4.503"/>
            <codeSystem system="http://hl7.org/fhir/FHIRSubstanceStatus"
                codeSystemName="/FHIRSubstanceStatus" root="2.16.840.1.113883.4.642.4.475"/>
            <codeSystem system="http://hl7.org/fhir/SupplyDeliveryStatus"
                codeSystemName="/SupplyDeliveryStatus" root="2.16.840.1.113883.4.642.4.701"/>
            <codeSystem system="http://hl7.org/fhir/SupplyRequestStatus"
                codeSystemName="/SupplyRequestStatus" root="2.16.840.1.113883.4.642.4.696"/>
            <codeSystem system="http://hl7.org/fhir/TaskIntent" codeSystemName="/TaskIntent"
                root="2.16.840.1.113883.4.642.4.1241"/>
            <codeSystem system="http://hl7.org/fhir/TaskStatus" codeSystemName="/TaskStatus"
                root="2.16.840.1.113883.4.642.4.791"/>
            <codeSystem system="http://hl7.org/fhir/TransactionMode" codeSystemName="/TransactionMode"
                root="2.16.840.1.113883.4.642.4.193"/>
            <codeSystem system="http://hl7.org/fhir/TriggerType" codeSystemName="/TriggerType"
                root="2.16.840.1.113883.4.642.4.104"/>
            <codeSystem system="http://hl7.org/fhir/TypeDerivationRule"
                codeSystemName="/TypeDerivationRule" root="2.16.840.1.113883.4.642.4.674"/>
            <codeSystem system="http://hl7.org/fhir/UDIEntryType" codeSystemName="/UDIEntryType"
                root="2.16.840.1.113883.4.642.4.212"/>
            <codeSystem system="http://hl7.org/fhir/UnknownContentCode"
                codeSystemName="/UnknownContentCode" root="2.16.840.1.113883.4.642.4.197"/>
            <codeSystem system="http://hl7.org/fhir/EvidenceVariableType"
                codeSystemName="/EvidenceVariableType" root="2.16.840.1.113883.4.642.4.1344"/>
            <codeSystem system="http://hl7.org/fhir/ResourceVersionPolicy"
                codeSystemName="/ResourceVersionPolicy" root="2.16.840.1.113883.4.642.4.191"/>
            <codeSystem system="http://hl7.org/fhir/VisionBase" codeSystemName="/VisionBase"
                root="2.16.840.1.113883.4.642.4.663"/>
            <codeSystem system="http://hl7.org/fhir/VisionEyes" codeSystemName="/VisionEyes"
                root="2.16.840.1.113883.4.642.4.661"/>
            <codeSystem system="http://hl7.org/fhir/W3cProvenanceActivityType"
                codeSystemName="/W3cProvenanceActivityType"/>
            <codeSystem system="URI (all prefixed with http://terminology.hl7​.org/CodeSystem/)"
                codeSystemName="Description" root="OID"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SurfaceCodes"
                codeSystemName="Surface Codes" root="2.16.840.1.113883.4.642.4.1154"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ActionType"
                codeSystemName="ActionType" root="2.16.840.1.113883.4.642.4.1246"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ActivityDefinitionCategory"
                codeSystemName="ActivityDefinitionCategory" root="2.16.840.1.113883.4.642.4.1243"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdjudicationValueCodes"
                codeSystemName="Adjudication Value Codes" root="2.16.840.1.113883.4.642.4.1171"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Adjudication Error Codes"
                codeSystemName="This value set includes a smattering of adjudication codes."
                root="2.16.840.1.113883.4.642.4.1053"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdjudicationReasonCodes"
                codeSystemName="Adjudication Reason Codes" root="2.16.840.1.113883.4.642.4.1172"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdmitSource"
                codeSystemName="Admit source" root="2.16.840.1.113883.4.642.4.1092"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdverseEventCategory"
                codeSystemName="AdverseEventCategory" root="2.16.840.1.113883.4.642.4.1251"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdverseEventCausalityAssessment"
                codeSystemName="AdverseEventCausalityAssessment"
                root="2.16.840.1.113883.4.642.4.1254"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdverseEventCausalityMethod"
                codeSystemName="AdverseEventCausalityMethod" root="2.16.840.1.113883.4.642.4.1255"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdverseEventOutcome"
                codeSystemName="AdverseEventOutcome" root="2.16.840.1.113883.4.642.4.1252"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdverseEventSeriousness"
                codeSystemName="AdverseEventSeriousness" root="2.16.840.1.113883.4.642.4.1253"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AdverseEventSeverity"
                codeSystemName="AdverseEventSeverity" root="2.16.840.1.113883.4.642.4.1256"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/AllergyIntoleranceSubstanceExposureRisk"
                codeSystemName="AllergyIntoleranceSubstanceExposureRisk"
                root="2.16.840.1.113883.4.642.4.1275"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/AllergyIntoleranceClinicalStatusCodes"
                codeSystemName="AllergyIntolerance Clinical Status Codes"
                root="2.16.840.1.113883.4.642.4.1373"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/AllergyIntoleranceVerificationStatusCodes"
                codeSystemName="AllergyIntolerance Verification Status Codes"
                root="2.16.840.1.113883.4.642.4.1371"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/BenefitCostApplicability"
                codeSystemName="Benefit cost applicability" root="2.16.840.1.113883.4.642.1.0"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AppointmentCancellationReason"
                codeSystemName="Appointment cancellation reason"
                root="2.16.840.1.113883.4.642.4.1382"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AuditEventEntityType"
                codeSystemName="Audit event entity type" root="2.16.840.1.113883.4.642.4.1134"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AuditEventID"
                codeSystemName="Audit Event ID" root="2.16.840.1.113883.4.642.4.1136"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/BasicResourceTypes"
                codeSystemName="Basic Resource Types" root="2.16.840.1.113883.4.642.4.1072"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/NetworkTypeCodes"
                codeSystemName="Network Type Codes" root="2.16.840.1.113883.4.642.4.1177"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/BenefitTermCodes"
                codeSystemName="Benefit Term Codes" root="2.16.840.1.113883.4.642.4.1179"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/BenefitTypeCodes"
                codeSystemName="Benefit Type Codes" root="2.16.840.1.113883.4.642.4.1176"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/UnitTypeCodes"
                codeSystemName="Unit Type Codes" root="2.16.840.1.113883.4.642.4.1178"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/can-push-updates"
                codeSystemName="Can-push-updates" root="2.16.840.1.113883.4.642.1.897"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CatalogType"
                codeSystemName="CatalogType" root="2.16.840.1.113883.4.642.4.1288"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CertaintySubcomponentRating"
                codeSystemName="CertaintySubcomponentRating" root="2.16.840.1.113883.4.642.4.1362"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CertaintySubcomponentType"
                codeSystemName="CertaintySubcomponentType" root="2.16.840.1.113883.4.642.4.1360"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ChargeItemCode"
                codeSystemName="ChargeItemCode" root="2.16.840.1.113883.4.642.4.1257"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ChoiceListOrientation"
                codeSystemName="ChoiceListOrientation" root="2.16.840.1.113883.4.642.4.1273"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/chromosome-human"
                codeSystemName="chromosome-human" root="2.16.840.1.113883.4.642.4.1086"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExceptionCodes"
                codeSystemName="Exception Codes" root="2.16.840.1.113883.4.642.4.1162"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ClaimTypeCodes"
                codeSystemName="Claim Type Codes" root="2.16.840.1.113883.4.642.4.1156"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ClaimCareTeamRoleCodes"
                codeSystemName="Claim Care Team Role Codes" root="2.16.840.1.113883.4.642.4.1165"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ClaimInformationCategoryCodes"
                codeSystemName="Claim Information Category Codes"
                root="2.16.840.1.113883.4.642.4.1163"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AlternativeCodeKind"
                codeSystemName="AlternativeCodeKind" root="2.16.840.1.113883.4.642.4.1284"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CommonTags"
                codeSystemName="Common Tags" root="2.16.840.1.113883.4.642.4.1067"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CommunicationCategory"
                codeSystemName="CommunicationCategory" root="2.16.840.1.113883.4.642.4.1076"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CommunicationNotDoneReason"
                codeSystemName="CommunicationNotDoneReason" root="2.16.840.1.113883.4.642.4.1077"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CommunicationTopic"
                codeSystemName="CommunicationTopic" root="2.16.840.1.113883.4.642.4.1078"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CompositeMeasureScoring"
                codeSystemName="CompositeMeasureScoring" root="2.16.840.1.113883.4.642.4.1235"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AlternativeCodeKind"
                codeSystemName="AlternativeCodeKind" root="2.16.840.1.113883.4.642.4.1406"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConditionCategoryCodes"
                codeSystemName="Condition Category Codes" root="2.16.840.1.113883.4.642.4.1073"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConditionClinicalStatusCodes"
                codeSystemName="Condition Clinical Status Codes"
                root="2.16.840.1.113883.4.642.4.1074"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConditionState"
                codeSystemName="ConditionState" root="2.16.840.1.113883.4.642.4.1287"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConditionVerificationStatus"
                codeSystemName="ConditionVerificationStatus" root="2.16.840.1.113883.4.642.4.1075"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConformanceExpectation"
                codeSystemName="ConformanceExpectation" root="2.16.840.1.113883.4.642.4.1271"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConsentActionCodes"
                codeSystemName="Consent Action Codes" root="2.16.840.1.113883.4.642.4.1227"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConsentCategoryCodes"
                codeSystemName="Consent Category Codes" root="2.16.840.1.113883.4.642.4.1226"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConsentPolicyRuleCodes"
                codeSystemName="Consent PolicyRule Codes" root="2.16.840.1.113883.4.642.4.1229"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ConsentScopeCodes"
                codeSystemName="Consent Scope Codes" root="2.16.840.1.113883.4.642.4.1228"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContactEntityType"
                codeSystemName="Contact entity type" root="2.16.840.1.113883.4.642.4.1129"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContainerCap"
                codeSystemName="ContainerCap" root="2.16.840.1.113883.4.642.4.1258"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractContentDerivationCodes"
                codeSystemName="Contract Content Derivation Codes"
                root="2.16.840.1.113883.4.642.4.1204"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractDataMeaning"
                codeSystemName="ContractDataMeaning" root="2.16.840.1.113883.4.642.4.1205"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractTypeCodes"
                codeSystemName="Contract Type Codes" root="2.16.840.1.113883.4.642.4.1330"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractActionCodes"
                codeSystemName="Contract Action Codes" root="2.16.840.1.113883.4.642.4.1202"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractActorRoleCodes"
                codeSystemName="Contract Actor Role Codes" root="2.16.840.1.113883.4.642.4.1203"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractSignerTypeCodes"
                codeSystemName="Contract Signer Type Codes" root="2.16.840.1.113883.4.642.4.1201"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractSubtypeCodes"
                codeSystemName="Contract Subtype Codes" root="2.16.840.1.113883.4.642.4.1198"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractTermSubtypeCodes"
                codeSystemName="Contract Term Subtype Codes" root="2.16.840.1.113883.4.642.4.1200"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ContractTermTypeCodes"
                codeSystemName="Contract Term Type Codes" root="2.16.840.1.113883.4.642.4.1199"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CopyNumberEvent"
                codeSystemName="CopyNumberEvent" root="2.16.840.1.113883.4.642.4.1087"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CoverageClassCodes"
                codeSystemName="Coverage Class Codes" root="2.16.840.1.113883.4.642.4.1147"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CoverageCopayTypeCodes"
                codeSystemName="Coverage Copay Type Codes" root="2.16.840.1.113883.4.642.4.1149"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/CoverageSelfPayCodes"
                codeSystemName="Coverage SelfPay Codes" root="2.16.840.1.113883.4.642.4.1148"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/CoverageEligibilityResponseAuthSupportCodes"
                codeSystemName="CoverageEligibilityResponse Auth Support Codes"
                root="2.16.840.1.113883.4.642.4.1394"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DataAbsentReason"
                codeSystemName="DataAbsentReason" root="2.16.840.1.113883.4.642.4.1048"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DefinitionStatus"
                codeSystemName="DefinitionStatus" root="2.16.840.1.113883.4.642.4.1070"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DefinitionTopic"
                codeSystemName="DefinitionTopic" root="2.16.840.1.113883.4.642.4.1244"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DefinitionUseCodes"
                codeSystemName="Structure Definition Use Codes / Keywords"
                root="2.16.840.1.113883.4.642.4.1191"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/FHIRDeviceStatusReason"
                codeSystemName="FHIRDeviceStatusReason" root="2.16.840.1.113883.4.642.4.1082"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DiagnosisRole"
                codeSystemName="This value set defines a set of codes that can be used to express the role of a diagnosis on the Encounter or EpisodeOfCare record."
                root="2.16.840.1.113883.4.642.4.1054"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/DICOM Audit Message Record Lifecycle Events"
                codeSystemName="Attached is vocabulary for the record lifecycle events, as per DICOM Audit Message,"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Diet" codeSystemName="Diet"
                root="2.16.840.1.113883.4.642.4.1091"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DischargeDisposition"
                codeSystemName="Discharge disposition" root="2.16.840.1.113883.4.642.4.1093"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DoseAndRateType"
                codeSystemName="DoseAndRateType" root="2.16.840.1.113883.4.642.4.1069"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EffectEstimateType"
                codeSystemName="EffectEstimateType" root="2.16.840.1.113883.4.642.4.1356"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SpecialArrangements"
                codeSystemName="Special arrangements" root="2.16.840.1.113883.4.642.4.1090"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EncounterType"
                codeSystemName="Encounter type" root="2.16.840.1.113883.4.642.4.1088"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EndpointConnectionType"
                codeSystemName="Endpoint Connection Type" root="2.16.840.1.113883.4.642.4.1140"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EndpointPayloadType"
                codeSystemName="Endpoint Payload Type" root="2.16.840.1.113883.4.642.4.1139"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EnteralFormulaAdditiveTypeCode"
                codeSystemName="Enteral Formula Additive Type Code"
                root="2.16.840.1.113883.4.642.4.1123"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EpisodeOfCareType"
                codeSystemName="Episode of care type" root="2.16.840.1.113883.4.642.4.1189"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/QualityOfEvidenceRating"
                codeSystemName="QualityOfEvidenceRating" root="2.16.840.1.113883.4.642.4.1267"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/EvidenceVariantState"
                codeSystemName="EvidenceVariantState" root="2.16.840.1.113883.4.642.4.1354"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/USCLSCodes"
                codeSystemName="USCLS Codes" root="2.16.840.1.113883.4.642.4.1153"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/BenefitCategoryCodes"
                codeSystemName="Benefit Category Codes" root="2.16.840.1.113883.4.642.4.1175"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleClaimSubTypeCodes"
                codeSystemName="Example Claim SubType Codes" root="2.16.840.1.113883.4.642.4.1158"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ExampleCoverageFinancialExceptionCodes"
                codeSystemName="Example Coverage Financial Exception Codes"
                root="2.16.840.1.113883.4.642.4.1329"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleDiagnosisOnAdmissionCodes"
                codeSystemName="Example Diagnosis on Admission Codes"
                root="2.16.840.1.113883.4.642.4.1170"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleDiagnosisRelatedGroupCodes"
                codeSystemName="Example Diagnosis Related Group Codes"
                root="2.16.840.1.113883.4.642.4.1166"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleDiagnosisTypeCodes"
                codeSystemName="Example Diagnosis Type Codes" root="2.16.840.1.113883.4.642.4.1167"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ClaimPayeeResourceType"
                codeSystemName="ClaimPayeeResourceType" root="2.16.840.1.113883.4.642.4.1164"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExamplePaymentTypeCodes"
                codeSystemName="Example Payment Type Codes" root="2.16.840.1.113883.4.642.4.1181"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleProcedureTypeCodes"
                codeSystemName="Example Procedure Type Codes" root="2.16.840.1.113883.4.642.4.1388"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleProgramReasonCodes"
                codeSystemName="Example Program Reason Codes" root="2.16.840.1.113883.4.642.4.1161"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleProviderQualificationCodes"
                codeSystemName="Example Provider Qualification Codes"
                root="2.16.840.1.113883.4.642.4.1160"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ExampleRelatedClaimRelationshipCodes"
                codeSystemName="Example Related Claim Relationship Codes"
                root="2.16.840.1.113883.4.642.4.1159"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleRevenueCenterCodes"
                codeSystemName="Example Revenue Center Codes" root="2.16.840.1.113883.4.642.4.1168"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleServicePlaceCodes"
                codeSystemName="Example Service Place Codes" root="2.16.840.1.113883.4.642.4.1157"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/OralSiteCodes"
                codeSystemName="Oral Site Codes" root="2.16.840.1.113883.4.642.4.1152"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ExampleVisionPrescriptionProductCodes"
                codeSystemName="Example Vision Prescription Product Codes"
                root="2.16.840.1.113883.4.642.4.1188"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExpansionParameterSource"
                codeSystemName="ExpansionParameterSource" root="2.16.840.1.113883.4.642.4.1279"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExpansionProcessingRule"
                codeSystemName="ExpansionProcessingRules for UI presentation."
                root="2.16.840.1.113883.4.642.4.1281"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SecurityRoleType"
                codeSystemName="This CodeSystem contains Additional FHIR-defined Security Role types not defined elsewhere"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/failure-action"
                codeSystemName="Failure-action" root="2.16.840.1.113883.4.642.1.891"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/FinancialTaskCodes"
                codeSystemName="Financial Task Codes" root="2.16.840.1.113883.4.642.4.1390"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/FinancialTaskInputTypeCodes"
                codeSystemName="Financial Task Input Type Codes"
                root="2.16.840.1.113883.4.642.4.1392"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/FlagCategory"
                codeSystemName="Flag Category" root="2.16.840.1.113883.4.642.4.1071"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Form Codes"
                codeSystemName="This value set includes a sample set of Forms codes."
                root="2.16.840.1.113883.4.642.4.1052"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Funds Reservation Codes"
                codeSystemName="This value set includes sample funds reservation type codes."
                root="2.16.840.1.113883.4.642.4.1051"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/GoalAcceptanceStatus"
                codeSystemName="GoalAcceptanceStatus" root="2.16.840.1.113883.4.642.4.1270"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/GoalAchievementStatus"
                codeSystemName="Goal achievement status" root="2.16.840.1.113883.4.642.4.1375"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/GoalCategory"
                codeSystemName="Goal category" root="2.16.840.1.113883.4.642.4.1097"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/GoalPriority"
                codeSystemName="Goal priority" root="2.16.840.1.113883.4.642.4.1096"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/GoalRelationshipType"
                codeSystemName="GoalRelationshipType" root="2.16.840.1.113883.4.642.4.1269"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/HandlingConditionSet"
                codeSystemName="HandlingConditionSet" root="2.16.840.1.113883.4.642.4.1259"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/FamilyHistoryAbsentReason"
                codeSystemName="FamilyHistoryAbsentReasons history is not available."
                root="2.16.840.1.113883.4.642.4.1094"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/HL7Workgroup"
                codeSystemName="HL7Workgroup" root="2.16.840.1.113883.4.642.4.1277"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ImmunizationEvaluationDoseStatusCodes"
                codeSystemName="Immunization Evaluation Dose Status codes"
                root="2.16.840.1.113883.4.642.4.1102"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ImmunizationEvaluationDoseStatusReasonCodes"
                codeSystemName="Immunization Evaluation Dose Status Reason codes"
                root="2.16.840.1.113883.4.642.4.1103"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ImmunizationFundingSource"
                codeSystemName="Immunization Funding Source" root="2.16.840.1.113883.4.642.4.1100"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ImmunizationOriginCodes"
                codeSystemName="Immunization Origin Codes" root="2.16.840.1.113883.4.642.4.1101"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ImmunizationProgramEligibility"
                codeSystemName="Immunization Program Eligibilitys eligibility for a vaccination program. This value set is provided as a suggestive example."
                root="2.16.840.1.113883.4.642.4.1099"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ImmunizationRecommendationStatusCodes"
                codeSystemName="Immunization Recommendation Status Codes"
                root="2.16.840.1.113883.4.642.4.1104"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ImmunizationSubpotentReason"
                codeSystemName="Immunization Subpotent Reason" root="2.16.840.1.113883.4.642.4.1098"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Implant Status"
                codeSystemName="Implant Status" root="2.16.840.1.113883.4.642.4.1283"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/InsurancePlanType"
                codeSystemName="Insurance plan type" root="2.16.840.1.113883.4.642.4.1261"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ISO 21089-2017 Health Record Lifecycle Events"
                codeSystemName="Attached is vocabulary for the 27 record lifecycle events, as per ISO TS 21089-2017, Health Informatics - Trusted End-to-End Information Flows, Section 3, Terms and Definitions (2017, at ISO Central Secretariat, passed ballot and ready for publication). This will also be included in the FHIR EHR Record Lifecycle Event Implementation Guide, balloted and (to be) published with FHIR STU-3."/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/LibraryType"
                codeSystemName="LibraryType" root="2.16.840.1.113883.4.642.4.1230"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ListEmptyReasons"
                codeSystemName="List Empty Reasons" root="2.16.840.1.113883.4.642.4.1106"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleUseCodesForList"
                codeSystemName="Example Use Codes for List" root="2.16.840.1.113883.4.642.4.1105"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ListOrderCodes"
                codeSystemName="List Order Codes" root="2.16.840.1.113883.4.642.4.1107"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/LocationType"
                codeSystemName="Location type" root="2.16.840.1.113883.4.642.4.1108"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MatchGrade"
                codeSystemName="MatchGrade" root="2.16.840.1.113883.4.642.4.1289"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MeasureDataUsage"
                codeSystemName="MeasureDataUsage" root="2.16.840.1.113883.4.642.4.1234"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MeasureImprovementNotation"
                codeSystemName="MeasureImprovementNotation" root="2.16.840.1.113883.4.642.4.1395"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MeasurePopulationType"
                codeSystemName="MeasurePopulationType" root="2.16.840.1.113883.4.642.4.1231"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MeasureScoring"
                codeSystemName="MeasureScoring" root="2.16.840.1.113883.4.642.4.1232"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MeasureType"
                codeSystemName="MeasureType" root="2.16.840.1.113883.4.642.4.1233"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/MedicationAdministration Performer Function Codes"
                codeSystemName="Medication administration performer function codes"
                root="2.16.840.1.113883.4.642.4.1112"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MediaModality"
                codeSystemName="Media Modality" root="2.16.840.1.113883.4.642.4.1109"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MediaType"
                codeSystemName="Media Type" root="2.16.840.1.113883.4.642.1.326"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/MedicationAdministration Category Codes"
                codeSystemName="Medication administration category codes"
                root="2.16.840.1.113883.4.642.4.1111"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/MedicationAdministration Status Codes"
                codeSystemName="Medication administration status codes"
                root="2.16.840.1.113883.4.642.4.1311"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Medication usage category codes"
                codeSystemName="Medication usage category codes"
                root="2.16.840.1.113883.4.642.4.1120"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/MedicationDispense Performer Function Codes"
                codeSystemName="Medication dispense performer function codes"
                root="2.16.840.1.113883.4.642.4.1319"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MedicationDispense Status Codes"
                codeSystemName="Medication dispense status codes"
                root="2.16.840.1.113883.4.642.4.1313"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/medicationKnowledge Characteristic Codes"
                codeSystemName="Medication knowledge characteristic codes"
                root="2.16.840.1.113883.4.642.4.1338"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/medicationKnowledge Package Type Codes"
                codeSystemName="Medication knowledge package type codes"
                root="2.16.840.1.113883.4.642.4.1340"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MedicationKnowledge Status Codes"
                codeSystemName="Medication knowledge status codes"
                root="2.16.840.1.113883.4.642.4.1336"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/medicationRequest Category Codes"
                codeSystemName="Medication request category codes"
                root="2.16.840.1.113883.4.642.4.1323"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/medicationRequest Course of Therapy Codes"
                codeSystemName="Medication request course of therapy codes"
                root="2.16.840.1.113883.4.642.4.1327"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/medicationRequest Status Reason Codes"
                codeSystemName="Medication request status reason codes"
                root="2.16.840.1.113883.4.642.4.1325"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ExampleMessageReasonCodes"
                codeSystemName="Example Message Reason Codes" root="2.16.840.1.113883.4.642.4.1122"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MessageTransport"
                codeSystemName="MessageTransport" root="2.16.840.1.113883.4.642.4.1080"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MissingToothReasonCodes"
                codeSystemName="Missing Tooth Reason Codes" root="2.16.840.1.113883.4.642.4.1150"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ModifierTypeCodes"
                codeSystemName="Modifier type Codes" root="2.16.840.1.113883.4.642.4.1151"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/HumanNameAssemblyOrder"
                codeSystemName="HumanNameAssemblyOrder" root="2.16.840.1.113883.4.642.4.1266"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/need" codeSystemName="Need"
                root="2.16.840.1.113883.4.642.1.883"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AuditEventEntityRole"
                codeSystemName="AuditEventEntityRole" root="2.16.840.1.113883.4.642.4.1135"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ObservationCategoryCodes"
                codeSystemName="Observation Category Codes" root="2.16.840.1.113883.4.642.4.1125"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/StatisticsCode"
                codeSystemName="StatisticsCode" root="2.16.840.1.113883.4.642.4.1126"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/OperationOutcomeCodes"
                codeSystemName="Operation Outcome Codes" root="2.16.840.1.113883.4.642.4.1127"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/OrganizationType"
                codeSystemName="Organization type" root="2.16.840.1.113883.4.642.4.1128"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/DeviceDefinitionParameterGroup"
                codeSystemName="DeviceDefinitionParameterGroup"
                root="2.16.840.1.113883.4.642.4.1264"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ParticipantType"
                codeSystemName="Participant type" root="2.16.840.1.113883.4.642.4.1089"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Claim Payee Type Codes"
                codeSystemName="This value set includes sample Payee Type codes."
                root="2.16.840.1.113883.4.642.4.1050"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PaymentAdjustmentReasonCodes"
                codeSystemName="Payment Adjustment Reason Codes"
                root="2.16.840.1.113883.4.642.4.1173"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PaymentTypeCodes"
                codeSystemName="Payment Type Codes" root="2.16.840.1.113883.4.642.4.1186"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PaymentStatusCodes"
                codeSystemName="Payment Status Codes" root="2.16.840.1.113883.4.642.4.1187"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PlanDefinitionType"
                codeSystemName="PlanDefinitionType" root="2.16.840.1.113883.4.642.4.1245"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PractitionerRole"
                codeSystemName="Practitioner role" root="2.16.840.1.113883.4.642.4.1132"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PrecisionEstimateType"
                codeSystemName="PrecisionEstimateType" root="2.16.840.1.113883.4.642.4.1358"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/primary-source-type"
                codeSystemName="Primary-source-type" root="2.16.840.1.113883.4.642.1.893"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ProcessPriorityCodes"
                codeSystemName="Process Priority Codes" root="2.16.840.1.113883.4.642.4.1155"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/Program" codeSystemName="Program"
                root="2.16.840.1.113883.4.642.4.1384"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ProvenanceParticipantType"
                codeSystemName="Provenance participant type" root="2.16.840.1.113883.4.642.4.1131"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/push-type-available"
                codeSystemName="Push-type-available" root="2.16.840.1.113883.4.642.1.899"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/MaxOccurs"
                codeSystemName="MaxOccurs" root="2.16.840.1.113883.4.642.4.1272"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/QuestionnaireItemUsageMode"
                codeSystemName="QuestionnaireItemUsageMode" root="2.16.840.1.113883.4.642.4.1274"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AllergyIntoleranceCertainty"
                codeSystemName="AllergyIntoleranceCertainty" root="2.16.840.1.113883.4.642.4.1276"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ReasonMedicationGivenCodes"
                codeSystemName="Reason Medication Given Codes" root="2.16.840.1.113883.4.642.4.1110"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/StrengthOfRecommendationRating"
                codeSystemName="StrengthOfRecommendationRating"
                root="2.16.840.1.113883.4.642.4.1268"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/ObservationReferenceRangeMeaningCodes"
                codeSystemName="Observation Reference Range Meaning Codes"
                root="2.16.840.1.113883.4.642.4.1124"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/RejectionCriterion"
                codeSystemName="RejectionCriterion" root="2.16.840.1.113883.4.642.4.1260"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ResearchStudyObjectiveType"
                codeSystemName="ResearchStudyObjectiveType" root="2.16.840.1.113883.4.642.4.1248"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ResearchStudyPhase"
                codeSystemName="ResearchStudyPhase" root="2.16.840.1.113883.4.642.4.1247"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ResearchStudyPrimaryPurposeType"
                codeSystemName="ResearchStudyPrimaryPurposeType"
                root="2.16.840.1.113883.4.642.4.1250"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ResearchStudyReasonStopped"
                codeSystemName="ResearchStudyReasonStopped" root="2.16.840.1.113883.4.642.4.1249"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ResourceSecurityCategory"
                codeSystemName="ResourceSecurityCategory" root="2.16.840.1.113883.4.642.4.1404"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/PayeeResourceType"
                codeSystemName="PayeeResourceType" root="2.16.840.1.113883.4.642.4.1180"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/RestfulSecurityService"
                codeSystemName="RestfulSecurityService" root="2.16.840.1.113883.4.642.4.1079"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/RiskEstimateType"
                codeSystemName="RiskEstimateType" root="2.16.840.1.113883.4.642.4.1364"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/RiskProbability"
                codeSystemName="Risk Probability" root="2.16.840.1.113883.4.642.4.1133"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/AuditEventSourceType"
                codeSystemName="Audit Event Source Type" root="2.16.840.1.113883.4.642.4.1137"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ServiceCategory"
                codeSystemName="Service category" root="2.16.840.1.113883.4.642.4.1144"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ServiceProvisionConditions"
                codeSystemName="ServiceProvisionConditions" root="2.16.840.1.113883.4.642.4.1143"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ReferralMethod"
                codeSystemName="ReferralMethod" root="2.16.840.1.113883.4.642.4.1142"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/ServiceType"
                codeSystemName="Service type" root="2.16.840.1.113883.4.642.4.1145"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SmartCapabilities"
                codeSystemName="SmartCapabilities" root="2.16.840.1.113883.4.642.4.1265"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SpecialValues"
                codeSystemName="SpecialValues" root="2.16.840.1.113883.4.642.4.1049"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/StandardsStatus"
                codeSystemName="StandardsStatus" root="2.16.840.1.113883.4.642.4.1366"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/StudyType"
                codeSystemName="StudyType" root="2.16.840.1.113883.4.642.4.1350"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SubscriberRelationshipCodes"
                codeSystemName="SubscriberPolicyholder Relationship Codes"
                root="2.16.840.1.113883.4.642.4.1386"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SubscriptionTag"
                codeSystemName="SubscriptionTag" root="2.16.840.1.113883.4.642.4.1141"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SubstanceCategoryCodes"
                codeSystemName="Substance Category Codes" root="2.16.840.1.113883.4.642.4.1138"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SupplyItemType"
                codeSystemName="Supply Item Type" root="2.16.840.1.113883.4.642.4.1194"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SupplyType"
                codeSystemName="Supply Type" root="2.16.840.1.113883.4.642.4.1192"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SupplyRequestReason"
                codeSystemName="SupplyRequestReason" root="2.16.840.1.113883.4.642.4.1193"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/SynthesisType"
                codeSystemName="SynthesisType" root="2.16.840.1.113883.4.642.4.1348"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/TestScriptOperationCode"
                codeSystemName="Test script operation code" root="2.16.840.1.113883.4.642.4.1195"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/TestScriptProfileDestinationType"
                codeSystemName="Test script profile destination type"
                root="2.16.840.1.113883.4.642.4.1197"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/TestScriptProfileOriginType"
                codeSystemName="Test script profile origin type"
                root="2.16.840.1.113883.4.642.4.1196"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/UsageContextType"
                codeSystemName="UsageContextType" root="2.16.840.1.113883.4.642.4.1068"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/validation-process"
                codeSystemName="Validation-process" root="2.16.840.1.113883.4.642.1.889"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/validation-status"
                codeSystemName="Validation-status" root="2.16.840.1.113883.4.642.1.895"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/validation-type"
                codeSystemName="Validation-type" root="2.16.840.1.113883.4.642.1.887"/>
            <codeSystem system="http://terminology.hl7​.org/CodeSystem/sequenceStatus"
                codeSystemName="sequenceStatus" root="2.16.840.1.113883.4.642.4.1085"/>
            <code
                system="http://terminology.hl7​.org/CodeSystem/verificationresult-communication-method"
                codeSystemName="VerificationResult Communication Methods API or information may be sent to the system of record. This value set defines a set of codes to describing the process, the how, a resource or data element is validated."
                root="2.16.840.1.113883.4.642.4.1402"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AcknowledgementCondition"
                codeSystemName="AcknowledgementCondition" root="2.16.840.1.113883.5.1050"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AcknowledgementDetailCode"
                codeSystemName="AcknowledgementDetailCode" root="2.16.840.1.113883.5.1100"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AcknowledgementDetailType"
                codeSystemName="AcknowledgementDetailType" root="2.16.840.1.113883.5.1082"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AcknowledgementType"
                codeSystemName="AcknowledgementType" root="2.16.840.1.113883.5.18"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActClass"
                codeSystemName="ActClass" root="2.16.840.1.113883.5.6"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActCode"
                codeSystemName="ActCode" root="2.16.840.1.113883.5.4"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActExposureLevelCode"
                codeSystemName="ActExposureLevelCode" root="2.16.840.1.113883.5.1114"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActInvoiceElementModifier"
                codeSystemName="ActInvoiceElementModifier" root="2.16.840.1.113883.5.1051"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActMood"
                codeSystemName="ActMood" root="2.16.840.1.113883.5.1001"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActPriority"
                codeSystemName="ActPriority" root="2.16.840.1.113883.5.7"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActReason"
                codeSystemName="ActReason" root="2.16.840.1.113883.5.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActRelationshipCheckpoint"
                codeSystemName="ActRelationshipCheckpoint" root="2.16.840.1.113883.5.10"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActRelationshipJoin"
                codeSystemName="ActRelationshipJoin" root="2.16.840.1.113883.5.12"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActRelationshipSplit"
                codeSystemName="ActRelationshipSplit" root="2.16.840.1.113883.5.13"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActRelationshipSubset"
                codeSystemName="ActRelationshipSubset" root="2.16.840.1.113883.5.1099"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActRelationshipType"
                codeSystemName="ActRelationshipType" root="2.16.840.1.113883.5.1002"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActSite"
                codeSystemName="ActSite" root="2.16.840.1.113883.5.1052"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActStatus"
                codeSystemName="ActStatus" root="2.16.840.1.113883.5.14"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActUSPrivacyLaw"
                codeSystemName="ActUSPrivacyLaw" root="2.16.840.1.113883.5.1138"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ActUncertainty"
                codeSystemName="ActUncertainty" root="2.16.840.1.113883.5.1053"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AddressPartType"
                codeSystemName="AddressPartType" root="2.16.840.1.113883.5.16"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AddressUse"
                codeSystemName="AddressUse" root="2.16.840.1.113883.5.1119"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-AdministrativeGender"
                codeSystemName="AdministrativeGender" root="2.16.840.1.113883.5.1"/>
            <code
                system="http://terminology.hl7.org/CodeSystem/v3-AmericanIndianAlaskaNativeLanguages"
                codeSystemName="AmericanIndianAlaskaNativeLanguages"
                root="2.16.840.1.113883.5.1054"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Calendar"
                codeSystemName="Calendar" root="2.16.840.1.113883.5.1055"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-CalendarCycle"
                codeSystemName="CalendarCycle" root="2.16.840.1.113883.5.9"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-CalendarType"
                codeSystemName="CalendarType" root="2.16.840.1.113883.5.1017"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Charset"
                codeSystemName="Charset" root="2.16.840.1.113883.5.21"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-CodingRationale"
                codeSystemName="CodingRationale" root="2.16.840.1.113883.5.1074"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-CommunicationFunctionType"
                codeSystemName="CommunicationFunctionType" root="2.16.840.1.113883.5.1056"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-CompressionAlgorithm"
                codeSystemName="CompressionAlgorithm" root="2.16.840.1.113883.5.1009"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Confidentiality"
                codeSystemName="Confidentiality" root="2.16.840.1.113883.5.25"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ContainerCap"
                codeSystemName="ContainerCap" root="2.16.840.1.113883.5.26"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ContainerSeparator"
                codeSystemName="ContainerSeparator" root="2.16.840.1.113883.5.27"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ContentProcessingMode"
                codeSystemName="ContentProcessingMode" root="2.16.840.1.113883.5.1110"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ContextControl"
                codeSystemName="ContextControl" root="2.16.840.1.113883.5.1057"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-DataOperation"
                codeSystemName="DataOperation" root="2.16.840.1.113883.5.1123"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Dentition"
                codeSystemName="Dentition" root="2.16.840.1.113883.5.1080"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-DeviceAlertLevel"
                codeSystemName="DeviceAlertLevel" root="2.16.840.1.113883.5.31"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-DocumentCompletion"
                codeSystemName="DocumentCompletion" root="2.16.840.1.113883.5.33"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-DocumentStorage"
                codeSystemName="DocumentStorage" root="2.16.840.1.113883.5.34"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EducationLevel"
                codeSystemName="EducationLevel" root="2.16.840.1.113883.5.1077"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EmployeeJobClass"
                codeSystemName="EmployeeJobClass" root="2.16.840.1.113883.5.1059"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EncounterAdmissionSource"
                codeSystemName="EncounterAdmissionSource" root="2.16.840.1.113883.5.37"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EncounterSpecialCourtesy"
                codeSystemName="EncounterSpecialCourtesy" root="2.16.840.1.113883.5.40"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityClass"
                codeSystemName="EntityClass" root="2.16.840.1.113883.5.41"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityCode"
                codeSystemName="EntityCode" root="2.16.840.1.113883.5.1060"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityDeterminer"
                codeSystemName="EntityDeterminer" root="2.16.840.1.113883.5.30"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityHandling"
                codeSystemName="EntityHandling" root="2.16.840.1.113883.5.42"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityNamePartQualifier"
                codeSystemName="EntityNamePartQualifier" root="2.16.840.1.113883.5.43"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityNamePartQualifierR2"
                codeSystemName="EntityNamePartQualifierR2" root="2.16.840.1.113883.5.1122"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityNamePartType"
                codeSystemName="EntityNamePartType" root="2.16.840.1.113883.5.44"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityNamePartTypeR2"
                codeSystemName="EntityNamePartTypeR2" root="2.16.840.1.113883.5.1121"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityNameUse"
                codeSystemName="EntityNameUse" root="2.16.840.1.113883.5.45"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityNameUseR2"
                codeSystemName="EntityNameUseR2" root="2.16.840.1.113883.5.1120"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityRisk"
                codeSystemName="EntityRisk" root="2.16.840.1.113883.5.46"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EntityStatus"
                codeSystemName="EntityStatus" root="2.16.840.1.113883.5.1061"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-EquipmentAlertLevel"
                codeSystemName="EquipmentAlertLevel" root="2.16.840.1.113883.5.49"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Ethnicity"
                codeSystemName="Ethnicity" root="2.16.840.1.113883.5.50"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ExposureMode"
                codeSystemName="ExposureMode" root="2.16.840.1.113883.5.1113"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-GTSAbbreviation"
                codeSystemName="GTSAbbreviation" root="2.16.840.1.113883.5.1022"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-GenderStatus"
                codeSystemName="GenderStatus" root="2.16.840.1.113883.5.51"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-HL7ContextConductionStyle"
                codeSystemName="HL7ContextConductionStyle" root="2.16.840.1.113883.5.1129"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-HL7StandardVersionCode"
                codeSystemName="HL7StandardVersionCode" root="2.16.840.1.113883.5.1097"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-HL7UpdateMode"
                codeSystemName="HL7UpdateMode" root="2.16.840.1.113883.5.57"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-HtmlLinkType"
                codeSystemName="HtmlLinkType" root="2.16.840.1.113883.5.58"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-IdentifierReliability"
                codeSystemName="IdentifierReliability" root="2.16.840.1.113883.5.1117"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-IdentifierScope"
                codeSystemName="IdentifierScope" root="2.16.840.1.113883.5.1116"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-IntegrityCheckAlgorithm"
                codeSystemName="IntegrityCheckAlgorithm" root="2.16.840.1.113883.5.1010"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityMode"
                codeSystemName="LanguageAbilityMode" root="2.16.840.1.113883.5.60"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-LanguageAbilityProficiency"
                codeSystemName="LanguageAbilityProficiency" root="2.16.840.1.113883.5.61"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-LivingArrangement"
                codeSystemName="LivingArrangement" root="2.16.840.1.113883.5.63"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-LocalMarkupIgnore"
                codeSystemName="LocalMarkupIgnore" root="2.16.840.1.113883.5.65"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-LocalRemoteControlState"
                codeSystemName="LocalRemoteControlState" root="2.16.840.1.113883.5.66"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ManagedParticipationStatus"
                codeSystemName="ManagedParticipationStatus" root="2.16.840.1.113883.5.1062"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-MapRelationship"
                codeSystemName="MapRelationship" root="2.16.840.1.113883.5.67"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-MaritalStatus"
                codeSystemName="MaritalStatus" root="2.16.840.1.113883.5.2"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-MessageWaitingPriority"
                codeSystemName="MessageWaitingPriority" root="2.16.840.1.113883.5.1083"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ModifyIndicator"
                codeSystemName="ModifyIndicator" root="2.16.840.1.113883.5.81"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-NullFlavor"
                codeSystemName="NullFlavor" root="2.16.840.1.113883.5.1008"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ObservationCategory"
                codeSystemName="ObservationCategory" root="2.16.840.1.113883.4.642.1.222"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation"
                codeSystemName="ObservationInterpretation" root="2.16.840.1.113883.5.83"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ObservationMethod"
                codeSystemName="ObservationMethod" root="2.16.840.1.113883.5.84"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ObservationValue"
                codeSystemName="ObservationValue" root="2.16.840.1.113883.5.1063"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ParticipationFunction"
                codeSystemName="ParticipationFunction" root="2.16.840.1.113883.5.88"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ParticipationMode"
                codeSystemName="ParticipationMode" root="2.16.840.1.113883.5.1064"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ParticipationSignature"
                codeSystemName="ParticipationSignature" root="2.16.840.1.113883.5.89"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ParticipationType"
                codeSystemName="ParticipationType" root="2.16.840.1.113883.5.90"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-PatientImportance"
                codeSystemName="PatientImportance" root="2.16.840.1.113883.5.1075"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-PaymentTerms"
                codeSystemName="PaymentTerms" root="2.16.840.1.113883.5.91"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-PersonDisabilityType"
                codeSystemName="PersonDisabilityType" root="2.16.840.1.113883.5.93"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ProbabilityDistributionType"
                codeSystemName="ProbabilityDistributionType" root="2.16.840.1.113883.5.1020"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ProcessingID"
                codeSystemName="ProcessingID" root="2.16.840.1.113883.5.100"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ProcessingMode"
                codeSystemName="ProcessingMode" root="2.16.840.1.113883.5.101"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-QueryParameterValue"
                codeSystemName="QueryParameterValue" root="2.16.840.1.113883.5.1096"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-QueryPriority"
                codeSystemName="QueryPriority" root="2.16.840.1.113883.5.102"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-QueryRequestLimit"
                codeSystemName="QueryRequestLimit" root="2.16.840.1.113883.5.1112"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-QueryResponse"
                codeSystemName="QueryResponse" root="2.16.840.1.113883.5.1067"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-QueryStatusCode"
                codeSystemName="QueryStatusCode" root="2.16.840.1.113883.5.103"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Race" codeSystemName="Race"
                root="2.16.840.1.113883.5.104"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RelationalOperator"
                codeSystemName="RelationalOperator" root="2.16.840.1.113883.5.105"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RelationshipConjunction"
                codeSystemName="RelationshipConjunction" root="2.16.840.1.113883.5.106"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ReligiousAffiliation"
                codeSystemName="ReligiousAffiliation" root="2.16.840.1.113883.5.1076"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ResponseLevel"
                codeSystemName="ResponseLevel" root="2.16.840.1.113883.5.108"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ResponseModality"
                codeSystemName="ResponseModality" root="2.16.840.1.113883.5.109"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-ResponseMode"
                codeSystemName="ResponseMode" root="2.16.840.1.113883.5.1126"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RoleClass"
                codeSystemName="RoleClass" root="2.16.840.1.113883.5.110"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RoleCode"
                codeSystemName="RoleCode" root="2.16.840.1.113883.5.111"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RoleLinkStatus"
                codeSystemName="RoleLinkStatus" root="2.16.840.1.113883.5.1137"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RoleLinkType"
                codeSystemName="RoleLinkType" root="2.16.840.1.113883.5.107"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RoleStatus"
                codeSystemName="RoleStatus" root="2.16.840.1.113883.5.1068"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-RouteOfAdministration"
                codeSystemName="RouteOfAdministration" root="2.16.840.1.113883.5.112"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-Sequencing"
                codeSystemName="Sequencing" root="2.16.840.1.113883.5.113"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-SetOperator"
                codeSystemName="SetOperator" root="2.16.840.1.113883.5.1069"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-SpecimenType"
                codeSystemName="SpecimenType" root="2.16.840.1.113883.5.129"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-SubstitutionCondition"
                codeSystemName="SubstitutionCondition" root="2.16.840.1.113883.5.1071"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TableCellHorizontalAlign"
                codeSystemName="TableCellHorizontalAlign" root="2.16.840.1.113883.5.131"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TableCellScope"
                codeSystemName="TableCellScope" root="2.16.840.1.113883.5.132"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TableCellVerticalAlign"
                codeSystemName="TableCellVerticalAlign" root="2.16.840.1.113883.5.133"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TableFrame"
                codeSystemName="TableFrame" root="2.16.840.1.113883.5.134"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TableRules"
                codeSystemName="TableRules" root="2.16.840.1.113883.5.136"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TargetAwareness"
                codeSystemName="TargetAwareness" root="2.16.840.1.113883.5.137"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TelecommunicationCapabilities"
                codeSystemName="TelecommunicationCapabilities" root="2.16.840.1.113883.5.1118"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TimingEvent"
                codeSystemName="TimingEvent" root="2.16.840.1.113883.5.139"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TransmissionRelationshipTypeCode"
                codeSystemName="TransmissionRelationshipTypeCode" root="2.16.840.1.113883.5.1111"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-TribalEntityUS"
                codeSystemName="TribalEntityUS" root="2.16.840.1.113883.5.140"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-VaccineManufacturer"
                codeSystemName="VaccineManufacturer" root="2.16.840.1.113883.5.144"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-WorkClassificationODH"
                codeSystemName="WorkClassificationODH" root="2.16.840.1.113883.5.1139"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm"
                codeSystemName="orderableDrugForm" root="2.16.840.1.113883.5.85"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-policyHolderRole"
                codeSystemName="policyHolderRole" root="2.16.840.1.113883.5.1128"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-styleType"
                codeSystemName="styleType" root="2.16.840.1.113883.5.1095"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-substanceAdminSubstitution"
                codeSystemName="substanceAdminSubstitution" root="2.16.840.1.113883.5.1070"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v3-triggerEventID"
                codeSystemName="triggerEventID" root="2.16.840.1.113883.1.18"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0001"
                codeSystemName="Administrative Sex" root="2.16.840.1.113883.18.2"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0002"
                codeSystemName="Marital Status" root="2.16.840.1.113883.18.179"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0003" codeSystemName="Event Type"
                root="2.16.840.1.113883.18.4"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0004"
                codeSystemName="Patient Class" root="2.16.840.1.113883.18.5"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0005" codeSystemName="Race"
                root="2.16.840.1.113883.6.238"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0006" codeSystemName="Religion"
                root="2.16.840.1.113883.18.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0007"
                codeSystemName="Admission Type" root="2.16.840.1.113883.18.9"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0008"
                codeSystemName="Acknowledgment code" root="2.16.840.1.113883.18.10"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0009"
                codeSystemName="Ambulatory Status" root="2.16.840.1.113883.18.11"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0010"
                codeSystemName="Physician ID" root="2.16.840.1.113883.12.10"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0011"
                codeSystemName="CHARGING SYSTEM" root="2.16.840.1.113883.12.11"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0012"
                codeSystemName="STOCK LOCATION" root="2.16.840.1.113883.18.12"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0016" codeSystemName="ISOLATION"
                root="2.16.840.1.113883.12.16"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0017"
                codeSystemName="Transaction Type" root="2.16.840.1.113883.18.13"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0018"
                codeSystemName="Patient Type" root="2.16.840.1.113883.12.18"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0019"
                codeSystemName="Anesthesia Code" root="2.16.840.1.113883.12.19"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0020"
                codeSystemName="UNUSED TABLE" root="2.16.840.1.113883.12.20"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0021"
                codeSystemName="Bad Debt Agency Code" root="2.16.840.1.113883.12.21"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0022"
                codeSystemName="Billing Status" root="2.16.840.1.113883.12.22"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0023"
                codeSystemName="Admit Source" root="2.16.840.1.113883.12.23"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0024"
                codeSystemName="Fee Schedule" root="2.16.840.1.113883.12.24"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0027" codeSystemName="Priority"
                root="2.16.840.1.113883.18.15"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0032"
                codeSystemName="Charge/Price Indicator" root="2.16.840.1.113883.12.32"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0033" codeSystemName="ROUTE"
                root="2.16.840.1.113883.18.16"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0034"
                codeSystemName="SITE ADMINISTERED" root="2.16.840.1.113883.18.17"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0038"
                codeSystemName="Order status" root="2.16.840.1.113883.18.18"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0042"
                codeSystemName="Company Plan Code" root="2.16.840.1.113883.12.42"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0043"
                codeSystemName="Condition Code" root="2.16.840.1.113883.12.43"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0044"
                codeSystemName="Contract Code" root="2.16.840.1.113883.12.44"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0045"
                codeSystemName="Courtesy Code" root="2.16.840.1.113883.12.45"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0046"
                codeSystemName="Credit Rating" root="2.16.840.1.113883.12.46"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0047"
                codeSystemName="DANGER CODE" root="2.16.840.1.113883.12.47"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0048"
                codeSystemName="What subject filter" root="2.16.840.1.113883.18.20"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0049"
                codeSystemName="Department Code" root="2.16.840.1.113883.12.49"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0050"
                codeSystemName="Accident Code" root="2.16.840.1.113883.12.50"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0051"
                codeSystemName="Diagnosis Code" root="2.16.840.1.113883.12.51"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0052"
                codeSystemName="Diagnosis Type" root="2.16.840.1.113883.18.21"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0053"
                codeSystemName="Diagnosis Coding Method" root="2.16.840.1.113883.12.53"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0055"
                codeSystemName="Diagnosis Related Group" root="2.16.840.1.113883.12.55"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0056"
                codeSystemName="DRG Grouper Review Code" root="2.16.840.1.113883.12.56"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0057" codeSystemName="DRUG CODE"
                root="2.16.840.1.113883.12.57"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0059"
                codeSystemName="Consent Code" root="2.16.840.1.113883.12.59"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0060" codeSystemName="ERROR CODE"
                root="2.16.840.1.113883.12.60"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0061"
                codeSystemName="Check Digit Scheme" root="2.16.840.1.113883.18.22"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0062"
                codeSystemName="Event Reason" root="2.16.840.1.113883.18.23"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0063"
                codeSystemName="Relationship" root="2.16.840.1.113883.18.24"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0064"
                codeSystemName="Financial Class" root="2.16.840.1.113883.12.64"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0065"
                codeSystemName="Specimen Action Code" root="2.16.840.1.113883.18.25"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0066"
                codeSystemName="Employment Status" root="2.16.840.1.113883.18.26"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0068"
                codeSystemName="Guarantor Type" root="2.16.840.1.113883.12.68"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0069"
                codeSystemName="Hospital Service" root="2.16.840.1.113883.18.27"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0070"
                codeSystemName="Specimen Source Codes" root="2.16.840.1.113883.18.28"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0072"
                codeSystemName="Insurance Plan ID" root="2.16.840.1.113883.12.72"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0073"
                codeSystemName="Interest Rate Code" root="2.16.840.1.113883.12.73"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0074"
                codeSystemName="Diagnostic Service Section ID" root="2.16.840.1.113883.18.29"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0075"
                codeSystemName="REPORT TYPES" root="2.16.840.1.113883.12.75"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0076"
                codeSystemName="Message Type" root="2.16.840.1.113883.18.30"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0078"
                codeSystemName="Interpretation Codes" root="2.16.840.1.113883.5.83"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0079" codeSystemName="Location"
                root="2.16.840.1.113883.12.79"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0080"
                codeSystemName="Nature of Abnormal Testing" root="2.16.840.1.113883.18.32"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0081"
                codeSystemName="NOTICE OF ADMISSION" root="2.16.840.1.113883.12.81"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0083"
                codeSystemName="Outlier Type" root="2.16.840.1.113883.18.33"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0084"
                codeSystemName="Performed by" root="2.16.840.1.113883.12.84"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0085"
                codeSystemName="Observation Result Status Codes Interpretation"
                root="2.16.840.1.113883.18.34"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0086" codeSystemName="Plan ID"
                root="2.16.840.1.113883.12.86"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0087"
                codeSystemName="Pre-Admit Test Indicator" root="2.16.840.1.113883.12.87"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0088"
                codeSystemName="Procedure Code" root="2.16.840.1.113883.12.88"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0089"
                codeSystemName="Procedure Coding Method" root="2.16.840.1.113883.12.89"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0090"
                codeSystemName="PROCEDURE TYPE" root="2.16.840.1.113883.12.90"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0091"
                codeSystemName="Query Priority" root="2.16.840.1.113883.18.35"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0092"
                codeSystemName="Re-Admission Indicator" root="2.16.840.1.113883.18.36"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0093"
                codeSystemName="Release Information" root="2.16.840.1.113883.12.93"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0094"
                codeSystemName="REPORT OF ELIGIBILITY" root="2.16.840.1.113883.12.94"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0096"
                codeSystemName="FINANCIAL TRANSACTION CODE" root="2.16.840.1.113883.12.96"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0098"
                codeSystemName="Type of Agreement" root="2.16.840.1.113883.18.37"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0099"
                codeSystemName="VIP Indicator" root="2.16.840.1.113883.12.99"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0100"
                codeSystemName="Invocation event" root="2.16.840.1.113883.18.38"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0101"
                codeSystemName="DISPLAY LEVEL" root="2.16.840.1.113883.12.101"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0102"
                codeSystemName="Delayed acknowledgment type" root="2.16.840.1.113883.18.39"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0103"
                codeSystemName="Processing ID" root="2.16.840.1.113883.18.40"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0104" codeSystemName="Version ID"
                root="2.16.840.1.113883.18.41"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0105"
                codeSystemName="Source of Comment" root="2.16.840.1.113883.18.42"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0106"
                codeSystemName="Query/response format code" root="2.16.840.1.113883.18.43"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0107"
                codeSystemName="Deferred response type" root="2.16.840.1.113883.18.44"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0108"
                codeSystemName="Query results level" root="2.16.840.1.113883.18.45"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0109"
                codeSystemName="Report priority" root="2.16.840.1.113883.18.46"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0110"
                codeSystemName="Transfer to Bad Debt Code" root="2.16.840.1.113883.12.110"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0111"
                codeSystemName="Delete Account Code" root="2.16.840.1.113883.12.111"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0112"
                codeSystemName="Discharge Disposition" root="2.16.840.1.113883.18.438"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0113"
                codeSystemName="Discharged to Location" root="2.16.840.1.113883.12.113"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0114" codeSystemName="Diet Type"
                root="2.16.840.1.113883.12.114"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0115"
                codeSystemName="Servicing Facilities" root="2.16.840.1.113883.12.115"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0116" codeSystemName="Bed Status"
                root="2.16.840.1.113883.18.47"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0117"
                codeSystemName="Account Status" root="2.16.840.1.113883.12.117"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0118"
                codeSystemName="Major Diagnostic Category" root="2.16.840.1.113883.12.118"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0119"
                codeSystemName="Order Control Codes" root="2.16.840.1.113883.18.48"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0121"
                codeSystemName="Response Flag" root="2.16.840.1.113883.18.49"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0122"
                codeSystemName="Charge Type" root="2.16.840.1.113883.18.50"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0123"
                codeSystemName="Result Status" root="2.16.840.1.113883.18.51"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0124"
                codeSystemName="Transportation Mode" root="2.16.840.1.113883.18.52"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0125" codeSystemName="Value Type"
                root="2.16.840.1.113883.18.280"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0126"
                codeSystemName="Quantity Limited Request" root="2.16.840.1.113883.18.53"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0127"
                codeSystemName="Allergen Type" root="2.16.840.1.113883.18.54"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0128"
                codeSystemName="Allergy Severity" root="2.16.840.1.113883.18.55"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0129"
                codeSystemName="Accommodation Code" root="2.16.840.1.113883.12.129"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0130"
                codeSystemName="Visit User Code" root="2.16.840.1.113883.18.56"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0131"
                codeSystemName="Contact Role" root="2.16.840.1.113883.18.58"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0132"
                codeSystemName="Transaction Code" root="2.16.840.1.113883.12.132"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0133"
                codeSystemName="Procedure Practitioner Identifier Code Type"
                root="2.16.840.1.113883.18.59"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0135"
                codeSystemName="Assignment of Benefits" root="2.16.840.1.113883.18.60"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0136"
                codeSystemName="Yes/no Indicator" root="2.16.840.1.113883.18.347"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0137"
                codeSystemName="Mail Claim Party" root="2.16.840.1.113883.18.61"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0139"
                codeSystemName="Employer Information Data" root="2.16.840.1.113883.12.139"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0140"
                codeSystemName="Military Service" root="2.16.840.1.113883.18.62"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0141"
                codeSystemName="Military Rank/Grade" root="2.16.840.1.113883.12.141"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0142"
                codeSystemName="Military Status" root="2.16.840.1.113883.18.64"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0143"
                codeSystemName="Non-covered Insurance Code" root="2.16.840.1.113883.12.143"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0144"
                codeSystemName="Eligibility Source" root="2.16.840.1.113883.18.65"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0145" codeSystemName="Room Type"
                root="2.16.840.1.113883.18.66"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0146"
                codeSystemName="Amount Type" root="2.16.840.1.113883.18.67"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0147"
                codeSystemName="Policy Type" root="2.16.840.1.113883.18.68"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0148"
                codeSystemName="Money or Percentage Indicator" root="2.16.840.1.113883.18.69"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0149" codeSystemName="Day Type"
                root="2.16.840.1.113883.18.70"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0150"
                codeSystemName="Certification Patient Type" root="2.16.840.1.113883.18.71"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0151"
                codeSystemName="Second Opinion Status" root="2.16.840.1.113883.12.151"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0152"
                codeSystemName="Second Opinion Documentation Received"
                root="2.16.840.1.113883.12.152"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0153" codeSystemName="Value Code"
                root="2.16.840.1.113883.6.301.6"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0155"
                codeSystemName="Accept/Application Acknowledgment Conditions"
                root="2.16.840.1.113883.18.73"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0156"
                codeSystemName="Which date/time qualifier" root="2.16.840.1.113883.18.74"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0157"
                codeSystemName="Which date/time status qualifier" root="2.16.840.1.113883.18.75"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0158"
                codeSystemName="Date/time selection qualifier" root="2.16.840.1.113883.18.76"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0159"
                codeSystemName="Diet Code Specification Type" root="2.16.840.1.113883.18.77"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0160" codeSystemName="Tray Type"
                root="2.16.840.1.113883.18.78"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0161"
                codeSystemName="Allow Substitution" root="2.16.840.1.113883.18.79"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0162"
                codeSystemName="Route of Administration" root="2.16.840.1.113883.18.80"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0163" codeSystemName="Body Site"
                root="2.16.840.1.113883.18.81"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0164"
                codeSystemName="Administration Device" root="2.16.840.1.113883.18.82"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0165"
                codeSystemName="Administration Method" root="2.16.840.1.113883.18.83"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0166"
                codeSystemName="RX Component Type" root="2.16.840.1.113883.18.84"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0167"
                codeSystemName="Substitution Status" root="2.16.840.1.113883.18.85"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0168"
                codeSystemName="Processing Priority" root="2.16.840.1.113883.18.86"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0169"
                codeSystemName="Reporting Priority" root="2.16.840.1.113883.18.87"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0170"
                codeSystemName="Derived Specimen" root="2.16.840.1.113883.18.88"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0171"
                codeSystemName="Citizenship" root="2.16.840.1.113883.12.171"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0172"
                codeSystemName="Veterans Military Status" root="2.16.840.1.113883.12.172"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0173"
                codeSystemName="Coordination of Benefits" root="2.16.840.1.113883.18.89"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0174"
                codeSystemName="Nature of Service/Test/Observation" root="2.16.840.1.113883.18.90"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0175"
                codeSystemName="Master File Identifier Code" root="2.16.840.1.113883.18.91"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0176"
                codeSystemName="Master File Application Identifier" root="2.16.840.1.113883.12.176"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0177"
                codeSystemName="Confidentiality Code" root="2.16.840.1.113883.18.92"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0178"
                codeSystemName="File Level Event Code" root="2.16.840.1.113883.18.93"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0179"
                codeSystemName="Response Level" root="2.16.840.1.113883.18.94"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0180"
                codeSystemName="Record-level Event Code" root="2.16.840.1.113883.18.95"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0181"
                codeSystemName="MFN Record-level Error Return" root="2.16.840.1.113883.18.96"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0182" codeSystemName="Staff type"
                root="2.16.840.1.113883.12.182"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0183"
                codeSystemName="Active/Inactive" root="2.16.840.1.113883.18.97"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0184" codeSystemName="Department"
                root="2.16.840.1.113883.12.184"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0185"
                codeSystemName="Preferred Method of Contact" root="2.16.840.1.113883.18.98"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0186"
                codeSystemName="Practitioner Category" root="2.16.840.1.113883.12.186"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0187"
                codeSystemName="Provider Billing" root="2.16.840.1.113883.18.99"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0188"
                codeSystemName="Operator ID" root="2.16.840.1.113883.12.188"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0189"
                codeSystemName="Ethnic Group" root="2.16.840.1.113883.18.100"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0190"
                codeSystemName="Address Type" root="2.16.840.1.113883.18.101"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0191"
                codeSystemName="Type of Referenced Data" root="2.16.840.1.113883.18.102"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0192"
                codeSystemName="Visit ID Type" root="2.16.840.1.113883.12.192"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0193"
                codeSystemName="Amount Class" root="2.16.840.1.113883.18.103"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0200" codeSystemName="Name Type"
                root="2.16.840.1.113883.18.105"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0201"
                codeSystemName="Telecommunication Use Code" root="2.16.840.1.113883.18.106"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0202"
                codeSystemName="Telecommunication Equipment Type" root="2.16.840.1.113883.18.107"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0203"
                codeSystemName="Identifier Type" root="2.16.840.1.113883.18.108"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0204"
                codeSystemName="Organizational Name Type" root="2.16.840.1.113883.18.109"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0205" codeSystemName="Price Type"
                root="2.16.840.1.113883.18.110"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0206"
                codeSystemName="Segment Action Code" root="2.16.840.1.113883.18.111"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0207"
                codeSystemName="Processing Mode" root="2.16.840.1.113883.18.112"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0208"
                codeSystemName="Query Response Status" root="2.16.840.1.113883.18.113"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0209"
                codeSystemName="Relational Operator" root="2.16.840.1.113883.18.114"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0210"
                codeSystemName="Relational Conjunction" root="2.16.840.1.113883.18.115"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0211"
                codeSystemName="Alternate Character Sets" root="2.16.840.1.113883.18.116"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0212"
                codeSystemName="Nationality" root="2.16.840.1.113883.12.212"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0213"
                codeSystemName="Purge Status Code" root="2.16.840.1.113883.18.117"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0214"
                codeSystemName="Special Program Code" root="2.16.840.1.113883.18.118"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0215"
                codeSystemName="Publicity Code" root="2.16.840.1.113883.18.119"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0216"
                codeSystemName="Patient Status Code" root="2.16.840.1.113883.18.120"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0217"
                codeSystemName="Visit Priority Code" root="2.16.840.1.113883.18.121"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0218"
                codeSystemName="Patient Charge Adjustment" root="2.16.840.1.113883.12.218"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0219"
                codeSystemName="Recurring Service Code" root="2.16.840.1.113883.12.219"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0220"
                codeSystemName="Living Arrangement" root="2.16.840.1.113883.18.122"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0222"
                codeSystemName="Contact Reason" root="2.16.840.1.113883.12.222"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0223"
                codeSystemName="Living Dependency" root="2.16.840.1.113883.18.124"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0224"
                codeSystemName="Transport Arranged" root="2.16.840.1.113883.18.125"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0225"
                codeSystemName="Escort Required" root="2.16.840.1.113883.18.126"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0227"
                codeSystemName="Manufacturers of Vaccines (code=MVX)"
                root="2.16.840.1.113883.12.227"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0228"
                codeSystemName="Diagnosis Classification" root="2.16.840.1.113883.18.128"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0229" codeSystemName="DRG Payor"
                root="2.16.840.1.113883.12.229"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0230"
                codeSystemName="Procedure Functional Type" root="2.16.840.1.113883.18.130"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0231"
                codeSystemName="Student Status" root="2.16.840.1.113883.18.131"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0232"
                codeSystemName="Insurance Company Contact Reason" root="2.16.840.1.113883.18.132"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0233"
                codeSystemName="Non-Concur Code/Description" root="2.16.840.1.113883.12.233"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0234"
                codeSystemName="Report Timing" root="2.16.840.1.113883.18.133"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0235"
                codeSystemName="Report Source" root="2.16.840.1.113883.18.134"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0236"
                codeSystemName="Event Reported To" root="2.16.840.1.113883.18.135"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0237"
                codeSystemName="Event Qualification" root="2.16.840.1.113883.18.136"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0238"
                codeSystemName="Event Seriousness" root="2.16.840.1.113883.18.137"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0239"
                codeSystemName="Event Expected" root="2.16.840.1.113883.18.138"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0240"
                codeSystemName="Event Consequence" root="2.16.840.1.113883.18.139"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0241"
                codeSystemName="Patient Outcome" root="2.16.840.1.113883.18.140"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0242"
                codeSystemName="Primary Observers Qualification" root="2.16.840.1.113883.18.141"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0243"
                codeSystemName="Identity May Be Divulged" root="2.16.840.1.113883.18.142"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0244"
                codeSystemName="Single Use Device" root="2.16.840.1.113883.12.244"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0245"
                codeSystemName="Product Problem" root="2.16.840.1.113883.12.245"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0246"
                codeSystemName="Product Available for Inspection" root="2.16.840.1.113883.12.246"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0247"
                codeSystemName="Status of Evaluation" root="2.16.840.1.113883.18.143"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0248"
                codeSystemName="Product Source" root="2.16.840.1.113883.18.144"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0249"
                codeSystemName="Generic Product" root="2.16.840.1.113883.12.249"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0250"
                codeSystemName="Relatedness Assessment" root="2.16.840.1.113883.18.145"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0251"
                codeSystemName="Action Taken in Response to the Event"
                root="2.16.840.1.113883.18.146"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0252"
                codeSystemName="Causality Observations" root="2.16.840.1.113883.18.147"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0253"
                codeSystemName="Indirect Exposure Mechanism" root="2.16.840.1.113883.18.148"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0254"
                codeSystemName="Kind of Quantity" root="2.16.840.1.113883.18.149"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0255"
                codeSystemName="Duration Categories" root="2.16.840.1.113883.18.150"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0256"
                codeSystemName="Time Delay Post Challenge" root="2.16.840.1.113883.18.151"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0257"
                codeSystemName="Nature of Challenge" root="2.16.840.1.113883.18.152"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0258"
                codeSystemName="Relationship Modifier" root="2.16.840.1.113883.18.153"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0259" codeSystemName="Modality"
                root="2.16.840.1.113883.12.259"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0260"
                codeSystemName="Patient Location Type" root="2.16.840.1.113883.18.155"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0261"
                codeSystemName="Location Equipment" root="2.16.840.1.113883.18.156"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0262"
                codeSystemName="Privacy Level" root="2.16.840.1.113883.18.157"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0263"
                codeSystemName="Level of Care" root="2.16.840.1.113883.18.158"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0264"
                codeSystemName="Location Department" root="2.16.840.1.113883.12.264"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0265"
                codeSystemName="Specialty Type" root="2.16.840.1.113883.18.159"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0267"
                codeSystemName="Days of the Week" root="2.16.840.1.113883.18.160"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0268" codeSystemName="Override"
                root="2.16.840.1.113883.18.161"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0269"
                codeSystemName="Charge On Indicator" root="2.16.840.1.113883.18.162"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0270"
                codeSystemName="Document Type" root="2.16.840.1.113883.18.163"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0271"
                codeSystemName="Document Completion Status" root="2.16.840.1.113883.18.164"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0272"
                codeSystemName="Document Confidentiality Status" root="2.16.840.1.113883.18.166"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0273"
                codeSystemName="Document Availability Status" root="2.16.840.1.113883.18.167"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0275"
                codeSystemName="Document Storage Status" root="2.16.840.1.113883.18.168"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0276"
                codeSystemName="Appointment reason codes" root="2.16.840.1.113883.18.169"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0277"
                codeSystemName="Appointment Type Codes" root="2.16.840.1.113883.18.170"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0278"
                codeSystemName="Filler status codes" root="2.16.840.1.113883.18.171"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0279"
                codeSystemName="Allow Substitution Codes" root="2.16.840.1.113883.18.172"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0280"
                codeSystemName="Referral Priority" root="2.16.840.1.113883.18.173"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0281"
                codeSystemName="Referral Type" root="2.16.840.1.113883.18.174"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0282"
                codeSystemName="Referral Disposition" root="2.16.840.1.113883.18.175"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0283"
                codeSystemName="Referral Status" root="2.16.840.1.113883.18.176"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0284"
                codeSystemName="Referral Category" root="2.16.840.1.113883.18.177"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0285"
                codeSystemName="Insurance Company ID Codes" root="2.16.840.1.113883.12.285"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0286"
                codeSystemName="Provider Role" root="2.16.840.1.113883.18.178"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0287"
                codeSystemName="Problem/Goal Action Code" root="2.16.840.1.113883.18.3"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0288"
                codeSystemName="Census Tract" root="2.16.840.1.113883.12.288"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0289"
                codeSystemName="County/Parish" root="2.16.840.1.113883.12.289"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0290"
                codeSystemName="MIME base64 encoding characters" root="2.16.840.1.113883.18.180"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0291"
                codeSystemName="Subtype of Referenced Data" root="2.16.840.1.113883.18.181"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0292"
                codeSystemName="Vaccines Administered" root="2.16.840.1.113883.12.292"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0293"
                codeSystemName="Billing Category" root="2.16.840.1.113883.12.293"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0294"
                codeSystemName="Time Selection Criteria Parameter Class Codes"
                root="2.16.840.1.113883.18.183"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0295" codeSystemName="Handicap"
                root="2.16.840.1.113883.12.295"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0296"
                codeSystemName="Primary Language" root="2.16.840.1.113883.12.296"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0297"
                codeSystemName="CN ID Source" root="2.16.840.1.113883.12.297"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0298"
                codeSystemName="CP Range Type" root="2.16.840.1.113883.18.184"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0299" codeSystemName="Encoding"
                root="2.16.840.1.113883.18.185"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0300"
                codeSystemName="Namespace ID" root="2.16.840.1.113883.12.300"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0301"
                codeSystemName="Universal ID Type" root="2.16.840.1.113883.18.186"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0302"
                codeSystemName="Point of Care" root="2.16.840.1.113883.12.302"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0303" codeSystemName="Room"
                root="2.16.840.1.113883.12.303"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0304" codeSystemName="Bed"
                root="2.16.840.1.113883.12.304"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0305"
                codeSystemName="Person Location Type" root="2.16.840.1.113883.18.187"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0306"
                codeSystemName="Location Status" root="2.16.840.1.113883.12.306"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0307" codeSystemName="Building"
                root="2.16.840.1.113883.12.307"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0308" codeSystemName="Floor"
                root="2.16.840.1.113883.12.308"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0309"
                codeSystemName="Coverage Type" root="2.16.840.1.113883.18.188"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0311" codeSystemName="Job Status"
                root="2.16.840.1.113883.18.189"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0312"
                codeSystemName="Policy Scope" root="2.16.840.1.113883.12.312"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0313"
                codeSystemName="Policy Source" root="2.16.840.1.113883.12.313"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0315"
                codeSystemName="Living Will Code" root="2.16.840.1.113883.18.190"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0316"
                codeSystemName="Organ Donor Code" root="2.16.840.1.113883.18.192"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0317"
                codeSystemName="Annotations" root="2.16.840.1.113883.18.193"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0319"
                codeSystemName="Department Cost Center" root="2.16.840.1.113883.12.319"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0320"
                codeSystemName="Item Natural Account Code" root="2.16.840.1.113883.12.320"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0321"
                codeSystemName="Dispense Method" root="2.16.840.1.113883.18.194"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0322"
                codeSystemName="Completion Status" root="2.16.840.1.113883.18.195"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0323"
                codeSystemName="Action Code" root="2.16.840.1.113883.18.196"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0324"
                codeSystemName="Location Characteristic ID" root="2.16.840.1.113883.18.197"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0325"
                codeSystemName="Location Relationship ID" root="2.16.840.1.113883.18.198"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0326"
                codeSystemName="Visit Indicator" root="2.16.840.1.113883.18.199"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0327" codeSystemName="Job Code"
                root="2.16.840.1.113883.12.327"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0328"
                codeSystemName="Employee Classification" root="2.16.840.1.113883.12.328"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0329"
                codeSystemName="Quantity Method" root="2.16.840.1.113883.18.200"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0330"
                codeSystemName="Marketing Basis" root="2.16.840.1.113883.18.201"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0331"
                codeSystemName="Facility Type" root="2.16.840.1.113883.18.202"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0332"
                codeSystemName="Source Type" root="2.16.840.1.113883.18.203"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0333"
                codeSystemName="Drivers License Issuing Authority" root="2.16.840.1.113883.12.333"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0334"
                codeSystemName="Disabled Person Code" root="2.16.840.1.113883.18.204"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0335"
                codeSystemName="Repeat Pattern" root="2.16.840.1.113883.18.205"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0336"
                codeSystemName="Referral Reason" root="2.16.840.1.113883.18.206"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0337"
                codeSystemName="Certification Status" root="2.16.840.1.113883.18.207"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0338"
                codeSystemName="Practitioner ID Number Type" root="2.16.840.1.113883.18.108"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0339"
                codeSystemName="Advanced Beneficiary Notice Code" root="2.16.840.1.113883.18.209"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0340"
                codeSystemName="Procedure Code Modifier" root="2.16.840.1.113883.12.340"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0341"
                codeSystemName="Guarantor Credit Rating Code" root="2.16.840.1.113883.12.341"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0342"
                codeSystemName="Military Recipient" root="2.16.840.1.113883.12.342"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0343"
                codeSystemName="Military Handicapped Program Code" root="2.16.840.1.113883.12.343"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0344"
                codeSystemName="Patients Relationship to Insured" root="2.16.840.1.113883.18.210"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0345"
                codeSystemName="Appeal Reason" root="2.16.840.1.113883.12.345"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0346"
                codeSystemName="Certification Agency" root="2.16.840.1.113883.12.346"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0347"
                codeSystemName="State/Province" root="1.0.3166.2"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0348"
                codeSystemName="Special Program Indicator" root="2.16.840.1.113883.12.348"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0349"
                codeSystemName="PSRO/UR Approval Indicator" root="2.16.840.1.113883.12.349"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0350"
                codeSystemName="Occurrence Code" root="2.16.840.1.113883.6.301.7"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0351"
                codeSystemName="Occurrence Span" root="2.16.840.1.113883.6.301.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0353"
                codeSystemName="CWE statuses" root="2.16.840.1.113883.18.213"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0354"
                codeSystemName="Message Structure" root="2.16.840.1.113883.18.214"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0355"
                codeSystemName="Primary Key Value Type" root="2.16.840.1.113883.18.215"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0356"
                codeSystemName="Alternate Character Set Handling Scheme"
                root="2.16.840.1.113883.18.216"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0357"
                codeSystemName="Message Error Condition Codes" root="2.16.840.1.113883.18.217"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0358"
                codeSystemName="Practitioner Group" root="2.16.840.1.113883.12.358"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0359"
                codeSystemName="Diagnosis Priority" root="2.16.840.1.113883.18.218"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0360"
                codeSystemName="Degree/License/Certificate" root="2.16.840.1.113883.18.220"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0361"
                codeSystemName="Application" root="2.16.840.1.113883.12.361"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0362" codeSystemName="Facility"
                root="2.16.840.1.113883.12.362"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0363"
                codeSystemName="Assigning Authority" root="2.16.840.1.113883.12.363"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0364"
                codeSystemName="Comment Type" root="2.16.840.1.113883.18.222"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0365"
                codeSystemName="Equipment State" root="2.16.840.1.113883.18.223"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0366"
                codeSystemName="Local/Remote Control State" root="2.16.840.1.113883.18.224"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0367"
                codeSystemName="Alert Level" root="2.16.840.1.113883.18.225"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0368"
                codeSystemName="Remote Control Command" root="2.16.840.1.113883.18.226"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0369"
                codeSystemName="Specimen Role" root="2.16.840.1.113883.18.227"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0370"
                codeSystemName="Container Status" root="2.16.840.1.113883.18.228"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0371"
                codeSystemName="Additive/Preservative" root="2.16.840.1.113883.18.229"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0372"
                codeSystemName="Specimen Component" root="2.16.840.1.113883.18.230"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0373" codeSystemName="Treatment"
                root="2.16.840.1.113883.18.231"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0374"
                codeSystemName="System Induced Contaminants" root="2.16.840.1.113883.18.232"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0375"
                codeSystemName="Artificial Blood" root="2.16.840.1.113883.18.233"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0376"
                codeSystemName="Special Handling Code" root="2.16.840.1.113883.18.234"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0377"
                codeSystemName="Other Environmental Factors" root="2.16.840.1.113883.18.235"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0378"
                codeSystemName="Carrier Type" root="2.16.840.1.113883.12.378"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0379" codeSystemName="Tray Type"
                root="2.16.840.1.113883.12.379"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0380"
                codeSystemName="Separator Type" root="2.16.840.1.113883.12.380"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0381" codeSystemName="Cap Type"
                root="2.16.840.1.113883.12.381"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0382"
                codeSystemName="Drug Interference" root="2.16.840.1.113883.12.382"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0383"
                codeSystemName="Substance Status" root="2.16.840.1.113883.18.236"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0384"
                codeSystemName="Substance Type" root="2.16.840.1.113883.18.237"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0385"
                codeSystemName="Manufacturer Identifier" root="2.16.840.1.113883.12.385"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0386"
                codeSystemName="Supplier Identifier" root="2.16.840.1.113883.12.386"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0387"
                codeSystemName="Command Response" root="2.16.840.1.113883.18.238"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0388"
                codeSystemName="Processing Type" root="2.16.840.1.113883.18.239"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0389"
                codeSystemName="Analyte Repeat Status" root="2.16.840.1.113883.18.240"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0391"
                codeSystemName="Segment Group" root="2.16.840.1.113883.18.242"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0392"
                codeSystemName="Match Reason" root="2.16.840.1.113883.18.243"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0393"
                codeSystemName="Match Algorithms" root="2.16.840.1.113883.18.244"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0394"
                codeSystemName="Response Modality" root="2.16.840.1.113883.18.245"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0395"
                codeSystemName="Modify Indicator" root="2.16.840.1.113883.18.246"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0396"
                codeSystemName="Coding System" root="2.16.840.1.113883.18.247"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0397" codeSystemName="Sequencing"
                root="2.16.840.1.113883.18.248"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0398"
                codeSystemName="Continuation Style Code" root="2.16.840.1.113883.18.249"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0399"
                codeSystemName="Country Code" root="1.0.3166.1"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0401"
                codeSystemName="Government Reimbursement Program" root="2.16.840.1.113883.18.250"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0402"
                codeSystemName="School Type" root="2.16.840.1.113883.18.251"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0403"
                codeSystemName="Language Ability" root="2.16.840.1.113883.18.252"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0404"
                codeSystemName="Language Proficiency" root="2.16.840.1.113883.18.253"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0405"
                codeSystemName="Organization Unit" root="2.16.840.1.113883.12.405"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0406"
                codeSystemName="Participant Organization Unit Type" root="2.16.840.1.113883.18.254"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0409"
                codeSystemName="Application Change Type" root="2.16.840.1.113883.18.255"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0411"
                codeSystemName="Supplemental Service Information Values"
                root="2.16.840.1.113883.12.411"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0412"
                codeSystemName="Category Identifier" root="2.16.840.1.113883.12.412"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0413"
                codeSystemName="Consent Identifier" root="2.16.840.1.113883.12.413"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0414"
                codeSystemName="Units of Time" root="2.16.840.1.113883.12.414"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0415"
                codeSystemName="Transfer Type" root="2.16.840.1.113883.18.257"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0416"
                codeSystemName="Procedure DRG Type" root="2.16.840.1.113883.18.258"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0417"
                codeSystemName="Tissue Type Code" root="2.16.840.1.113883.18.259"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0418"
                codeSystemName="Procedure Priority" root="2.16.840.1.113883.18.260"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0421"
                codeSystemName="Severity of Illness Code" root="2.16.840.1.113883.18.262"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0422"
                codeSystemName="Triage Code" root="2.16.840.1.113883.18.263"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0423"
                codeSystemName="Case Category Code" root="2.16.840.1.113883.18.264"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0424"
                codeSystemName="Gestation Category Code" root="2.16.840.1.113883.18.265"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0425"
                codeSystemName="Newborn Code" root="2.16.840.1.113883.18.266"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0426"
                codeSystemName="Blood Product Code" root="2.16.840.1.113883.18.267"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0427"
                codeSystemName="Risk Management Incident Code" root="2.16.840.1.113883.18.268"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0428"
                codeSystemName="Incident Type Code" root="2.16.840.1.113883.18.269"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0429"
                codeSystemName="Production Class Code" root="2.16.840.1.113883.18.270"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0430"
                codeSystemName="Mode of Arrival Code" root="2.16.840.1.113883.18.271"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0431"
                codeSystemName="Recreational Drug Use Code" root="2.16.840.1.113883.18.272"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0432"
                codeSystemName="Admission Level of Care Code" root="2.16.840.1.113883.18.273"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0433"
                codeSystemName="Precaution Code" root="2.16.840.1.113883.18.274"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0434"
                codeSystemName="Patient Condition Code" root="2.16.840.1.113883.18.275"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0435"
                codeSystemName="Advance Directive Code" root="2.16.840.1.113883.18.276"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0436"
                codeSystemName="Sensitivity to Causative Agent Code" root="2.16.840.1.113883.18.277"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0437"
                codeSystemName="Alert Device Code" root="2.16.840.1.113883.18.278"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0438"
                codeSystemName="Allergy Clinical Status" root="2.16.840.1.113883.18.279"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0440" codeSystemName="Data Types"
                root="2.16.840.1.113883.18.280"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0441"
                codeSystemName="Immunization Registry Status" root="2.16.840.1.113883.18.281"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0442"
                codeSystemName="Location Service Code" root="2.16.840.1.113883.18.282"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0443"
                codeSystemName="Provider Role" root="2.16.840.1.113883.18.283"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0444"
                codeSystemName="Name Assembly Order" root="2.16.840.1.113883.18.284"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0445"
                codeSystemName="Identity Reliability Code" root="2.16.840.1.113883.18.285"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0446"
                codeSystemName="Species Code" root="2.16.840.1.113883.12.446"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0447" codeSystemName="Breed Code"
                root="2.16.840.1.113883.12.447"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0448"
                codeSystemName="Name Context" root="2.16.840.1.113883.12.448"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0449"
                codeSystemName="Conformance statements" root="2.16.840.1.113883.12.449"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0450" codeSystemName="Event Type"
                root="2.16.840.1.113883.18.286"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0451"
                codeSystemName="Substance Identifier" root="2.16.840.1.113883.12.451"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0452"
                codeSystemName="Health Care Provider Type Code" root="2.16.840.1.113883.12.452"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0453"
                codeSystemName="Health Care Provider Classification" root="2.16.840.1.113883.12.453"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0454"
                codeSystemName="Health Care Provider Area of Specialization"
                root="2.16.840.1.113883.12.454"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0455"
                codeSystemName="Type of Bill Code" root="2.16.840.1.113883.12.455"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0456"
                codeSystemName="Revenue code" root="2.16.840.1.113883.6.301.3"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0457"
                codeSystemName="Overall Claim Disposition Code" root="2.16.840.1.113883.18.292"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0458"
                codeSystemName="OCE Edit Code" root="2.16.840.1.113883.12.458"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0459"
                codeSystemName="Reimbursement Action Code" root="2.16.840.1.113883.12.459"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0460"
                codeSystemName="Denial or Rejection Code" root="2.16.840.1.113883.12.460"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0461"
                codeSystemName="License Number" root="2.16.840.1.113883.12.461"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0462"
                codeSystemName="Location Cost Center" root="2.16.840.1.113883.12.462"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0463"
                codeSystemName="Inventory Number" root="2.16.840.1.113883.12.463"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0464"
                codeSystemName="Facility ID" root="2.16.840.1.113883.12.464"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0465"
                codeSystemName="Name/Address Representation" root="2.16.840.1.113883.18.295"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0466"
                codeSystemName="Ambulatory Payment Classification Code"
                root="2.16.840.1.113883.18.296"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0467"
                codeSystemName="Modifier Edit Code" root="2.16.840.1.113883.12.467"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0468"
                codeSystemName="Payment Adjustment Code" root="2.16.840.1.113883.18.297"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0469"
                codeSystemName="Packaging Status Code" root="2.16.840.1.113883.18.298"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0470"
                codeSystemName="Reimbursement Type Code" root="2.16.840.1.113883.18.299"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0471" codeSystemName="Query Name"
                root="2.16.840.1.113883.12.471"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0472"
                codeSystemName="TQ Conjunction ID" root="2.16.840.1.113883.18.300"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0473"
                codeSystemName="Formulary Status" root="2.16.840.1.113883.18.301"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0474"
                codeSystemName="Practitioner Organization Unit Type" root="2.16.840.1.113883.18.302"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0475"
                codeSystemName="Charge Type Reason" root="2.16.840.1.113883.18.303"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0476"
                codeSystemName="Medically Necessary Duplicate Procedure Reason"
                root="2.16.840.1.113883.12.476"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0477"
                codeSystemName="Controlled Substance Schedule" root="2.16.840.1.113883.18.304"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0478"
                codeSystemName="Formulary Status" root="2.16.840.1.113883.18.305"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0479"
                codeSystemName="Pharmaceutical Substances" root="2.16.840.1.113883.12.479"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0480"
                codeSystemName="Pharmacy Order Types" root="2.16.840.1.113883.18.306"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0482" codeSystemName="Order Type"
                root="2.16.840.1.113883.18.307"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0483"
                codeSystemName="Authorization Mode" root="2.16.840.1.113883.18.308"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0484"
                codeSystemName="Dispense Type" root="2.16.840.1.113883.18.309"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0485"
                codeSystemName="Extended Priority Codes" root="2.16.840.1.113883.18.310"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0487"
                codeSystemName="Specimen Type" root="2.16.840.1.113883.18.311"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0488"
                codeSystemName="Specimen Collection Method" root="2.16.840.1.113883.18.312"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0489" codeSystemName="Risk Codes"
                root="2.16.840.1.113883.18.313"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0490"
                codeSystemName="Specimen Reject Reason" root="2.16.840.1.113883.18.314"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0491"
                codeSystemName="Specimen Quality" root="2.16.840.1.113883.18.315"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0492"
                codeSystemName="Specimen Appropriateness" root="2.16.840.1.113883.18.316"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0493"
                codeSystemName="Specimen Condition" root="2.16.840.1.113883.18.317"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0494"
                codeSystemName="Specimen Child Role" root="2.16.840.1.113883.18.318"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0495"
                codeSystemName="Body Site Modifier" root="2.16.840.1.113883.18.319"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0496"
                codeSystemName="Consent Type" root="2.16.840.1.113883.18.320"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0497"
                codeSystemName="Consent Mode" root="2.16.840.1.113883.18.321"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0498"
                codeSystemName="Consent Status" root="2.16.840.1.113883.18.322"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0499"
                codeSystemName="Consent Bypass Reason" root="2.16.840.1.113883.18.323"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0500"
                codeSystemName="Consent Disclosure Level" root="2.16.840.1.113883.18.324"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0501"
                codeSystemName="Consent Non-Disclosure Reason" root="2.16.840.1.113883.18.325"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0502"
                codeSystemName="Non-Subject Consenter Reason" root="2.16.840.1.113883.18.326"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0503"
                codeSystemName="Sequence/Results Flag" root="2.16.840.1.113883.18.327"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0504"
                codeSystemName="Sequence Condition Code" root="2.16.840.1.113883.18.328"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0505"
                codeSystemName="Cyclic Entry/Exit Indicator" root="2.16.840.1.113883.18.329"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0506"
                codeSystemName="Service Request Relationship" root="2.16.840.1.113883.18.330"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0507"
                codeSystemName="Observation Result Handling" root="2.16.840.1.113883.18.331"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0508"
                codeSystemName="Blood Product Processing Requirements"
                root="2.16.840.1.113883.18.332"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0509"
                codeSystemName="Indication for Use" root="2.16.840.1.113883.12.509"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0510"
                codeSystemName="Blood Product Dispense Status" root="2.16.840.1.113883.18.333"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0511"
                codeSystemName="BP Observation Status Codes Interpretation"
                root="2.16.840.1.113883.18.334"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0512"
                codeSystemName="Commercial Product" root="2.16.840.1.113883.12.512"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0513"
                codeSystemName="Blood Product Transfusion/Disposition Status"
                root="2.16.840.1.113883.18.335"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0514"
                codeSystemName="Transfusion Adverse Reaction" root="2.16.840.1.113883.18.336"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0515"
                codeSystemName="Transfusion Interrupted Reason" root="2.16.840.1.113883.12.515"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0516"
                codeSystemName="Error Severity" root="2.16.840.1.113883.18.337"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0517"
                codeSystemName="Inform Person Code" root="2.16.840.1.113883.18.338"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0518"
                codeSystemName="Override Type" root="2.16.840.1.113883.18.339"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0519"
                codeSystemName="Override Reason" root="2.16.840.1.113883.12.519"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0520"
                codeSystemName="Message Waiting Priority" root="2.16.840.1.113883.18.340"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0521"
                codeSystemName="Override Code" root="2.16.840.1.113883.12.521"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0523"
                codeSystemName="Computation Type" root="2.16.840.1.113883.18.341"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0524"
                codeSystemName="Sequence condition" root="2.16.840.1.113883.18.342"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0525" codeSystemName="Privilege"
                root="2.16.840.1.113883.12.525"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0526"
                codeSystemName="Privilege Class" root="2.16.840.1.113883.12.526"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0527"
                codeSystemName="Calendar Alignment" root="2.16.840.1.113883.18.343"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0528"
                codeSystemName="Event Related Period" root="2.16.840.1.113883.18.344"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0529" codeSystemName="Precision"
                root="2.16.840.1.113883.18.345"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0530"
                codeSystemName="Organization, Agency, Department" root="2.16.840.1.113883.18.346"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0531"
                codeSystemName="Institution" root="2.16.840.1.113883.12.531"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0532"
                codeSystemName="Expanded Yes/no Indicator" root="2.16.840.1.113883.18.347"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0533"
                codeSystemName="Application Error Code" root="2.16.840.1.113883.12.533"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0534"
                codeSystemName="Notify Clergy Code" root="2.16.840.1.113883.18.348"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0535"
                codeSystemName="Signature Code" root="2.16.840.1.113883.18.349"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0536"
                codeSystemName="Certificate Status" root="2.16.840.1.113883.18.350"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0537"
                codeSystemName="Institution" root="2.16.840.1.113883.12.537"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0538"
                codeSystemName="Institution Relationship Type" root="2.16.840.1.113883.18.351"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0539"
                codeSystemName="Cost Center Code" root="2.16.840.1.113883.12.539"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0540"
                codeSystemName="Inactive Reason Code" root="2.16.840.1.113883.18.352"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0541"
                codeSystemName="Specimen Type Modifier" root="2.16.840.1.113883.12.541"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0542"
                codeSystemName="Specimen Source Type Modifier" root="2.16.840.1.113883.12.542"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0543"
                codeSystemName="Specimen Collection Site" root="2.16.840.1.113883.12.543"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0544"
                codeSystemName="Container Condition" root="2.16.840.1.113883.18.353"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0545"
                codeSystemName="Language Translated To" root="2.16.840.1.113883.12.545"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0547"
                codeSystemName="Jurisdictional Breadth" root="2.16.840.1.113883.18.354"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0548"
                codeSystemName="Signatorys Relationship to Subject" root="2.16.840.1.113883.18.355"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0549" codeSystemName="NDC Codes"
                root="2.16.840.1.113883.12.549"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0550" codeSystemName="Body Parts"
                root="2.16.840.1.113883.18.356"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0552"
                codeSystemName="Advanced Beneficiary Notice Override Reason"
                root="2.16.840.1.113883.12.552"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0553"
                codeSystemName="Invoice Control Code" root="2.16.840.1.113883.18.357"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0554"
                codeSystemName="Invoice Reason Codes" root="2.16.840.1.113883.18.358"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0555"
                codeSystemName="Invoice Type" root="2.16.840.1.113883.18.359"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0556"
                codeSystemName="Benefit Group" root="2.16.840.1.113883.18.360"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0557" codeSystemName="Payee Type"
                root="2.16.840.1.113883.18.361"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0558"
                codeSystemName="Payee Relationship to Invoice" root="2.16.840.1.113883.18.362"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0559"
                codeSystemName="Product/Service Status" root="2.16.840.1.113883.18.363"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0560"
                codeSystemName="Quantity Units" root="2.16.840.1.113883.18.455"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0561"
                codeSystemName="Product/Services Clarification Codes"
                root="2.16.840.1.113883.18.364"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0562"
                codeSystemName="Processing Consideration Codes" root="2.16.840.1.113883.18.365"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0564"
                codeSystemName="Adjustment Category Code" root="2.16.840.1.113883.18.366"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0565"
                codeSystemName="Provider Adjustment Reason Code" root="2.16.840.1.113883.18.367"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0566"
                codeSystemName="Blood Unit Type" root="2.16.840.1.113883.18.368"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0567"
                codeSystemName="Weight Units" root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0568"
                codeSystemName="Volume Units" root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0569"
                codeSystemName="Adjustment Action" root="2.16.840.1.113883.18.369"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0570"
                codeSystemName="Payment Method Code" root="2.16.840.1.113883.18.370"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0571"
                codeSystemName="Invoice Processing Results Status" root="2.16.840.1.113883.18.371"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0572" codeSystemName="Tax status"
                root="2.16.840.1.113883.18.372"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0615"
                codeSystemName="User Authentication Credential Type Code"
                root="2.16.840.1.113883.18.373"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0616"
                codeSystemName="Address Expiration Reason" root="2.16.840.1.113883.18.374"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0617"
                codeSystemName="Address Usage" root="2.16.840.1.113883.18.375"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0618"
                codeSystemName="Protection Code" root="2.16.840.1.113883.18.376"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0625"
                codeSystemName="Item Status Codes" root="2.16.840.1.113883.18.377"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0634"
                codeSystemName="Item Importance Codes" root="2.16.840.1.113883.18.378"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0642"
                codeSystemName="Reorder Theory Codes" root="2.16.840.1.113883.18.379"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0651"
                codeSystemName="Labor Calculation Type" root="2.16.840.1.113883.18.380"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0653"
                codeSystemName="Date Format" root="2.16.840.1.113883.18.381"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0657"
                codeSystemName="Device Type" root="2.16.840.1.113883.18.382"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0659"
                codeSystemName="Lot Control" root="2.16.840.1.113883.18.383"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0667"
                codeSystemName="Device Data State" root="2.16.840.1.113883.18.384"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0669"
                codeSystemName="Load Status" root="2.16.840.1.113883.18.385"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0682"
                codeSystemName="Device Status" root="2.16.840.1.113883.18.386"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0702" codeSystemName="Cycle Type"
                root="2.16.840.1.113883.18.387"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0717"
                codeSystemName="Access Restriction Value" root="2.16.840.1.113883.18.388"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0719"
                codeSystemName="Access Restriction Reason Code" root="2.16.840.1.113883.5.4"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0725" codeSystemName="Mood Codes"
                root="2.16.840.1.113883.5.1001"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0728" codeSystemName="CCL Value"
                root="2.16.840.1.113883.18.391"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0731"
                codeSystemName="DRG Diagnosis Determination Status" root="2.16.840.1.113883.18.392"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0734"
                codeSystemName="Grouper Status" root="2.16.840.1.113883.18.393"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0739"
                codeSystemName="DRG Status Patient" root="2.16.840.1.113883.18.394"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0742"
                codeSystemName="DRG Status Financial Calculation" root="2.16.840.1.113883.18.395"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0749"
                codeSystemName="DRG Grouping Status" root="2.16.840.1.113883.18.396"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0755"
                codeSystemName="Status Weight At Birth" root="2.16.840.1.113883.18.397"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0757"
                codeSystemName="DRG Status Respiration Minutes" root="2.16.840.1.113883.18.398"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0759"
                codeSystemName="Status Admission" root="2.16.840.1.113883.18.399"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0761"
                codeSystemName="DRG Procedure Determination Status" root="2.16.840.1.113883.18.400"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0763"
                codeSystemName="DRG Procedure Relevance" root="2.16.840.1.113883.18.401"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0771"
                codeSystemName="Resource Type or Category" root="2.16.840.1.113883.12.771"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0776"
                codeSystemName="Item Status" root="2.16.840.1.113883.18.402"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0778" codeSystemName="Item Type"
                root="2.16.840.1.113883.18.403"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0790"
                codeSystemName="Approving Regulatory Agency" root="2.16.840.1.113883.18.404"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0793" codeSystemName="Ruling Act"
                root="2.16.840.1.113883.18.405"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0806"
                codeSystemName="Sterilization Type" root="2.16.840.1.113883.18.406"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0809"
                codeSystemName="Maintenance Cycle" root="2.16.840.1.113883.12.809"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0811"
                codeSystemName="Maintenance Type" root="2.16.840.1.113883.12.811"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0818" codeSystemName="Package"
                root="2.16.840.1.113883.18.407"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0834" codeSystemName="MIME Types"
                root="2.16.840.1.113883.18.408"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0836"
                codeSystemName="Problem Severity" root="2.16.840.1.113883.12.836"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0838"
                codeSystemName="Problem Perspective" root="2.16.840.1.113883.12.838"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0865"
                codeSystemName="Referral Documentation Completion Status"
                root="2.16.840.1.113883.12.865"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0868"
                codeSystemName="Telecommunication Expiration Reason" root="2.16.840.1.113883.18.409"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0871"
                codeSystemName="Supply Risk Codes" root="2.16.840.1.113883.18.410"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0879"
                codeSystemName="Product/Service Code" root="2.16.840.1.113883.12.879"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0880"
                codeSystemName="Product/Service Code Modifier" root="2.16.840.1.113883.12.880"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0881"
                codeSystemName="Role Executing Physician" root="2.16.840.1.113883.18.411"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0882"
                codeSystemName="Medical Role Executing Physician" root="2.16.840.1.113883.18.412"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0894"
                codeSystemName="Side of body" root="2.16.840.1.113883.18.413"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0895"
                codeSystemName="Present On Admission (POA) Indicator"
                root="2.16.840.1.113883.6.301.11"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0904"
                codeSystemName="Security Check Scheme" root="2.16.840.1.113883.18.415"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0905"
                codeSystemName="Shipment Status" root="2.16.840.1.113883.18.416"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0906"
                codeSystemName="ActPriority" root="2.16.840.1.113883.18.417"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0907"
                codeSystemName="Confidentiality" root="2.16.840.1.113883.18.418"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0908"
                codeSystemName="Package Type" root="2.16.840.1.113883.12.908"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0909"
                codeSystemName="Patient Results Release Categorization Scheme"
                root="2.16.840.1.113883.18.419"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0910"
                codeSystemName="Acquisition Modality" root="2.16.840.1.113883.12.910"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0912"
                codeSystemName="Participation" root="2.16.840.1.113883.18.420"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0913"
                codeSystemName="Monetary Denomination Code" root="1.0.4217"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0914" codeSystemName="Root Cause"
                root="2.16.840.1.113883.18.421"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0915"
                codeSystemName="Process Control Code" root="2.16.840.1.113883.12.915"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0916"
                codeSystemName="Relevant Clincial Information" root="2.16.840.1.113883.18.422"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0917" codeSystemName="Bolus Type"
                root="2.16.840.1.113883.18.423"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0918" codeSystemName="PCA Type"
                root="2.16.840.1.113883.18.424"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0919"
                codeSystemName="Exclusive Test" root="2.16.840.1.113883.18.425"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0920"
                codeSystemName="Preferred Specimen/Attribute Status" root="2.16.840.1.113883.18.426"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0921"
                codeSystemName="Certification Type Code" root="2.16.840.1.113883.18.427"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0922"
                codeSystemName="Certification Category Code" root="2.16.840.1.113883.18.428"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0923"
                codeSystemName="Process Interruption" root="2.16.840.1.113883.18.429"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0924"
                codeSystemName="Cumulative Dosage Limit UoM" root="2.16.840.1.113883.18.430"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0925"
                codeSystemName="Phlebotomy Issue" root="2.16.840.1.113883.18.431"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0926"
                codeSystemName="Phlebotomy Status" root="2.16.840.1.113883.18.432"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0927" codeSystemName="Arm Stick"
                root="2.16.840.1.113883.18.433"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0929"
                codeSystemName="Weight Units" root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0930"
                codeSystemName="Volume Units" root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0931"
                codeSystemName="Temperature Units" root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0932"
                codeSystemName="Donation Duration Units" root="2.16.840.1.113883.6.8"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0933"
                codeSystemName="Intended Procedure Type" root="2.16.840.1.113883.18.434"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0934"
                codeSystemName="Order Workflow Profile" root="2.16.840.1.113883.12.934"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0935"
                codeSystemName="Process Interruption Reason" root="2.16.840.1.113883.18.435"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0936"
                codeSystemName="Observation Type" root="2.16.840.1.113883.18.439"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0937"
                codeSystemName="Observation Sub-Type" root="2.16.840.1.113883.18.440"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0938"
                codeSystemName="Collection Event/Process Step Limit" root="2.16.840.1.113883.18.441"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0939"
                codeSystemName="Communication Location" root="2.16.840.1.113883.18.442"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0940"
                codeSystemName="Limitation Type Codes" root="2.16.840.1.113883.18.443"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0941"
                codeSystemName="Procedure Code" root="2.16.840.1.113883.12.941"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0942"
                codeSystemName="Equipment State Indicator Type Code" root="2.16.840.1.113883.18.444"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0943"
                codeSystemName="Transport Destination" root="2.16.840.1.113883.12.943"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0944"
                codeSystemName="Transport Route" root="2.16.840.1.113883.12.944"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0945"
                codeSystemName="Auto-Dilution Type" root="2.16.840.1.113883.18.445"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0946"
                codeSystemName="Supplier Type" root="2.16.840.1.113883.18.446"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0947"
                codeSystemName="Class of Trade" root="2.16.840.1.113883.12.947"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0948"
                codeSystemName="Relationship Type" root="2.16.840.1.113883.18.448"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0949"
                codeSystemName="Order Control Code Reason" root="2.16.840.1.113883.18.449"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0950"
                codeSystemName="Order Status Modifier" root="2.16.840.1.113883.18.450"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0951"
                codeSystemName="Reason for Study" root="2.16.840.1.113883.18.451"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0952"
                codeSystemName="Confidentiality Classification" root="2.16.840.1.113883.5.25"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0959"
                codeSystemName="Work Classification ODH" root="2.16.840.1.113883.5.1139"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0960"
                codeSystemName="Data Absent Reason" root="2.16.840.1.113883.4.642.1.1048"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0961"
                codeSystemName="Device Type" root="2.16.840.1.113883.12.961"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0962"
                codeSystemName="Device Status" root="2.16.840.1.113883.4.642.4.210"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0963"
                codeSystemName="Device Safety" root="2.16.840.1.113883.3.26.1.1"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0964"
                codeSystemName="Service Reason" root="2.16.840.1.113883.12.964"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0965"
                codeSystemName="Contract Type" root="2.16.840.1.113883.12.965"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0966"
                codeSystemName="Pricing Tier Level" root="2.16.840.1.113883.12.966"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0967"
                codeSystemName="Container Form" root="2.16.840.1.113883.12.967"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0968"
                codeSystemName="Container Material" root="2.16.840.1.113883.12.968"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0969"
                codeSystemName="Container Common Name" root="2.16.840.1.113883.12.969"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0970"
                codeSystemName="Online Verification Result" root="2.16.840.1.113883.18.453"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-0971"
                codeSystemName="Online Verification Result Error Code"
                root="2.16.840.1.113883.18.454"/>
            <codeSystem system="http://terminology.hl7.org/CodeSystem/v2-4000"
                codeSystemName="Name/address representation" root="2.16.840.1.113883.18.436"/>
        </codeSystems>
    </xsl:variable>
</xsl:stylesheet>

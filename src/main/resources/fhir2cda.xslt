<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0" xmlns="urn:hl7:v3"
    xmlns:cda="urn:hl7:v3" xmlns:sdtc="urn:hl7-org:sdtc" xmlns:fhir="http://hl7.org/fhir"
    xmlns:f='functions'
    exclude-result-prefixes="xs fhir cda">
    <xsl:include href="cdacodes.xslt"/>
    <xsl:param name="OID">2.16.840.1.113883.3.3081.9999</xsl:param>
    <xsl:output indent="yes"/>
    <xsl:template match="/fhir:Bundle">
        <ClinicalDocument xmlns="urn:hl7:v3">
            <realmCode code="US"/>
            <typeId root="2.16.840.1.113883.1.3" extension="POCD_HD000040"/>
            <templateId root="2.16.840.1.113883.10.20.22.1.1"/>
            <templateId root="2.16.840.1.113883.10.20.22.1.2"/>
            <id root="{$OID}" extension="{/fhir:Bundle/fhir:id/@value}"/>
            <code code="34133-9" codeSystem="2.16.840.1.113883.6.1"
                displayName="Summarization of episode note"/>
            <title>Transition of Care/Referral Summary</title>
            <xsl:variable name="timestamp" select="/fhir:Bundle/fhir:timestamp/@value"/>
            <effectiveTime>
                <xsl:apply-templates select='/fhir:Bundle/fhir:timestamp/@value' mode='dateTime'/>
            </effectiveTime>
            <confidentialityCode code="N" displayName="Normal" codeSystem="2.16.840.1.113883.5.25"/>
            <languageCode code="en-US"/>
            <xsl:apply-templates select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Patient"
                mode="recordTarget"/>
            <author typeCode="AUT" contextControlCode="OP">
                <time value="20130717114446.302-0500"/>
                <assignedAuthor classCode="ASSIGNED">
                    <id nullFlavor="NI"/>
                    <addr nullFlavor="NI"/>
                    <telecom nullFlavor="NI"/>
                    <assignedAuthoringDevice>
                        <manufacturerModelName>Audacious Inquiry</manufacturerModelName>
                        <softwareName>Modernization C-CDA Generator</softwareName>
                    </assignedAuthoringDevice>
                </assignedAuthor>
            </author>
            <custodian typeCode="CST">
                <assignedCustodian classCode="ASSIGNED">
                    <representedCustodianOrganization classCode="ORG" determinerCode="INSTANCE">
                        <id root="{$OID}.2"/>
                        <name>Audacious Inquiry</name>
                    </representedCustodianOrganization>
                </assignedCustodian>
            </custodian>
            <documentationOf typeCode="DOC">
                <serviceEvent classCode="PCPR" moodCode="EVN">
                    <xsl:apply-templates select="//fhir:Encounter/fhir:period" mode="effectiveTime"/>
                    <xsl:for-each select="//fhir:Encounter/fhir:participant[fhir:type/fhir:coding/fhir:system/@value = 'http://terminology.hl7.org/CodeSystem/v3-ParticipationType']">
                        <performer typeCode="PRF">
                            <!-- Get the practitioner -->
                            <xsl:variable name="pract" select="f:getPractitioner(fhir:individual)"/>
                            <xsl:apply-templates select="f:getPractitioner(fhir:individual)" mode="assignedEntity"/>
                        </performer>
                    </xsl:for-each>
                </serviceEvent>
            </documentationOf>
            <xsl:apply-templates select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Encounter" mode="encounter"/>
        </ClinicalDocument>
    </xsl:template>
    <xsl:template match="fhir:Practitioner" mode="assignedEntity">
        <assignedEntity classCode="ASSIGNED">
            <xsl:apply-templates select="fhir:identifier" mode="id"/>
            <xsl:apply-templates select="fhir:address" mode="address"/>
            <xsl:apply-templates select="fhir:telecom" mode="telecom"/>
            <xsl:if test="fhir:name">
                <assignedPerson>
                    <xsl:apply-templates select="fhir:name" mode="personName"/>
                </assignedPerson>
            </xsl:if>
        </assignedEntity>
    </xsl:template>
    <xsl:template match="@value" mode="dateTime">
        <xsl:attribute name='value'>
            <xsl:value-of select="translate(substring(.,0,20),'-:T','')"/>
            <xsl:value-of select="translate(substring(.,20,6),':','')"/>
        </xsl:attribute>
    </xsl:template>
    <xsl:template match="fhir:Patient" mode="recordTarget">
        <recordTarget>
            <patientRole>
                <xsl:apply-templates select="fhir:identifier" mode="id"/>
                <xsl:apply-templates select="fhir:address" mode="address"/>
                <xsl:apply-templates select="fhir:telecom" mode="telecom"/>
                <patient classCode="PSN" determinerCode="INSTANCE">
                    <id root="{$OID}.1" extension="{fhir:id/@value}"/>
                    <xsl:apply-templates select="fhir:name" mode="personName"/>
                    <xsl:apply-templates select="fhir:telecom" mode="telecom"/>
                    <xsl:if test="fhir:gender">
                        <administrativeGenderCode codeSystem="2.16.840.1.113883.5.1" codeSystemName="administrativeGender">
                            <xsl:choose>
                                <xsl:when test="fhir:gender/@value = 'male'">
                                    <xsl:attribute name="code">M</xsl:attribute>
                                    <xsl:attribute name="displayName">Male</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="fhir:gender/@value = 'female'">
                                    <xsl:attribute name="code">F</xsl:attribute>
                                    <xsl:attribute name="displayName">Female</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="fhir:gender/@value = 'other'">
                                    <xsl:attribute name="nullFlavor">OTH</xsl:attribute>
                                </xsl:when>
                                <xsl:when test="fhir:gender/@value = 'unknown'">
                                    <xsl:attribute name="nullFlavor">UNK</xsl:attribute>
                                </xsl:when>
                            </xsl:choose>
                        </administrativeGenderCode>
                    </xsl:if>
                    <xsl:if test="fhir:birthDate">
                        <birthTime>
                            <xsl:apply-templates select='fhir:birthDate/@value' mode='dateTime'/>
                        </birthTime>
                    </xsl:if>
                    <xsl:variable name='msCode'
                        select="fhir:maritalStatus/fhir:coding[
                        fhir:system='http://terminology.hl7.org/CodeSystem/v3-MaritalStatus' or
                        fhir:system='urn:oid:2.16.840.1.113883.5.2']"/>
                    <xsl:if test="$msCode">
                        <maritialStatusCode 
                            code="{$msCode/@code}"
                            codeSystem='2.16.840.1.113883.5.2'
                            codeSystemName='MaritalStatus'
                        >
                            <xsl:if test='$msCode/fhir:display/@value'>
                                <xsl:attribute name='displayName' select='$msCode/fhir:display/@value'/>
                            </xsl:if>
                        </maritialStatusCode>
                    </xsl:if>
                    <xsl:variable name='raceExt' select="fhir:extension[@url=' http://hl7.org/fhir/us/core/StructureDefinition/us-core-race']"/>
                    <xsl:if test='$raceExt[0]'>
                        <raceCode code="$raceExt[0]/fhir:extension[@url='ombCategory']/fhir:valueCoding/fhir:code/@value"
                            codeSystem='2.16.840.1.113883.6.238'
                            codeSystemName='CDC Race and Ethnicity'>
                            <xsl:if test="$raceExt[0]/fhir:extension[@url='ombCategory']/fhir:valueCoding/fhir:display/@value">
                                <xsl:attribute name='displayName' select="$raceExt[0]/fhir:extension[@url='ombCategory']/fhir:valueCoding/fhir:display/@value"/>
                            </xsl:if>
                        </raceCode>
                        <xsl:for-each select="$raceExt[0]/fhir:extension[@url='detailed']/fhir:valueCoding">
                            <sdtc:raceCode code="fhir:code/@value"
                                codeSystem='2.16.840.1.113883.6.238'
                                codeSystemName='CDC Race and Ethnicity'>
                                <xsl:if test='fhir:display/@value'>
                                    <xsl:attribute name='displayName' select='fhir:display/@value'/>
                                </xsl:if>
                            </sdtc:raceCode>
                        </xsl:for-each>
                        <xsl:for-each select="$raceExt[position() > 0]/fhir:extension[@url='ombCategory' or @url='detailed']/fhir:valueCoding">
                            <sdtc:raceCode code="fhir:code/@value"
                                codeSystem='2.16.840.1.113883.6.238'
                                codeSystemName='CDC Race and Ethnicity'>
                                <xsl:if test='fhir:display/@value'>
                                    <xsl:attribute name='displayName' select='fhir:display/@value'/>
                                </xsl:if>
                            </sdtc:raceCode>
                        </xsl:for-each>
                    </xsl:if>
                    <xsl:variable name='ethnExt' select="fhir:extension[@url=' http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity']"/>
                    <xsl:if test='$ethnExt[0]'>
                        <ethnicGroupCode code="$ethnExt[0]/fhir:extension[@url='ombCategory']/fhir:valueCoding/fhir:code/@value"
                            codeSystem='2.16.840.1.113883.6.238'
                            codeSystemName='CDC Race and Ethnicity'>
                            <xsl:if test="$ethnExt[0]/fhir:extension[@url='ombCategory']/fhir:valueCoding/fhir:display/@value">
                                <xsl:attribute name='displayName' select="$ethnExt[0]/fhir:extension[@url='ombCategory']/fhir:valueCoding/fhir:display/@value"/>
                            </xsl:if>
                        </ethnicGroupCode>
                        <!-- There is no support in CDA for multiple ethnic groups, even as an extension -->
                    </xsl:if>
                    <xsl:for-each select="fhir:communication">
                        <languageCommunication>
                            <languageCode code="fhir:language"/>
                            <xsl:if test="fhir:communication/fhir:preferred/@value">
                                <prefererenceInd value='{fhir:communication/fhir:preferred/@value}'/>
                            </xsl:if>
                        </languageCommunication>
                    </xsl:for-each>
                </patient>
                <xsl:if test="fhir:managingOrganization">
                    <providerOrganization>
                        <xsl:apply-templates select='fhir:managingOrganization' mode='organization'/>
                    </providerOrganization>
                </xsl:if>
            </patientRole>
        </recordTarget>
    </xsl:template>
    <xsl:template match="fhir:name" mode="personName">
        <name>
            <xsl:if test="fhir:use/@value = 'official' or fhir:use/@value = 'anonymous'">
                <xsl:attribute name='use'>
                    <!--  usual | official | temp | nickname | anonymous | old | maiden -->
                    <xsl:choose>
                        <xsl:when test="fhir:use/@value = 'official'">L</xsl:when>
                        <xsl:when test="fhir:use/@value = 'anonymous'">P</xsl:when>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:for-each select="fhir:prefix">
                <prefix><xsl:value-of select="@value"/></prefix>
            </xsl:for-each>
            <xsl:for-each select="fhir:given">
                <given><xsl:value-of select="@value"/></given>
            </xsl:for-each>
            <xsl:for-each select="fhir:family">
                <family><xsl:value-of select="@value"/></family>
            </xsl:for-each>
            <xsl:for-each select="fhir:suffix">
                <suffix><xsl:value-of select="@value"/></suffix>
            </xsl:for-each>
        </name>
    </xsl:template>
    <xsl:template match="fhir:address" mode="address">
        <addr>
            <xsl:call-template name='use-attribute'/>
            <xsl:for-each select="fhir:line">
                <streetAddressLine><xsl:value-of select="@value"/></streetAddressLine>
            </xsl:for-each>
            <xsl:for-each select="fhir:city">
                <city><xsl:value-of select="@value"/></city>
            </xsl:for-each>
            <xsl:for-each select="fhir:district">
                <county><xsl:value-of select="@value"/></county>
            </xsl:for-each>
            <xsl:for-each select="fhir:state">
                <state><xsl:value-of select="@value"/></state>
            </xsl:for-each>
            <xsl:for-each select="fhir:postalCode">
                <postalCode><xsl:value-of select="@value"/></postalCode>
            </xsl:for-each>
            <xsl:for-each select="fhir:country">
                <country><xsl:value-of select="@value"/></country>
            </xsl:for-each>
        </addr>
    </xsl:template>
    <xsl:template match="fhir:telecom" mode="telecom">
        <xsl:variable name='proto'>
            <xsl:choose>
                <xsl:when test="fhir:system/@value='phone'">tel:</xsl:when>
                <xsl:when test="fhir:system/@value='fax'">fax:</xsl:when>
                <xsl:when test="fhir:system/@value='email'">mailto:</xsl:when>
                <xsl:when test="fhir:system/@value='pager'">tel:</xsl:when>
                <xsl:when test="fhir:system/@value='url'"></xsl:when>
                <xsl:when test="fhir:system/@value='sms'">sms:</xsl:when>
                <xsl:when test="fhir:system/@value='other'"></xsl:when>
            </xsl:choose>
        </xsl:variable>
        <telecom value='{$proto}{fhir:value/@value}'>
            <xsl:call-template name='use-attribute'/>
            <xsl:if test='fhir:period'>
                <usablePeriod>
                    <xsl:if test='fhir:period/fhir:start'>
                        <low>
                            <xsl:apply-templates select="fhir:period/fhir:start/@value" mode='dateTime'/>
                        </low>
                    </xsl:if>
                    <xsl:if test='fhir:period/fhir:end'>
                        <high>
                            <xsl:apply-templates select="fhir:period/fhir:start/@value" mode='dateTime'/>
                        </high>
                    </xsl:if>
                </usablePeriod>
            </xsl:if>
        </telecom>
    </xsl:template>
    <xsl:template name='use-attribute'>
        <xsl:variable name='use'>
            <xsl:if test="fhir:use/@value = 'work'">
                <xsl:text>WP</xsl:text>
            </xsl:if>
            <xsl:if test="fhir:use/@value = 'home'">
                <xsl:text>H</xsl:text>
            </xsl:if>
            <xsl:if test="fhir:use/@value = 'temp'">
                <xsl:text>TMP</xsl:text>
            </xsl:if>
            <xsl:text> </xsl:text>
            <xsl:if test="fhir:system/@value = 'pager'">
                <xsl:text>PG</xsl:text>
            </xsl:if>
            <xsl:if test="fhir:system/@value = 'sms'">
                <xsl:text>MC</xsl:text>
            </xsl:if>
        </xsl:variable>
        <xsl:if test="normalize-space($use) != ''">
            <xsl:attribute name='use'>
                <xsl:value-of select='normalize-space($use)'/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>
    <xsl:template match="fhir:identifier" mode="id">
        <id extension='fhir:value/@value'>
            <xsl:choose>
                <xsl:when test="starts-with(fhir:system/@value,'urn:oid:')">
                    <xsl:attribute name="root">
                        <xsl:value-of select="substring(fhir:system/@value, 8)"/>
                    </xsl:attribute>
                </xsl:when>
                <!-- Convert UUID to OID using 2.25. + decimal representation of UUID -->
                <xsl:when test="starts-with(fhir:system/@value,'urn:uuid:')">
                    <xsl:variable name='uuid' select="translate(substring(fhir:system/@value, 9),'-','')"/>
                    <xsl:attribute name="root">
                        <xsl:text>2.25.</xsl:text>
                        <xsl:value-of select='f:hexToDec($uuid)'/>
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="root">2.16.840.1.113883.3.3081.99999</xsl:attribute>
                    <xsl:attribute name="extension">
                        <xsl:if test="fhir:system/@value">
                            <xsl:value-of select="fhir:system/@value"/>
                            <xsl:text>#</xsl:text>
                        </xsl:if>
                        <xsl:value-of select="fhir:value/@value"/>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </id>
    </xsl:template>
    <xsl:template match="fhir:managingOrganization" mode="organization">
        <xsl:variable name='id' select="replace(fhir:reference/@value, '^.*/', '')"/>
        <xsl:variable name='org' select="/fhir:Bundle/fhir:entry/fhir:resource/fhir:Organization[@id = $id]"/>
        <xsl:choose>
            <xsl:when test="$org">
                <xsl:apply-templates select="$org/fhir:identifier" mode="id"/>
                <xsl:if test="$org/fhir:name">
                    <name><xsl:value-of select="$org/fhir:name/@value"/></name>
                </xsl:if>
                <xsl:apply-templates select="$org/fhir:address" mode="address"/>
                <xsl:apply-templates select="$org/fhir:telecom" mode="telecom"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="fhir:identifier/fhir:value/@value">
                    <xsl:apply-templates select="fhir:identifier" mode="id"/>
                </xsl:if> 
                <xsl:if test="fhir:display/@value">
                   <name><xsl:value-of select="fhir:display/@value"/></name>
               </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="fhir:Encounter" mode="encounter">
        <componentOf>
            <encompassingEncounter>
                <xsl:apply-templates select="fhir:identifier" mode="id"/>
                <xsl:apply-templates select="fhir:class" mode="code"/>
                <xsl:apply-templates select="fhir:type/fhir:coding" mode="code"/>
                <xsl:apply-templates select="fhir:period" mode="effectiveTime"/>
                <xsl:for-each select="fhir:participant">
                    <xsl:if test="fhir:type/fhir:coding/fhir:system/@value = 'http://terminology.hl7.org/CodeSystem/v3-ParticipationType'">
                        <encounterParticipant typeCode="{fhir:type/fhir:coding/fhir:code/@value}">
                            <xsl:apply-templates select="f:getPractitioner(fhir:individual)" mode="assignedEntity"/>
                        </encounterParticipant>
                    </xsl:if>
                </xsl:for-each>
            </encompassingEncounter>
        </componentOf>
    </xsl:template>
    <xsl:template match="fhir:period" mode="effectiveTime">
        <effectiveTime>
            <xsl:if test="fhir:start/@value">
                <low>
                    <xsl:apply-templates select="fhir:start/@value" mode='dateTime'/>
                </low>
            </xsl:if>
            <xsl:if test="not(fhir:start/@value)">
                <low nullFlavor="NI"/>
            </xsl:if>
            <xsl:if test="fhir:end/@value">
                <high>
                    <xsl:apply-templates select="fhir:end/@value" mode='dateTime'/>
                </high>
            </xsl:if>
            <xsl:if test="not(fhir:end/@value)">
                <high nullFlavor="NI"/>
            </xsl:if>
        </effectiveTime>
    </xsl:template>
    <xsl:template match="*" mode="code">
            <code>
                <xsl:if test="fhir:code/@value">
                    <xsl:attribute name="code" select="fhir:code/@value"/>
                </xsl:if>
                <xsl:if test="not(fhir:code/@value)">
                    <xsl:attribute name="nullFlavor">NI</xsl:attribute>
                </xsl:if>
                <xsl:if test="fhir:display/@value">
                    <xsl:attribute name="displayName" select="fhir:display/@value"/>
                </xsl:if>
                <xsl:apply-templates select="fhir:system" mode="codeSystem"/>
                <xsl:variable name="system" select="fhir:system/@value"/>
                <xsl:variable name='codeSystem'>
                    <xsl:choose>
                        <xsl:when test="starts-with($system, 'urn:oid:')">
                            <xsl:variable name="root" select="substring($system, 8)"/>
                            <xsl:copy-of select="$HL7_CODE_MAP/codeSystems/codeSystem[@system=$root]"/>
                        </xsl:when>
                        <xsl:when test="starts-with($system, 'urn:uuid:')">
                            <xsl:variable name='uuid' select="translate(substring(fhir:system/@value, 9),'-','')"/>
                            <codeSystem root='2.25.{f:hexToDec($uuid)}' codeSystemName="$system"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="$HL7_CODE_MAP/codeSystems/codeSystem[@system=$system]"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test='$codeSystem/codeSystem/@root'>
                    <xsl:attribute name="codeSystem" select="$codeSystem/codeSystem/@root"/>
                </xsl:if>
                <xsl:if test='$codeSystem/codeSystem/@codeSystemName'>
                    <xsl:attribute name="codeSystemName" select="$codeSystem/codeSystem/@codeSystemName"/>
                </xsl:if>
                <xsl:if test='not($codeSystem/codeSystem/@codeSystemName)'>
                    <xsl:attribute name="codeSystemName" select="fhir:system/@value"/>
                </xsl:if>
            </code>
    </xsl:template>
    <xsl:function name="f:hexToDec">
        <xsl:param name="hex"/>
        <xsl:variable name="dec"
            select="string-length(substring-before('0123456789ABCDEF', substring($hex,1,1)))"/>
        <xsl:choose>
            <xsl:when test="matches($hex, '([0-9]*|[A-F]*)')">
                <xsl:value-of
                    select="if ($hex = '') then 0
                    else $dec * f:power(16, string-length($hex) - 1) + f:hexToDec(substring($hex,2))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message>Provided value is not hexadecimal...</xsl:message>
                <xsl:value-of select="$hex"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
    <xsl:function name="f:power">
        <xsl:param name="base"/>
        <xsl:param name="exp"/>
        <xsl:sequence
            select="if ($exp lt 0) then f:power(1.0 div $base, -$exp)
            else if ($exp eq 0)
            then 1e0
            else $base * f:power($base, $exp - 1)"
        />
    </xsl:function>
    
    <xsl:function name='f:getPractitioner'>
        <xsl:param name='ref'/>
        <xsl:variable name='id' select="replace($ref/fhir:reference/@value, '^.*/', '')"/>
        <xsl:variable name='pract' select="$ref/ancestor::fhir:Bundle//fhir:Practitioner[fhir:id/@value = $id]"/>
        <xsl:choose>
            <xsl:when test="$pract">
                <xsl:message select="'Got Here'"/>
                <xsl:sequence select="$pract"/>            
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name='role' select="$ref/ancestor::fhir:Bundle//fhir:PractitionerRole[fhir:id/@value = $id]"/>
                <xsl:if test='$role'>
                    <xsl:sequence select="f:getPractitioner($role)"/>                   
                </xsl:if>
                <xsl:if test='not($role)'>
                    <xsl:sequence/>                   
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
</xsl:stylesheet>

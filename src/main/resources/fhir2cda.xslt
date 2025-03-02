<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0" xmlns="urn:hl7:v3"
    xmlns:cda="urn:hl7:v3" xmlns:sdtc="urn:hl7-org:sdtc" xmlns:fhir="http://hl7.org/fhir"
    xmlns:f='functions'
    exclude-result-prefixes="xs fhir cda">
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
        </ClinicalDocument>
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
                <providerOrganization>
                    <id root="2.16.840.1.113883.1.13.99999"/>
                    <name>Local Community Hospital Organization</name>
                    <telecom use="WP" value="tel:(555) 555-1010"/>
                    <addr use="WP">
                        <streetAddressLine>4000 Hospital Dr.</streetAddressLine>
                        <city>Portland</city>
                        <state>OR</state>
                        <postalCode>97005- </postalCode>
                        <country>US</country>
                    </addr>
                </providerOrganization>
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
                        <xsl:value-of select="fhir:system/@value"/>
                        <xsl:text>#</xsl:text>
                        <xsl:value-of select="fhir:value/@value"/>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
        </id>
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
</xsl:stylesheet>

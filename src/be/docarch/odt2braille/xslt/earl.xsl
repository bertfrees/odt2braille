<?xml version="1.0" encoding="UTF-8"?>

    <!--

    odt2braille - Braille authoring in OpenOffice.org.

    Copyright (c) 2010 by DocArch <http://www.docarch.be>.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    -->

<xsl:stylesheet version="2.0"

            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
            xmlns:earl="http://www.w3.org/ns/earl#"
            xmlns:foaf="http://xmlns.com/foaf/0.1/"
            xmlns:dct="http://purl.org/dc/terms/"
            xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
            xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
            xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
            xmlns:ns1="http://www.docarch.be/accessibility/"
            xmlns:ns2="http://www.docarch.be/accessibility/properties#"
            xmlns:ns3="http://www.docarch.be/accessibility/types#"

            exclude-result-prefixes="xsl xsd rdf earl foaf dct ns2">

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"/>

        <xsl:param    name="paramNoPreliminarySection"   as="xsd:string" />
        <xsl:param    name="paramNoTitlePage"            as="xsd:string" />
        <xsl:param    name="paramNoBrailleToc"           as="xsd:string" />
        <xsl:param    name="paramNotInBrailleVolume"     as="xsd:string" />
        <xsl:param    name="paramOmittedInBraille"       as="xsd:string" />
        <xsl:param    name="paramTransposedInBraille"    as="xsd:string" />
        <xsl:param    name="paramUnnaturalVolumeBreak"   as="xsd:string" />

        <xsl:param    name="paramTimestamp"              as="xsd:string" />
        <xsl:param    name="paramTocEnabled"             as="xsd:string" />
        <xsl:param    name="content-url"                 as="xsd:string" />
    <!--<xsl:param    name="meta-url"                    as="xsd:string" />-->

        <xsl:variable name="body"            select="doc($content-url)/office:document-content/office:body" />
    <!--<xsl:variable name="meta"            select="doc($meta-url)/office:document-meta" />-->

        <xsl:variable name="content-base"    select="'../../content.xml#'" />
        

    <xsl:template match="/">
        <rdf:RDF>
            <foaf:Group rdf:nodeID="assertor" >
                <earl:mainAssertor>
                    <earl:Software rdf:about="http://www.docarch.be/odt2braille" />
                </earl:mainAssertor>
                <foaf:member>
                    <foaf:Person rdf:about="http://www.docarch.be/bert" />
                </foaf:member>
            </foaf:Group>

            <xsl:if test="not(//ns2:frontmatter)">
                <earl:Assertion>
                    <earl:assertedBy rdf:nodeID="assertor" />
                    <earl:subject>
                        <ns3:document>
                            <rdf:type rdf:resource="http://www.w3.org/ns/earl#TestSubject"/>
                        </ns3:document>
                    </earl:subject>
                    <earl:test>
                        <earl:TestCase>
                            <xsl:attribute name="rdf:about" select="$paramNoPreliminarySection" />
                        </earl:TestCase>
                    </earl:test>
                    <earl:result>
                        <earl:TestResult>
                            <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                            <ns1:lastChecked>
                                <xsl:value-of select="$paramTimestamp" />
                            </ns1:lastChecked>
                        </earl:TestResult>
                    </earl:result>
                </earl:Assertion>
            </xsl:if>


            <xsl:if test="not(//ns2:titlepage)">
                <earl:Assertion>
                    <earl:assertedBy rdf:nodeID="assertor" />
                    <earl:subject>
                        <ns3:document>
                            <rdf:type rdf:resource="http://www.w3.org/ns/earl#TestSubject"/>
                        </ns3:document>
                    </earl:subject>
                    <earl:test>
                        <earl:TestCase>
                            <xsl:attribute name="rdf:about" select="$paramNoTitlePage" />
                        </earl:TestCase>
                    </earl:test>
                    <earl:result>
                        <earl:TestResult>
                            <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                            <ns1:lastChecked>
                                <xsl:value-of select="$paramTimestamp" />
                            </ns1:lastChecked>
                        </earl:TestResult>
                    </earl:result>
                </earl:Assertion>
            </xsl:if>
            
            <xsl:if test="not($paramTocEnabled) and $body/office:text//text:table-of-content">
                <earl:Assertion>
                    <earl:assertedBy rdf:nodeID="assertor" />
                    <earl:subject>
                        <ns3:document>
                            <rdf:type rdf:resource="http://www.w3.org/ns/earl#TestSubject"/>
                        </ns3:document>
                    </earl:subject>
                    <earl:test>
                        <earl:TestCase>
                            <xsl:attribute name="rdf:about" select="$paramNoBrailleToc" />
                        </earl:TestCase>
                    </earl:test>
                    <earl:result>
                        <earl:TestResult>
                            <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                            <ns1:lastChecked>
                                <xsl:value-of select="$paramTimestamp" />
                            </ns1:lastChecked>
                        </earl:TestResult>
                    </earl:result>
                </earl:Assertion>
            </xsl:if>

            <earl:TestCase>
                <xsl:attribute name="rdf:about" select="$paramOmittedInBraille"/>
            </earl:TestCase>
            <xsl:apply-templates select="//ns2:caption"/>
        </rdf:RDF>
    </xsl:template>
    

    <xsl:template match="ns2:caption">
        <xsl:variable name="caption-id" select="./@rdf:about" />
        <xsl:if test="not(//ns2:hasCaption[@rdf:resource=$caption-id])">
            <earl:Assertion>
                <earl:assertedBy rdf:nodeID="assertor" />
                <earl:subject>
                    <ns3:paragraph>
                        <ns1:start>
                            <xsl:attribute name="rdf:resource" select="concat($content-base, $caption-id)" />
                        </ns1:start>
                        <rdf:type rdf:resource="http://www.w3.org/ns/earl#TestSubject"/>
                    </ns3:paragraph>
                </earl:subject>
                <earl:test>
                    <xsl:attribute name="rdf:resource" select="$paramOmittedInBraille" />
                </earl:test>
                <earl:result>
                    <earl:TestResult>
                        <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                        <ns1:lastChecked>
                            <xsl:value-of select="$paramTimestamp" />
                        </ns1:lastChecked>
                    </earl:TestResult>
                </earl:result>
            </earl:Assertion>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

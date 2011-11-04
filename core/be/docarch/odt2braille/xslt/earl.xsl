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
            xmlns:dct="http://purl.org/dc/elements/1.1/"
            xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
            xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
            xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
            xmlns:ns1="http://www.docarch.be/accessodf/"
            xmlns:ns2="http://www.docarch.be/accessodf/types#"
            xmlns:ns3="http://www.docarch.be/odt2braille/"

            exclude-result-prefixes="xsl xsd rdf earl foaf dct">

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"/>

        <xsl:param    name="paramNoBrailleToc"    as="xsd:string" />
        <xsl:param    name="paramOmittedCaption"  as="xsd:string" />

        <xsl:param    name="checkerID"            as="xsd:string"  select="'http://docarch.be/odt2braille/checker/BrailleChecker'" />
        <xsl:param    name="paramTimestamp"       as="xsd:string"  />
        <xsl:param    name="paramTocEnabled"      as="xsd:boolean" />
        <xsl:param    name="content-url"          as="xsd:string"  />
    <!--<xsl:param    name="meta-url"             as="xsd:string" />-->

        <xsl:variable name="body"            select="doc($content-url)/office:document-content/office:body" />

        <xsl:variable name="content-base"    select="'../content.xml#'" />
        

    <xsl:template match="/">
        <rdf:RDF>
            <ns1:Checker>
                <xsl:attribute name="rdf:about" select="$checkerID" />
                <rdf:type rdf:resource="http://www.w3.org/ns/earl#Assertor"/>
            </ns1:Checker>
            
            <xsl:if test="not($paramTocEnabled) and $body/office:text//text:table-of-content">
                <earl:Assertion>
                    <earl:assertedBy>
                        <xsl:attribute name="rdf:resource" select="$checkerID" />
                    </earl:assertedBy>
                    <earl:subject>
                        <ns2:Document>
                            <rdf:type rdf:resource="http://www.w3.org/ns/earl#TestSubject"/>
                        </ns2:Document>
                    </earl:subject>
                    <earl:test>
                        <earl:TestCase>
                            <xsl:attribute name="rdf:about" select="$paramNoBrailleToc" />
                        </earl:TestCase>
                    </earl:test>
                    <earl:result>
                        <earl:TestResult>
                            <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                            <dct:date>
                                <xsl:value-of select="$paramTimestamp" />
                            </dct:date>
                        </earl:TestResult>
                    </earl:result>
                </earl:Assertion>
            </xsl:if>

            <earl:TestCase>
                <xsl:attribute name="rdf:about" select="$paramOmittedCaption"/>
            </earl:TestCase>
            <xsl:apply-templates select="//ns3:Caption"/>
        </rdf:RDF>
    </xsl:template>
    

    <xsl:template match="ns3:Caption">
        <xsl:variable name="caption-id" select="./@rdf:about" />
        <xsl:if test="not(//ns3:hasCaption[@rdf:resource=$caption-id])">
            <earl:Assertion>
                <earl:assertedBy>
                    <xsl:attribute name="rdf:resource" select="$checkerID" />
                </earl:assertedBy>
                <earl:subject>
                    <ns2:Paragraph>
                        <ns1:start>
                            <xsl:attribute name="rdf:resource" select="concat($content-base, $caption-id)" />
                        </ns1:start>
                        <rdf:type rdf:resource="http://www.w3.org/ns/earl#TestSubject"/>
                    </ns2:Paragraph>
                </earl:subject>
                <earl:test>
                    <xsl:attribute name="rdf:resource" select="$paramOmittedCaption" />
                </earl:test>
                <earl:result>
                    <earl:TestResult>
                        <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                        <dct:date>
                            <xsl:value-of select="$paramTimestamp" />
                        </dct:date>
                    </earl:TestResult>
                </earl:result>
            </earl:Assertion>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

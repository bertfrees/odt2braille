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
            xmlns:ns1="http://www.docarch.be/accessibility-checker/properties#"
            xmlns:ns2="http://www.docarch.be/accessibility_checker/checks#"

            exclude-result-prefixes="xsl xsd rdf earl foaf dct ns1 ns2">

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"/>


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
            <earl:TestCase rdf:about="http://www.docarch.be/accessibility_checker/checks#A_NoTitlePage" />

            <xsl:copy>
                <xsl:apply-templates select="node()"/>
            </xsl:copy>
        </rdf:RDF>
    </xsl:template>


    <xsl:template match="rdf:Description">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
        <xsl:variable name="about" select="@rdf:about" />
        <xsl:if test="not(//rdf:Description/ns1:hasCaption[@rdf:resource=$about])">
            <earl:Assertion>
                <earl:assertedBy rdf:nodeID="assertor" />
                <earl:subject>
                    <earl:TestSubject>
                        <xsl:attribute name="rdf:about" select="$about" />
                    </earl:TestSubject>
                </earl:subject>
                <earl:test rdf:resource="http://www.docarch.be/accessibility_checker/checks#A_NoTitlePage" />
                <earl:result>
                    <earl:TestResult>
                        <earl:outcome rdf:resource="http://www.w3.org/ns/earl#failed"/>
                    </earl:TestResult>
                </earl:result>
            </earl:Assertion>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

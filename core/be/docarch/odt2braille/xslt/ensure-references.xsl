<?xml version="1.0" encoding="UTF-8"?>

    <!--

    odt2braille - Braille authoring in OpenOffice.org.

    Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.

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
                xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
                exclude-result-prefixes="xsl text xsd office">

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="no"
                    omit-xml-declaration="no"/>

        <xsl:param    name="styles-url"       as="xsd:string" />
        <xsl:variable name="styles"           select="doc($styles-url)/office:document-styles/office:styles" />
        <xsl:variable name="automatic-styles" select="/office:document-content/office:automatic-styles" />
        <xsl:variable name="all-styles"       select="$styles | $automatic-styles" />

        <xsl:include  href="common-templates.xsl" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text:p">
        <xsl:variable name="style-name" select="@text:style-name" />
        <xsl:variable name="is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:copy>
            <xsl:if test="$is-caption">
                <xsl:if test="not(@xml:id)">
                    <xsl:attribute name="xml:id">
                        <xsl:value-of select="generate-id(.)" />
                    </xsl:attribute>
                </xsl:if>
            </xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

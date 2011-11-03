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
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    -->

<xsl:stylesheet version="2.0"

            xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:o2b="http://odt2braille.sf.net"
            xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
            xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
            xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"

            exclude-result-prefixes="xsd o2b office style fo" >

    <xsl:output method="xml"
                encoding="UTF-8"
                media-type="text/xml"
                indent="yes"
                omit-xml-declaration="no"/>

    <xsl:param    name="styles-url"       as="xsd:string" />
    <xsl:variable name="styles"           select="doc($styles-url)/office:document-styles/office:styles" />
    <xsl:variable name="automatic-styles" select="/office:document-content/office:automatic-styles" />
    <xsl:variable name="all-styles"       select="$styles | $automatic-styles" />

    <xsl:template match="/">

        <xsl:variable name="main-language">
            <xsl:value-of select="$styles/style:default-style[@style:family='paragraph'][1]/style:text-properties/@fo:language" />
            <xsl:text>-</xsl:text>
            <xsl:value-of select="$styles/style:default-style[@style:family='paragraph'][1]/style:text-properties/@fo:country" />
        </xsl:variable>

        <xsl:variable name="languages" as="xsd:string*" >
            <xsl:for-each select="$all-styles/style:style/style:text-properties[@fo:language]">
                <xsl:variable name="language">
                    <xsl:value-of select="@fo:language" />
                    <xsl:text>-</xsl:text>
                    <xsl:value-of select="@fo:country" />
                </xsl:variable>
                <xsl:sequence select="$language" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="distinct-languages"
                      select="distinct-values($languages)" />

        <o2b:languages>
            <o2b:language>
                <xsl:attribute name="name"  select="$main-language" />
                <xsl:attribute name="class" select="'main'" />
            </o2b:language>
            <xsl:for-each select="$distinct-languages">
                <o2b:language>
                    <xsl:attribute name="name" select="." />
                </o2b:language>
            </xsl:for-each>
        </o2b:languages>
    </xsl:template>
</xsl:stylesheet>

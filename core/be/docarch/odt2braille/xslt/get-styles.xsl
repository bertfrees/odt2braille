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
            xmlns:o2b="http://odt2braille.sf.net"
            xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
            xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
            xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"

            exclude-result-prefixes="xsd o2b office style text" >

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"/>

        <xsl:param    name="styles-url"       as="xsd:string" />
        <xsl:variable name="styles"           select="doc($styles-url)/office:document-styles/office:styles" />
        <xsl:variable name="automatic-styles" select="/office:document-content/office:automatic-styles" />
        <xsl:variable name="all-styles"       select="$styles | $automatic-styles" />
        <xsl:variable name="body"             select="/office:document-content/office:body" />

        <xsl:include href="common-templates.xsl" />

    <xsl:template match="/">

        <xsl:variable name="paragraph-styles" as="xsd:string*" >
            <xsl:for-each select="$body/office:text//text:p">
                <xsl:variable name="is-paragraph" as="xsd:boolean">
                    <xsl:call-template name="is-paragraph">
                        <xsl:with-param name="node" select="." />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="$is-paragraph">
                    <xsl:call-template name="get-style-sequence">
                        <xsl:with-param name="style-name" select="@text:style-name" />
                        <xsl:with-param name="family"     select="'paragraph'" />
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="character-styles" as="xsd:string*" >
            <xsl:for-each select="$body/office:text//text:span">
                <xsl:call-template name="get-style-sequence">
                    <xsl:with-param name="style-name" select="@text:style-name" />
                    <xsl:with-param name="family"     select="'text'" />
                </xsl:call-template>
            </xsl:for-each>
            <xsl:if test="$body/office:text//text:a">
                <xsl:sequence select="'Internet_20_link'" />
            </xsl:if>
        </xsl:variable>

        <xsl:variable name="distinct-paragraph-styles"
                      select="distinct-values($paragraph-styles)" />

        <xsl:variable name="distinct-character-styles"
                      select="distinct-values($character-styles)" />

        <o2b:styles>
            <xsl:for-each select="$distinct-paragraph-styles">
                <xsl:variable name="family" select="'paragraph'" />
                <xsl:variable name="style-name" select="." />
                <xsl:variable name="display-name">
                    <xsl:call-template name="get-display-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <o2b:style>
                    <xsl:attribute name="family" select="$family" />
                    <xsl:attribute name="name"   select="." />
                    <xsl:if test="not($display-name = '')">
                        <xsl:attribute name="display-name" select="$display-name" />
                     </xsl:if>
                    <xsl:if test="not($parent-style-name = '')">
                        <xsl:attribute name="parent-style-name" select="$parent-style-name" />
                    </xsl:if>
                    <xsl:if test="$automatic-styles/style:style[@style:name=$style-name and @style:family=$family]">
                        <xsl:attribute name="automatic" select="'true'" />
                    </xsl:if>
                </o2b:style>
            </xsl:for-each>
            <xsl:for-each select="$distinct-character-styles">
                <xsl:variable name="family" select="'text'" />
                <xsl:variable name="style-name" select="." />
                <xsl:variable name="display-name">
                    <xsl:call-template name="get-display-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="not($automatic-styles/style:style[@style:name=$style-name and @style:family=$family])">
                    <o2b:style>
                        <xsl:attribute name="family" select="$family" />
                        <xsl:attribute name="name"   select="." />
                        <xsl:if test="not($display-name = '')">
                            <xsl:attribute name="display-name" select="$display-name" />
                         </xsl:if>
                        <xsl:if test="not($parent-style-name = '')">
                            <xsl:attribute name="parent-style-name" select="$parent-style-name" />
                        </xsl:if>
                    </o2b:style>
                </xsl:if>
            </xsl:for-each>
        </o2b:styles>
    </xsl:template>
    
    <xsl:template name="get-style-sequence">
        <xsl:param name="style-name" />
        <xsl:param name="family"     />
        <xsl:sequence select="$style-name" />
        <xsl:variable name="parent-style-name">
            <xsl:call-template name="get-parent-style-name">
                <xsl:with-param name="style-name" select="$style-name" />
                <xsl:with-param name="family"     select="$family"     />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="not($parent-style-name = '')">
            <xsl:call-template name="get-style-sequence">
                <xsl:with-param name="style-name" select="$parent-style-name" />
                <xsl:with-param name="family"     select="$family"            />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

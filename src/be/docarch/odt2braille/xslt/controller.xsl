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
            xmlns:ns1="http://www.docarch.be/accessibility/properties#"
            xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
            xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
            xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
            xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
            xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
            xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
            xmlns:earl="http://www.w3.org/ns/earl#"
            xmlns:foaf="http://xmlns.com/foaf/0.1/"
            xmlns:dct="http://purl.org/dc/terms/"

            exclude-result-prefixes="xsl xsd office style text fo rdf earl foaf dct draw table">
            
        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="no"
                    omit-xml-declaration="no"/>

        <xsl:param    name="styles-url"       as="xsd:string" />
        <xsl:variable name="styles"           select="doc($styles-url)/office:document-styles/office:styles" />
        <xsl:variable name="automatic-styles" select="/office:document-content/office:automatic-styles" />

        <xsl:include href="common-templates.xsl" />


    <xsl:template match="/">
        <rdf:RDF>
            <xsl:apply-templates select="//table:table |
                                         //draw:frame |
                                         //text:p[@xml:id]"/>
        </rdf:RDF>
    </xsl:template>

    <!-- VOLUME MANAGEMENT -->
    
    <xsl:template match="text:section">
         
        



         
         
    </xsl:template>


    <!-- TABLE CAPTIONS -->

    <xsl:template match="table:table">
        <xsl:variable name="table-name" select="@table:name" />
        <xsl:variable name="following-paragraph"
                      select="current()/following-sibling::*[1][name()='text:p' and not(descendant::draw:frame)]" />
        <xsl:variable name="is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$following-paragraph/@text:style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$is-caption">
            <xsl:variable name="caption-id">
                <xsl:value-of select="$following-paragraph/@xml:id" />
            </xsl:variable>
            <xsl:variable name="is-empty" as="xsd:boolean">
                <xsl:call-template name="is-empty">
                    <xsl:with-param name="node" select="$following-paragraph" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$caption-id and not($is-empty)">
                <ns1:table>
                    <xsl:attribute name="rdf:about" select="$table-name" />
                    <ns1:hasCaption>
                        <xsl:attribute name="rdf:resource" select="$caption-id" />
                    </ns1:hasCaption>
                </ns1:table>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!-- TEXTBOX & IMAGE CAPTIONS -->

    <xsl:template match="draw:frame">
        <xsl:variable name="frame-name" select="@draw:name" />
        <xsl:variable name="parent-paragraph"
                      select="current()/parent::text:p[1]
                            | current()/parent::draw:a/parent::text:p[1]" />
        <xsl:variable name="following-paragraph"
                      select="(current()/parent::*/following-sibling::*[1][name()='text:p' and not(descendant::draw:frame)])
                            | (current()/parent::draw:a/parent::*/following-sibling::*[1][name()='text:p' and not(descendant::draw:frame)])
                            | (current()/self::*[parent::draw:text-box]/following-sibling::*[1][name()='text:p' and not(descendant::draw:frame)])
                            | (current()/parent::draw:a[parent::draw:text-box]/following-sibling::*[1][name()='text:p' and not(descendant::draw:frame)])" />
        <xsl:variable name="last-paragraph-in-textbox"
                      select="current()/draw:text-box[not(descendant::draw:frame)]/text:p[last()]" />
        <xsl:variable name="parent-paragraph-is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$parent-paragraph/@text:style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="following-paragraph-is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$following-paragraph/@text:style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="last-paragraph-in-textbox-is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$last-paragraph-in-textbox/@text:style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="parent-paragraph-is-empty" as="xsd:boolean">
            <xsl:call-template name="is-empty">
                <xsl:with-param name="node" select="$parent-paragraph" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="following-paragraph-is-empty" as="xsd:boolean">
            <xsl:call-template name="is-empty">
                <xsl:with-param name="node" select="$following-paragraph" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="last-paragraph-in-textbox-is-empty" as="xsd:boolean">
            <xsl:call-template name="is-empty">
                <xsl:with-param name="node" select="$last-paragraph-in-textbox" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="caption-id">
            <xsl:choose>
                <xsl:when test="(./child::draw:image
                              or ./child::draw:text-box)
                                    and     $parent-paragraph-is-caption
                                    and not($parent-paragraph-is-empty)
                                    and     $parent-paragraph/@xml:id">
                    <xsl:value-of select="$parent-paragraph/@xml:id" />
                </xsl:when>
                <xsl:when test="(./child::draw:image
                              or ./child::draw:text-box)
                                    and     $following-paragraph-is-caption
                                    and not($following-paragraph-is-empty)
                                    and     $following-paragraph/@xml:id">
                    <xsl:value-of select="$following-paragraph/@xml:id" />
                </xsl:when>
                <xsl:when test=" ./child::draw:text-box
                                    and     $last-paragraph-in-textbox-is-caption
                                    and not($last-paragraph-in-textbox-is-empty)
                                    and     $last-paragraph-in-textbox/@xml:id">
                    <xsl:value-of select="$last-paragraph-in-textbox/@xml:id" />
                </xsl:when>
                <xsl:otherwise />
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="string-length($caption-id)>0">
            <ns1:frame>
                <xsl:attribute name="rdf:about" select="$frame-name" />
                <ns1:hasCaption>
                    <xsl:attribute name="rdf:resource" select="$caption-id" />
                </ns1:hasCaption>
            </ns1:frame>
        </xsl:if>
    </xsl:template>


    <xsl:template match="text:p">
        <xsl:variable name="style-name" select="@text:style-name" />
        <xsl:variable name="is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="is-empty" as="xsd:boolean">
            <xsl:call-template name="is-empty">
                <xsl:with-param name="node" select="." />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$is-caption and not($is-empty)">
            <ns1:caption>
                <xsl:attribute name="rdf:about" select="@xml:id" />
            </ns1:caption>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

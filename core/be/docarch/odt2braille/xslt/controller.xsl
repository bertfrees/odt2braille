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
            xmlns:ns1="http://www.docarch.be/odt2braille/"
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

        <xsl:param name="paramFrontMatterSection"       as="xsd:string"   />
        <xsl:param name="paramRepeatFrontMatterSection" as="xsd:string"   />
        <xsl:param name="paramTitlePageSection"         as="xsd:string"   />
        <xsl:param name="paramRearMatterSection"        as="xsd:string"   />
        <xsl:param name="paramVolumeSections"           as="xsd:string*"  />

        <xsl:param    name="styles-url"       as="xsd:string" />
        <xsl:variable name="styles"           select="doc($styles-url)/office:document-styles/office:styles" />
        <xsl:variable name="automatic-styles" select="/office:document-content/office:automatic-styles" />
        <xsl:variable name="all-styles"       select="$styles | $automatic-styles" />

        <!-- Frontmatter -->
        <xsl:variable name="frontmatter" as="element()*"
                      select="//text:section[@text:name=$paramFrontMatterSection]"/>

        <!-- Repeated frontmatter -->
        <xsl:variable name="repeat-frontmatter" as="element()*">
            <xsl:choose>
                <xsl:when test="$paramFrontMatterSection = $paramRepeatFrontMatterSection">
                    <xsl:sequence select="$frontmatter"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$frontmatter/descendant::text:section[@text:name=$paramRepeatFrontMatterSection]"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Title page -->
        <xsl:variable name="title-page" as="element()*">
            <xsl:choose>
                <xsl:when test="$paramFrontMatterSection = $paramTitlePageSection">
                    <xsl:sequence select="$frontmatter"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$frontmatter/descendant::text:section[@text:name=$paramTitlePageSection]"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Rearmatter -->
        <xsl:variable name="rearmatter" as="element()*">
            <xsl:choose>
                <xsl:when test="$frontmatter">
                    <xsl:sequence select="$frontmatter/following::text:section[@text:name=$paramRearMatterSection]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="//text:section[@text:name=$paramRearMatterSection]"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:include href="common-templates.xsl" />


    <xsl:template match="/">
        <rdf:RDF>
            <xsl:apply-templates select="//text:section |
                                         //table:table |
                                         //draw:frame |
                                         //text:p[@xml:id]"/>
        </rdf:RDF>
    </xsl:template>

    <!-- VOLUME MANAGEMENT -->
    
    <xsl:template match="text:section">
        <xsl:variable name="section-name" select="@text:name" />
        <xsl:if test="$frontmatter[@text:name=$section-name]">
            <ns1:Frontmatter>
                <xsl:attribute name="rdf:about" select="concat('section:/', $section-name)" />
            </ns1:Frontmatter>
        </xsl:if>
        <xsl:if test="$repeat-frontmatter[@text:name=$section-name]">
            <ns1:RepeatFrontmatter>
                <xsl:attribute name="rdf:about" select="concat('section:/', $section-name)" />
            </ns1:RepeatFrontmatter>
        </xsl:if>
        <xsl:if test="$title-page[@text:name=$section-name]">
            <ns1:Titlepage>
                <xsl:attribute name="rdf:about" select="concat('section:/', $section-name)" />
            </ns1:Titlepage>
        </xsl:if>
        <xsl:if test="$rearmatter[@text:name=$section-name]">
            <ns1:Rearmatter>
                <xsl:attribute name="rdf:about" select="concat('section:/', $section-name)" />
            </ns1:Rearmatter>
        </xsl:if>
        <xsl:variable name="i" as="xsd:integer">
            <xsl:call-template name="get-volume-index">
                <xsl:with-param name="section-name" select="$section-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$i>0">
            <xsl:choose>
                <xsl:when test="$frontmatter[@text:name=$section-name]"/>
                <xsl:when test="$repeat-frontmatter[@text:name=$section-name]"/>
                <xsl:when test="$title-page[@text:name=$section-name]"/>
                <xsl:when test="$rearmatter[@text:name=$section-name]"/>
                <xsl:when test="$frontmatter/descendant::text:section[@text:name=$section-name]"/>
                <xsl:when test="$frontmatter/preceding::text:section[@text:name=$section-name]"/>
                <xsl:when test="$frontmatter/ancestor::text:section[@text:name=$section-name]"/>
                <xsl:when test="$rearmatter/ancestor::text:section[@text:name=$section-name]"/>
                <xsl:when test="$rearmatter/following::text:section[@text:name=$section-name]"/>
                <xsl:otherwise>
                    <ns1:Volume>
                        <xsl:attribute name="rdf:about" select="concat('section:/', $section-name)" />
                    </ns1:Volume>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template name="get-volume-index">
        <xsl:param name="section-name" as="xsd:string" />
        <xsl:variable name="occurences" as="xsd:integer*">
            <xsl:for-each select="$paramVolumeSections">
                <xsl:variable name="i" select="position()" />
                <xsl:if test=".=$section-name">
                    <xsl:sequence select="$i" />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$occurences[1]">
                <xsl:value-of select="$occurences[1]" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="-1" />
            </xsl:otherwise>
        </xsl:choose>
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
                <ns1:Table>
                    <xsl:attribute name="rdf:about" select="concat('table:/',$table-name)" />
                    <ns1:hasCaption>
                        <xsl:attribute name="rdf:resource" select="$caption-id" />
                    </ns1:hasCaption>
                </ns1:Table>
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
            <ns1:Frame>
                <xsl:attribute name="rdf:about" select="concat('frame:/',$frame-name)" />
                <ns1:hasCaption>
                    <xsl:attribute name="rdf:resource" select="$caption-id" />
                </ns1:hasCaption>
            </ns1:Frame>
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
            <ns1:Caption>
                <xsl:attribute name="rdf:about" select="@xml:id" />
            </ns1:Caption>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

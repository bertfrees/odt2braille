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
            xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
            xmlns:xalan="http://xml.apache.org/xslt"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            exclude-result-prefixes="dtb xalan xsd" >

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"
                    xalan:indent-amount="3" />

        <!-- XSLT Parameters  -->

        <xsl:param name="paramTranscriptionInfoEnabled"       as="xsd:boolean"  select="false()" />
        <xsl:param name="paramTranscriptionInfoLine"          as="xsd:string"   select="''" />

        <xsl:param name="paramFrontmatter"                    as="xsd:boolean"  select="false()" />

        <!-- paramFrontmatter -->

            <xsl:param name="paramVolumeInfoEnabled"          as="xsd:boolean"  select="true()" />
            <xsl:param name="paramVolumeInfoLines"            as="xsd:string*" />

            <xsl:param name="paramSpecialSymbolsListEnabled"  as="xsd:boolean"  select="false()" />
            <xsl:param name="paramSpecialSymbolsListTitle"    as="xsd:string"   select="'SPECIAL SYMBOLS USED IN THIS VOLUME'" />
            <xsl:param name="paramSpecialSymbols"             as="xsd:string*" />
            <xsl:param name="paramSpecialSymbolsDots"         as="xsd:string*" />
            <xsl:param name="paramSpecialSymbolsDescription"  as="xsd:string*" />

            <xsl:param name="paramTNPageEnabled"              as="xsd:boolean"  select="false()"  />
            <xsl:param name="paramTNPageTitle"                as="xsd:string"   select='"TRANSCRIBER&apos;S NOTES"' />
            <xsl:param name="paramTranscribersNotes"          as="xsd:string*"  />

            <xsl:param name="paramTableOfContentEnabled"      as="xsd:boolean"  select="true()" />
            <xsl:param name="paramTableOfContentTitle"        as="xsd:string"   select="'TABLE OF CONTENTS'" />

            <xsl:param name="paramExtendedFront"              as="xsd:boolean"  select="false()" />

            <!-- !paramExtendedFront -->

                <xsl:param name="paramNormalOrSupplementary"  as="xsd:boolean"  select="true()" />
                <xsl:param name="paramVolumeNr"               as="xsd:integer"  select="1" />

        <xsl:strip-space elements="dtb:book dtb:div" />
        

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- FRONTMATTER -->

    <xsl:template match="dtb:frontmatter">

        <xsl:if test="$paramFrontmatter">
            <xsl:copy>
                <xsl:choose>
                    <xsl:when test="$paramExtendedFront">
                        <xsl:apply-templates select="*"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="dtb:titlepage |
                                                     dtb:pagenum[not(ancestor::dtb:titlepage)] |
                                                     dtb:pagebreak[not(ancestor::dtb:titlepage)]"/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$paramSpecialSymbolsListEnabled">
                    <xsl:call-template name="special-symbols-list" />
                </xsl:if>
                <xsl:if test="$paramTNPageEnabled">
                    <xsl:call-template name="tn-page" />
                </xsl:if>
                <xsl:if test="$paramTableOfContentEnabled">
                    <xsl:call-template name="toc" />
                </xsl:if>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <!-- BODYMATTER -->

    <xsl:template match="dtb:bodymatter">

        <xsl:choose>
            <xsl:when test="not($paramFrontmatter)">
                <xsl:copy>
                    <xsl:apply-templates select="*"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$paramTableOfContentEnabled">
                    <xsl:choose>
                        <xsl:when test="$paramExtendedFront">
                            <xsl:copy>
                                <xsl:apply-templates select="*"/>
                            </xsl:copy>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy>
                                <xsl:choose>
                                    <xsl:when test="$paramNormalOrSupplementary">
                                        <xsl:apply-templates select="dtb:div[@class='volume'][$paramVolumeNr]"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:apply-templates select="dtb:div[@class='supplement'][$paramVolumeNr]"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:copy>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- TITLE PAGE -->

    <xsl:template match="dtb:titlepage">

        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>

            <!-- VOLUME INFO -->
            <xsl:if test="$paramVolumeInfoEnabled">
                <dtb:div class="volume-info">
                    <dtb:p>
                        <xsl:for-each select="$paramVolumeInfoLines">
                            <xsl:variable name="i" select="position()" />
                            <xsl:value-of select="$paramVolumeInfoLines[$i]" />
                            <xsl:if test="$i &lt; last()">
                                <dtb:br />
                            </xsl:if>
                        </xsl:for-each>
                    </dtb:p>
                </dtb:div>
            </xsl:if>

            <!-- TRANSCRIPTION INFO -->
            <xsl:if test="$paramTranscriptionInfoEnabled">
                <dtb:div class="transcription-info">
                    <dtb:p>
                        <xsl:if test="not ($paramTranscriptionInfoLine = '')">
                            <xsl:value-of select="$paramTranscriptionInfoLine" />
                        </xsl:if>
                    </dtb:p>
                </dtb:div>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- LIST OF SPECIAL SYMBOLS -->

    <xsl:template name="special-symbols-list">

        <xsl:if test="$paramSpecialSymbols[1]">
            <dtb:special-symbols-list newpage="yes">
                <dtb:heading>
                    <dtb:h1 class="dummy">
                        <xsl:value-of select="$paramSpecialSymbolsListTitle" />
                    </dtb:h1>
                </dtb:heading>
                <dtb:list>
                    <xsl:for-each select="$paramSpecialSymbols">
                        <xsl:variable name="i" select="position()" />
                        <dtb:li>
                            <dtb:p>
                                <dtb:span class="spaced"><xsl:value-of select="$paramSpecialSymbols[$i]" /></dtb:span>
                                <dtb:span class="spaced"><xsl:value-of select="$paramSpecialSymbolsDots[$i]" /></dtb:span>
                                <dtb:span class="spaced"><xsl:value-of select="$paramSpecialSymbolsDescription[$i]" /></dtb:span>
                            </dtb:p>
                        </dtb:li>
                    </xsl:for-each>
                </dtb:list>
            </dtb:special-symbols-list>
        </xsl:if>
    </xsl:template>

    <!-- TRANSCRIBER'S NOTES PAGE -->

    <xsl:template name="tn-page">

        <xsl:if test="$paramTranscribersNotes[1]">
            <dtb:tn-page newpage="yes">
                <dtb:heading>
                    <dtb:h1 class="dummy">
                        <xsl:value-of select="$paramTNPageTitle" />
                    </dtb:h1>
                </dtb:heading>
                <xsl:for-each select="$paramTranscribersNotes">
                    <xsl:variable name="i" select="position()" />
                    <dtb:note>
                         <xsl:value-of select="$paramTranscribersNotes[$i]" />
                    </dtb:note>
                </xsl:for-each>
            </dtb:tn-page>
        </xsl:if>
    </xsl:template>

    <!-- TABLE OF CONTENTS -->

    <xsl:template name="toc">

        <dtb:div class="toc">
            <dtb:h1 class="dummy">
                <xsl:value-of select="$paramTableOfContentTitle" />
            </dtb:h1>
            <dtb:list class="toc" />
        </dtb:div>
    </xsl:template>

    <xsl:template match="dtb:div[@class='not-in-volume']">

        <xsl:if test="count(./*) > 0">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

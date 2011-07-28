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

        <xsl:param name="paramBodyMatterEnabled"              as="xsd:boolean"  select="true()"  />
            <xsl:param name="paramAllVolumes"                 as="xsd:boolean"  select="true()"  />
                <xsl:param name="paramVolumeId"               as="xsd:string"   select="''"      />

        <xsl:param name="paramFrontMatterEnabled"             as="xsd:boolean"  select="false()" />
            <xsl:param name="paramExtendedFront"              as="xsd:boolean"  select="false()" />

        <xsl:param name="paramRearMatterEnabled"              as="xsd:boolean"  select="false()" />

        <xsl:param name="paramTableOfContentEnabled"          as="xsd:boolean"  select="false()" />
            <xsl:param name="paramExtendedToc"                as="xsd:boolean"  select="false()" />
            <xsl:param name="paramTableOfContentTitle"        as="xsd:string"   select="'TABLE OF CONTENTS'" />

        <xsl:param name="paramTNPageEnabled"                  as="xsd:boolean"  select="false()" />
            <xsl:param name="paramTNPageTitle"                as="xsd:string"   select='"TRANSCRIBER&apos;S NOTES"' />
            <xsl:param name="paramTranscribersNotes"          as="xsd:string*"  />

        <xsl:param name="paramSpecialSymbolsListEnabled"      as="xsd:boolean"  select="false()" />
            <xsl:param name="paramSpecialSymbolsListTitle"    as="xsd:string"   select="'SPECIAL SYMBOLS USED IN THIS VOLUME'" />
            <xsl:param name="paramSpecialSymbols"             as="xsd:string*"  />
            <xsl:param name="paramSpecialSymbolsDots"         as="xsd:string*"  />
            <xsl:param name="paramSpecialSymbolsDescription"  as="xsd:string*"  />

        <xsl:param name="paramTranscriptionInfoEnabled"       as="xsd:boolean"  select="false()" />
            <xsl:param name="paramTranscriptionInfoLine"      as="xsd:string"   select="''" />

        <xsl:param name="paramVolumeInfoEnabled"              as="xsd:boolean"  select="true()" />
            <xsl:param name="paramVolumeInfoLines"            as="xsd:string*"  />

        <xsl:param name="paramNoteSectionTitle"               as="xsd:string"   select="'NOTES'"    />
        <xsl:param name="paramContinuedHeadingSuffix"         as="xsd:string"   select="'(Cont.)'"    />

        <xsl:strip-space elements="dtb:book dtb:div" />
        

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- FRONTMATTER -->

    <xsl:template match="dtb:frontmatter">

        <xsl:if test="$paramFrontMatterEnabled or
                      $paramTNPageEnabled or
                      $paramSpecialSymbolsListEnabled or
                      $paramTableOfContentEnabled">
            <xsl:copy>
                <xsl:if test="$paramFrontMatterEnabled">
                    <xsl:variable name="front" as="element()">
                        <xsl:choose>
                            <xsl:when test="$paramExtendedFront">
                                <xsl:sequence select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select=".//dtb:repeat-frontmatter"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>

                    <!-- Frontmatter -->
                    <xsl:apply-templates select="$front/*"/>

                    <!-- Notesection -->
                    <xsl:variable name="endnotes" as="element()*">
                        <xsl:for-each select="$front/descendant::dtb:note[@class='endnote']">                            
                            <xsl:variable name="end-of-section" select="@end-of-section" />
                            <xsl:if test="(string-length($end-of-section)=0)
                                    or not($front/dtb:note-section[@section-name=$end-of-section])">
                                <xsl:sequence select="." />
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:if test="$endnotes">
                        <xsl:call-template name="note-section">
                            <xsl:with-param name="notes" select="$endnotes" />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:if>

                <!-- List of special symbols -->
                <xsl:if test="$paramSpecialSymbolsListEnabled">
                    <xsl:call-template name="special-symbols-list" />
                </xsl:if>

                <!-- Transcriber's notes page -->
                <xsl:if test="$paramTNPageEnabled">
                    <xsl:call-template name="tn-page" />
                </xsl:if>

                <!-- Table of contents -->
                <xsl:if test="$paramTableOfContentEnabled">
                    <xsl:call-template name="toc" />
                </xsl:if>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <!-- BODYMATTER -->

    <xsl:template match="dtb:bodymatter">

        <xsl:choose>
            <xsl:when test="(($paramBodyMatterEnabled or $paramTableOfContentEnabled)
                                    and $paramAllVolumes)
                                or ($paramTableOfContentEnabled and $paramExtendedToc)">
                <xsl:copy>
                    <xsl:apply-templates select="dtb:volume"/>
                </xsl:copy>
            </xsl:when>
            <xsl:when test="($paramBodyMatterEnabled or $paramTableOfContentEnabled)
                                and string-length($paramVolumeId) > 0">
                <xsl:copy>
                    <xsl:apply-templates select="dtb:volume[@id=$paramVolumeId]"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise />
        </xsl:choose>
    </xsl:template>

    <!-- REARMATTER -->

    <xsl:template match="dtb:rearmatter">
        <xsl:choose>
            <xsl:when test="(($paramRearMatterEnabled or $paramTableOfContentEnabled)
                                    and $paramAllVolumes)
                                or ($paramTableOfContentEnabled and $paramExtendedToc)">
                <xsl:copy>
                    <xsl:apply-templates select="dtb:volume"/>
                </xsl:copy>
            </xsl:when>
            <xsl:when test="($paramRearMatterEnabled or $paramTableOfContentEnabled)
                                and string-length($paramVolumeId) > 0">
                <xsl:copy>
                    <xsl:apply-templates select="dtb:volume[@id=$paramVolumeId]"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise />
        </xsl:choose>
    </xsl:template>

    <!-- VOLUME -->

    <xsl:template match="dtb:volume">
        <xsl:variable name="id"     select="@id" />
        <xsl:variable name="omitted-preceding-siblings"
                      select="preceding-sibling::*[not(self::dtb:volume) and following-sibling::dtb:volume[1][@id=$id]]" />
        <xsl:variable name="omitted-following-siblings"
                      select="following-sibling::*[not(self::dtb:volume) and preceding-sibling::dtb:volume[1][@id=$id] and not(following-sibling::dtb:volume)]" />
        <xsl:copy>
            <xsl:apply-templates select="@*" />

            <!-- Omitted content -->
            <xsl:if test="$omitted-preceding-siblings">
                <dtb:div class="not-in-volume">
                    <xsl:apply-templates select="$omitted-preceding-siblings"/>
                </dtb:div>
            </xsl:if>
            
            <!-- Body -->
            <xsl:variable name="first-child" select="./child::*[not(self::dtb:pagenum or
                                                                    self::dtb:pagebreak or
                                                                    self::dtb:div[@class='omission'])][1]" />
            <xsl:choose>
                <xsl:when test="not($first-child)" />
                <xsl:when test="name($first-child)='dtb:heading'">
                    <xsl:apply-templates select="node()"/>
                </xsl:when>

                <!-- Continued heading -->
                <xsl:otherwise>
                    <xsl:apply-templates select="$first-child/preceding-sibling::*"/>
                    <xsl:apply-templates select="$first-child/preceding::dtb:heading[parent::dtb:volume][1]">
                        <xsl:with-param name="continued-heading" select="true()" />
                    </xsl:apply-templates>
                    <xsl:apply-templates select="$first-child" />
                    <xsl:apply-templates select="$first-child/following-sibling::*" />
                </xsl:otherwise>
            </xsl:choose>

            <!-- Omitted content -->
            <xsl:if test="omitted-following-siblings">
                <dtb:div class="not-in-volume">
                    <xsl:apply-templates select="$omitted-following-siblings"/>
                </dtb:div>
            </xsl:if>

            <!-- Notesection -->
            <xsl:variable name="volume" select="." />
            <xsl:variable name="endnotes" as="element()*">
                <xsl:for-each select="$volume/descendant::dtb:note[@class='endnote']">
                    <xsl:variable name="end-of-section" select="@end-of-section" />
                    <xsl:if test="(string-length($end-of-section)=0)
                            or not($volume/dtb:note-section[@section-name=$end-of-section])">
                        <xsl:sequence select="." />
                    </xsl:if>
                </xsl:for-each>
            </xsl:variable>
            <xsl:if test="$endnotes">
                <xsl:call-template name="note-section">
                    <xsl:with-param name="notes" select="$endnotes" />
                </xsl:call-template>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- NOTES -->

    <xsl:template match="dtb:note">

        <xsl:if test="not(@class='endnote')">
            <xsl:copy>
                <xsl:apply-templates select="@class|node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <!-- NOTESECTION -->

    <xsl:template match="dtb:note-section">

        <xsl:variable name="section-name" select="@section-name" />
        <xsl:variable name="endnotes" select="./parent::*/descendant::dtb:note[@class='endnote' and @end-of-section=$section-name]" />
        <xsl:if test="$endnotes">
            <xsl:call-template name="note-section">
                <xsl:with-param name="notes" select="$endnotes" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="note-section">
        <xsl:param name="notes" />
        
        <dtb:note-section newpage="yes">
            <dtb:heading>
                <dtb:h1 class="dummy">
                    <xsl:value-of select="$paramNoteSectionTitle" />
                </dtb:h1>
            </dtb:heading>
            <xsl:for-each select="$notes">
                <xsl:copy>
                    <xsl:apply-templates select="@class|node()"/>
                </xsl:copy>
            </xsl:for-each>
        </dtb:note-section>
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

    <!-- CONTINUED HEADING -->

    <xsl:template match="dtb:heading|dtb:div[@class='border']">
        <xsl:param name="continued-heading" select="false()" as="xsd:boolean "/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="continued-heading" select="$continued-heading" />
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:h7|dtb:h8|dtb:h9|dtb:h10">
        <xsl:param name="continued-heading" select="false()" as="xsd:boolean "/>
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
            <xsl:if test="$continued-heading">
                <dtb:span class="spaced">
                    <xsl:value-of select="$paramContinuedHeadingSuffix" />
                </dtb:span>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

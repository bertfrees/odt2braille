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
            xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
            xmlns:saxon="http://saxon.sf.net/"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            exclude-result-prefixes="dtb saxon xsd" >

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"/>
                    <!-- saxon:indent-spaces="3" -->

        <!-- XSLT Parameters  -->

        <xsl:param name="paramAutoVolumeBoundaries"  as="xsd:integer*"  select="(1)" />
        <xsl:param name="paramAutoVolumeIds"         as="xsd:string*"   select="('auto-volume-1')" />

        <xsl:variable name="single-volume"      as="element()"   select="//dtb:bodymatter/dtb:volume[1]" />
        <xsl:variable name="number-of-pages"    as="xsd:integer" select="count(//dtb:bodymatter//dtb:pagenum)" />
        <xsl:variable name="number-of-volumes"  as="xsd:integer" select="count($paramAutoVolumeIds)" />


    <xsl:template match="dtb:bodymatter">

        <xsl:copy>
            <xsl:for-each select="$paramAutoVolumeIds">
                <xsl:variable name="i" select="position()" />
                <xsl:variable name="start-element" as="element()">                    
                    <xsl:choose>
                        <xsl:when test="$i=1">
                            <xsl:sequence select="$single-volume/child::*[1]" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="get-element">
                                <xsl:with-param name="page"
                                                select="$paramAutoVolumeBoundaries[$i]" />
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="end-element" as="element()">
                    <xsl:choose>
                        <xsl:when test="$i=$number-of-volumes">
                            <xsl:sequence select="$single-volume/child::*[last()]" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="start-of-next" as="element()">
                                <xsl:call-template name="get-element">
                                    <xsl:with-param name="page"
                                                    select="$paramAutoVolumeBoundaries[$i+1]" />
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:sequence select="$start-of-next/preceding-sibling::*[1]" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <dtb:volume>
                    <xsl:attribute name="id" select="$paramAutoVolumeIds[$i]" />
                    <xsl:variable name="start"
                                  as="xsd:integer"
                                  select="count($start-element/preceding-sibling::*)+1" />
                    <xsl:variable name="end"
                                  as="xsd:integer"
                                  select="count($end-element/preceding-sibling::*)+1" />
                    <xsl:apply-templates select="$single-volume/child::*[position() >= $start and position() &lt;= $end]" />
                </dtb:volume>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>


    <xsl:template name="get-element">
        <xsl:param name="page" as="xsd:integer" />

        <xsl:variable name="headings" as="element()*"
                      select="$single-volume/dtb:heading[descendant::*[
                                     (self::dtb:h1 or self::dtb:h2 or self::dtb:h3 or
                                      self::dtb:h4 or self::dtb:h5 or self::dtb:h6 or
                                      self::dtb:h7 or self::dtb:h8 or self::dtb:h9 or self::dtb:h10)
                                 and not(@dummy)
                                 and count(preceding::dtb:pagenum[ancestor::dtb:bodymatter])=$page]]" />

        <xsl:choose>
            <xsl:when test="$headings[descendant::dtb:h1]">
                <xsl:sequence select="$headings[descendant::dtb:h1][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h2]">
                <xsl:sequence select="$headings[descendant::dtb:h2][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h3]">
                <xsl:sequence select="$headings[descendant::dtb:h3][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h4]">
                <xsl:sequence select="$headings[descendant::dtb:h4][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h5]">
                <xsl:sequence select="$headings[descendant::dtb:h5][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h6]">
                <xsl:sequence select="$headings[descendant::dtb:h6][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h7]">
                <xsl:sequence select="$headings[descendant::dtb:h7][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h8]">
                <xsl:sequence select="$headings[descendant::dtb:h8][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h9]">
                <xsl:sequence select="$headings[descendant::dtb:h9][1]" />
            </xsl:when>
            <xsl:when test="$headings[descendant::dtb:h10]">
                <xsl:sequence select="$headings[descendant::dtb:h10][1]" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$single-volume
                                        /*[self::dtb:pagenum[count(preceding::dtb:pagenum
                                                 [ancestor::dtb:bodymatter])=($page - 1)]
                                        or descendant::dtb:pagenum[count(preceding::dtb:pagenum
                                                 [ancestor::dtb:bodymatter])=($page - 1)]][1]" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

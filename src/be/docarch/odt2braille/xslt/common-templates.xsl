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
            xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
            xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
            xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
            xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
            xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"

            exclude-result-prefixes="xsd style text table fo office" >

            <xsl:variable name="all-styles" select="$styles | $automatic-styles" />


    <xsl:template name="is-caption">
        <xsl:param name="style-name" />

        <xsl:choose>
            <xsl:when test="$style-name='Caption'">
                <xsl:value-of select="true()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="is-caption">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="false()" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="is-paragraph">
        <xsl:param name="node" />

        <xsl:choose>
            <xsl:when test="$node/self::text:h">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:when test="$node/ancestor::table:table">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:when test="$node/ancestor::text:list">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:when test="$node/ancestor::text:note">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:when test="$node/ancestor::text:table-of-content">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:when test="$node/ancestor::text:bibliography">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="is-caption" as="xsd:boolean">
                    <xsl:call-template name="is-caption">
                        <xsl:with-param name="style-name" select="$node/@text:style-name" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$is-caption">
                        <xsl:value-of select="false()" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="true()" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-parent-style-name">
        <xsl:param name="style-name" />
        <xsl:param name="family" select="'paragraph'" />
        <xsl:value-of select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]/@style:parent-style-name" />
    </xsl:template>

    <xsl:template name="get-display-name">
        <xsl:param name="style-name" />
        <xsl:param name="family" select="'paragraph'" />
        <xsl:value-of select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]/@style:display-name" />
    </xsl:template>

    <xsl:template name="get-character-style-name">
        <xsl:param name="style-name" />
        <xsl:choose>
            <xsl:when test="$styles/style:style[@style:name=$style-name and @style:family='text']">
                <xsl:value-of select="$style-name" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="'text'"      />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-character-style-name">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-font-style">
        <xsl:param name="style-name" />
        <xsl:param name="family"     />
        <xsl:variable name="font-style"
                      select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]
                                          /style:text-properties/@fo:font-style" />
        <xsl:choose>
            <xsl:when test="not($font-style='')">
                <xsl:value-of select="$font-style" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-font-style">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                            <xsl:with-param name="family"     select="$family"            />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-font-weight">
        <xsl:param name="style-name" />
        <xsl:param name="family"     />
        <xsl:variable name="font-weight"
                      select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]
                                          /style:text-properties/@fo:font-weight" />
        <xsl:choose>
            <xsl:when test="not($font-weight='')">
                <xsl:value-of select="$font-weight" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-font-weight">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                            <xsl:with-param name="family"     select="$family"            />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-font-variant">
        <xsl:param name="style-name" />
        <xsl:param name="family"     />
        <xsl:variable name="font-variant"
                      select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]
                                          /style:text-properties/@fo:font-variant" />
        <xsl:choose>
            <xsl:when test="not($font-variant='')">
                <xsl:value-of select="$font-variant" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-font-variant">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                            <xsl:with-param name="family"     select="$family"            />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-text-transform">
        <xsl:param name="style-name" />
        <xsl:param name="family"     />
        <xsl:variable name="text-transform"
                      select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]
                                          /style:text-properties/@fo:text-transform" />
        <xsl:choose>
            <xsl:when test="not($text-transform='')">
                <xsl:value-of select="$text-transform" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-text-transform">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                            <xsl:with-param name="family"     select="$family"            />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-underline-style">
        <xsl:param name="style-name" />
        <xsl:param name="family"     />
        <xsl:variable name="underline-style"
                      select="$all-styles//style:style[@style:name=$style-name and @style:family=$family]
                                          /style:text-properties/@style:text-underline-style" />
        <xsl:choose>
            <xsl:when test="not($underline-style='')">
                <xsl:value-of select="$underline-style" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name" />
                        <xsl:with-param name="family"     select="$family"     />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-underline-style">
                            <xsl:with-param name="style-name" select="$parent-style-name" />
                            <xsl:with-param name="family"     select="$family"            />
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="''" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>

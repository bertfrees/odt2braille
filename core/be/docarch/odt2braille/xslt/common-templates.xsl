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
                xmlns:my="http://odt2braille.sf.net">


    <xsl:template name="format-number">
        <xsl:param name="num-in" />
        <xsl:param name="num-format-in"       as="xsd:string"  select="'1'" />
        <xsl:param name="num-format-out"      as="xsd:string"  select="'1'" />
        <xsl:param name="num-letter-sync-in"  as="xsd:boolean" select="false()" />
        <xsl:param name="num-letter-sync-out" as="xsd:boolean" select="false()" />
        <xsl:choose>
            <xsl:when test="$num-format-in=$num-format-out and
                            $num-letter-sync-in=$num-letter-sync-out">
                <xsl:value-of select="$num-in" />
            </xsl:when>
            <xsl:when test="$num-format-in='1' and $num-format-out='a'">
                <xsl:value-of select="my:integer-to-letter($num-in, $num-letter-sync-out)" />
            </xsl:when>
            <xsl:when test="$num-format-in='1' and $num-format-out='A'">
                <xsl:value-of select="upper-case(my:integer-to-letter($num-in, $num-letter-sync-out))" />
            </xsl:when>
            <xsl:when test="$num-format-in='1' and $num-format-out='i'">
                <xsl:value-of select="my:integer-to-roman($num-in)" />
            </xsl:when>
            <xsl:when test="$num-format-in='1' and $num-format-out='I'">
                <xsl:value-of select="upper-case(my:integer-to-roman($num-in))" />
            </xsl:when>
            <xsl:when test="$num-format-in='a' and $num-format-out='A' and
                            $num-letter-sync-in=$num-letter-sync-out">
                <xsl:value-of select="upper-case($num-in)" />
            </xsl:when>
            <xsl:when test="$num-format-in='A' and $num-format-out='a' and 
                            $num-letter-sync-in=$num-letter-sync-out">
                <xsl:value-of select="lower-case($num-in)" />
            </xsl:when>
            <xsl:when test="$num-format-in='i' and $num-format-out='I'">
                <xsl:value-of select="upper-case($num-in)" />
            </xsl:when>
            <xsl:when test="$num-format-in='I' and $num-format-out='i'">
                <xsl:value-of select="lower-case($num-in)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="format-number">
                    <xsl:with-param name="num-in">
                        <xsl:choose>
                            <xsl:when test="$num-format-in='a'">
                                <xsl:value-of select="my:letter-to-integer($num-in, $num-letter-sync-in)" />
                            </xsl:when>
                            <xsl:when test="$num-format-in='A'">
                                <xsl:value-of select="my:letter-to-integer(lower-case($num-in), $num-letter-sync-in)" />
                            </xsl:when>
                            <xsl:when test="$num-format-in='i'">
                                <xsl:value-of select="my:roman-to-integer($num-in)" />
                            </xsl:when>
                            <xsl:when test="$num-format-in='I'">
                                <xsl:value-of select="my:roman-to-integer(lower-case($num-in))" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$num-in" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:with-param>
                    <xsl:with-param name="num-format-in" select="'1'" />
                    <xsl:with-param name="num-format-out" select="$num-format-out" />
                    <xsl:with-param name="num-letter-sync-in" select="false()" />
                    <xsl:with-param name="num-letter-sync-out" select="$num-letter-sync-out" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="first-index-of">
        <xsl:param name="array"/>
        <xsl:param name="value"/>
        <xsl:param name="case-sensitive" as="xsd:boolean" select="true()"/>
        <xsl:variable name="occurences" as="xsd:integer*">
            <xsl:for-each select="$array">
                <xsl:if test="(.=$value) or (not($case-sensitive) and (lower-case(.)=lower-case($value)))">
                    <xsl:sequence select="position()" />
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

    <xsl:template name="array-contains-value">
        <xsl:param name="array"/>
        <xsl:param name="value"/>
        <xsl:variable name="index">
            <xsl:call-template name="first-index-of">
                <xsl:with-param name="array" select="$array"/>
                <xsl:with-param name="value" select="$value"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$index = -1">
                <xsl:value-of select="false()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="true()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="my:integer-to-letter" as="xsd:string">
        <xsl:param name="i"               as="xsd:integer" />
        <xsl:param name="num-letter-sync" as="xsd:boolean" />
        <xsl:choose>
            <xsl:when test="$num-letter-sync">
                <xsl:variable name="r">
                    <xsl:number value="(($i - 1) mod 26) + 1" format="a" />
                </xsl:variable>
                <xsl:value-of select="my:repeat-string($r, (($i - 1) idiv 26) + 1)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:number value="$i" format="a" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="my:integer-to-roman" as="xsd:string">
        <xsl:param name="i" as="xsd:integer" />
        <xsl:number value="$i" format="i" />
    </xsl:function>
    
    <xsl:function name="my:letter-to-integer" as="xsd:integer">
        <xsl:param name="r"               as="xsd:string" />
        <xsl:param name="num-letter-sync" as="xsd:boolean" />
        <xsl:variable name="len" select="string-length($r)" />
        <xsl:choose>
            <xsl:when test="$len=1">
                <xsl:sequence select="string-to-codepoints($r)-string-to-codepoints('a')+1"/>
            </xsl:when>
            <xsl:when test="$len>1">
                <xsl:choose>
                    <xsl:when test="$num-letter-sync">
                        <xsl:sequence select="26 * ($len - 1)
                                                 + my:letter-to-integer(substring($r, $len - 1), $num-letter-sync)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="26 * my:letter-to-integer(substring($r, 0, $len - 1), $num-letter-sync)
                                                 + my:letter-to-integer(substring($r, $len - 1), $num-letter-sync)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="0"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="my:roman-to-integer" as="xsd:integer">
        <xsl:param name="r" as="xsd:string"/>
        <xsl:choose>
            <xsl:when test="ends-with($r,'i')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 1"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'iv')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-2)) + 4"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'v')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 5"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'ix')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-2)) + 9"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'x')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 10"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'xl')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-2)) + 40"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'l')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 50"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'xc')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-2)) + 90"/>
            </xsl:when>            
            <xsl:when test="ends-with($r,'c')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 100"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'cd')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-2)) + 400"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'d')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 500"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'cm')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-2)) + 900"/>
            </xsl:when>
            <xsl:when test="ends-with($r,'m')">
                <xsl:sequence select="my:roman-to-integer(substring($r,1,string-length($r)-1)) + 1000"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:sequence select="0"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="my:repeat-string">
        <xsl:param name="string" as="xsd:string" />
        <xsl:param name="repeat" as="xsd:integer" />
	    <xsl:if test="$repeat > 0">
            <xsl:value-of select="$string"/>
            <xsl:value-of select="my:repeat-string($string, $repeat - 1)" />
	    </xsl:if>
    </xsl:function>
    
</xsl:stylesheet>

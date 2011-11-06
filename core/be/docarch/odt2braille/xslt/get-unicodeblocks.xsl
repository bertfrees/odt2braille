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
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:o2b="http://odt2braille.sf.net"

                exclude-result-prefixes="dtb xsd o2b" >
				
    <xsl:output method="xml"
                encoding="UTF-8"
                media-type="text/xml"
                indent="yes"
                omit-xml-declaration="no"/>

    <xsl:template match="dtb:dtbook">

        <xsl:variable name="u"      select="doc('unicodeblocks.xml')" />
        <xsl:variable name="blocks" select="$u/unicodeblocks/block"   />

        <xsl:variable name="distinct-characters"
                      select="distinct-values(string-to-codepoints(string(.)))" />

        <xsl:variable name="unicodeblocks" as="xsd:string*" >
            <xsl:for-each select="$distinct-characters">
                <xsl:sequence select="o2b:unicodeblock(.,'BASIC_LATIN')" />
            </xsl:for-each>
        </xsl:variable>

        <xsl:variable name="distinct-unicodeblocks"
                      select="distinct-values($unicodeblocks)" />

        <o2b:unicodeblocks>
            <xsl:for-each select="$distinct-unicodeblocks">
                <o2b:block>
                    <xsl:attribute name="name" select="." />
                </o2b:block>
            </xsl:for-each>
        </o2b:unicodeblocks>
		
    </xsl:template>
	
    <xsl:function name="o2b:unicodeblock">
        <xsl:param name="codepoint" />
        <xsl:param name="block" />
        <xsl:variable name="blocks" select="doc('unicodeblocks.xml')/unicodeblocks/block" />
        <xsl:variable name="end"    select="$blocks[@name=$block]/@end" />
        <xsl:choose>
            <xsl:when test="$codepoint > o2b:hex-to-dec($end)">
                <xsl:variable name="nextblock" select="$blocks[@name=$block]/following-sibling::*[1]/@name" />
                <xsl:sequence select="o2b:unicodeblock($codepoint,$nextblock)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$block" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="o2b:hex-to-dec">
        <xsl:param name="x" />
        <xsl:sequence select="(o2b:hex(for $i in string-to-codepoints(upper-case($x)) return if ($i > 64) then $i - 55 else $i - 48))" />
    </xsl:function>

    <xsl:function name="o2b:hex">
        <xsl:param name="x" />
        <xsl:sequence select="if (empty($x)) then 0 else ($x[last()] + 16* o2b:hex($x[position()!=last()]))"/>
    </xsl:function>

</xsl:stylesheet>

<?xml version="1.0" encoding="utf-8"?>
<!--
	
	docbook2odf - DocBook to OpenDocument XSL Transformation
	Copyright (C) 2006 Roman Fordinal
	http://open.comsultia.com/docbook2odf/
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
-->
<xsl:stylesheet
	version="1.0"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0" 
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
	xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
	xmlns:math="http://www.w3.org/1998/Math/MathML"
	xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
	xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
	xmlns:dom="http://www.w3.org/2001/xml-events"
	xmlns:xforms="http://www.w3.org/2002/xforms"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
	office:class="text"
	office:version="1.0">



<xsl:template match="mediaobject">
	<xsl:choose>
		<xsl:when test="parent::para or parent::node()/parent::para">
			<xsl:apply-templates/>
		</xsl:when>
		<xsl:otherwise>
			<text:p>
				<xsl:attribute name="text:style-name">para-default</xsl:attribute>
				<xsl:apply-templates/>
			</text:p>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="imageobject">
	
	<!-- @align                                                  -->
	<!-- @contentwidth                                           -->
	<!-- @contentheight                                          -->
	<!-- @fileref                                                -->
	<!-- @format                                                 -->
	<!-- @scale                                                  -->
	<!-- @scalefit                                               -->
	<!-- @valign                                                 -->
	<!-- @width                                                  -->
	<!-- @depth                                                  -->
	
	<xsl:element name="draw:frame">
		<xsl:if test="parent::inlinemediaobject">
			<xsl:attribute name="draw:style-name">imageobject-inline</xsl:attribute>
		</xsl:if>
		<xsl:if test="parent::mediaobject">
			<xsl:attribute name="draw:style-name">imageobject</xsl:attribute>
		</xsl:if>
		<xsl:attribute name="text:anchor-type">paragraph</xsl:attribute>
		<xsl:attribute name="draw:name">imageobject-<xsl:value-of select="generate-id()"/></xsl:attribute>
		
		<!--<xsl:attribute name="style:rel-width">50%</xsl:attribute>-->
		<!--<xsl:attribute name="style:rel-height">100%</xsl:attribute>-->
		
		<xsl:choose>
			<xsl:when test="imagedata/@width|imagedata/@height">
				<!-- hmmmm.... -->
			</xsl:when>
			<xsl:otherwise>
				<!-- shit, in OpenDocument must be svg:width and height defined :(( -->
				<!-- but I have no data!!!                                          -->
				<!-- I love you Image::Magick!                                      -->
				<xsl:attribute name="svg:width">function:getimage-width:(<xsl:value-of select="imagedata/@fileref"/>)</xsl:attribute>
				<xsl:attribute name="svg:height">function:getimage-height:(<xsl:value-of select="imagedata/@fileref"/>)</xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:attribute name="svg:y"><xsl:value-of select="$para.padding"/></xsl:attribute>
		
		<xsl:attribute name="draw:z-index">1</xsl:attribute>
		<draw:image
			xlink:type="embed"
			xlink:actuate="onLoad">
			<xsl:attribute name="xlink:href"><xsl:value-of select="imagedata/@fileref"/></xsl:attribute>
		</draw:image>
		
	</xsl:element>
	
</xsl:template>


<xsl:template match="screenshot">
	<xsl:apply-templates/>
</xsl:template>


</xsl:stylesheet>
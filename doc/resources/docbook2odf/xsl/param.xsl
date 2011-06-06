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
	
	
	<xsl:param name="style.font-color">#000000</xsl:param>
	<xsl:param name="style.font-name">Arial</xsl:param>
	<xsl:param name="style.font-size.default">11pt</xsl:param>
	<xsl:param name="style.font-size.presentation.para">28pt</xsl:param>
	<xsl:param name="style.font-name.bold">Arial</xsl:param> <!-- or Arial Black -->
	
	<!-- FORMATTING -->
	<xsl:param name="para.padding">0.20cm</xsl:param>
	<xsl:param name="para.padding.odd.left">1cm</xsl:param>
	<xsl:param name="para.text-align">left</xsl:param>
	
	<!-- TOC -->
	<xsl:param name="generate.toc">0</xsl:param> <!-- generating TOC -->
	
	<!-- META -->
	<xsl:param name="generate.meta">1</xsl:param> <!-- generating metadata -->
	
	<!-- LAYOUT -->
	<xsl:param name="page.layout">A4</xsl:param>
	<xsl:param name="page.text-layout"><xsl:value-of select="$page.layout"/></xsl:param>
	<xsl:param name="page.presentation-layout">screen</xsl:param>
	<!--A4-->
	<xsl:param name="layout.A4.units">cm</xsl:param>
	<xsl:param name="layout.A4.width">21</xsl:param>
	<xsl:param name="layout.A4.height">29.7</xsl:param>
	<xsl:param name="layout.A4.margin-left">1.75</xsl:param>
	<xsl:param name="layout.A4.margin-right">2.00</xsl:param>
	<xsl:param name="layout.A4.margin-top">0.97</xsl:param>
	<xsl:param name="layout.A4.margin-bottom">0.61</xsl:param>
	<xsl:param name="layout.A4.header.min-height">0.5</xsl:param>
	<xsl:param name="layout.A4.header.margin-left">0</xsl:param>
	<xsl:param name="layout.A4.header.margin-right">0</xsl:param>
	<xsl:param name="layout.A4.header.margin-top">0</xsl:param>
	<xsl:param name="layout.A4.header.margin-bottom">0</xsl:param>
	<xsl:param name="layout.A4.footer.min-height">1.2</xsl:param>
	<xsl:param name="layout.A4.footer.margin-left">0</xsl:param>
	<xsl:param name="layout.A4.footer.margin-right">0</xsl:param>
	<xsl:param name="layout.A4.footer.margin-top">0.5</xsl:param>
	<xsl:param name="layout.A4.footer.margin-bottom">0</xsl:param>
	<xsl:param name="layout.A4.footer.border-top">0.002cm solid #000000</xsl:param>
	<!--A5-->
	<xsl:param name="layout.A5.units">cm</xsl:param>
	<xsl:param name="layout.A5.width">14.8</xsl:param>
	<xsl:param name="layout.A5.height">21.0</xsl:param>
	<xsl:param name="layout.A5.margin-left">1.75</xsl:param>
	<xsl:param name="layout.A5.margin-right">2.00</xsl:param>
	<xsl:param name="layout.A5.margin-top">0.97</xsl:param>
	<xsl:param name="layout.A5.margin-bottom">0.61</xsl:param>
	<xsl:param name="layout.A5.header.min-height">1.5</xsl:param>
	<xsl:param name="layout.A5.header.margin-left">0</xsl:param>
	<xsl:param name="layout.A5.header.margin-right">0</xsl:param>
	<xsl:param name="layout.A5.header.margin-top">0</xsl:param>
	<xsl:param name="layout.A5.header.margin-bottom">0.5</xsl:param>
	<xsl:param name="layout.A5.footer.min-height">1.5</xsl:param>
	<xsl:param name="layout.A5.footer.margin-left">0</xsl:param>
	<xsl:param name="layout.A5.footer.margin-right">0</xsl:param>
	<xsl:param name="layout.A5.footer.margin-top">0.5</xsl:param>
	<xsl:param name="layout.A5.footer.margin-bottom">0</xsl:param>
	<xsl:param name="layout.A5.footer.border-top">0.002cm solid #000000</xsl:param>
	<!--screen-->
	<!--...-->
	
</xsl:stylesheet>


















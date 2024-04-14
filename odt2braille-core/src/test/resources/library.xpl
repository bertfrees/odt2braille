<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:da="http://docarch.be/">
	
	<p:declare-step type="da:odt2braille" >
		<p:option name="source" required="true"/>
		<p:option name="liblouis-dir" required="true"/>
		<p:input port="config" sequence="true"/>
		<p:option name="result-brf" required="false"/>
		<p:output port="result"/>
	</p:declare-step>
	
</p:library>

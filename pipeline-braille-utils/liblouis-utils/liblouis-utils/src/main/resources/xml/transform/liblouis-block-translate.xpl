<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="louis:block-translate" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:louis="http://liblouis.org/liblouis"
                exclude-inline-prefixes="#all">
	
	
	<p:input port="source"/>
	<p:output port="result" primary="true"/>
	<p:output port="resource-map" sequence="true">
		<p:pipe step="transform" port="resource-map"/>
	</p:output>
	
	<p:option name="query" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-template.xpl"/>
	
	<css:block-translate name="transform">
		<p:input port="translator">
			<p:document href="liblouis-block-translate.xsl"/>
		</p:input>
		<p:with-param name="query" select="$query"/>
	</css:block-translate>
	
</p:declare-step>

<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:transform"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all">
	
	<p:input port="source"/>
	
	<p:output port="result" primary="true">
		<p:pipe step="result" port="result"/>
	</p:output>
	<p:output port="resource-map" sequence="true">
		<p:pipe step="resource-map" port="result"/>
	</p:output>
	
	<p:option name="query" required="true"/>
	<p:option name="type" required="true"/>
	<p:option name="temp-dir" required="false" select="''"/>
	
	<p:declare-step type="pxi:transform">
		<p:input port="source"/>
		<p:output port="result" sequence="true"/>
		<p:option name="query" required="true"/>
		<p:option name="type" required="true"/>
		<p:option name="temp-dir" required="false"/>
	</p:declare-step>
	
	<p:wrap match="/*" wrapper="cx:document"/>
	<p:add-attribute match="/*" attribute-name="port" attribute-value="source"/>
	
	<p:choose>
		<p:when test="p:value-available('temp-dir')">
			<pxi:transform>
				<p:with-option name="query" select="$query"/>
				<p:with-option name="type" select="$type"/>
				<p:with-option name="temp-dir" select="$temp-dir"/>
			</pxi:transform>
		</p:when>
		<p:otherwise>
			<pxi:transform>
				<p:with-option name="query" select="$query"/>
				<p:with-option name="type" select="$type"/>
			</pxi:transform>
		</p:otherwise>
	</p:choose>
	<p:identity name="transform"/>
	
	<p:for-each name="result">
		<p:output port="result"/>
		<p:iteration-source select="/cx:document[@port='result']/*"/>
		<p:identity/>
	</p:for-each>
	
	<p:for-each name="resource-map">
		<p:output port="result"/>
		<p:iteration-source select="/cx:document[@port='resource-map']/*">
			<p:pipe step="transform" port="result"/>
		</p:iteration-source>
		<p:identity/>
	</p:for-each>
	
</p:declare-step>

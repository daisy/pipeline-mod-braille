<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="css:inline" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-inline-prefixes="#all">
	
	<p:input port="source" primary="true"/>
	<p:input port="context" sequence="true">
		<p:empty/>
	</p:input>
	<p:output port="result"/>
	<p:option name="default-stylesheet" required="false" select="''"/>
	
	<p:declare-step type="pxi:css-inline">
		<p:input port="source" primary="true"/>
		<p:input port="context" sequence="true"/>
		<p:output port="result"/>
		<p:option name="default-stylesheet"/>
	</p:declare-step>
	
	<pxi:css-inline>
		<p:input port="context">
			<p:pipe step="main" port="context"/>
		</p:input>
		<p:with-option name="default-stylesheet" select="$default-stylesheet"/>
	</pxi:css-inline>
	
</p:declare-step>

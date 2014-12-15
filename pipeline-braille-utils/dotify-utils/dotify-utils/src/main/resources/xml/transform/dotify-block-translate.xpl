<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="dotify:block-translate" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
            xmlns:dotify="http://code.google.com/p/dotify/"
            exclude-inline-prefixes="#all">
	
	<p:option name="query" select="''"/>
	
	<p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-template.xpl"/>
	
	<css:block-translate>
		<p:input port="translator">
			<p:document href="dotify-block-translate.xsl"/>
		</p:input>
		<p:with-param name="query" select="$query"/>
	</css:block-translate>
	
</p:pipeline>

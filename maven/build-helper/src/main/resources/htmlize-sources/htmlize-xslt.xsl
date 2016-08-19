<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:param name="input-base-uri"/>
	<xsl:param name="output-base-uri"/>
	
	<xsl:include href="serialize.xsl"/>
	<xsl:include href="../xprocdoc/uri-functions.xsl"/>
	
	<xsl:output name="html" method="html"/>
	
	<xsl:variable name="output-uri" select="concat(resolve-uri(pf:relativize-uri(base-uri(/*),$input-base-uri),$output-base-uri), '.html')"/>
	
	<xsl:template match="/">
		<xsl:result-document format="html" href="{$output-uri}">
			<html prefix="dp2: http://www.daisy.org/ns/pipeline/">
				<head>
					<link rev="dp2:doc" href="{pf:relativize-uri(base-uri(/*),$output-uri)}"/>
					<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/source"/>
					<style type="text/css">
						.code {
						  white-space: pre;
						  font-family: monospace;
						}
						.code-xml-element-local-name {
						  color: #1d58d0;
						}
						.code-xml-attribute-local-name {
						  color: #dca000;
						}
						.code-xml-attribute-value {
						  color: #2994a1;
						}
						.code-xml-element-prefix,
						.code-xml-attribute-prefix {
						  color: #9030b0;
						}
					</style>
				</head>
				<body>
					<div class="code">
						<xsl:apply-templates select="/*" mode="serialize"/>
					</div>
				</body>
			</html>
		</xsl:result-document>
	</xsl:template>
	
	<xsl:template match="xsl:import/@href|
	                     xsl:include/@href"
	              mode="attribute-value">
		<a href="{pf:relativize-uri(resolve-uri(.,base-uri(/*)),$output-uri)}" class="source">
			<xsl:value-of select="."/>
		</a>
	</xsl:template>
	
</xsl:stylesheet>

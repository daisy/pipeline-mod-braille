<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:louis="http://liblouis.org/liblouis"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">
	
	<xsl:param name="query"/>
	
	<xsl:template match="css:block" mode="#default">
		<xsl:param name="source-path" as="xs:integer*" required="yes"/>
		<xsl:param name="result-path" as="xs:integer*" required="yes"/>
		<xsl:variable name="new-text-nodes" as="xs:string*">
			<xsl:call-template name="translate-text-nodes"/>
		</xsl:variable>
		<xsl:apply-templates select="node()[1]" mode="treewalk">
			<xsl:with-param name="source-path" select="$source-path"/>
			<xsl:with-param name="result-path" select="$result-path"/>
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="css:block" mode="before after string-set">
		<xsl:variable name="new-text-nodes" as="xs:string*">
			<xsl:call-template name="translate-text-nodes"/>
		</xsl:variable>
		<xsl:value-of select="string-join($new-text-nodes,'')"/>
	</xsl:template>
	
	<xsl:template name="translate-text-nodes" as="xs:string*">
		<xsl:variable name="text" as="text()*" select=".//text()"/>
		<xsl:variable name="style" as="xs:string*">
			<xsl:for-each select="$text">
				<xsl:variable name="inline-style" as="element()*"
				              select="css:computed-properties($inline-properties, true(), parent::*)"/>
				<xsl:sequence select="css:serialize-declaration-list($inline-style[not(@value=css:initial-value(@name))])"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:sequence select="louis:translate(concat($query,'(locale:',string(@xml:lang),')'), $text, $style)"/>
	</xsl:template>
	
	<xsl:template match="css:property[@name=('text-transform','font-style','font-weight','text-decoration','color')]"
	              mode="translate-declaration-list"/>
	
	<xsl:template match="css:property[@name='hyphens' and @value='auto']" mode="translate-declaration-list">
		<xsl:sequence select="css:property('hyphens','manual')"/>
	</xsl:template>
	
</xsl:stylesheet>

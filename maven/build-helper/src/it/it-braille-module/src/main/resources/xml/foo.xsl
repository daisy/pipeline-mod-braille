<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/foo-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>

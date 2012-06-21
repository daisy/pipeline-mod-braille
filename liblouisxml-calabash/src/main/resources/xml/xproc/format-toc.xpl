<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="lblxml:format-toc" name="format-toc"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:brl="http://www.daisy.org/ns/pipeline/braille"
    xmlns:lblxml="http://xmlcalabash.com/ns/extensions/liblouisxml"
    version="1.0">
    
    <p:input port="source" sequence="false" primary="true"/>
    <p:input port="toc-styles" sequence="false"/>
    <p:input port="config-files" sequence="false"/>
    <p:input port="semantic-files" sequence="false"/>
    <p:option name="temp-dir" required="true"/>
    <p:output port="result" sequence="false" primary="true"/>
    
    <!-- TODO: support multiple TOCs / indexes -->
    
    <p:import href="update-toc.xpl"/>
    <p:import href="store-files.xpl"/>
    
    <p:choose>
        <p:when test="//lblxml:toc">
            
            <p:xslt>
                <p:input port="source">
                    <p:pipe step="format-toc" port="toc-styles"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../xslt/create-toc-styles-cfg-file.xsl"/>
                </p:input>
                <p:with-param name="toc-title-style" select="string(//lblxml:toc[1]//lblxml:toc-title/@brl:style)">
                    <p:pipe step="format-toc" port="source"/>
                </p:with-param>
                <p:with-param name="toc-item-styles" select="distinct-values(//lblxml:toc[1]//lblxml:toc-item/@brl:style)">
                    <p:pipe step="format-toc" port="source"/>
                </p:with-param>
            </p:xslt>
            
            <lblxml:store-files name="config-files">
                <p:input port="directory">
                    <p:pipe step="format-toc" port="config-files"/>
                </p:input>
            </lblxml:store-files>
            
            <p:xslt template-name="initial">
                <p:input port="stylesheet">
                    <p:document href="../xslt/create-toc-styles-sem-file.xsl"/>
                </p:input>
                <p:with-param name="toc-item-styles" select="distinct-values(//lblxml:toc[1]//lblxml:toc-item/@brl:style)">
                    <p:pipe step="format-toc" port="source"/>
                </p:with-param>
            </p:xslt>
            
            <lblxml:store-files name="semantic-files">
                <p:input port="directory">
                    <p:pipe step="format-toc" port="semantic-files"/>
                </p:input>
            </lblxml:store-files>
            
            <lblxml:update-toc>
                <p:input port="source">
                    <p:pipe step="format-toc" port="source"/>
                </p:input>
                <p:input port="config-files">
                    <p:pipe step="config-files" port="result"/>
                </p:input>
                <p:input port="semantic-files">
                    <p:pipe step="semantic-files" port="result"/>
                </p:input>
                <p:with-option name="temp-dir" select="$temp-dir">
                    <p:empty/>
                </p:with-option>
            </lblxml:update-toc>
        </p:when>
        
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
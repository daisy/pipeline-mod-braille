<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xslt-delete" exclude-inline-prefixes="#all" version="1.0" name="main"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc">
    
    <p:documentation>XSLT implementation of p:delete.</p:documentation>
    <!-- NOTE: namespaces in XPath expressions refer to those in the source document, not in the XProc script invoking this step. -->
    
    <p:input port="source" primary="true"/>
    <p:input port="namespaces" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result"/>
    <p:option name="match" required="true"/> <!-- XSLTMatchPattern -->
    
    <p:variable name="match-variable" select="$match"/>

    <p:identity name="template">
        <p:input port="source">
            <p:inline>
                <xsl:stylesheet version="2.0">
                    <xsl:template match="@*|node()">
                        <xsl:copy>
                            <xsl:apply-templates select="@*|node()"/>
                        </xsl:copy>
                    </xsl:template>
                    <xsl:template/>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:identity>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="source">
            <p:pipe port="result" step="template"/>
            <p:pipe port="source" step="main"/>
            <p:pipe port="namespaces" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet version="2.0">
                    <xsl:template match="/*">
                        <xsl:copy>
                            <xsl:namespace name="p" select="'http://www.w3.org/ns/xproc'"/>
                            <xsl:copy-of select="collection()/*//namespace::*"/>
                            <xsl:copy-of select="@*|node()"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <p:add-attribute match="xsl:template[last()]" attribute-name="match">
        <!-- for some reason $match here refers to the match attribute of this p:add-attribute, which is why it's renamed to $match-variable -->
        <p:with-option name="attribute-value" select="$match-variable"/>
    </p:add-attribute>
    <p:identity name="xslt"/>

    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:pipe port="result" step="xslt"/>
        </p:input>
    </p:xslt>

</p:declare-step>

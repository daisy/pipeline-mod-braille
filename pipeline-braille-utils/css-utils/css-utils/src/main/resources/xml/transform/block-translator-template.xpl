<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="css:block-translate" name="main" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-inline-prefixes="#all">
    
    <p:input port="source" primary="true"/>
    <p:input port="translator"/>
    <p:input port="parameters" kind="parameter"/>
    
    <p:output port="result" primary="true"/>
    <p:output port="resource-map" sequence="true">
        <p:pipe step="resource-map" port="result"/>
    </p:output>
    
    <p:add-attribute match="xsl:import" attribute-name="href" name="import">
        <p:input port="source">
            <p:inline>
                <xsl:import/>
            </p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="resolve-uri('block-translator-template.xsl')">
            <p:inline>
                <irrelevant/>
            </p:inline>
        </p:with-option>
    </p:add-attribute>
    
    <p:insert name="stylesheet" position="first-child">
        <p:input port="source">
            <p:pipe step="main" port="translator"/>
        </p:input>
        <p:input port="insertion">
            <p:pipe step="import" port="result"/>
        </p:input>
    </p:insert>
    
    <p:xslt name="transform">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:input port="stylesheet">
            <p:pipe step="stylesheet" port="result"/>
        </p:input>
        <p:input port="parameters">
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </p:xslt>
    
    <p:wrap-sequence wrapper="d:resource-map" name="resource-map">
        <p:input port="source" select="//d:sync"/>
    </p:wrap-sequence>
    
    <p:delete match="d:sync">
        <p:input port="source">
            <p:pipe step="transform" port="result"/>
        </p:input>
    </p:delete>
    
</p:declare-step>

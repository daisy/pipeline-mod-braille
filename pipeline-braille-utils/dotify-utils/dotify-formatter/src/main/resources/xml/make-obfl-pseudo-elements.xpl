<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:make-obfl-pseudo-elements"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:input port="source"/>
    <p:output port="result" sequence="true"/>
    
    <p:xslt name="xslt" px:progress="1">
        <p:input port="stylesheet">
            <p:document href="make-obfl-pseudo-elements.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="xslt" port="result"/>
            <p:pipe step="xslt" port="secondary"/>
        </p:input>
    </p:identity>
    
</p:declare-step>

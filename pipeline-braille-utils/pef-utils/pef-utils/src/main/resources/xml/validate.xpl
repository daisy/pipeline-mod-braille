<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0"
        xmlns:p="http://www.w3.org/ns/xproc"
        xmlns:pef="http://www.daisy.org/ns/2008/pef"
        xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
        xmlns:l="http://xproc.org/library"
        exclude-inline-prefixes="#all"
        type="pef:validate"
        name="main">
    
    <p:input port="source" primary="true"/>
    
    <p:output port="result" primary="true"/>
    <p:output port="html-report">
      <p:pipe step="html-report" port="result"/>
    </p:output>
    
    <p:option name="assert-valid" required="false" select="'false'"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>
    
    <p:variable name="document-type" select="'PEF'"/>
    <p:variable name="base-uri" select="base-uri()"/>
    <p:variable name="document-name" select="tokenize($base-uri, '/')[last()]"/>
    <p:variable name="document-path" select="'test'"/> <!-- TODO -->

    <l:relax-ng-report name="validate-against-relaxng">
      <p:input port="schema">
        <p:document href="schema/pef-2008-1.rng"/>
      </p:input>
      <p:input port="source">
        <p:pipe step="main" port="source"/>
      </p:input>
    </l:relax-ng-report>

    <px:combine-validation-reports name="combined-error-report">
      <p:with-option name="document-type" select="$document-type"/>
      <p:with-option name="document-name" select="$document-name"/>
      <p:with-option name="document-path" select="$document-path"/>
      <p:input port="source">
        <p:pipe port="result" step="validate-against-relaxng"/>
      </p:input>
    </px:combine-validation-reports>

    <px:validation-report-to-html name="html-report">
      <p:input port="source">
        <p:pipe port="result" step="combined-error-report"/>
      </p:input>
    </px:validation-report-to-html>
    
</p:declare-step>

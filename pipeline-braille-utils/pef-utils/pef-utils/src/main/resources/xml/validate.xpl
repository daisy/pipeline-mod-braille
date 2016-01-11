<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0"
        xmlns:p="http://www.w3.org/ns/xproc"
        xmlns:pef="http://www.daisy.org/ns/2008/pef"
	xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
        xmlns:d="http://www.daisy.org/ns/pipeline/data"
        exclude-inline-prefixes="#all"
        type="pef:validate"
        name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>pef:validate</h1>
        <p>Validates a PEF-file using RelaxNG and schematron rules.</p>
    </p:documentation>
    
    <p:input port="source" primary="true"/>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output from this step is a copy of the input; may include PSVI annotations.</p>
        </p:documentation>
    </p:output>
    
    <p:output port="report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Schema assertations and reports.</p>
        </p:documentation>
        <p:pipe step="error-report" port="result"/>
    </p:output>
    
    <!--
        TODO: add output ports `html-report` (use px:px:validation-report-to-html)
        and `validation-status` (use px:validation-status)
    -->

    <p:output port="html-report">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>HTML validation report.</p>
      </p:documentation>
      <p:pipe step="html-report" port="result"/>
    </p:output>
    
    <p:option name="assert-valid" required="false" select="'true'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>It is a dynamic error if the assert-valid option is true and the input document is not valid.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>
    
    <!--
        TODO: use l:relax-ng-report
    -->
    <p:try name="validate-with-relax-ng">
        <p:group>
            <p:output port="result" primary="true"/>
            <p:output port="report" sequence="true">
                <p:empty/>
            </p:output>
            <p:validate-with-relax-ng assert-valid="true">
                <p:input port="source">
                    <p:pipe step="main" port="source"/>
                </p:input>
                <p:input port="schema">
                    <p:document href="schema/pef-2008-1.rng"/>
                </p:input>
            </p:validate-with-relax-ng>
        </p:group>
        <p:catch name="catch">
            <p:output port="result" primary="true"/>
            <p:output port="report" sequence="true">
                <p:pipe step="copy-errors" port="result"/>
            </p:output>
            <p:identity name="copy-errors">
                <p:input port="source">
                    <p:pipe step="catch" port="error"/>
                </p:input>
            </p:identity>
            <p:sink/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="main" port="source"/>
                </p:input>
            </p:identity>
        </p:catch>
    </p:try>
    
    <p:try name="validate-with-schematron">
        <p:group>
            <p:output port="result" primary="true"/>
            <p:output port="report" sequence="true">
                <p:empty/>
            </p:output>
            <p:validate-with-schematron assert-valid="true">
                <p:input port="schema">
                    <p:document href="schema/pef-2008-1.sch"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:validate-with-schematron>
        </p:group>
        <p:catch name="catch">
            <p:output port="result" primary="true"/>
            <p:output port="report" sequence="true">
                <p:pipe step="validate" port="report"/>
            </p:output>
            <!--
                Validate again with assert-valid=false to get the report.
            -->
            <p:validate-with-schematron name="validate" assert-valid="false">
                <p:input port="schema">
                    <p:document href="schema/pef-2008-1.sch"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:validate-with-schematron>
        </p:catch>
    </p:try>
    
    <p:identity name="copy-of-input"/>
    <p:sink/>
    
    <!--
        TODO: use px:combine-validation-reports
    -->
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="validate-with-relax-ng" port="report"/>
            <p:pipe step="validate-with-schematron" port="report"/>
        </p:iteration-source>
        <p:wrap match="/" wrapper="d:report"/>
    </p:for-each>
    
    <p:wrap-sequence name="error-report" wrapper="d:reports"/>
    <px:validation-report-to-html name="html-report">
      <p:input port="source">
        <p:pipe port="result" step="error-report"/>
      </p:input>
    </px:validation-report-to-html>

    <p:choose name="assert-valid">
        <p:when test="$assert-valid='true' and count(/*/*)&gt;0">
            <p:output port="result"/>
            <p:error code="error">
                <p:input port="source">
                    <p:pipe step="error-report" port="result"/>
                </p:input>
            </p:error>
        </p:when>
        <p:otherwise>
            <p:output port="result"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="copy-of-input" port="result"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>

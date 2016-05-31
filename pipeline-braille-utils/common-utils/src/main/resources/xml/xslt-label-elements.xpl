<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:xslt-label-elements" version="1.0" name="main"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="">
    
    <p:documentation>XSLT implementation of p:label-elements.</p:documentation>
    <!-- NOTE: namespaces in XPath expressions refer to those in the source document, not in the XProc script invoking this step. -->
    
    <p:input port="source" primary="true"/>
    <p:input port="namespaces" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result"/>
    <p:option name="attribute" select="'xml:id'"/>                          <!-- QName -->
    <p:option name="attribute-prefix" select="''"/>                         <!-- NCName -->
    <p:option name="attribute-namespace" select="''"/>                      <!-- anyURI -->
    <p:option name="label" select="'concat(&quot;_&quot;,$p:index)'"/>      <!-- XPathExpression -->
    <p:option name="match" select="'*'"/>                                   <!-- XSLTMatchPattern -->
    <p:option name="replace" select="'true'"/>                              <!-- boolean -->
    
    <p:variable name="match-variable" select="$match"/>
    
    <p:identity name="template">
        <p:input port="source">
            <p:inline>
                <xsl:stylesheet version="2.0">
                    <xsl:param name="attribute" required="yes"/>
                    <xsl:param name="attribute-prefix"/>
                    <xsl:param name="attribute-namespace"/>
                    <xsl:param name="replace" required="yes"/>
                    <xsl:template match="@*|node()">
                        <xsl:copy>
                            <xsl:apply-templates select="@*|node()"/>
                        </xsl:copy>
                    </xsl:template>
                    <xsl:template>
                        <xsl:variable name="p:index">
                            <xsl:number/>
                        </xsl:variable>
                        <xsl:copy>
                            <xsl:copy-of select="@*|namespace::*"/>
                            <xsl:if test="$replace = 'true' or not(@*[local-name()=tokenize($attribute,':')[last()] and (if ($attribute-namespace) then ($attribute-namespace = string(namespace-uri-for-prefix(if (contains($attribute,':')) then tokenize($attribute,':')[1] else string($attribute-prefix),/*))) else true())])">
                                <xsl:choose>
                                    <xsl:when test="$attribute-prefix and not(contains($attribute,':')) and namespace-uri-for-prefix($attribute-prefix,.) = '' or namespace-uri-for-prefix($attribute-prefix,.) = $attribute-namespace">
                                        <xsl:attribute name="{concat($attribute-prefix,':',$attribute)}"/>
                                    </xsl:when>
                                    <xsl:when test="$attribute-namespace">
                                        <xsl:attribute name="{$attribute}" namespace="{$attribute-namespace}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:attribute name="{$attribute}"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                            <xsl:apply-templates select="node()"/>
                        </xsl:copy>
                    </xsl:template>
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
    <p:add-attribute match="xsl:attribute" attribute-name="select">
        <p:with-option name="attribute-value" select="$label"/>
    </p:add-attribute>
    <p:identity name="xslt"/>
    
    <p:xslt>
        <p:with-param name="attribute" select="$attribute"/>
        <p:with-param name="attribute-prefix" select="$attribute-prefix"/>
        <p:with-param name="attribute-namespace" select="$attribute-namespace"/>
        <p:with-param name="replace"  select="$replace"/>
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:pipe port="result" step="xslt"/>
        </p:input>
    </p:xslt>
    
</p:declare-step>

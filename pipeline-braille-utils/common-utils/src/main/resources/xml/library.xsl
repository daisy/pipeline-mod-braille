<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema">
    
    <!--
        Import this library whenever one of the following functions is used:
        * pf:text-transform
        * pf:message
        * brl:unicode-braille-to-escape-sequence
        * brl:unicode-braille-to-nabcc
        * brl:nabcc-to-unicode-braille
    -->
    
    <xsl:import href="encoding-functions.xsl"/>
    
    <xsl:function name="pf:error">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:error($msg, ())"/>
    </xsl:function>
    
    <xsl:function name="pf:error">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('ERROR', $msg, $args)"/>
    </xsl:function>
    
    <xsl:template name="pf:error">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:error($msg, $args)"/>
    </xsl:template>
    
    <xsl:function name="pf:warn">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:warn($msg, ())"/>
    </xsl:function>
    
    <xsl:function name="pf:warn">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('WARN', $msg, $args)"/>
    </xsl:function>
    
    <xsl:template name="pf:warn">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:warn($msg, $args)"/>
    </xsl:template>
    
    <xsl:function name="pf:info">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:info($msg, ())"/>
    </xsl:function>
    
    <xsl:function name="pf:info">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('INFO', $msg, $args)"/>
    </xsl:function>
    
    <xsl:template name="pf:info">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:info($msg, $args)"/>
    </xsl:template>
    
    <xsl:function name="pf:debug">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:debug($msg, ())"/>
    </xsl:function>
    
    <xsl:function name="pf:debug">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('DEBUG', $msg, $args)"/>
    </xsl:function>
    
    <xsl:template name="pf:debug">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:debug($msg, $args)"/>
    </xsl:template>
    
    <xsl:function name="pf:trace">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:trace($msg, ())"/>
    </xsl:function>
    
    <xsl:function name="pf:trace">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('TRACE', $msg, $args)"/>
    </xsl:function>
    
    <xsl:template name="pf:trace">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:trace($msg, $args)"/>
    </xsl:template>
    
</xsl:stylesheet>

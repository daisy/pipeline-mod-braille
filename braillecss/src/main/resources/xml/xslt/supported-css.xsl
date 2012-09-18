<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    xmlns:my="http://github.com/bertfrees"
    exclude-result-prefixes="xs css my"
    version="2.0">
    
    <xsl:variable name="properties" as="xs:string*"
        select="('display',
                 'margin-left',
                 'margin-right',
                 'margin-top',
                 'margin-bottom',
                 'padding-left',
                 'padding-right',
                 'padding-bottom',
                 'padding-top',
                 'border-left',
                 'border-right',
                 'border-bottom',
                 'border-top',
                 'text-indent',
                 'list-style-type',
                 'text-align',
                 'page-break-before',
                 'page-break-after',
                 'page-break-inside',
                 'orphans',
                 'widows')"/>

    <xsl:variable name="ALWAYS" select="'always'"/>
    <xsl:variable name="ATTR" select="'attr\(.+?\)'"/>
    <xsl:variable name="AUTO" select="'auto'"/>
    <xsl:variable name="AVOID" select="'avoid'"/>
    <xsl:variable name="BLOCK" select="'block'"/>
    <xsl:variable name="CONTENT" select="'content\(\)'"/>
    <xsl:variable name="CENTER" select="'center'"/>
    <xsl:variable name="DECIMAL" select="'decimal'"/>
    <xsl:variable name="DOT_PATTERN" select="'\p{IsBraillePatterns}'"/>
    <xsl:variable name="IDENT" select="'\p{L}|_(\p{L}|_|-)*'"/>
    <xsl:variable name="INHERIT" select="'inherit'"/>
    <xsl:variable name="INLINE" select="'inline'"/>
    <xsl:variable name="INTEGER" select="'(0|-?[1-9][0-9]*)(\.0*)?'"/>
    <xsl:variable name="JUSTIFY" select="'justify'"/>
    <xsl:variable name="LEFT" select="'left'"/>
    <xsl:variable name="LIST_ITEM" select="'list-item'"/>
    <xsl:variable name="LOWER_ALPHA" select="'lower-alpha'"/>
    <xsl:variable name="LOWER_ROMAN" select="'lower-roman'"/>
    <xsl:variable name="NATURAL_NUMBER" select="'(0|[1-9][0-9]*)(\.0*)?'"/>
    <xsl:variable name="NONE" select="'none'"/>
    <xsl:variable name="RIGHT" select="'right'"/>
    <xsl:variable name="STRING">'.+?'|".+?"</xsl:variable>
    <xsl:variable name="TOC" select="'toc'"/>
    <xsl:variable name="TOC_ITEM" select="'tic-item'"/>
    <xsl:variable name="UPPER_ALPHA" select="'upper-alpha'"/>
    <xsl:variable name="UPPER_ROMAN" select="'upper-roman'"/>
    
    <xsl:variable name="valid-properties" as="xs:string*"
        select="(concat('^', $BLOCK, '|', $INLINE, '|', $LIST_ITEM, '|', $NONE, '|', $TOC, '|', $TOC_ITEM, '$'),
                 concat('^', $INTEGER, '$'),
                 concat('^', $INTEGER, '$'),
                 concat('^', $NATURAL_NUMBER, '$'),
                 concat('^', $NATURAL_NUMBER, '$'),
                 concat('^', $NATURAL_NUMBER, '$'),
                 concat('^', $NATURAL_NUMBER, '$'),
                 concat('^', $NATURAL_NUMBER, '$'),
                 concat('^', $NATURAL_NUMBER, '$'),
                 concat('^', $DOT_PATTERN, '|', $NONE, '$'),
                 concat('^', $DOT_PATTERN, '|', $NONE, '$'),
                 concat('^', $DOT_PATTERN, '|', $NONE, '$'),
                 concat('^', $DOT_PATTERN, '|', $NONE, '$'),
                 concat('^', $INTEGER, '|', $INHERIT, '$'),
                 concat('^', $DOT_PATTERN, '|', $DECIMAL, '|', $LOWER_ALPHA, '|', $LOWER_ROMAN, '|', $NONE, '|', $UPPER_ALPHA, '|', $UPPER_ROMAN, '|', $INHERIT, '$'),
                 concat('^', $CENTER, '|', $JUSTIFY, '|', $LEFT, '|', $RIGHT, '|', $INHERIT, '$'),
                 concat('^', $ALWAYS, '|', $AUTO, '|', $AVOID, '|', $LEFT, '|', $RIGHT, '|', $INHERIT, '$'),
                 concat('^', $ALWAYS, '|', $AUTO, '|', $AVOID, '|', $LEFT, '|', $RIGHT, '|', $INHERIT, '$'),
                 concat('^', $AUTO, '|', $AVOID, '|', $INHERIT, '$'),
                 concat('^', $INTEGER, '|', $INHERIT, '$'),
                 concat('^', $INTEGER, '|', $INHERIT, '$'))"/>
    
    <xsl:variable name="applies-to" as="xs:string*"
        select="('.*',
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '|', $TOC_ITEM, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '|', $TOC_ITEM, '$'),
                 concat('^', $LIST_ITEM, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'),
                 concat('^', $BLOCK, '|', $LIST_ITEM, '|', $TOC, '$'))"/>
    
    <xsl:variable name="default-values" as="xs:string*"
        select="('inline',
                 '0.0',
                 '0.0',
                 '0.0',
                 '0.0',
                 '0.0',
                 '0.0',
                 '0.0',
                 '0.0',
                 'none',
                 'none',
                 'none',
                 'none',
                 '0.0',
                 'none',
                 'left',
                 'auto',
                 'auto',
                 'auto',
                 '2.0',
                 '2.0')"/>
    
    <xsl:variable name="inherited-properties" as="xs:string*"
        select="('-brl-text-indent',
                 '-brl-list-style-type',
                 'text-align',
                 'orphans',
                 'widows')"/>

    <xsl:function name="css:get-properties" as="xs:string*">
        <xsl:sequence select="$properties"/>
    </xsl:function>

    <xsl:function name="css:is-property" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:sequence select="boolean(index-of($properties, $property))"/>
    </xsl:function>

    <xsl:function name="css:is-valid-property" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="value" as="xs:string"/>
        <xsl:variable name="index" select="my:index-of($properties, $property)"/>
        <xsl:sequence select="if ($index) then matches($value, $valid-properties[$index]) else false()"/>
    </xsl:function>

    <xsl:function name="css:get-default-value" as="xs:string?">
        <xsl:param name="property" as="xs:string"/>
        <xsl:variable name="index" select="my:index-of($properties, $property)"/>
        <xsl:if test="$index">
            <xsl:sequence select="$default-values[$index]"/>
        </xsl:if>
    </xsl:function>
    
    <xsl:function name="css:is-inherited-property" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:sequence select="boolean(my:index-of($inherited-properties, $property))"/>
    </xsl:function>
    
    <xsl:function name="css:applies-to" as="xs:boolean">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="display" as="xs:string"/>
        <xsl:variable name="index" select="my:index-of($properties, $property)"/>
        <xsl:sequence select="if ($index) then matches($display, $applies-to[$index]) else false()"/>
    </xsl:function>
    
    <xsl:function name="my:index-of" as="xs:integer?">
        <xsl:param name="sequence"/>
        <xsl:param name="property"/>
        <xsl:choose>
            <xsl:when test="index-of($sequence, $property)">
                <xsl:sequence select="index-of($sequence, $property)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="index-of($sequence, concat('-brl-', $property))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>
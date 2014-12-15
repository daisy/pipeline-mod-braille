<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">
	
	<xsl:import href="../library.xsl"/>
	
	<!--
	    API: implement xsl:template match="css:block"
	    - @return: translation of the element's content as a sequence of nodes
	    - modes:
	      - #default: the children of css:block represent the contents of a css block container that
	        contains no other block-level boxes. the result nodes will be inserted directly in the
	        result document.
	      - after, before, string-set: the only child of css:block is a text node that represents
	        one css value in a <content-list>, either within a content property of a ::before or
	        ::after pseudo-element, or within a string-set property. the string value of the result
	        node(s) will be used as the according css value in the result document.
	    - in #default mode, the result sequence may contain d:sync elements with source-ref and
	      result-ref attributes, which represent synchronization points between input and
	      output. the references are expressed as relative epubcfi paths consisting of simple
	      steps. their syntax is [ "/" integer ]+. source-ref is resolved starting from the root
	      element of the input document. result-ref is resolved starting from the root element of
	      the output document. d:sync elements are channelled to the resource-map port.
	    - @see http://www.idpf.org/epub/linking/cfi/epub-cfi.html#sec-epubcfi-syntax
	    - @see org.daisy.pipeline.braille.common.CSSBlockTransform
	-->
	<xsl:template match="css:block" mode="#default after before string-set">
		<xsl:message terminate="yes">Coding error</xsl:message>
	</xsl:template>
	
	<xsl:template match="/*">
		<xsl:apply-templates select="." mode="identify-blocks">
			<xsl:with-param name="source-path" select="()"/>
			<xsl:with-param name="result-path" select="()"/>
			<xsl:with-param name="next-blocks" select="()"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:variable name="inline-properties" as="xs:string*"
	              select="$css:properties[not(.='display') and css:applies-to(., 'inline')]"/>
	
	<xsl:template match="*" mode="identify-blocks">
		<xsl:param name="source-path" as="xs:integer*" required="yes"/>
		<xsl:param name="result-path" as="xs:integer*" required="yes"/>
		<xsl:param name="next-blocks" as="element()*" required="yes"/>
		<xsl:param name="is-block" as="xs:boolean" select="true()" tunnel="yes"/>
		<xsl:param name="display" as="xs:string" select="'block'" tunnel="yes"/>
		<xsl:variable name="source-path" as="xs:integer*" select="pxi:inc-cfipath($source-path,2)"/>
		<xsl:variable name="result-path" as="xs:integer*" select="pxi:inc-cfipath($result-path,2)"/>
		<xsl:variable name="is-block" as="xs:boolean" select="$is-block and pxi:is-block(.)"/>
		<xsl:variable name="display" as="xs:string" select="if ($display='none') then 'none' else pxi:display(.)"/>
		<xsl:variable name="translated-rules" as="element()*">
			<xsl:apply-templates select="css:parse-stylesheet(@style)" mode="translate-rule-list">
				<xsl:with-param name="context" select="." tunnel="yes"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($translated-rules))"/>
			<xsl:choose>
				<xsl:when test="$display='none'">
					<xsl:apply-templates select="*" mode="#current">
						<xsl:with-param name="source-path" select="()"/>
						<xsl:with-param name="result-path" select="()"/>
						<xsl:with-param name="next-blocks" select="()"/>
						<xsl:with-param name="is-block" select="$is-block" tunnel="yes"/>
						<xsl:with-param name="display" select="$display" tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="context" as="element()" select="."/>
					<xsl:variable name="lang" as="xs:string?" select="pxi:lang(.)"/>
					<xsl:variable name="blocks" as="element()*">
						<xsl:for-each-group select="*|text()" group-adjacent="$is-block and pxi:is-block(.)">
							<xsl:choose>
								<xsl:when test="current-grouping-key()">
									<xsl:sequence select="current-group()"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:element name="block" namespace="http://www.daisy.org/ns/pipeline/braille-css">
										<xsl:if test="$lang">
											<xsl:attribute name="xml:lang" select="$lang"/>
										</xsl:if>
										<xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
										                        css:computed-properties($inline-properties, false(), $context)
										                        [not(@value=css:initial-value(@name))]))"/>
										<xsl:sequence select="current-group()"/>
									</xsl:element>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each-group>
					</xsl:variable>
					<xsl:apply-templates select="$blocks[1]" mode="identify-blocks">
						<xsl:with-param name="source-path" select="($source-path,0)"/>
						<xsl:with-param name="result-path" select="($result-path,0)"/>
						<xsl:with-param name="next-blocks" select="$blocks[position()&gt;1]"/>
						<xsl:with-param name="is-block" select="$is-block" tunnel="yes"/>
						<xsl:with-param name="display" select="$display" tunnel="yes"/>
						<xsl:with-param name="context" select="$context" tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
		<xsl:apply-templates select="$next-blocks[1]" mode="#current">
			<xsl:with-param name="source-path" select="$source-path"/>
			<xsl:with-param name="result-path" select="$result-path"/>
			<xsl:with-param name="next-blocks" select="$next-blocks[position()&gt;1]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="css:block" mode="identify-blocks">
		<xsl:param name="source-path" as="xs:integer*" required="yes"/>
		<xsl:param name="result-path" as="xs:integer*" required="yes"/>
		<xsl:param name="next-blocks" as="element()*" required="yes"/>
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:choose>
			<xsl:when test="normalize-space(string(.))=''">
				<xsl:apply-templates select="node()[1]" mode="treewalk">
					<xsl:with-param name="source-path" select="$source-path"/>
					<xsl:with-param name="result-path" select="$result-path"/>
					<xsl:with-param name="new-text-nodes" select=".//text()"/>
				</xsl:apply-templates>
				<xsl:apply-templates select="$next-blocks[1]" mode="#current">
					<xsl:with-param name="source-path" select="pxi:inc-cfipath($source-path,2*count(child::*))"/>
					<xsl:with-param name="result-path" select="pxi:inc-cfipath($result-path,2*count(child::*))"/>
					<xsl:with-param name="next-blocks" select="$next-blocks[position()&gt;1]"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="translated-block" as="node()*">
					<xsl:apply-templates select=".">
						<xsl:with-param name="source-path" select="$source-path"/>
						<xsl:with-param name="result-path" select="$result-path"/>
						<xsl:with-param name="context" select="$context"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:sequence select="$translated-block"/>
				<xsl:if test="not($translated-block/descendant-or-self::d:sync)">
					<d:sync source-ref="{pxi:serialize-cfipath(pxi:inc-cfipath($source-path,1))}"
					        result-ref="{pxi:serialize-cfipath(pxi:inc-cfipath($result-path,1))}"/>
				</xsl:if>
				<xsl:apply-templates select="$next-blocks[1]" mode="#current">
					<xsl:with-param name="source-path" select="pxi:inc-cfipath($source-path,2*count(child::*))"/>
					<xsl:with-param name="result-path" select="pxi:inc-cfipath($result-path,2*count($translated-block/(self::* except self::d:sync)))"/>
					<xsl:with-param name="next-blocks" select="$next-blocks[position()&gt;1]"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="css:rule|css:property|css:content|css:string[@name]|css:counter|css:text|css:leader"
	              mode="translate-rule-list translate-declaration-list translate-content-list">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template match="css:rule[not(@selector) or @selector=('::before', '::after')]" mode="translate-rule-list">
		<xsl:variable name="properties" as="element()*" select="css:parse-declaration-list(@declaration-list)"/>
		<xsl:variable name="translated-properties" as="element()*">
			<xsl:apply-templates select="$properties" mode="translate-declaration-list">
				<xsl:with-param name="mode" tunnel="yes"
				                select="if (@selector='::before') then 'before'
				                        else if (@selector='::after') then 'after'
				                        else '#default'"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:if test="$translated-properties">
			<xsl:copy>
				<xsl:sequence select="@selector"/>
				<xsl:attribute name="declaration-list" select="css:serialize-declaration-list($translated-properties)"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="css:property[@name='string-set']" mode="translate-declaration-list">
		<xsl:if test="@value!='none'">
			<xsl:variable name="translated-string-set-pairs" as="element()*">
				<xsl:apply-templates select="css:parse-string-set(@value)" mode="translate-string-set-pair"/>
			</xsl:variable>
			<xsl:copy>
				<xsl:sequence select="@name"/>
				<xsl:attribute name="value" select="css:serialize-string-set($translated-string-set-pairs)"/>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="css:string-set" mode="translate-string-set-pair" as="element()">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:copy>
			<xsl:sequence select="@name"/>
			<xsl:variable name="translated-content-list" as="element()*">
				<xsl:apply-templates select="css:parse-content-list(@value, $context)" mode="translate-content-list">
					<xsl:with-param name="string-name" select="@name" tunnel="yes"/>
					<xsl:with-param name="mode" select="'string-set'" tunnel="yes"/>
				</xsl:apply-templates>
			</xsl:variable>
			<xsl:attribute name="value" select="if (exists($translated-content-list))
			                                    then css:serialize-content-list($translated-content-list)
			                                    else '&quot;&quot;'"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="css:property[@name='content']" mode="translate-declaration-list">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:variable name="translated-content-list" as="element()*">
			<xsl:apply-templates select="css:parse-content-list(@value, $context)" mode="translate-content-list"/>
		</xsl:variable>
		<xsl:sequence select="css:property('content', if (exists($translated-content-list))
					                                  then css:serialize-content-list($translated-content-list)
					                                  else '&quot;&quot;')"/>
	</xsl:template>
	
	<xsl:template match="css:string[@value]|css:attr" mode="translate-content-list" as="element()?">
		<xsl:param name="context" as="element()" tunnel="yes"/>
		<xsl:param name="mode" as="xs:string" tunnel="yes"/>
		<xsl:variable name="evaluated-string" as="xs:string">
			<xsl:apply-templates select="." mode="css:eval"/>
		</xsl:variable>
		<xsl:variable name="lang" as="xs:string?" select="pxi:lang($context)"/>
		<xsl:variable name="block">
			<xsl:element name="css:block">
				<xsl:if test="$lang">
					<xsl:attribute name="xml:lang" select="$lang"/>
				</xsl:if>
				<!--
				    FIXME: what about style?
				-->
				<xsl:value-of select="$evaluated-string"/>
			</xsl:element>
		</xsl:variable>
		<xsl:variable name="translated-block" as="node()*">
			<xsl:choose>
				<xsl:when test="$mode='string-set'">
					<xsl:apply-templates select="$block/css:block" mode="string-set"/>
				</xsl:when>
				<xsl:when test="$mode='before'">
					<xsl:apply-templates select="$block/css:block" mode="before"/>
				</xsl:when>
				<xsl:when test="$mode='after'">
					<xsl:apply-templates select="$block/css:block" mode="after"/>
				</xsl:when>
			</xsl:choose>
		</xsl:variable>
		<css:string value="{string-join($translated-block/string(.),'')}"/>
	</xsl:template>
	
	<xsl:template match="*" mode="treewalk">
		<xsl:param name="source-path" as="xs:integer*" required="yes"/>
		<xsl:param name="result-path" as="xs:integer*" required="yes"/>
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:variable name="source-path" as="xs:integer*" select="pxi:inc-cfipath($source-path,1+((1+$source-path[last()]) mod 2))"/>
		<xsl:variable name="result-path" as="xs:integer*" select="pxi:inc-cfipath($result-path,1+((1+$result-path[last()]) mod 2))"/>
		<xsl:variable name="text-node-count" select="count(.//text())"/>
		<xsl:copy>
			<xsl:sequence select="@* except @style"/>
			<xsl:if test="@style">
				<xsl:variable name="translated-rules" as="element()*">
					<xsl:apply-templates select="css:parse-stylesheet(@style)" mode="translate-rule-list">
						<xsl:with-param name="context" select="." tunnel="yes"/>
					</xsl:apply-templates>
				</xsl:variable>
				<xsl:sequence select="css:style-attribute(css:serialize-stylesheet($translated-rules))"/>
			</xsl:if>
			<xsl:apply-templates select="child::node()[1]" mode="#current">
				<xsl:with-param name="source-path" select="($source-path,0)"/>
				<xsl:with-param name="result-path" select="($result-path,0)"/>
				<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&lt;=$text-node-count]"/>
			</xsl:apply-templates>
		</xsl:copy>
		<xsl:apply-templates select="following-sibling::node()[1]" mode="#current">
			<xsl:with-param name="source-path" select="$source-path"/>
			<xsl:with-param name="result-path" select="$result-path"/>
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;$text-node-count]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="text()" mode="treewalk">
		<xsl:param name="source-path" as="xs:integer*" required="yes"/>
		<xsl:param name="result-path" as="xs:integer*" required="yes"/>
		<xsl:param name="new-text-nodes" as="xs:string*" required="yes"/>
		<xsl:variable name="source-path" as="xs:integer*" select="pxi:inc-cfipath($source-path,1+($source-path[last()] mod 2))"/>
		<xsl:variable name="result-path" as="xs:integer*" select="pxi:inc-cfipath($result-path,1+($result-path[last()] mod 2))"/>
		<xsl:choose>
			<xsl:when test="normalize-space(.)='' and normalize-space(new-text-nodes[1])=''">
				<xsl:sequence select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$new-text-nodes[1]"/>
				<d:sync source-ref="{pxi:serialize-cfipath($source-path)}"
				        result-ref="{pxi:serialize-cfipath($result-path)}"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="following-sibling::node()[1]" mode="#current">
			<xsl:with-param name="source-path" select="$source-path"/>
			<xsl:with-param name="result-path" select="$result-path"/>
			<xsl:with-param name="new-text-nodes" select="$new-text-nodes[position()&gt;1]"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:function name="pxi:lang" as="xs:string?">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="($element/ancestor-or-self::*[@xml:lang][1]/@xml:lang,'und')[1]"/>
	</xsl:function>
	
	<xsl:function name="pxi:display" as="xs:string">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="css:specified-properties('display', true(), true(), false(), $element)/@value"/>
	</xsl:function>
	
	<xsl:function name="pxi:string-set" as="xs:string">
		<xsl:param name="element" as="element()"/>
		<xsl:sequence select="css:specified-properties('string-set', true(), true(), false(), $element)/@value"/>
	</xsl:function>
	
	<xsl:function name="pxi:is-block" as="xs:boolean">
		<xsl:param name="node" as="node()"/>
		<xsl:sequence select="boolean($node/descendant-or-self::*[pxi:display(.) != 'inline'])"/>
	</xsl:function>
	
	<xsl:function name="pxi:parse-cfipath" as="xs:integer*">
		<xsl:param name="path" as="xs:string"/>
		<xsl:sequence select="for $x in tokenize($path,'/')[position()&gt;1] return xs:integer(number($x))"/>
	</xsl:function>
	
	<xsl:function name="pxi:serialize-cfipath" as="xs:string">
		<xsl:param name="path" as="xs:integer*"/>
		<xsl:sequence select="string-join(('',for $x in $path return string($x)),'/')"/>
	</xsl:function>
	
	<xsl:function name="pxi:inc-cfipath" as="xs:integer*">
		<xsl:param name="path" as="xs:integer*"/>
		<xsl:param name="inc-with-path" as="xs:integer*"/>
		<xsl:choose>
			<xsl:when test="count($path)=0">
				<xsl:if test="$inc-with-path[1]!=2">
					<xsl:message terminate="yes"/>
				</xsl:if>
				<xsl:sequence select="$inc-with-path[position()&gt;1]"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="($path[position()&lt;last()],$path[last()]+$inc-with-path[1],$inc-with-path[position()&gt;1])"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
</xsl:stylesheet>

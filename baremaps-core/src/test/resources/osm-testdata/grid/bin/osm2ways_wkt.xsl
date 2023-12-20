<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="utf-8" indent="no"/>

    <xsl:key name="nodes" match="/osm/node" use="@id"/>

    <xsl:template match="/">
        <xsl:apply-templates select="/osm/way"/>
    </xsl:template>

    <xsl:template match="/osm/way">
        <xsl:if test="count(nd) &gt; 1">
            <xsl:value-of select="@id"/>
            <xsl:text> LINESTRING(</xsl:text>
            <xsl:apply-templates select="nd"/>
            <xsl:text>)
</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/osm/way/nd">
        <xsl:value-of select="key('nodes', @ref)/@lon"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="key('nodes', @ref)/@lat"/>
        <xsl:if test="position()!=last()">
            <xsl:text>,</xsl:text>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

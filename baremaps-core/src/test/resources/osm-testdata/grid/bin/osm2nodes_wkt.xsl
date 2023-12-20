<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" encoding="utf-8" indent="no"/>

    <xsl:template match="/">
        <xsl:apply-templates select="/osm/node"/>
    </xsl:template>

    <xsl:template match="/osm/node">
        <xsl:value-of select="@id"/>
        <xsl:text> POINT(</xsl:text>
        <xsl:value-of select="@lon"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@lat"/>
        <xsl:text>)
</xsl:text>
    </xsl:template>

</xsl:stylesheet>

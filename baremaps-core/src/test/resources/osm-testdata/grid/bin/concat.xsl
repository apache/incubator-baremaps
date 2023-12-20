<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" encoding="utf-8" indent="yes"/>

    <xsl:template match="/">
<osm version="0.6" generator="testdata" upload="false">
        <xsl:apply-templates select="files/file" mode="nodes"/>
        <xsl:apply-templates select="files/file" mode="ways"/>
        <xsl:apply-templates select="files/file" mode="relations"/>
</osm>
    </xsl:template>

    <xsl:template match="files/file" mode="nodes">
        <xsl:copy-of select="document(@name)/osm/node"/>
    </xsl:template>

    <xsl:template match="files/file" mode="ways">
        <xsl:copy-of select="document(@name)/osm/way"/>
    </xsl:template>

    <xsl:template match="files/file" mode="relations">
        <xsl:copy-of select="document(@name)/osm/relation"/>
    </xsl:template>

</xsl:stylesheet>

package com.daonvshu.pokkit.bangumi.repository

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object ItemsTable : IntIdTable("items") {
    val title = text("title")
    val type = text("type")
    val lang = text("lang")
    val officialSite = text("official_site").nullable()
    val begin = integer("begin").nullable()
    val broadcast = text("broadcast").nullable()
    val broadcastBegin = integer("broadcast_begin").nullable()
    val end = integer("end").nullable()
    val comment = text("comment").nullable()
}

object MetaTable : Table("meta") {
    val key = text("key")
    val value = text("value").nullable()

    override val primaryKey = PrimaryKey(key)
}

object SiteMetaTable : Table("site_meta") {
    val siteName = text("site_name")
    val title = text("title")
    val urlTemplate = text("url_template")
    val type = text("type")
    val regions = text("regions").nullable()

    override val primaryKey = PrimaryKey(siteName)
}

object SitesTable : IntIdTable("sites") {
    val itemId = integer("item_id").references(ItemsTable.id)
    val siteName = text("site_name")
    val siteTitle = text("site_title")
    val siteType = text("site_type")
    val siteId = text("site_id").nullable()
    val url = text("url").nullable()
    val urlTemplate = text("url_template").nullable()
    val urlResolved = text("url_resolved").nullable()
    val begin = integer("begin").nullable()
    val end = integer("end").nullable()
    val broadcast = text("broadcast").nullable()
    val broadcastBegin = integer("broadcast_begin").nullable()
    val comment = text("comment").nullable()
    val regions = text("regions").nullable()
}

object TitleTranslationsTable : IntIdTable("title_translations") {
    val itemId = integer("item_id").references(ItemsTable.id)
    val language = text("language")
    val title = text("title")
}
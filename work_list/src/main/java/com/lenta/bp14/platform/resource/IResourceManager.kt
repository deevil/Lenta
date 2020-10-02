package com.lenta.bp14.platform.resource

interface IResourceManager {
    val serverConnectionError: String
    val threeLinesPattern: String
    val fourLinesPattern: String
    val prodDatePattern: String
    val prodDateLongPattern: String
    val expirDatePattern: String
    val expirDateLongPattern: String
    val datesDivider: String
    val storageZPartsPattern: String
}
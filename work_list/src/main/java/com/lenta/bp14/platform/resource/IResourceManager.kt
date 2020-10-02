package com.lenta.bp14.platform.resource

interface IResourceManager {
    val serverConnectionError: String
    val threeLinesPattern: String
    val prodDatePattern: String
    val expirDatePattern: String
    val datesDivider: String
    val storageZPartsPattern: String
}
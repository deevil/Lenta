package com.lenta.bp7.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Path
import org.simpleframework.xml.Root
import java.util.*

data class DataForSend(
        val shop: String,
        val terminalId: String = "5a4c3555-730c-493a-821f-8780a22d8d31",
        val data: DisplayOfGoods,
        val saveDoc: Int = 1
) {

}

@Root(strict = false, name = "DisplayOfGoods")
data class DisplayOfGoods @JvmOverloads constructor(
        @field:Attribute(name = "documentDate")
        var sendDateTime: String = Date().toString(),
        @field:Attribute(name = "id")
        var marketIp: String = "11.111.11.111",
        @field:Attribute(name = "gid")
        var gid: String = UUID.randomUUID().toString(),
        @Path("equipment") @field:ElementList(name = "equipment", inline = true)
        var segments: MutableList<SegmentSend> = mutableListOf()
) {
    // <DisplayOfGoods documentDate="2019-07-28T01:48:15" id="10.254.22.119" gid="a4ba84be-8198-4b89-95b9-9be5a8198102">
}

@Root(name = "equipment")
data class SegmentSend @JvmOverloads constructor(
        @field:Attribute(name = "number")
        var number: String,
        @field:Attribute(name = "startTime")
        var startTime: String,
        @field:Attribute(name = "completionTime")
        var completionTime: String,
        @field:Attribute(name = "canceled")
        var canceled: Int,
        @Path("shelf") @field:ElementList(name = "shelf", inline = true)
        var shelves: MutableList<ShelfSend> = mutableListOf()
) {
    // <equipment number="032 007" startTime="2019-07-28T01:45:03" completionTime="2019-07-28T01:48:13">
}

@Root(name = "shelf")
data class ShelfSend @JvmOverloads constructor(
        @field:Attribute(name = "number")
        var number: String,
        @field:Attribute(name = "startTime")
        var startTime: String,
        @field:Attribute(name = "completionTime")
        var completionTime: String,
        @field:Attribute(name = "counted")
        var counted: Int,
        @field:Attribute(name = "canceled")
        var canceled: Int,
        @Path("goods") @field:ElementList(name = "goods", inline = true)
        var goods: MutableList<GoodSend> = mutableListOf()
) {
    // <shelf number="1" startTime="2019-07-28T01:45:08" completionTime="2019-07-28T01:45:38" counted="0">
}

@Root(strict = false, name = "goods")
data class GoodSend @JvmOverloads constructor(
        @field:Attribute(name = "SAPCode")
        var sapCodeForSend: String,
        @field:Attribute(name = "barcode")
        var barCode: String,
        @field:Attribute(name = "count", required = false)
        var count: Int? = null,
        @field:Attribute(name = "labeled", required = false)
        var labeled: Int? = null
) {
    // <goods SAPCode="169398_ST" barcode="4820043010028" count="0" labeled="1" />
}
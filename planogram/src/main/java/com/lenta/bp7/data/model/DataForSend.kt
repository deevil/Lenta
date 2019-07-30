package com.lenta.bp7.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
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
data class DisplayOfGoods (
        @Attribute(name = "documentDate")
        var sendDateTime: String = Date().toString(),
        @Attribute(name = "id")
        var marketIp: String = "11.111.11.111",
        @Attribute(name = "gid")
        var gid: String = UUID.randomUUID().toString(),
        @field:ElementList(name = "equipment") @param:ElementList(name = "equipment")
        var segments: MutableList<SegmentSend> = mutableListOf()
) {
    // <DisplayOfGoods documentDate="2019-07-28T01:48:15" id="10.254.22.119" gid="a4ba84be-8198-4b89-95b9-9be5a8198102">
}

data class SegmentSend (
        @Attribute(name = "number")
        var number: String,
        @Attribute(name = "startTime")
        var startTime: String,
        @Attribute(name = "completionTime")
        var completionTime: String,
        @Attribute(name = "canceled")
        var canceled: Int,
        @field:ElementList(name = "shelf") @param:ElementList(name = "shelf")
        var shelves: MutableList<ShelfSend> = mutableListOf()
) {
    // <equipment number="032 007" startTime="2019-07-28T01:45:03" completionTime="2019-07-28T01:48:13">
}

data class ShelfSend (
        @Attribute(name = "number")
        var number: String,
        @Attribute(name = "startTime")
        var startTime: String,
        @Attribute(name = "completionTime")
        var completionTime: String,
        @Attribute(name = "counted")
        var counted: Int,
        @Attribute(name = "canceled")
        var canceled: Int,
        @field:ElementList(name = "goods") @param:ElementList(name = "goods")
        var goods: MutableList<GoodSend> = mutableListOf()
) {
    // <shelf number="1" startTime="2019-07-28T01:45:08" completionTime="2019-07-28T01:45:38" counted="0">
}

data class GoodSend(
        @Attribute(name = "SAPCode")
        var sapCodeForSend: String,
        @Attribute(name = "barcode")
        var barCode: String,
        @Attribute(name = "count", required = false)
        var count: Int? = null,
        @Attribute(name = "labeled", required = false)
        var labeled: Int? = null
) {
    // <goods SAPCode="169398_ST" barcode="4820043010028" count="0" labeled="1" />
}
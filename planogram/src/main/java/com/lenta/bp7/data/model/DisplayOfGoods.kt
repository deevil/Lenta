package com.lenta.bp7.data.model

import com.lenta.shared.platform.constants.Constants.CHECK_DATA_TIME_FORMAT
import org.simpleframework.xml.*
import java.text.SimpleDateFormat
import java.util.*

@Root(strict = false, name = "DisplayOfGoods")
@Order(attributes = ["documentDate", "id", "gid"])
data class DisplayOfGoods @JvmOverloads constructor(
        @field:Attribute(name = "documentDate")
        var sendDateTime: String = SimpleDateFormat(CHECK_DATA_TIME_FORMAT, Locale.getDefault()).format(Date()),
        @field:Attribute(name = "id")
        var marketIp: String,
        @field:Attribute(name = "gid")
        var gid: String = UUID.randomUUID().toString(),
        @Path("equipment") @field:ElementList(name = "equipment", inline = true)
        var segments: MutableList<SegmentSend> = mutableListOf()
) {
    // <DisplayOfGoods documentDate="2019-07-28T01:48:15" id="10.254.22.119" gid="a4ba84be-8198-4b89-95b9-9be5a8198102">
}

@Root(name = "equipment")
@Order(attributes = ["number", "startTime", "completionTime", "canceled"])
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
@Order(attributes = ["number", "startTime", "completionTime", "counted", "canceled"])
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
@Order(attributes = ["SAPCode", "barcode", "count", "labeled"])
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
package com.lenta.bp7.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.util.*

@Root(name = "DisplayOfGoods")
data class DisplayOfGoods(
        @Attribute(name = "documentDate")
        val sendDateTime: String = Date().toString(),
        @Attribute(name = "id")
        val marketIp: String = "11.111.11.111",
        @Attribute(name = "gid")
        val gid: String = UUID.randomUUID().toString(),
        @ElementList(name = "equipment")
        val segments: MutableList<Segment>
)

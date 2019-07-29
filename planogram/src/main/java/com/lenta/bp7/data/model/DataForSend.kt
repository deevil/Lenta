package com.lenta.bp7.data.model

data class DataForSend(
        val shop: String,
        val terminalId: String = "5a4c3555-730c-493a-821f-8780a22d8d31",
        val data: DisplayOfGoods,
        val saveDoc: Int = 1
) {

}
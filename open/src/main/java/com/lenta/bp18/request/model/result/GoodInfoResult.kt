package com.lenta.bp18.request.model.result

import com.lenta.bp18.request.pojo.GoodInfo
import com.lenta.bp18.request.pojo.RetCode

data class GoodInfoResult (
        /**Таблица возврата*/
        val retCode: List<RetCode>
)
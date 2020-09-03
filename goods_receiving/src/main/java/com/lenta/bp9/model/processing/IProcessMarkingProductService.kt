package com.lenta.bp9.model.processing

import com.lenta.bp9.features.goods_information.marking.TypeLastStampScanned
import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo

interface IProcessMarkingProductService {
    fun newProcessMarkingProductService(inputProductInfo: TaskProductInfo): IProcessMarkingProductService?
    fun getConfirmedByScanning(): Double
    fun getCountBlocksUnderload(paramGrzGrundMarkCode: String): Double
    fun denialOfFullProductAcceptance(typeDiscrepancies: String)
    fun refusalToAcceptPartlyByProduct(typeDiscrepancies: String)
}
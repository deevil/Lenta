package com.lenta.bp9.features.goods_information.baseGoods

interface IInterface {
}

//interface ISpinPosition {
//    val spinQualitySelectedPosition: MutableLiveData<Int>
//    val qualityInfo: MutableLiveData<List<QualityInfo>>
//    val spinReasonRejectionSelectedPosition: MutableLiveData<Int>
//    val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>>
//    val productInfo: MutableLiveData<TaskProductInfo>
//    var taskManager: IReceivingTaskManager
//    val taskRepository : ITaskRepository
//}
//
//interface IDefectable : IBaseTaskManager {
//
//    val isDefect: MutableLiveData<Boolean>
//        get() = spinQualitySelectedPosition.map {
//            it != 0
//        }
//}
//
//interface IQualityInfo : IBaseTaskManager {
//
//    val currentQualityInfoCode: String
//        get() {
//            val position = spinQualitySelectedPosition.value ?: -1
//            return position
//                    .takeIf { it >= 0 }
//                    ?.run {
//                        qualityInfo.value?.getOrNull(this)?.code.orEmpty()
//                    }.orEmpty()
//        }
//}
//
//interface IReasonRejectionInfo : IBaseTaskManager {
//
//    val currentReasonRejectionInfoCode: String
//        get() {
//            val position = spinReasonRejectionSelectedPosition.value ?: -1
//            return position
//                    .takeIf { it >= 0 }
//                    ?.run {
//                        reasonRejectionInfo.value
//                        ?.getOrNull(this)?.code.orEmpty()
//                    }
//                    .orEmpty()
//        }
//}
//
//interface ICurrentTypeInfo : IBaseTaskManager, IQualityInfo, IReasonRejectionInfo {
//    private val currentTypeDiscrepanciesCode: String
//        get() {
//            return currentQualityInfoCode
//                    .takeIf { it == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
//                    ?: currentReasonRejectionInfoCode
//        }
//}
//
//interface ICountAcceptInfo : IBaseTaskManager {
//    private val countAcceptOfProduct: Double
//    get()
//    {
//        return productInfo.value
//                ?.let { product ->
//                    taskRepository
//                            .getProductsDiscrepancies()
//                            .getCountAcceptOfProduct(product)
//                }
//                ?: 0.0
//    }
//}
//
//interface ICountRefusalInfo : IBaseTaskManager {
//    private val countRefusalOfProduct: Double
//        get() {
//            return productInfo.value
//                    ?.let { product ->
//                        taskRepository
//                                .getProductsDiscrepancies()
//                                .getCountRefusalOfProduct(product)
//                    }
//                    ?: 0.0
//        }
//}
package com.lenta.bp9.features.goods_information.baseGoods

interface IBaseIsPerishable : IBaseVariables {

//    val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
//    val productGeneralShelfLifeInt = productInfo.value?.generalShelfLife?.toInt() ?: 0
//    val productRemainingShelfLifeInt = productInfo.value?.remainingShelfLife?.toInt() ?: 0
//    val productMhdhbDays = productInfo.value?.mhdhbDays ?: 0
//
//    isPerishable.value = productGeneralShelfLifeInt > 0 || productRemainingShelfLifeInt > 0
//    || (productMhdhbDays in 1 until paramGrzUffMhdhb)
//    if (isPerishable.value == true)
//    {
//        shelfLifeInfo.value = dataBase.getTermControlInfo()
//        spinShelfLife.value = shelfLifeInfo.value?.map { it.name }.orEmpty()
//        currentDate.value = timeMonitor.getServerDate()
//        expirationDate.value = Calendar.getInstance()
//        shelfLifeDate.value =
//                currentDate.value
//                        ?.let {
//                            DateTimeUtil.formatDate(it, Constants.DATE_FORMAT_dd_mm_yyyy)
//                        }
//                        .orEmpty()
//        if (productGeneralShelfLifeInt > 0 || productRemainingShelfLifeInt > 0) { //https://trello.com/c/XSAxdgjt
//            generalShelfLife.value = productInfo.value?.generalShelfLife.orEmpty()
//            remainingShelfLife.value = productInfo.value?.remainingShelfLife.orEmpty()
//        } else {
//            generalShelfLife.value = productInfo.value?.mhdhbDays.toString()
//            remainingShelfLife.value = productInfo.value?.mhdrzDays.toString()
//        }
//    }
}

package com.lenta.bp14.models.work_list.repo

import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.work_list.*
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.Uom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WorkListRepo {

    fun getCommonGoodInfoByEan(ean: String): CommonGoodInfo? {
        return CommonGoodInfo(
                ean = "11111111",
                material = "000000000000222222",
                matcode = "333333333333",
                name = "Товар",
                unit = Uom.ST,
                goodGroup = "123456",
                purchaseGroup = "1111",
                serverComments = MutableList(3) {
                    "Комментарий ${it + 1}"
                },
                options = GoodOptions(
                        matrixType = MatrixType.Active,
                        section = "5",
                        goodType = GoodType.COMMON
                )
        )
    }

    fun getAdditionalGoodInfo(ean: String): AdditionalGoodInfo? {
        return AdditionalGoodInfo(
                storagePlaces = "125635; 652148; 635894",
                minStock = (10..50).random(),
                movement = Movement(
                        inventory = "19.07.19 (-25 шт.)",
                        arrival = "29.07.19 (+50 шт; Z5)"
                ),
                price = Price(
                        commonPrice = (110..140).random(),
                        discountPrice = (80..100).random()
                ),
                promo = Promo(
                        name = "Распродажа кукурузы ТК 0007",
                        period = "Период 30.05.19 - 12.09.19"
                ))
    }

}
package com.lenta.shared.platform.toolbar.bottom_toolbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.R

class BottomToolbarUiModel {
    private val _visibility: MutableLiveData<Boolean> = MutableLiveData()
    val uiModelButton1: ButtonUiModel = ButtonUiModel()
    val uiModelButton2: ButtonUiModel = ButtonUiModel()
    val uiModelButton3: ButtonUiModel = ButtonUiModel()
    val uiModelButton4: ButtonUiModel = ButtonUiModel()
    val uiModelButton5: ButtonUiModel = ButtonUiModel()
    val buttonsUiModels = listOf(uiModelButton1, uiModelButton2, uiModelButton3, uiModelButton4, uiModelButton5)

    fun show() {
        _visibility.postValue(true)
    }

    fun hide() {
        _visibility.postValue(false)
    }

    fun cleanAll() {
        buttonsUiModels.forEach { it.clean() }
        show()
    }

    fun getVisibility(): LiveData<Boolean> {
        return _visibility
    }

}

data class ButtonUiModel(
        val buttonDecorationInfo: MutableLiveData<ButtonDecorationInfo?> = MutableLiveData(),
        val visibility: MutableLiveData<Boolean> = MutableLiveData(),
        val enabled: MutableLiveData<Boolean> = MutableLiveData(),
        val requestFocus: MutableLiveData<Any> = MutableLiveData()
) {

    fun clean() {
        buttonDecorationInfo.value = ButtonDecorationInfo.empty
        visibility.value = false
        enabled.value = true
    }

    fun show(buttonDecorationInfo: ButtonDecorationInfo? = null, visible: Boolean = true, enabled: Boolean = true) {
        this.visibility.value = visible
        this.enabled.value = enabled

        buttonDecorationInfo?.let {
            this.buttonDecorationInfo.value = it
        }
    }

    fun requestFocus() {
        requestFocus.postValue(true)
    }

}

data class ButtonDecorationInfo(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int
) {

    companion object {
        val empty: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(0, 0)
        }

        val filter: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_search_white_24dp, R.string.filter)
        }

        val search: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_search_white_24dp, R.string.search)
        }

        val update: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sync_white_24dp, R.string.update)
        }

        val menu: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_home_white_24dp, R.string.menu)
        }

        val sap: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sap_white_24dp, R.string.sap_code)
        }

        val barcode: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_barcode_white_24dp, R.string.barcode)
        }

        val no: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.no)
        }

        val missing: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.missing)
        }

        val cancel: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.cancel)
        }

        val yes: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.yes)
        }

        val apply: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.apply)
        }

        val confirm: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.confirm)
        }

        val inStock: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.inStock)
        }

        val review: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_visibility_white_24dp, R.string.review)
        }

        val add: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_add_white_24dp, R.string.add)
        }

        val create: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_add_white_24dp, R.string.create)
        }

        val details: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_details_white_24dp, R.string.details)
        }

        val goOver: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.go_over)
        }

        val save: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.save)
        }

        val complete: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.complete)
        }

        val nextAlternate: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.to_next)
        }

        val skip: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.skip)
        }

        val processAlternate: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.process)
        }

        val mark: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.mark)
        }

        val delete: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_basket_white_24dp, R.string.delete)
        }

        val deleteShelf: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_basket_white_24dp, R.string.shelf)
        }

        val deleteSegment: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_basket_white_24dp, R.string.segment)
        }

        val clean: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_basket_white_24dp, R.string.clean)
        }

        val defect: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_basket_white_24dp, R.string.defect)
        }

        val supply: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_bookmark_white_24dp, R.string.supply)
        }

        val docs: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_bookmark_white_24dp, R.string.docs)
        }

        val print: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.print)
        }

        val label: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.label)
        }

        val labels: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.labels)
        }

        val next: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_right_white_24dp, R.string.to_next)
        }

        val process: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_right_white_24dp, R.string.process)
        }

        val browsingNext: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_right_white_24dp, R.string.review)
        }

        val back: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_left_white_24dp, R.string.to_back)
        }

        val backNo: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_left_white_24dp, R.string.no)
        }

        val exit: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_left_white_24dp, R.string.to_exit)
        }

        val enterToApp: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exit_to_app_white_24dp, R.string.enter)
        }

        val exitToApp: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exit_to_app_white_24dp, R.string.to_exit)
        }

        val rollback: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_rollback_white_24dp, R.string.rollback)
        }

        val noPrice: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_no_price_white_24dp, R.string.missing)
        }

        val cancelBack: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_left_white_24dp, R.string.cancel)
        }

        val framed: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_framed_yes_white_24dp, R.string.framed)
        }

        val not_framed: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_framed_no_white_24dp, R.string.not_framed)
        }

        val skipAlternate: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_double_arrow_right_white_24dp, R.string.skip)
        }

        val untie: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_untie_white_24dp, R.string.untie)
        }

        val published: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_calc_play_white_24dp, R.string.published)
        }

        val counted: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_calc_done_white_24dp, R.string.counted)
        }

        val find: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_loupe_white_24dp, R.string.find)
        }

        val deliveries: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delivery_white_24dp, R.string.deliveries)
        }

        val sales: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sales_white_24dp, R.string.sales)
        }

        val errorPrice: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_price_error_white_24dp, R.string.error)
        }

        val rightPrice: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_price_right_white_24dp, R.string.correct)
        }

        val video: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_video_white_24dp, R.string.video)
        }

        val refusal: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_refusal_white_24dp, R.string.refusal)
        }

        val batches: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_batches_white_24dp, R.string.batchs_products)
        }

        val fix: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_edit_white_24dp, R.string.fix)
        }

        val full: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_full_rejection_white_24dp, R.string.full)
        }

        val temporary: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_temp_rejection_white_24dp, R.string.temporary)
        }

        val sort: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sort_white_24dp, R.string.sort)
        }

        val restore: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_crossed_out_basket_white_24dp, R.string.restore)
        }

        val getWeight: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_weight_kg_white_24dp, R.string.get_weight)
        }

        val verify: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_double_arrow_left_white_24dp, R.string.to_verify)
        }

        val recount: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_double_arrow_left_white_24dp, R.string.to_recount)
        }

        val cancellation: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_double_arrow_left_white_24dp, R.string.cancellation)
        }

        val browsing: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_browsing_white_24dp, R.string.review)
        }

        val tied: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_tied_white_24dp, R.string.to_tie)
        }

        val exclude: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exclude_white_24dp, R.string.exclude)
        }

        val breaking: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_seal_white_24dp, R.string.breaking)
        }

        val transportMarriage: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_transport_defect_white_24dp, R.string.transport_marriage_abbr)
        }

        val entirely: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_entirely_white_24dp, R.string.entirely)
        }

        val selectAll: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_entirely_white_24dp, R.string.select_all)
        }

        val proceed: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.proceed)
        }

        val properties: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_basket_info_24dp, R.string.properties)
        }

        val boxes: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_box_white_24dp, R.string.boxes)
        }

        val handleGoods: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_handle_goods_white_24dp, R.string.handle_goods)
        }

        val completeRejection: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_full_white_24dp, R.string.complete_rejection)
        }

        val partialFailure: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_partial, R.string.partial_failure)
        }

        val reset: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_rollback_white_24dp, R.string.reset)
        }

        val completeRejectionMarking: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_complete_rejection_marking, R.string.complete_rejection)
        }

        val partialFailureMarking: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_partial_failure_marking, R.string.partial_failure)
        }
    }

}






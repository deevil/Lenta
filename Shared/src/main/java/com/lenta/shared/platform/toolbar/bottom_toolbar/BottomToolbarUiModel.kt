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
        visibility.postValue(false)
        enabled.postValue(true)
    }

    fun show(buttonDecorationInfo: ButtonDecorationInfo? = null, visible: Boolean = true, enabled: Boolean = true) {
        this.visibility.postValue(visible)
        this.enabled.postValue(enabled)

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
        val filter: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_search, R.string.filter)
        }

        val update: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sync_white_24dp, R.string.update)
        }

        val menu: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_home_white_24dp, R.string.menu)
        }

        val sap: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sap, R.string.sap_code)
        }

        val barcode: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_barcode, R.string.barcode)
        }

        val yes: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.yes)
        }

        val no: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.no)
        }

        val goOver: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.go_over)
        }

        val apply: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.apply)
        }

        val confirm: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.confirm)
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

        val save: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.save)
        }

        val complete: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.complete)
        }

        val print: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.print)
        }

        val delete: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_white_24dp, R.string.delete)
        }

        val deleteShelf: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_shelf_white_24dp, R.string.shelf)
        }

        val deleteSegment: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_segment_white_24dp, R.string.segment)
        }

        val clean: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_white_24dp, R.string.clean)
        }

        val next: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_forward_white_24dp, R.string.to_next)
        }

        val nextAlternate: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.to_next)
        }

        val back: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_back_white_24dp, R.string.to_back)
        }

        val backNo: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_back_white_24dp, R.string.no)
        }

        val exit: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_back_white_24dp, R.string.to_exit)
        }

        val enterToApp: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exit_to_app_white, R.string.enter)
        }

        val exitToApp: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exit_to_app_white, R.string.to_exit)
        }

        val rollback: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_rollback_white_24dp, R.string.rollback)
        }

        val missing: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.missing)
        }

        val noPrice: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_no_price_24dp, R.string.missing)
        }

        val cancel: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_close_white_24dp, R.string.cancel)
        }

        val framed: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_framed_yes_24dp, R.string.framed)
        }

        val not_framed: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_framed_no_24dp, R.string.not_framed)
        }

        val empty: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(0, 0)
        }

        val skip: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.skip)
        }

        val skipAlternate: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_skip_white_24dp, R.string.skip)
        }

        val untie: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_untie_white_24dp, R.string.untie)
        }

        val published: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_published, R.string.published)
        }

        val counted: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_counted, R.string.counted)
        }

        val find: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_find, R.string.find)
        }

        val search: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_search, R.string.search)
        }

        val deliveries: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_deliveries_24dp, R.string.deliveries)
        }

        val sales: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sales_24dp, R.string.sales)
        }

        val errorPrice: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_price_error_24dp, R.string.error)
        }

        val rightPrice: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_price_right_24dp, R.string.correct)
        }

        val video: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_video_24dp, R.string.video)
        }

        val refusal: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.is_refusal_24dp, R.string.refusal)
        }

        val batches: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.is_batches_24dp, R.string.batchs_products)
        }

        val fix: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_edit, R.string.fix)
        }

        val full: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_full_rejection, R.string.full)
        }

        val temporary: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_temp_rejection, R.string.temporary)
        }

        val supply: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_supply_white_24dp, R.string.supply)
        }

        val docs: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_supply_white_24dp, R.string.docs)
        }

        val sort: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_sort, R.string.sort)
        }

        val process: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_forward_white_24dp, R.string.process)
        }

        val processAlternate: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.process)
        }

        val restore: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_restore, R.string.restore)
        }

        val getWeight: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_weight_kg_24dp, R.string.get_weight)
        }

        val verify: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_verify_white_24dp, R.string.to_verify)
        }

        val browsing: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_browsing_white_24dp, R.string.review)
        }

        val recount: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_verify_white_24dp, R.string.to_recount)
        }

        val tied: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_tied_white_24dp, R.string.to_tie)
        }

        val mark: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_send_white_24dp, R.string.mark)
        }

        val exclude: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_exclude_white_24dp, R.string.exclude)
        }

        val browsingNext: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_forward_white_24dp, R.string.review)
        }

        val breaking: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_seal_24dp, R.string.breaking)
        }

        val transportMarriage: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_transport_marriage_24dp, R.string.transport_marriage_abbr)
        }

        val entirely: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_entirely_24dp, R.string.entirely)
        }

        val cancellation: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_arrow_verify_white_24dp, R.string.cancellation)
        }

        val inStock: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_done_white_24dp, R.string.inStock)
        }

        val label: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.label)
        }

        val labels: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_print_white_24dp, R.string.labels)
        }

        val defect: ButtonDecorationInfo by lazy {
            ButtonDecorationInfo(R.drawable.ic_delete_white_24dp, R.string.defect)
        }
    }

}






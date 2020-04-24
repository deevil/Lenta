package com.lenta.bp9.features.search_task

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentSearchTaskBinding
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.keys.OnKeyDownListener
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.bottom_toolbar.ToolbarButtonsClickListener
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.scan.OnScanResultListener
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.provideViewModel


class SearchTaskFragment: CoreFragment<FragmentSearchTaskBinding, SearchTaskViewModel>(), ToolbarButtonsClickListener,
        OnKeyDownListener,
        OnScanResultListener {

    companion object {
        fun create(loadingMode: TaskListLoadingMode): SearchTaskFragment {
            SearchTaskFragment().let {
                it.loadingMode = loadingMode
                return it
            }
        }
    }

    private var loadingMode: TaskListLoadingMode = TaskListLoadingMode.None

    private var viewFocus: View? = null

    override fun getLayoutId(): Int = R.layout.fragment_search_task

    override fun getPageNumber(): String = "09/34"

    override fun getViewModel(): SearchTaskViewModel {
        provideViewModel(SearchTaskViewModel::class.java).let {vm ->
            getAppComponent()?.inject(vm)
            vm.loadingMode = this.loadingMode
            return vm
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.description.value = vm.getDescription()
        topToolbarUiModel.title.value = "${getString(R.string.tk)} - ${vm.tkNumber}"
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.find)
        connectLiveData(source = vm.searchEnabled, target = bottomToolbarUiModel.uiModelButton5.enabled)
    }

    override fun onToolbarButtonClick(view: View) {
        when (view.id) {
            R.id.b_5 -> vm.onClickFind()
        }
    }

    override fun onKeyDown(keyCode: KeyCode): Boolean {
        keyCode.digit?.let { digit ->
            vm.onDigitPressed(digit)
        }
        return true
    }

    override fun onScanResult(data: String) {
        vm.onScanResult(data, viewFocus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.etSupplier?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

        binding?.etOrder?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

        binding?.etInvoice?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

        binding?.etTransportation?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

        binding?.etNumberGE?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

        binding?.etNumberEO?.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                viewFocus = v
            }
        }

    }
}
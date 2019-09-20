package com.lenta.bp14.features.check_list.ean_scanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.lenta.bp14.R
import com.lenta.bp14.databinding.FragmentEanVideoScannerBinding
import com.lenta.bp14.ml.FireBaseMlScanHelper
import com.lenta.bp14.ml.ScanType
import com.lenta.bp14.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.generateScreenNumberFromPostfix
import com.lenta.shared.utilities.extentions.provideViewModel

class EanVideoScannerFragment : CoreFragment<FragmentEanVideoScannerBinding, EanVideoScannerViewModel>() {

    private lateinit var fireBaseMlScanHelper: FireBaseMlScanHelper

    override fun getLayoutId(): Int = R.layout.fragment_ean_video_scanner

    override fun getPageNumber(): String? = generateScreenNumberFromPostfix("24")

    override fun getViewModel(): EanVideoScannerViewModel {
        provideViewModel(EanVideoScannerViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.getTitle()
        topToolbarUiModel.description.value = getString(R.string.point_camera_to_barcode)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fireBaseMlScanHelper = FireBaseMlScanHelper(context!!, ScanType.EAN)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let {
            fireBaseMlScanHelper.onViewCreated(
                    viewLifecycleOwner = viewLifecycleOwner,
                    canvasForScanDetection = it.myCanvas,
                    rootView = it.flRootScan,
                    textureView = it.textureView,
                    checkStatusFunction = vm::checkStatus
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        fireBaseMlScanHelper.onDestroyView()
    }


}

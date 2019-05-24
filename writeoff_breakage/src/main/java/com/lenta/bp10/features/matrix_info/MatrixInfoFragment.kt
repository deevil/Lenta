package com.lenta.bp10.features.matrix_info

import com.lenta.bp10.R
import com.lenta.bp10.databinding.FragmentMatrixInfoBinding
import com.lenta.bp10.platform.extentions.getAppComponent
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import com.lenta.shared.utilities.state.state

class MatrixInfoFragment : CoreFragment<FragmentMatrixInfoBinding, MatrixInfoViewModel>() {

    var matrixType by state<MatrixType?>(null)

    override fun getLayoutId(): Int = R.layout.fragment_matrix_info

    override fun getPageNumber(): String {
        return "10/12"
    }

    override fun getViewModel(): MatrixInfoViewModel {
        provideViewModel(MatrixInfoViewModel::class.java).let {
            getAppComponent()?.inject(it)
            it.matrixType.value = matrixType
            it.message = getString(matrixType!!.getMessageRes())
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {

    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
    }

    companion object {
        fun create(matrixType: MatrixType): MatrixInfoFragment {
            return MatrixInfoFragment().apply {
                this.matrixType = matrixType
            }
        }
    }


}

private fun MatrixType.getMessageRes(): Int {
    return when (this) {
        MatrixType.Active -> R.string.active_matrix
        MatrixType.Passive -> R.string.passive_matrix
        MatrixType.Deleted -> R.string.deleted_matrix
        else -> R.string.unknown_matrix
    }

}

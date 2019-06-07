package com.lenta.shared.features.matrix_info

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.features.message.MessageViewModel
import com.lenta.shared.models.core.MatrixType

class MatrixInfoViewModel : MessageViewModel() {
    val matrixType: MutableLiveData<MatrixType> = MutableLiveData()
}

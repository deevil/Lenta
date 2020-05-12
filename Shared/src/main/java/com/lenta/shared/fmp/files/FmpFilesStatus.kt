package com.lenta.shared.fmp.files;

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.lenta.shared.fmp.ObjectRawStatus

class FmpFilesStatus : ObjectRawStatus<FmpFilesRaw?>()

data class FmpFilesRaw(
        @Expose
        @SerializedName("files")
        val files: List<String>?
)
package com.lenta.bp9.model.task.revise

import com.google.gson.annotations.SerializedName

//ET_VBELN_COM - Таблица примечаний к ВП
data class CommentToVP(
        val lineNumber: Int, // ??? - NUMC3 внутр. использование
        val lineText: String // ??? - TEXT132
) {

    companion object {
        fun from(restData: CommentToVPRestData): CommentToVP {
            return CommentToVP(
                    lineNumber = restData.lineNumber.toInt(),
                    lineText = restData.lineText
            )
        }
    }
}

data class CommentToVPRestData(
        @SerializedName("NUM_LINE")
        val lineNumber: String,
        @SerializedName("TEXT_LINE")
        val lineText: String) {

    companion object {
        fun from(data: CommentToVP): CommentToVPRestData {
            return CommentToVPRestData(
                    lineNumber = data.lineNumber.toString(),
                    lineText = data.lineText
            )
        }
    }
}
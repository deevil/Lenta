package com.lenta.bp12.managers.interfaces

import com.lenta.bp12.features.create_task.marked_good_info.GoodProperty
import com.lenta.bp12.model.MarkScreenStatus
import com.lenta.bp12.model.WorkType
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.shared.exception.Failure

/**
 * Имплементация:
 * @see com.lenta.bp12.managers.MarkManager
 * */
interface IMarkManager {

    fun setWorkType(workType: WorkType)
    suspend fun checkMark(number: String, workType: WorkType, isCheckFromGoodCard: Boolean): MarkScreenStatus
    suspend fun handleYesDeleteMappedMarksFromTempCallBack()
    fun handleYesSaveAndOpenAnotherBox()
    fun handleEnterMrcFromBox(): MarkScreenStatus
    fun getMarkFailure(): Failure
    fun getTempMarks(): MutableList<Mark>
    fun getProperties(): MutableList<GoodProperty>
    fun getCreatedGoodForError(): Good?
    fun getInternalErrorMessage(): String
    suspend fun loadBoxInfo(number: String, workType: WorkType): MarkScreenStatus
    fun onRollback()
    fun clearData()
}
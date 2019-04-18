package com.lenta.bp10

import com.lenta.bp10.models.task.TaskDescription
import com.lenta.bp10.models.task.TaskType
import org.junit.Assert
import org.junit.Test
import java.util.*

class  Test1 {

    val tastType: TaskType = TaskType("1", "Списание продукции")
    val taskName: String = "Test task"
    val storloc: String = "0001"
    val moveTypes: List<String> = listOf("949")
    val gisControls: List<String> = listOf("A")
    val materialTypes: List<String> = listOf("2FER")
    var taskDescription: TaskDescription

    init {
        taskDescription = TaskDescription(tastType, taskName, storloc, moveTypes, gisControls, materialTypes)
    }

    private fun getTestTaskDescription (): TaskDescription {
        val tastType = TaskType("1", "Списание продукции")
        val taskName = "Test task"
        val storloc = "0001"

        val moveTypes = ArrayList<String>()
        moveTypes.add("949")

        val gisControls = ArrayList<String>()
        gisControls.add("A")

        val materialTypes = ArrayList<String>()
        materialTypes.add("2FER")

        return TaskDescription(tastType, taskName, storloc, moveTypes, gisControls, materialTypes)
    }

    @Test
    fun taskDescription_isCorrect() {
        var td = getTestTaskDescription()
        Assert.assertEquals(tastType, td.taskType)
        Assert.assertEquals(taskName, td.taskName)
        Assert.assertEquals(storloc, td.selectedStorloc)

        Assert.assertNotNull(td.moveTypes)
        Assert.assertEquals(moveTypes, td.moveTypes)

        Assert.assertNotNull(td.gisControls)
        Assert.assertEquals(gisControls, td.gisControls)

        Assert.assertNotNull(td.materialTypes)
        Assert.assertEquals(materialTypes, td.materialTypes)
    }

}
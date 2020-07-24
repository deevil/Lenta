package com.lenta.bp14.models.check_list

import com.google.gson.GsonBuilder
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CheckListTaskTest {

    private lateinit var task: CheckListTask
    private lateinit var checkListTaskDescription: CheckListTaskDescription

    @BeforeEach
    fun createCheckListTask() {
        checkListTaskDescription = CheckListTaskDescription(
                tkNumber = "111",
                taskNumber = "555",
                taskName = "Test task",
                comment = "Test comment",
                description = "Test description",
                isStrictList = false)

        task = CheckListTask(
                checkListRepo = mock(),
                generalRepo = mock(),
                taskDescription = checkListTaskDescription,
                gson = GsonBuilder().create()
        )
    }


    // TODO Разобраться почему не работает...
    @Test
    fun `Save data to string and loading`() {
        /*val goods = List(3) {
            Good(
                    material = "000000000000123456",
                    name = "Test good ${it + 1}",
                    units = Uom.DEFAULT,
                    quantity = MutableLiveData("1")
            )
        }
        assertEquals(3, goods.size)

        task.goods.value = goods

        val savedData = task.getStateAsString()
        assertNotNull(savedData)

        task.goods.value = emptyList()
        assertEquals(0, task.goods.value!!.size)

        task.loadStateFromString(savedData)
        assertEquals(3, task.goods.value!!.size)*/

    }

}
package com.lenta.bp10.stamps_collector_manager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lenta.bp10.models.StampCollector
import com.lenta.bp10.models.memory.MemoryTaskExciseStampRepository
import com.lenta.bp10.models.memory.MemoryTaskProductRepository
import com.lenta.bp10.models.memory.MemoryTaskRepository
import com.lenta.bp10.models.memory.MemoryTaskWriteOffReasonRepository
import com.lenta.bp10.models.repositories.ITaskExciseStampRepository
import com.lenta.bp10.models.repositories.ITaskProductRepository
import com.lenta.bp10.models.repositories.ITaskRepository
import com.lenta.bp10.models.repositories.ITaskWriteOffReasonRepository
import com.lenta.bp10.models.task.*
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.utilities.tests_utils.observeOnce
import io.mockk.mockk
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.lang.UnsupportedOperationException
import java.util.*


@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StampCollectorTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var processExciseAlcoProductService: ProcessExciseAlcoProductService
    private lateinit var stampCollector : StampCollector
    private lateinit var taskDescription: TaskDescription

    var taskProductRepository: ITaskProductRepository = MemoryTaskProductRepository()
    var taskExciseStampRepository: ITaskExciseStampRepository = MemoryTaskExciseStampRepository()
    var taskWriteOfReasonRepository: ITaskWriteOffReasonRepository = MemoryTaskWriteOffReasonRepository()
    var taskRepository: ITaskRepository = MemoryTaskRepository(taskProductRepository, taskExciseStampRepository, taskWriteOfReasonRepository)



    @Before
    fun setUp() {
        Dispatchers.setMain(TestCoroutineDispatcher())
        setupTestObjects()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }

    private fun setupTestObjects() {
        taskDescription = TaskDescription(
                TaskType("СГП", "nСГП"),
                "Списание от 04.06 10:23",
                "0002",
                ArrayList(Arrays.asList(WriteOffReason("949ВД", "Лом/Бой", "A"))),
                ArrayList(Arrays.asList("N")),
                ArrayList(Arrays.asList("2FER", "3ROH")), "perNo", "printer", "tkNumber", "ipAddress"
        )

        val product1 = ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
                false, "1", MatrixType.Active, "materialType")

        processExciseAlcoProductService = ProcessExciseAlcoProductService(taskDescription, taskRepository, product1)

        stampCollector = StampCollector(processExciseAlcoProductService)

    }

    @Test(expected = UnsupportedOperationException::class)
    fun `Добавление марки без подготовки`() {
        stampCollector.add("1","", writeOffReason = "001", isBadStamp = false)

    }

    @Test()
    fun `Добавление марки правильно`() = runBlocking {

        assertEquals("", stampCollector.getPreparedStampCode())
        val stampCode = "123456789123456789"

        assertEquals(null, stampCollector.observeCount().value)

        stampCollector.prepare(stampCode = stampCode).apply {
            assertTrue(this)
        }

        assertEquals(stampCode, stampCollector.getPreparedStampCode())

        stampCollector.add("1","", writeOffReason = "001", isBadStamp = false).apply {
            assertTrue(this)
        }

        assertEquals("", stampCollector.getPreparedStampCode())


        stampCollector.observeCount().observeOnce {
            assertEquals(1.0, it)
        }


    }






}
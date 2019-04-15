package com.lenta.lentabp10;

import com.lenta.lentabp10.models.memory.MemoryTaskExciseStampRepository;
import com.lenta.lentabp10.models.memory.MemoryTaskProductRepository;
import com.lenta.lentabp10.models.memory.MemoryTaskRepository;
import com.lenta.lentabp10.models.memory.MemoryTaskWriteOffReasonRepository;
import com.lenta.lentabp10.models.repositories.ITaskExciseStampRepository;
import com.lenta.lentabp10.models.repositories.ITaskProductRepository;
import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.lentabp10.models.repositories.ITaskWriteOffReasonRepository;
import com.lenta.lentabp10.models.task.TaskDescription;
import com.lenta.lentabp10.models.task.TaskExciseStamp;
import com.lenta.lentabp10.models.task.TaskType;
import com.lenta.lentabp10.models.task.WriteOffReason;
import com.lenta.lentabp10.models.task.WriteOffTask;
import com.lenta.shared.models.core.MatrixType;
import com.lenta.shared.models.core.ProductInfo;
import com.lenta.shared.models.core.ProductType;
import com.lenta.shared.models.core.Uom;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class testWriteoffTask_ProcessExciseAlcoProductService {
    TaskDescription taskDescription;
    ITaskProductRepository taskProductRepository = new MemoryTaskProductRepository();
    ITaskExciseStampRepository taskExciseStampRepository = new MemoryTaskExciseStampRepository();
    ITaskWriteOffReasonRepository taskWriteOfReasonRepository = new MemoryTaskWriteOffReasonRepository();
    ITaskRepository taskRepository = new MemoryTaskRepository(taskProductRepository, taskExciseStampRepository, taskWriteOfReasonRepository);
    WriteOffTask task;

    public void creatingObjectsForTest() {
        taskDescription = new TaskDescription(new TaskType("СГП", "nСГП"),
                "Списание от 04.06 10:23",
                "0002",
                new ArrayList<>(Arrays.asList("949ВД")),
                new ArrayList<>(Arrays.asList("N")),
                new ArrayList<>(Arrays.asList("2FER", "3ROH"))
        );

        task = new WriteOffTask(taskDescription, taskRepository);

    }

    @Test
    public void testProcessExciseAlcoProduct() {
        boolean test = false;
        ProductInfo product1 = new ProductInfo("materialNumber1", "description", new Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType");

        ProductInfo product2 = new ProductInfo("materialNumber1", "description", new Uom("ST", "шт"), ProductType.NonExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        ProductInfo product3 = new ProductInfo("materialNumber1", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        creatingObjectsForTest();

        if (task.processExciseAlcoProduct(product1) == null) {
            test = true;
        }
        assertTrue(test);

        test = false;
        if (task.processExciseAlcoProduct(product2) == null) {
            test = true;
        }
        assertTrue(test);

        test = false;
        if (task.processExciseAlcoProduct(product3) != null) {
            test = true;
        }
        assertTrue(test);

    }

    @Test
    public void testAddProduct() {
        creatingObjectsForTest();

        ProductInfo product1 = new ProductInfo("materialNumber1", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        ProductInfo product2 = new ProductInfo("materialNumber2", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        //дублирует продукт2, который не должен добавляться
        ProductInfo product3 = new ProductInfo("materialNumber2", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        WriteOffReason reason1 = new WriteOffReason("01", "Срок годности");
        WriteOffReason reason2 = new WriteOffReason("02", "Срок негодности");

        TaskExciseStamp exciseStamp1 = new TaskExciseStamp("materialNumber1", "1", "материал набора", "Срок годности", false);
        TaskExciseStamp exciseStamp2 = new TaskExciseStamp("materialNumber2", "2", "материал набора", "Срок негодности", false);
        //дублирующая марка, которая не должна добавляться
        TaskExciseStamp exciseStamp3 = new TaskExciseStamp("materialNumber2", "2", "материал набора", "Срок негодности", false);

        task = task.processExciseAlcoProduct(product1)
                .add(reason1, 1, exciseStamp1)
                .apply();

        assertEquals(1, task.getProcessedProducts().size());
        assertEquals(2,task.getTotalCountOfProduct(product1),0);

        //марка exciseStamp3 не должна добавляться
        task = task.processExciseAlcoProduct(product2)
                .add(reason1, 1, exciseStamp2)
                .add(reason2, 2, exciseStamp3)
                .apply();

        assertEquals(2, task.getProcessedProducts().size());
        assertEquals(4, task.getTotalCountOfProduct(product2),0);

        ////продукт product3 и марка exciseStamp1 не должены добавляться, а TotalCount (итого списано) у продукта product2 должен быть увеличен до 6
        task = task.processExciseAlcoProduct(product3)
                .add(reason1, 2, exciseStamp1)
                .apply();

        assertEquals(2, task.getProcessedProducts().size());
        assertEquals(6, task.getTotalCountOfProduct(product2),0);
    }

    @Test
    public void testDelProduct() {
        creatingObjectsForTest();

        ProductInfo product1 = new ProductInfo("materialNumber1", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        ProductInfo product2 = new ProductInfo("materialNumber2", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        ProductInfo product3 = new ProductInfo("materialNumber3", "description", new Uom("ST", "шт"), ProductType.ExciseAlcohol,
                false, 1, MatrixType.Active, "materialType");

        WriteOffReason reason1 = new WriteOffReason("01", "Срок годности");
        WriteOffReason reason2 = new WriteOffReason("02", "Срок негодности");

        TaskExciseStamp exciseStamp1 = new TaskExciseStamp("materialNumber1", "1", "материал набора", "Срок годности", false);
        TaskExciseStamp exciseStamp2 = new TaskExciseStamp("materialNumber2", "2", "материал набора", "Срок негодности", false);
        TaskExciseStamp exciseStamp3 = new TaskExciseStamp("materialNumber2", "3", "материал набора", "Срок негодности", false);


        task = task.processExciseAlcoProduct(product1)
                .add(reason1, 1, exciseStamp1)
                .apply();

        task = task.processExciseAlcoProduct(product2)
                .add(reason1, 1, exciseStamp2)
                .add(reason2, 2, exciseStamp3)
                .apply();

        task = task.processExciseAlcoProduct(product3)
                .add(reason1, 3, exciseStamp3)
                .apply();

        List<ProductInfo> arrDelProduct = new ArrayList<>();
        arrDelProduct.add(product1);
        arrDelProduct.add(product3);

        task = task.deleteProducts(arrDelProduct);

        assertEquals(1, task.getProcessedProducts().size());
        assertEquals(0, task.getTotalCountOfProduct(product1),0);
        assertEquals(5, task.getTotalCountOfProduct(product2),0);
        assertEquals(0, task.getTotalCountOfProduct(product3),0);
    }

}

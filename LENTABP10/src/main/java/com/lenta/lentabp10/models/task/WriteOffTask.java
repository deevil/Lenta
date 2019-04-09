package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.shared.models.core.IProduct;
import com.lenta.shared.models.core.ProductInfo;

import java.util.List;

public class WriteOffTask {

    private TaskDescription taskDescription;
    private ITaskRepository taskRepository;

    public WriteOffTask(TaskDescription taskDescription, ITaskRepository taskRepository){
        this.taskDescription = taskDescription;
        this.taskRepository = taskRepository;
    }


    public WriteOffTask deleteProducts(List<ProductInfo> products) {
        return null;
    }

    public ProcessGeneralProductService processGeneralProduct(IProduct product) {
        return null;
    }

    public ProcessNonExciseAlcoProductService processNonExciseAlcoProduct(IProduct product) {
        return null;
    }

    public ProcessExciseAlcoProductService processExciseAlcoProduct(IProduct product) {
        return null;
    }

    public int TotalCountOfProduct(ProductInfo product) {
        return 0;
    }

    public TaskSaveModel getTaskSaveModel() {
        return null;
    }

    void clearTask() {

    }
}

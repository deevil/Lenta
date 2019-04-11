package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.IProcessProductService;
import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.shared.models.core.ProductInfo;

public class ProcessExciseAlcoProductService implements IProcessProductService {

    private TaskDescription taskDescription;
    private ITaskRepository taskRepository;
    private ProductInfo productInfo;

    public ProcessExciseAlcoProductService(TaskDescription taskDescription, ITaskRepository taskRepository, ProductInfo productInfo) {
        this.taskDescription = taskDescription;
        this.taskRepository = taskRepository;
        this.productInfo = productInfo;
    }

    @Override
    public int getTotalCount() {
        return 0;
    }

    @Override
    public WriteOffTask apply() {
        //todo сохранить текущую сессию
        return new WriteOffTask(taskDescription, taskRepository);
    }

    @Override
    public WriteOffTask discard() {
        return new WriteOffTask(taskDescription, taskRepository);
    }

    public ProcessGeneralProductService add(WriteOffReason reason, int count, TaskExciseStamp stamp) {
        return null;
    }
}
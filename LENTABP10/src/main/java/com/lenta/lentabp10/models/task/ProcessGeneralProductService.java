package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.IProcessProductService;
import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.shared.models.core.ProductInfo;

public class ProcessGeneralProductService implements IProcessProductService {

    private TaskDescription taskDescription;
    private ITaskRepository taskRepository;
    private ProductInfo productInfo;

    public ProcessGeneralProductService(TaskDescription taskDescription, ITaskRepository taskRepository, ProductInfo productInfo) {
        this.taskDescription = taskDescription;
        this.taskRepository = taskRepository;
        this.productInfo = productInfo;
    }

    @Override
    public int getTotalCount() {
        //todo по данному продукту сумма причин списания
        return 0;
    }

    @Override
    public WriteOffTask apply() {
        return new WriteOffTask(taskDescription, taskRepository);
    }

    @Override
    public WriteOffTask discard() {
        return new WriteOffTask(taskDescription, taskRepository);
    }

    public ProcessGeneralProductService add(WriteOffReason reason, int count) {
        //todo в репозитории найти причину спичания для данного товара если есть, то увеличить count иначе создать новый
        return null;
    }
}

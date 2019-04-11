package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.IProcessProductService;
import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.shared.models.core.ProductInfo;

import java.util.List;

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
    public double getTotalCount() {
        //todo (Артем И., 09.04.2019) по данному продукту ИТОГО причин списания + кол-во марок
        List<TaskWriteOffReason> arrTaskWriteOffReason = taskRepository.getWriteOffReasons().findWriteOffReasonsOfProduct(productInfo);
        List<TaskExciseStamp> arrTaskExciseStamp = taskRepository.getExciseStamps().findExciseStampsOfProduct(productInfo);
        double totalCount = 0;
        for(int i=0; i<arrTaskWriteOffReason.size(); i++) {
            totalCount = totalCount + arrTaskWriteOffReason.get(i).getCount();

        }

        totalCount = totalCount + arrTaskExciseStamp.size();

        return totalCount;
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

    public ProcessExciseAlcoProductService add(WriteOffReason reason, double count, TaskExciseStamp stamp) {
        //todo добавить товар если его нету в таске товаров, в репозитории найти причину списания для данного товара, если есть, то увеличить count иначе создать новый, добавить марку
        TaskWriteOffReason taskWriteOfReason = new TaskWriteOffReason(reason, productInfo.getMaterialNumber(), count);
        if ( taskRepository.getProducts().findProduct(productInfo) == null ) {
            taskRepository.getProducts().addProduct(productInfo);
            taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason);
        }
        else  {
            List<TaskWriteOffReason> arrTaskWriteOffReason = taskRepository.getWriteOffReasons().findWriteOffReasonsOfProduct(productInfo);
            int index = -1;
            for(int i=0; i<arrTaskWriteOffReason.size(); i++) {
                if ( reason == arrTaskWriteOffReason.get(i).getWriteOffReason() ) {
                    taskRepository.getWriteOffReasons().deleteWriteOffReason(taskWriteOfReason);
                    double newCount;
                    newCount = arrTaskWriteOffReason.get(i).getCount() + count;
                    taskWriteOfReason = new TaskWriteOffReason(reason, productInfo.getMaterialNumber(), newCount);
                    taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason);
                    index = i;
                }
            }

            if (index == -1)
            {
                taskRepository.getWriteOffReasons().addWriteOffReason(taskWriteOfReason);
            }

        }
        taskRepository.getExciseStamps().addExciseStamp(stamp);
        return new ProcessExciseAlcoProductService(taskDescription, taskRepository, productInfo);
    }
}

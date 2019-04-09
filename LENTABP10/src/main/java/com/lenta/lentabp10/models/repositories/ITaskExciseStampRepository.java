package com.lenta.lentabp10.models.repositories;

import com.lenta.lentabp10.models.task.TaskExciseStamp;
import com.lenta.shared.models.core.IProduct;

import java.util.List;

public interface ITaskExciseStampRepository {
    List<TaskExciseStamp> findExciseStampsOfProduct(IProduct product);

    boolean addExciseStamp(TaskExciseStamp exciseStamp);
    boolean deleteExciseStamp(TaskExciseStamp exciseStamp);
    boolean deleteExciseStampsForProduct(IProduct product);
    boolean addExciseStamps(List<TaskExciseStamp> exciseStamps150);
    boolean deleteExciseStamps(List<TaskExciseStamp> exciseStamps150);
    void clear();

    TaskExciseStamp get(int index);
    int lenght();
}

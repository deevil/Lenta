package com.lenta.lentabp10.models.repositories;

import com.lenta.lentabp10.models.task.TaskWriteOfReason;
import com.lenta.shared.models.core.IProduct;

import java.util.List;

public interface ITaskWriteOffReasonRepository {
    List<TaskWriteOfReason> findWriteOfReasonsOfProduct(IProduct product);

    boolean addWriteOfReason(TaskWriteOfReason writeOfReason);
    boolean deeleteWriteOfReason(TaskWriteOfReason writeOfReason);
    boolean deleteWriteOfReasonsForProduct(IProduct product);
    void clear();

    TaskWriteOfReason get(int index);
    int lenght();
}

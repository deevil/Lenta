package com.lenta.lentabp10.models.repositories;

import com.lenta.lentabp10.models.task.TaskWriteOffReason;
import com.lenta.shared.models.core.IProduct;

import java.util.List;

public interface ITaskWriteOffReasonRepository {
    List<TaskWriteOffReason> findWriteOffReasonsOfProduct(IProduct product);

    boolean addWriteOffReason(TaskWriteOffReason writeOffReason);
    boolean deleteWriteOffReason(TaskWriteOffReason writeOffReason);
    boolean deleteWriteOffReasonsForProduct(IProduct product);
    void clear();

    TaskWriteOffReason get(int index);
    int lenght();
}

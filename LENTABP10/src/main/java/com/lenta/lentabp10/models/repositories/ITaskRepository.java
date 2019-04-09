package com.lenta.lentabp10.models.repositories;

public interface ITaskRepository {
    ITaskProductRepository getProducts();
    ITaskExciseStampRepository getExciseStamps();
    ITaskWriteOffReasonRepository gettReasonWriteoff();
}

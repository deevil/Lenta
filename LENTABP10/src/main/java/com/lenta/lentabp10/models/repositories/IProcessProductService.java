package com.lenta.lentabp10.models.repositories;

import com.lenta.lentabp10.models.task.WriteOffTask;

public interface IProcessProductService {
    int getTotalCount();
    WriteOffTask apply();
    WriteOffTask discard();
}

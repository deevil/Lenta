package com.lenta.lentabp10.models.memory;

import com.lenta.lentabp10.models.repositories.ITaskExciseStampRepository;
import com.lenta.lentabp10.models.repositories.ITaskProductRepository;
import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.lentabp10.models.repositories.ITaskWriteOffReasonRepository;

import lombok.Getter;

public class MemoryTaskRepository implements ITaskRepository {

    @Getter private ITaskProductRepository products;
    @Getter private ITaskExciseStampRepository exciseStamps;
    @Getter private ITaskWriteOffReasonRepository writeOffReasons;

    public MemoryTaskRepository(ITaskProductRepository taskProductRepository, ITaskExciseStampRepository taskExciseStampRepository, ITaskWriteOffReasonRepository taskWriteOfReasonRepository) {
        if (taskProductRepository == null)
        {
            throw new NullPointerException("taskProductRepository");
        }
        if (taskExciseStampRepository == null)
        {
            throw new NullPointerException("taskExciseStampRepository");
        }
        if (taskWriteOfReasonRepository == null)
        {
            throw new NullPointerException("taskWriteOfReasonRepository");
        }

        this.products = taskProductRepository;
        this.exciseStamps = taskExciseStampRepository;
        this.writeOffReasons = taskWriteOfReasonRepository;
    }

}

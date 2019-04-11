package com.lenta.lentabp10.models.memory;

import com.lenta.lentabp10.models.repositories.ITaskWriteOffReasonRepository;
import com.lenta.lentabp10.models.task.TaskWriteOffReason;
import com.lenta.shared.models.core.IProduct;

import java.util.ArrayList;
import java.util.List;

public class MemoryTaskWriteOffReasonRepository implements ITaskWriteOffReasonRepository {

    private final List<TaskWriteOffReason> arrWriteOffReason = new ArrayList<>();

    public MemoryTaskWriteOffReasonRepository() {
    }

    @Override
    public List<TaskWriteOffReason> findWriteOffReasonsOfProduct(IProduct product) {
        if (product == null)
        {
            throw new NullPointerException("product");
        }

        List<TaskWriteOffReason> foundWriteOffReason = new ArrayList<>();
        for(int i=0; i<arrWriteOffReason.size(); i++) {
            if ( product.getMaterialNumber() == arrWriteOffReason.get(i).getMaterialNumber() ) {
                foundWriteOffReason.add(arrWriteOffReason.get(i));
            }
        }
        return foundWriteOffReason;
    }

    @Override
    public boolean addWriteOffReason(TaskWriteOffReason writeOffReason) {
        if (writeOffReason == null)
        {
            throw new NullPointerException("writeOffReason");
        }

        int index = -1;
        for(int i=0; i<arrWriteOffReason.size(); i++) {
            if ( writeOffReason.getMaterialNumber() == arrWriteOffReason.get(i).getMaterialNumber()
                    && writeOffReason.getWriteOffReason().getCode() == arrWriteOffReason.get(i).getWriteOffReason().getCode() ) {
                index = i;
            }
        }

        if (index == -1)
        {
            arrWriteOffReason.add(writeOffReason);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteWriteOffReason(TaskWriteOffReason writeOffReason) {
        if (writeOffReason == null)
        {
            throw new NullPointerException("writeOffReason");
        }

        int index = -1;
        for(int i=0; i<arrWriteOffReason.size(); i++) {
            if ( writeOffReason.getMaterialNumber() == arrWriteOffReason.get(i).getMaterialNumber()
                    && writeOffReason.getWriteOffReason().getCode() == arrWriteOffReason.get(i).getWriteOffReason().getCode() ) {
                index = i;
            }
        }

        if (index == -1)
        {
            return false;
        }
        arrWriteOffReason.remove(index);
        return true;
    }

    @Override
    public boolean deleteWriteOffReasonsForProduct(IProduct product) {
        if (product == null)
        {
            throw new NullPointerException("product");
        }

        List<TaskWriteOffReason> delWriteOffReason = new ArrayList<>();
        for(int i=0; i<arrWriteOffReason.size(); i++) {
            if ( product.getMaterialNumber() == arrWriteOffReason.get(i).getMaterialNumber() ) {
                delWriteOffReason.add(arrWriteOffReason.get(i));
            }
        }

        if ( delWriteOffReason.isEmpty() )
        {
            return false;
        }

        arrWriteOffReason.removeAll(delWriteOffReason);
        return true;
    }

    @Override
    public void clear() {
        arrWriteOffReason.clear();
    }

    @Override
    public TaskWriteOffReason get(int index) {
        return arrWriteOffReason.get(index);
    }

    @Override
    public int lenght() {
        return arrWriteOffReason.size();
    }
}

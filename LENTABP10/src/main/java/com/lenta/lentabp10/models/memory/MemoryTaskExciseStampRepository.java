package com.lenta.lentabp10.models.memory;

import com.lenta.lentabp10.models.repositories.ITaskExciseStampRepository;
import com.lenta.lentabp10.models.task.TaskExciseStamp;
import com.lenta.shared.models.core.IProduct;

import java.util.ArrayList;
import java.util.List;

public class MemoryTaskExciseStampRepository implements ITaskExciseStampRepository {

    private final List<TaskExciseStamp> stamps = new ArrayList<>();

    public MemoryTaskExciseStampRepository() {
    }

    @Override
    public List<TaskExciseStamp> getExciseStamps() {
        return stamps;
    }

    @Override
    public List<TaskExciseStamp> findExciseStampsOfProduct(IProduct product) {
        if (product == null)
        {
            throw new NullPointerException("product");
        }

        List<TaskExciseStamp> foundStamps = new ArrayList<>();
        for(int i=0; i<stamps.size(); i++) {
            if ( product.getMaterialNumber() == stamps.get(i).getMaterialNumber() ) {
                foundStamps.add(stamps.get(i));
            }
        }
        return foundStamps;
    }

    @Override
    public boolean addExciseStamp(TaskExciseStamp exciseStamp) {
        if (exciseStamp == null)
        {
            throw new NullPointerException("exciseStamp");
        }

        int index = -1;
        for(int i=0; i<stamps.size(); i++) {
            // (Артем И., 05.04.2019) code (exciseStamp.getCode()) уникальный, поэтому сравнение делаем только по нему, по товару materialNumber не сравниваем
            if ( exciseStamp.getCode() == stamps.get(i).getCode() ) {
                index = i;
            }
        }

        if (index == -1)
        {
            stamps.add(exciseStamp);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteExciseStamp(TaskExciseStamp exciseStamp) {
        if (exciseStamp == null)
        {
            throw new NullPointerException("exciseStamp");
        }

        int index = -1;
        for(int i=0; i<stamps.size(); i++) {
            // (Артем И., 05.04.2019) code (exciseStamp.getCode()) уникальный, поэтому сравнение делаем только по нему, по товару materialNumber не сравниваем
            if ( exciseStamp.getCode() == stamps.get(i).getCode() ) {
                index = i;
            }
        }

        if (index == -1)
        {
            return false;
        }
        stamps.remove(index);
        return true;
    }

    @Override
    public boolean deleteExciseStampsForProduct(IProduct product) {
        if (product == null)
        {
            throw new NullPointerException("product");
        }

        List<TaskExciseStamp> deleteStamps = new ArrayList<>();
        for(int i=0; i<stamps.size(); i++) {
            if ( product.getMaterialNumber() == stamps.get(i).getMaterialNumber() ) {
                deleteStamps.add(stamps.get(i));
            }
        }

        if ( deleteStamps.isEmpty() )
        {
            return false;
        }

        stamps.removeAll(deleteStamps);
        return true;
    }

    @Override
    public boolean addExciseStamps(List<TaskExciseStamp> exciseStamps150) {
        if (exciseStamps150 == null)
        {
            throw new NullPointerException("exciseStamps150");
        }

        if ( exciseStamps150.isEmpty() )
        {
            return false;
        }

        List<TaskExciseStamp> distinctStamp = new ArrayList<>();
        for(int i=0; i<exciseStamps150.size(); i++) {
            /**if ( exciseStamps150.get(i).egaisVersion() != EgaisStampVersion.V3) {
                throw new IllegalStateException("exciseStamps150 should contains only excise stamp with lenght " + String.valueOf(EgaisStampVersion.V3));
            }*/
            //убираем дубликаты
            if ( !distinctStamp.contains(exciseStamps150.get(i)) ) {
                distinctStamp.add(exciseStamps150.get(i));
            }
        }

        stamps.addAll(distinctStamp);
        return true;
    }

    @Override
    public boolean deleteExciseStamps(List<TaskExciseStamp> exciseStamps150) {
        if (exciseStamps150 == null)
        {
            throw new NullPointerException("exciseStamps150");
        }

        if ( exciseStamps150.isEmpty() )
        {
            return false;
        }

        stamps.removeAll(exciseStamps150);
        return true;
    }

    @Override
    public void clear() {
        stamps.clear();
    }

    @Override
    public TaskExciseStamp get(int index) {
        return stamps.get(index);
    }

    @Override
    public int lenght() {
        return stamps.size();
    }
}

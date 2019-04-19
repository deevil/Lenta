package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.shared.models.core.ProductInfo;

import java.util.List;

public class TaskSaveModel {

    private TaskDescription taskDescription;
    private ITaskRepository taskRepository;

    public TaskSaveModel(TaskDescription taskDescription, ITaskRepository taskRepository) {
        this.taskDescription = taskDescription;
        this.taskRepository = taskRepository;
    }

    public String getPerNo(){
        return taskDescription.getPerNo();
    }

    public String getPrinter(){
        return taskDescription.getPrinter();
    }

    public String getTaskName(){
        return taskDescription.getTaskName();
    }

    public TaskType getTaskType(){
        return taskDescription.getTaskType();
    }

    public String getTkNumber(){
        return taskDescription.getTkNumber();
    }

    public String getStorloc(){
        return taskDescription.getStock();
    }

    public String getIpAdress(){
        return taskDescription.getIpAdress();
    }

    public List<String> getMoveTypes(){
        return taskDescription.getMoveTypes();
    }

    public List<String> getGisControls(){
        return taskDescription.getGisControls();
    }

    public List<String> getMaterialTypes(){
        return taskDescription.getMaterialTypes();
    }

    public List<ProductInfo> getMaterials(){
        return taskRepository.getProducts().getProducts();
    }

    public List<TaskWriteOffReason> getWriteOffReasons(){
        return taskRepository.getWriteOffReasons().getWriteOffReasons();
    }

    public List<TaskExciseStamp> getExciseStamps(){
        return taskRepository.getExciseStamps().getExciseStamps();
    }

}

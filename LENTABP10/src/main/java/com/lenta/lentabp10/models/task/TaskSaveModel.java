package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.ITaskRepository;

public class TaskSaveModel {

    private TaskDescription taskDescription;
    private ITaskRepository taskRepository;

    public TaskSaveModel(TaskDescription taskDescription, ITaskRepository taskRepository) {
        this.taskDescription = taskDescription;
        this.taskRepository = taskRepository;
    }
}

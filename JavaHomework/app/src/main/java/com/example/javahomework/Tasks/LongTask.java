package com.example.javahomework.Tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LongTask extends BaseTask {
    private List<SubTask> subTasks;

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public LongTask() {
        subTasks = new ArrayList<>();
    }

    public void addSubTask(SubTask subTask) {
        subTasks.add(subTask);
    }

    public void removeSubTask(SubTask subTask) {
        subTasks.remove(subTask);
    }
}

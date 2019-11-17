package com.example.javahomework.Tasks;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TaskList extends LitePalSupport implements Serializable {
    private String name;
    private String type;
    private String selfID;  // selfID for self

    public String getSelfID() {
        return selfID;
    }

    public void setSelfID(String selfID) {
        this.selfID = selfID;
    }

    private List<BaseTask> baseTasks;

    public TaskList(String name, String type) {
        this.name = name;
        this.type = type;
        baseTasks = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<BaseTask> getBaseTasks() {
        return baseTasks;
    }

    public void setBaseTasks(List<BaseTask> baseTasks) {
        this.baseTasks = baseTasks;
    }

    public void addTask(BaseTask baseTask) {
        this.baseTasks.add(baseTask);
    }

    public void removeTask(BaseTask baseTask) {
        this.baseTasks.remove(baseTask);
    }

}

package com.example.javahomework.Tasks;


import org.litepal.crud.LitePalSupport;

public class BaseTask extends LitePalSupport {
    public final static int FINISHED = 0;
    public final static int UNFINISHED = 1;


    private String name;
    private String description;
    private String deadline;
    private String fatherID;        // 父任务/列表ID
    private String selfID;          // 自身ID

    public String getFatherID() {
        return fatherID;
    }

    public String getSelfID() {
        return selfID;
    }

    public void setSelfID(String selfID) {
        this.selfID = selfID;
    }

    public void setFatherID(String fatherID) {
        this.fatherID = fatherID;
    }


    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    private int status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}

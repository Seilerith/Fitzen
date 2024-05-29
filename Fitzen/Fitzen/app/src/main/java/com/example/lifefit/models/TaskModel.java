package com.example.lifefit.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class TaskModel {
    private String prefix;
    private String description;
    final private Date createdAt;

    public TaskModel(String prefix, String description) {
        this.prefix = prefix;
        this.description = description;
        this.createdAt = Calendar.getInstance().getTime();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
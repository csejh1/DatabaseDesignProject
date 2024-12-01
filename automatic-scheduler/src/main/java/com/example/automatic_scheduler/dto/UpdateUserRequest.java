package com.example.automatic_scheduler.dto;

public class UpdateUserRequest {
    private int sleep_time;
    private String bedtime;

    // Getter 및 Setter
    public int getSleep_time() {
        return sleep_time;
    }

    public void setSleep_time(int sleep_time) {
        this.sleep_time = sleep_time;
    }

    public String getBedtime() {
        return bedtime;
    }

    public void setBedtime(String bedtime) {
        this.bedtime = bedtime;
    }
}

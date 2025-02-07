package com.fiap.video.core.domain;

public class VideoMessage {

    private String id;
    private String user;
    private String email;
    private String videoKeyS3;
    private Integer intervalSeconds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public String getVideoKeyS3() {
        return videoKeyS3;
    }

    public void setVideoKeyS3(String videoKeyS3) {
        this.videoKeyS3 = videoKeyS3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

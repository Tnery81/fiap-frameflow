package com.fiap.video.core.domain;

public class VideoMessage {

    private Long id;
    private String user;
    private String email;
    private String videoKeyS3;
    private Integer intervalSeconds;

    public Long getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public String getVideoKeyS3() {
        return videoKeyS3;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }
}

package com.fiap.video.core.domain;

import java.time.Duration;

public class Video {
    private String path;
    private Duration duration;

    public Video(String path, Duration duration) {
        this.path = path;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public Duration getDuration() {
        return duration;
    }
}

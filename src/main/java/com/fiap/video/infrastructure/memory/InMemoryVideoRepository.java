package com.fiap.video.infrastructure.memory;

import com.fiap.video.core.domain.Video;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryVideoRepository {

    private final Map<String, Video> videoStore = new HashMap<>();

    public void save(Video video) {
        videoStore.put(video.getPath(), video);
    }

    public Video findByPath(String path) {
        return videoStore.getOrDefault(path, null);
    }
}

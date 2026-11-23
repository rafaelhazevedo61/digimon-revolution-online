package com.dro.modules.tutorial.api.dto.response;

import java.util.List;

public record TutorialGuideResponse(TutorialProgressResponse initial, int readTopics, int totalTopics, List<Topic> topics) {
    public record Topic(String code, String group, String title, String description, String route,
                        boolean available, String unlockHint, boolean read) {}
}

package com.example.eduaceai.dto.res;

import java.util.List;

public record LearningRoadmapResponse(
        String overallComment,
        List<WeakTopic> weakTopics,
        List<StudyStep> studyPlan,
        String nextStepSuggestion
) {
    public record WeakTopic(String topic, int wrongCount, String priority) {}

    public record StudyStep(int day, String topic, String goal, String practice) {}
}

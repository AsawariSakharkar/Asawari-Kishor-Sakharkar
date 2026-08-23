package com.example.data.model

enum class Emotion(val displayName: String, val description: String) {
    ANXIOUS("Anxious", "Nervous, on edge, or uneasy"),
    OVERWHELMED("Overwhelmed", "Too much happening at once"),
    SAD("Sad", "Down, heavy, or low energy"),
    FRUSTRATED("Frustrated", "Stuck, irritable, or blocked"),
    EMBARRASSED("Embarrassed", "Replaying a past moment"),
    LONELY("Lonely", "Disconnected or isolated"),
    RESTLESS("Restless", "Unable to settle down"),
    UNCERTAIN("Uncertain", "Struggling with unknowns"),
    OTHER("Other", "Something else")
}

enum class Trigger(val displayName: String) {
    SOCIAL("Social interaction"),
    WORK_STUDY("Work/study"),
    RELATIONSHIP("Relationship"),
    FUTURE("Future"),
    DECISION("Decision"),
    SLEEP("Sleep"),
    SELF_IMAGE("Self-image"),
    OTHER("Other")
}

enum class ExerciseType(val id: String, val title: String, val durationMinutes: Int) {
    SLOW_BREATHING("slow_breathing", "Slow Breathing", 5),
    THOUGHT_CAPTURE("thought_capture", "First Thought Capture", 1),
    LET_IT_PASS("let_it_pass", "Let It Pass", 2),
    GROUNDING_333("grounding_333", "3-3-3 Grounding", 3),
    GROUNDING_54321("grounding_54321", "5-4-3-2-1 Grounding", 4),
    WALK("walk", "Five-Minute Walk", 5),
    BELLY_BREATHING("belly_breathing", "Belly Breathing", 3),
    MEDITATION("meditation", "Mindful Meditation", 5),
    IDENTIFY_LOOP("identify_loop", "Identify Overthinking", 3),
    QUESTION_THOUGHT("question_thought", "Question the Thought", 3),
    CHALLENGE_THOUGHT("challenge_thought", "Challenge a Thought", 4)
}

enum class EndFeeling(val displayName: String, val score: Int) {
    WORSE("Worse", 1),
    THE_SAME("The same", 2),
    A_LITTLE_LIGHTER("A little lighter", 3),
    MUCH_LIGHTER("Much lighter", 4)
}

enum class HelpfulPart(val displayName: String) {
    BREATHING("Breathing"),
    WRITING("Writing"),
    LET_IT_PASS("Let it pass"),
    GROUNDING("Grounding"),
    WALKING("Walking"),
    NOT_SURE("Not sure")
}

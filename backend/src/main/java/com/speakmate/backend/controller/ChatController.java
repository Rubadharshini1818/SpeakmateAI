package com.speakmate.backend.controller;

import com.speakmate.backend.model.entity.ChatMessage;
import com.speakmate.backend.model.entity.User;
import com.speakmate.backend.repository.ChatMessageRepository;
import com.speakmate.backend.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final GeminiService geminiService;

    // ─── GET /api/chat/history ────────────────────────────────────────────────
    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@AuthenticationPrincipal User user) {
        List<ChatMessage> history = chatMessageRepository.findByUserOrderByCreatedAtAsc(user);

        List<Map<String, Object>> response = history.stream().map(msg -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", msg.getId());
            map.put("sender", msg.getSender());
            map.put("content", msg.getContent());
            map.put("createdAt", msg.getCreatedAt().toString());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ─── POST /api/chat/send  (text message) ──────────────────────────────────
    @PostMapping("/send")
    public ResponseEntity<?> sendTextMessage(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> payload) {

        String userText = payload.get("message");
        if (userText == null || userText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message content cannot be empty"));
        }
        userText = userText.trim();

        // 1. Fetch existing history BEFORE saving the new user message
        //    so we can pass it as prior context to Gemini
        List<ChatMessage> priorHistory = chatMessageRepository.findByUserOrderByCreatedAtAsc(user);

        // 2. Save the user's message
        ChatMessage userMsg = ChatMessage.builder()
                .user(user)
                .sender("USER")
                .content(userText)
                .build();
        chatMessageRepository.save(userMsg);

        // 3. Generate Luna's reply — passing the prior history + the actual user message
        String aiReply = generateLunaReply(user, priorHistory, userText);

        // 4. Save Luna's reply
        ChatMessage aiMsg = ChatMessage.builder()
                .user(user)
                .sender("AI")
                .content(aiReply)
                .build();
        chatMessageRepository.save(aiMsg);

        return ResponseEntity.ok(Map.of(
                "userText", userText,
                "aiText", aiReply
        ));
    }

    // ─── POST /api/chat/voice  (voice note — Gemini transcribes then replies) ─
    @PostMapping("/voice")
    public ResponseEntity<?> sendVoiceMessage(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Audio file is required"));
        }

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "audio/webm";

            // 1. Transcribe the audio via Gemini
            String userText = geminiService.transcribeAudio(bytes, contentType);
            if (userText == null || userText.isBlank() || userText.toLowerCase().contains("could not transcribe")) {
                userText = "[Voice message — could not transcribe]";
            }

            // 2. Fetch prior history before saving
            List<ChatMessage> priorHistory = chatMessageRepository.findByUserOrderByCreatedAtAsc(user);

            // 3. Save the transcribed user message
            ChatMessage userMsg = ChatMessage.builder()
                    .user(user)
                    .sender("USER")
                    .content(userText)
                    .build();
            chatMessageRepository.save(userMsg);

            // 4. Generate Luna's reply
            String aiReply = generateLunaReply(user, priorHistory, userText);

            // 5. Save Luna's reply
            ChatMessage aiMsg = ChatMessage.builder()
                    .user(user)
                    .sender("AI")
                    .content(aiReply)
                    .build();
            chatMessageRepository.save(aiMsg);

            return ResponseEntity.ok(Map.of(
                    "userText", userText,
                    "aiText", aiReply
            ));

        } catch (Exception e) {
            log.error("Failed to process voice message: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Failed to process audio message"));
        }
    }

    // ─── DELETE /api/chat/clear ───────────────────────────────────────────────
    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearChatHistory(@AuthenticationPrincipal User user) {
        chatMessageRepository.deleteByUser(user);
        return ResponseEntity.ok(Map.of("message", "Conversation history cleared successfully"));
    }

    // ─── POST /api/chat/ask-teacher  (AI Classroom doubt box) ────────────────
    @PostMapping("/ask-teacher")
    public ResponseEntity<?> askTeacher(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> payload) {

        String question = payload.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Question cannot be empty"));
        }

        String systemInstruction =
                "You are an expert English grammar, vocabulary, and pronunciation teacher. " +
                "A student is asking you a question. " +
                "Provide a concise, clear, and encouraging explanation. Use examples where helpful. " +
                "Keep the answer focused — no more than 4–5 sentences unless the topic truly requires more.";

        String answer = geminiService.generateContent(systemInstruction, "Student asks: " + question.trim());

        return ResponseEntity.ok(Map.of(
                "question", question.trim(),
                "answer", answer
        ));
    }

    // ─── Internal: build Luna's reply ────────────────────────────────────────
    /**
     * Builds a Luna reply using:
     *  - systemInstruction  : Luna's personality, correction rules, level context
     *  - priorHistory       : the conversation so far (before the new user message)
     *  - userText           : the new user message (last turn)
     *
     * GeminiService.generateConversation() passes these as proper multi-turn
     * `contents` so Gemini sees the full dialogue, not just a text dump.
     */
    private String generateLunaReply(User user, List<ChatMessage> priorHistory, String userText) {

        String systemInstruction = """
                You are Luna — a warm, encouraging, and witty AI friend who helps Indian learners practice conversational English naturally.
                The user's current English level is %s.

                Your personality:
                - Friendly, upbeat, and genuinely interested in the user's life
                - You ask one natural follow-up question to keep the conversation flowing
                - You share your own (fictional) thoughts and opinions to make it feel like a real friendship
                - Respond concisely — 2–4 sentences max — so it feels like a real back-and-forth chat
                - You NEVER lecture or give formal lessons unless the user explicitly asks

                Grammar correction rule (VERY IMPORTANT):
                - If the user makes a clear grammar mistake, weave a gentle correction naturally into your reply
                - Do NOT say "You made an error" or "Wrong sentence"
                - Instead say something like: "By the way, just a small tip — we say '...' instead of '...' 😊" and then continue talking normally
                - Only correct ONE mistake per message, never everything at once
                - If the user's message is grammatically fine, do NOT mention grammar at all

                Respond as Luna. Be natural, warm, and human. Do not prefix your reply with "Luna:".
                """.formatted(user.getLearningLevel());

        // Limit history to last 20 exchanges (40 messages) to stay within token limits
        int start = Math.max(0, priorHistory.size() - 40);
        List<ChatMessage> recentHistory = priorHistory.subList(start, priorHistory.size());

        return geminiService.generateConversation(systemInstruction, recentHistory, userText);
    }
}

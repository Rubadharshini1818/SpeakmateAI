package com.speakmate.backend.service;

import com.speakmate.backend.model.entity.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * AI service backed by the Groq API (OpenAI-compatible endpoint).
 *
 * Public method signatures are intentionally kept identical to the original
 * GeminiService so every controller using this class requires zero changes.
 *
 *  generateContent(systemInstruction, prompt)
 *      → single-turn: dashboard tip, ask-teacher, interview, GD, MCQ, etc.
 *
 *  generateConversation(systemInstruction, history, newUserMessage)
 *      → multi-turn: AI Friend Chat (Luna)
 *
 *  transcribeAudio(bytes, mimeType)
 *      → Groq free tier does not expose a public audio transcription endpoint,
 *        so this method returns a clear "use browser speech recognition" message.
 *        The friend.html frontend already handles this via SpeechRecognition API.
 */
@Service
@Slf4j
public class GeminiService {

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Single-turn content generation.
     * Used by: Dashboard, Ask-Teacher, Interview, GD, MCQ, Weekly Assessment.
     */
    public String generateContent(String systemInstruction, String prompt) {
        if (isKeyMissing()) {
            log.warn("Groq API key not configured — returning fallback response.");
            return getFallbackResponse(systemInstruction, prompt);
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (StringUtils.hasText(systemInstruction)) {
                messages.add(Map.of("role", "system", "content", systemInstruction));
            }
            messages.add(Map.of("role", "user", "content", prompt));

            return callGroq(messages);
        } catch (Exception e) {
            log.error("Groq generateContent error: {}", e.getMessage(), e);
            return getFallbackResponse(systemInstruction, prompt);
        }
    }

    /**
     * Multi-turn conversation generation.
     * Used by: AI Friend Chat (Luna).
     *
     * Maps ChatMessage history to OpenAI-style user/assistant roles,
     * then appends the new user message as the final turn.
     */
    public String generateConversation(String systemInstruction,
                                       List<ChatMessage> history,
                                       String newUserMessage) {
        if (isKeyMissing()) {
            log.warn("Groq API key not configured — returning fallback conversation response.");
            return getFallbackConversationResponse();
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();

            // System instruction (Luna's persona + rules)
            if (StringUtils.hasText(systemInstruction)) {
                messages.add(Map.of("role", "system", "content", systemInstruction));
            }

            // Prior conversation history
            for (ChatMessage msg : history) {
                String role = msg.getSender().equals("USER") ? "user" : "assistant";
                messages.add(Map.of("role", role, "content", msg.getContent()));
            }

            // The new user message is always the final turn
            messages.add(Map.of("role", "user", "content", newUserMessage));

            return callGroq(messages);
        } catch (Exception e) {
            log.error("Groq generateConversation error: {}", e.getMessage(), e);
            return getFallbackConversationResponse();
        }
    }

    /**
     * Audio transcription.
     * Groq's free tier does not expose a public speech-to-text endpoint
     * via the chat completions API. The frontend already handles voice input
     * through the browser's built-in SpeechRecognition API (Web Speech API),
     * which sends transcribed text to /api/chat/send — so this path is only
     * hit if a raw audio blob is explicitly POSTed to /api/chat/voice.
     */
    public String transcribeAudio(byte[] audioBytes, String mimeType) {
        // Return a graceful message — the frontend SpeechRecognition handles
        // transcription client-side and sends text, not audio blobs.
        log.info("transcribeAudio called — browser SpeechRecognition handles this client-side.");
        return "Please use the microphone button to speak. Your voice will be captured directly in the browser.";
    }

    // ─── Core HTTP call ──────────────────────────────────────────────────────

    /**
     * Sends a messages array to the Groq Chat Completions endpoint and
     * returns the assistant's reply text.
     *
     * Groq's API is fully OpenAI-compatible:
     *   POST https://api.groq.com/openai/v1/chat/completions
     *   Authorization: Bearer <key>
     *   { "model": "...", "messages": [...], "temperature": 0.85, "max_tokens": 512 }
     */
    private String callGroq(List<Map<String, String>> messages) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.85);
        requestBody.put("max_tokens", 2048);
        requestBody.put("top_p", 0.95);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    apiUrl, new HttpEntity<>(requestBody, headers), Map.class);

            String text = extractText(resp.getBody());
            if (text == null) {
                log.warn("Groq returned no text. Body: {}", resp.getBody());
                return getFallbackConversationResponse();
            }
            log.debug("Groq response received ({} chars)", text.length());
            return text;

        } catch (HttpClientErrorException e) {
            log.error("Groq API client error: HTTP {} — {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("Groq API server error: HTTP {} — {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    // ─── Response parsing ────────────────────────────────────────────────────

    /**
     * Navigates the OpenAI-compatible response:
     *   choices[0].message.content
     */
    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> body) {
        try {
            if (body == null) return null;
            List<?> choices = (List<?>) body.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map<?, ?> choice  = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            if (message == null) return null;

            String content = (String) message.get("content");
            return content != null ? content.trim() : null;
        } catch (Exception e) {
            log.error("Failed to parse Groq response: {}", e.getMessage());
            return null;
        }
    }

    // ─── Guards ──────────────────────────────────────────────────────────────

    private boolean isKeyMissing() {
        return !StringUtils.hasText(apiKey)
                || "YOUR_GROQ_API_KEY".equals(apiKey)
                || apiKey.isBlank();
    }

    // ─── Fallback responses ──────────────────────────────────────────────────

    private String getFallbackConversationResponse() {
        String[] replies = {
                "That's really interesting! Tell me more — what made you feel that way? 😊",
                "Ha, I love that! I think about similar things sometimes. What do you usually do in situations like that?",
                "Oh nice! English is all about practice and you're doing great just by showing up. What topic would you like to explore today?",
                "That sounds fun! If you could go anywhere in the world, where would it be?",
                "Great point! I completely agree. What else has been on your mind lately?"
        };
        return replies[new Random().nextInt(replies.length)];
    }

    private String getFallbackResponse(String systemInstruction, String prompt) {
        if (systemInstruction == null) systemInstruction = "";
        String si = systemInstruction.toLowerCase();
        String p  = prompt != null ? prompt.toLowerCase() : "";

        if (si.contains("friend") || si.contains("luna")) {
            return getFallbackConversationResponse();
        }
        if (si.contains("teacher") || si.contains("grammar") || si.contains("vocabulary")) {
            return "Great question! Could you give me an example sentence? That way I can explain the rule in context.";
        }
        if (si.contains("interview") || p.contains("interview")) {
            return "Tell me about a challenge you faced and how you resolved it.";
        }
        if (si.contains("tip") || si.contains("mentor") || si.contains("suggest")) {
            return "Keep up the momentum! Try the <strong>Tenses</strong> lesson today to sharpen your grammar confidence.";
        }
        return "That's a great point. Let's practice saying it clearly and focus on the grammar structure.";
    }
}

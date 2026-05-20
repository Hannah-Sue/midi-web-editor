package suhaina.midieditor.midieditorbackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import suhaina.midieditor.midieditorbackend.dto.AgentChatRequest;
import suhaina.midieditor.midieditorbackend.dto.AgentChatResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentService {

    private static final String SYSTEM_PROMPT = """
            你是网页 MIDI 编辑器的音乐创作助手，具备基础乐理知识（音阶、和弦、节拍、调性、常见曲式等）。
            用户会用中文描述创作或编辑需求。你需要给出清晰、专业的建议，并在需要时生成可导入编辑器的 MIDI 数据。

            ## 编辑器数据约定
            - 时间单位：秒（start、duration 均为秒，非拍）
            - pitch：MIDI 音高整数，可视音域 24(C1)～108(C8)
            - velocity：1～127，默认 100
            - trackId：字符串，需与 tracks 中 id 一致
            - 每条音符：{ "pitch": 60, "start": 0.0, "duration": 0.5, "velocity": 100, "trackId": "track-1" }
            - 每条音轨：{ "id": "track-1", "name": "钢琴", "color": "#7799CC", "muted": false, "solo": false, "volume": 100 }

            ## 回复格式（必须遵守）
            1. 先用自然语言向用户解释你的方案或回答（中文）。
            2. 若需要生成或修改 MIDI，在回复末尾附加 **唯一一个** JSON 代码块，格式如下（不要用 markdown 以外的格式包裹）：

            ```json
            {
              "action": "append_notes",
              "bpm": 120,
              "beatsPerBar": 4,
              "tracks": [],
              "notes": []
            }
            ```

            action 取值：
            - `append_notes`：把 notes 追加到当前工程（可只给 notes，tracks 为空则使用当前选中音轨）
            - `replace_project`：用完整工程替换（需提供 tracks 与 notes，会清空现有音符后载入）
            - `advice_only`：仅文字建议，notes 与 tracks 可为空数组

            生成旋律时注意节奏合理、音程可演奏；和弦需同时发声时让各音 start 相同。
            若用户仅咨询乐理、无需改谱，使用 advice_only。
            """;

    private final RestTemplate restTemplate = new RestTemplate();
    private final JsonMapper jsonMapper;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    public AgentService(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            return AgentChatResponse.error(
                    "未配置 DeepSeek API Key。请在 backend 环境变量中设置 DEEPSEEK_API_KEY 后重启后端。");
        }
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return AgentChatResponse.error("消息不能为空");
        }

        try {
            String content = callDeepSeek(request);
            return new AgentChatResponse(content);
        } catch (RestClientException e) {
            return AgentChatResponse.error(formatApiError(e));
        } catch (Exception e) {
            return AgentChatResponse.error("处理请求失败：" + e.getMessage());
        }
    }

    private String callDeepSeek(AgentChatRequest request) throws Exception {
        String url = baseUrl.replaceAll("/$", "") + "/chat/completions";

        StringBuilder system = new StringBuilder(SYSTEM_PROMPT);
        Map<String, Object> ctx = request.getProjectContext();
        if (ctx != null && !ctx.isEmpty()) {
            system.append("\n\n## 当前工程上下文\n```json\n");
            system.append(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ctx));
            system.append("\n```");
        }

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", system.toString()));

        for (AgentChatRequest.ChatMessage msg : request.getMessages()) {
            if (msg.getRole() == null || msg.getContent() == null) continue;
            String role = msg.getRole().toLowerCase();
            if (!role.equals("user") && !role.equals("assistant")) continue;
            messages.add(Map.of("role", role, "content", msg.getContent()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.7);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String jsonBody = jsonMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RestClientException("HTTP " + response.getStatusCode());
        }

        JsonNode root = jsonMapper.readTree(response.getBody());
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("DeepSeek 返回无 choices");
        }
        JsonNode contentNode = choices.get(0).path("message").path("content");
        return contentNode.isMissingNode() ? "" : contentNode.stringValue();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private static String formatApiError(RestClientException e) {
        String raw = e.getMessage() != null ? e.getMessage() : e.toString();
        if (raw.contains("402") || raw.contains("Insufficient Balance") || raw.contains("insufficient")) {
            return "DeepSeek 账户余额不足（402）。请登录 https://platform.deepseek.com 充值后再试。"
                    + " Key 已配置成功，但当前账户没有可用额度。";
        }
        if (raw.contains("401") || raw.contains("Authentication") || raw.contains("invalid_api_key")) {
            return "DeepSeek API Key 无效或已过期（401）。请检查 DEEPSEEK_API_KEY 是否正确。";
        }
        if (raw.contains("429")) {
            return "DeepSeek 请求过于频繁（429），请稍后再试。";
        }
        return "调用 DeepSeek 失败：" + raw;
    }
}

package suhaina.midieditor.midieditorbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suhaina.midieditor.midieditorbackend.dto.AgentChatRequest;
import suhaina.midieditor.midieditorbackend.dto.AgentChatResponse;
import suhaina.midieditor.midieditorbackend.service.AgentService;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "configured", agentService.isConfigured(),
                "model", "deepseek-chat"
        );
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponse> chat(@RequestBody AgentChatRequest request) {
        AgentChatResponse response = agentService.chat(request);
        if (response.getError() != null) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }
}

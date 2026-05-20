package suhaina.midieditor.midieditorbackend.dto;

import java.util.List;
import java.util.Map;

public class AgentChatRequest {

    private List<ChatMessage> messages;
    private Map<String, Object> projectContext;

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Map<String, Object> getProjectContext() {
        return projectContext;
    }

    public void setProjectContext(Map<String, Object> projectContext) {
        this.projectContext = projectContext;
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}

package suhaina.midieditor.midieditorbackend.dto;

public class AgentChatResponse {

    private String content;
    private String error;

    public AgentChatResponse() {
    }

    public AgentChatResponse(String content) {
        this.content = content;
    }

    public static AgentChatResponse error(String message) {
        AgentChatResponse r = new AgentChatResponse();
        r.setError(message);
        return r;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

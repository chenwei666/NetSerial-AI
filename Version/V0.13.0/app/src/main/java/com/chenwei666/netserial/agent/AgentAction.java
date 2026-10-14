package com.chenwei666.netserial.agent;

/** One validated model proposal. It is not execution authority. */
public final class AgentAction {
    public enum Type { COMMAND, ASK, FINISH, WEB_ACCESS }
    private final Type type;
    private final String command;
    private final String message;
    private final String username;
    private final boolean https;
    private final boolean http;

    AgentAction(Type type, String command, String message) {
        this(type, command, message, "", false, false);
    }

    AgentAction(Type type, String command, String message, String username, boolean https, boolean http) {
        this.type = type;
        this.command = command;
        this.message = message;
        this.username = username;
        this.https = https;
        this.http = http;
    }

    public Type getType() { return type; }
    public String getCommand() { return command; }
    public String getMessage() { return message; }
    public String getUsername() { return username; }
    public boolean isHttps() { return https; }
    public boolean isHttp() { return http; }
}

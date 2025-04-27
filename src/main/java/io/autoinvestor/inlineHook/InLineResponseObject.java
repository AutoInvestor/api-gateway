package io.autoinvestor.inlineHook;

import java.util.List;
import java.util.Map;

public class InLineResponseObject {

    private List<Map<String, Object>> commands;

    public List<Map<String, Object>> getCommands() {
        return commands;
    }

    public void setCommands(List<Map<String, Object>> commands) {
        this.commands = commands;
    }
}

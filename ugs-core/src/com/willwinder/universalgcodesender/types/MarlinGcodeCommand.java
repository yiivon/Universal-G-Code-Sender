package com.willwinder.universalgcodesender.types;


import com.willwinder.universalgcodesender.GrblUtils;

public class MarlinGcodeCommand extends GcodeCommand {
    public MarlinGcodeCommand(String command) {
        super(command);
    }

    public MarlinGcodeCommand(String command, int num) {
        super(command, num);
    }

    public MarlinGcodeCommand(String command, String originalCommand, String comment, int num) {
        super(command, originalCommand, comment, num);
    }

    public MarlinGcodeCommand(GcodeCommand command) {
        super(command.getCommandString(), command.getOriginalCommandString(), command.getComment(), command.getCommandNumber());
    }

    @Override
    public Boolean isDone() {
        return getResponse().endsWith("ok") || getResponse().startsWith("echo");
    }
}

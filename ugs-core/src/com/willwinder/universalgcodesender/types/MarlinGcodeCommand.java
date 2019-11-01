package com.willwinder.universalgcodesender.types;

public class MarlinGcodeCommand extends GcodeCommand {
    public MarlinGcodeCommand(String command) {
        super(command);
    }

    public MarlinGcodeCommand(String command, int num) {
        super(command, num);
    }

    public MarlinGcodeCommand(String command, String originalCommand, String comment, int num) {
        super(command, originalCommand, comment, num, false);
    }

    public MarlinGcodeCommand(GcodeCommand command) {
        super(command.getCommandString(), command.getOriginalCommandString(), command.getComment(), command.getCommandNumber(), false);
    }

    public static boolean isOkErrorResponse(String response) {
        return response.startsWith("ok");
    }

    public static boolean isEchoResponse(String response) {
        return response.startsWith("echo");
    }

    @Override
    public Boolean isDone() {
        return getResponse().endsWith("ok") || getResponse().startsWith("echo");
    }
}
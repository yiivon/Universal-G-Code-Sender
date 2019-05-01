package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.types.MarlinGcodeCommand;

public class MarlinCommunicator extends BufferedCommunicator {

    @Override
    public int getBufferSize() {
        return 254;
    }

    @Override
    public String getLineTerminator() {
        return "\n";
    }

    @Override
    protected boolean processedCommand(String response) {
        return MarlinGcodeCommand.isOkErrorResponse(response);
    }

    /**
     * Allows detecting errors and pausing the stream.
     */
    @Override
    protected boolean processedCommandIsError(String response) {
        return false;
    }

    @Override
    protected void sendingCommand(String response) {
        // no-op for this protocol.
    }
}

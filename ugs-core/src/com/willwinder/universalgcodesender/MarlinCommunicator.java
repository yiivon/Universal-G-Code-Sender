package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.MarlinGcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MarlinCommunicator extends AbstractCommunicator {
    private final ConcurrentLinkedDeque<GcodeCommand> commandDeque;
    private GcodeStreamReader gcodeStream;
    private GcodeCommand pendingCommand;
    private Set<MarlinCommunicatorListener> listeners = new HashSet<>();
    private UGSEvent.ControlState controlState = UGSEvent.ControlState.COMM_DISCONNECTED;

    public MarlinCommunicator() {
        commandDeque = new ConcurrentLinkedDeque<>();
    }

    @Override
    public boolean getSingleStepMode() {
        return true;
    }

    @Override
    public void setSingleStepMode(boolean enable) {
        // never mind
    }

    public void addListener(MarlinCommunicatorListener listener) {
        listeners.add(listener);
    }

    /**
     * Sends the command immidiatly
     *
     * @param command
     * @throws Exception
     */
    public void sendCommand(GcodeCommand command) throws Exception {
        commandDeque.addFirst(new MarlinGcodeCommand(command));
        if (!isStreaming()) {
            streamCommands();
        }
    }

    @Override
    public void queueStringForComm(String input) {
        GcodeCommand command = new MarlinGcodeCommand(input);
        commandDeque.addFirst(command);
    }

    @Override
    public void queueStreamForComm(GcodeStreamReader gcodeStream) {
        this.gcodeStream = gcodeStream;
    }

    @Override
    public void sendByteImmediately(byte b) throws Exception {
        // Never mind
    }

    @Override
    public String activeCommandSummary() {
        return "";
    }

    @Override
    public boolean areActiveCommands() {
        return !commandDeque.isEmpty();
    }

    @Override
    public synchronized void streamCommands() {
        if (controlState == UGSEvent.ControlState.COMM_SENDING ||
                controlState == UGSEvent.ControlState.COMM_SENDING_PAUSED) {
            return;
        }

        setControlState(UGSEvent.ControlState.COMM_SENDING);
        ThreadHelper.invokeLater(() -> {
            while (!commandDeque.isEmpty() || (gcodeStream != null && gcodeStream.getNumRowsRemaining() > 0)) {

                if (commandDeque.isEmpty() && gcodeStream.getNumRowsRemaining() > 0) {
                    try {
                        commandDeque.addLast(new MarlinGcodeCommand(gcodeStream.getNextCommand()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (pendingCommand == null && !isPaused()) {
                    pendingCommand = commandDeque.pop();
                    if (StringUtils.isEmpty(pendingCommand.getCommandString())) {
                        pendingCommand = null;
                    } else {
                        try {
                            conn.sendStringToComm(pendingCommand.getCommandString() + getLineTerminator());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            setControlState(UGSEvent.ControlState.COMM_IDLE);
        });
    }

    @Override
    public void pauseSend() {
        if (controlState == UGSEvent.ControlState.COMM_SENDING) {
            setControlState(UGSEvent.ControlState.COMM_SENDING_PAUSED);
        }
    }

    @Override
    public void resumeSend() {
        if (controlState == UGSEvent.ControlState.COMM_SENDING_PAUSED) {
            setControlState(UGSEvent.ControlState.COMM_SENDING);
        }
    }

    @Override
    public boolean isPaused() {
        return controlState == UGSEvent.ControlState.COMM_SENDING_PAUSED;
    }

    @Override
    public void cancelSend() {
        try {
            gcodeStream.close();
            gcodeStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        commandDeque.clear();
        pendingCommand = null;
    }

    @Override
    public void softReset() {

    }

    @Override
    public void responseMessage(String response) {
        if (pendingCommand != null) {
            System.out.println(pendingCommand.getCommandString() + " -> " + response);
            // If the command couldn't be processed
            if (StringUtils.startsWithIgnoreCase(response, "echo:busy: processing")) {
                pendingCommand.setResponse("");
                commandDeque.addFirst(pendingCommand);
                pendingCommand = null;
                return;
            }

            // If we got a response and have a pending command, append the response
            if (StringUtils.isEmpty(pendingCommand.getResponse())) {
                pendingCommand.setResponse(response);
            } else {
                pendingCommand.setResponse(pendingCommand.getResponse() + "\n" + response);
            }

            if (pendingCommand.isDone()) {
                ThreadHelper.invokeLater(() -> listeners.forEach(l -> l.onCommandComplete(pendingCommand)));
                pendingCommand = null;
            }
        } else {
            if (response.endsWith("start")) {
                ThreadHelper.invokeLater(() -> {
                    try {
                        sendCommand(new MarlinGcodeCommand("M999"));
                        listeners.forEach(l -> l.onCommandComplete(new GcodeCommand("start")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 2000);

            } else if (response.startsWith("echo:")) {
                ThreadHelper.invokeLater(() -> listeners.forEach(l -> l.onMessageReceived(response)));
            } else {
                System.out.println("We got unexpected data from controller: " + response);
            }
        }
    }

    @Override
    public int numActiveCommands() {
        return commandDeque.size() + gcodeStream.getNumRowsRemaining();
    }

    @Override
    public boolean isStreaming() {
        return controlState == UGSEvent.ControlState.COMM_SENDING;
    }

    @Override
    public void resetBuffersInternal() {
        commandDeque.clear();
    }

    @Override
    public String getLineTerminator() {
        return "\n";
    }

    private void setControlState(UGSEvent.ControlState controlState) {
        if (this.controlState != controlState) {
            this.controlState = controlState;
        }
    }
}

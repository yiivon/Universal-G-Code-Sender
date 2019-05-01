package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

public interface MarlinCommunicatorListener {

    void onCommandComplete(GcodeCommand command);

    void onMessageReceived(String message);

    void onStateChange(UGSEvent.ControlState controlState);
}

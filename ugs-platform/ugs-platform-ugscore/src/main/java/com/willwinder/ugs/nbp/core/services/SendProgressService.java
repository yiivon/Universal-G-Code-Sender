package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = SendProgressService.class)
public class SendProgressService implements ControllerListener {
    private final BackendAPI backend;
    private long lastUpdated;
    private ProgressHandle progressHandle;

    public SendProgressService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addControllerListener(this);
        lastUpdated = System.currentTimeMillis();
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
        if (backend.isSendingFile() && progressHandle == null) {
            progressHandle = ProgressHandle.createHandle(backend.getGcodeFile().getName(), this::cancel);
            progressHandle.start(100);
            lastUpdated = System.currentTimeMillis();
        } else if (!backend.isSendingFile() && progressHandle != null) {
            progressHandle.finish();
            progressHandle = null;
            lastUpdated = System.currentTimeMillis();
        }
    }

    private boolean cancel() {
        try {
            backend.cancel();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        progressHandle.finish();
        progressHandle = null;
    }

    @Override
    public void receivedAlarm(Alarm alarm) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComplete(GcodeCommand command) {
        long timeSinceLastUpdate = System.currentTimeMillis() - lastUpdated;
        if (progressHandle != null && timeSinceLastUpdate > 500) {
            int progressPercent = Long.valueOf(Math.round(((double) backend.getNumCompletedRows() / (double) backend.getNumRows()) * 100)).intValue();
            String message = Localization.getString("mainWindow.swing.remainingTimeLabel") + " " + Utils.formattedMillis(backend.getSendRemainingDuration()) + "\n" +
                    Localization.getString("mainWindow.swing.remainingRowsLabel") + " " + backend.getNumRemainingRows() + "/" + backend.getNumRows();
            progressHandle.progress(message, progressPercent);
            lastUpdated = System.currentTimeMillis();
        }
    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void probeCoordinates(Position p) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {

    }
}

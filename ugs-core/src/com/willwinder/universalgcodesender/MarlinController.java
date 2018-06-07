package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.firmware.DefaultFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.MarlinGcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A class for communicating with marlin controllers
 *
 * @author Joacim Breiler
 */
public class MarlinController implements IController, MarlinCommunicatorListener {
    private final DecimalFormat decimalFormatter = new DecimalFormat("0.0000", Localization.dfs);
    private final IFirmwareSettings firmwareSettings;
    private final StringBuffer responseBuffer;
    private final GcodeCommandCreator commandCreator;
    private final MarlinCommunicator communicator;
    private ControllerStatus controllerStatus;
    private ControllerState controllerState;
    private UGSEvent.ControlState controlState;
    private String firmwareVersion;
    private int outstandingPolls;
    private Timer positionPollTimer;
    private Set<ControllerListener> listeners;
    private Position position = new Position(0,0,0, UnitUtils.Units.MM);

    public MarlinController() {
        setStatusUpdateRate(1000);
        firmwareSettings = new DefaultFirmwareSettings();
        commandCreator = new GcodeCommandCreator();
        responseBuffer = new StringBuffer();
        listeners = new HashSet<>();

        communicator = new MarlinCommunicator();
        communicator.addListener(this);

        controlState = UGSEvent.ControlState.COMM_DISCONNECTED;
        controllerStatus = new ControllerStatusBuilder()
                .setStateString("")
                .setState(ControllerState.UNKNOWN)
                .setMachineCoord(new Position(0, 0, 0, UnitUtils.Units.MM))
                .setWorkCoord(new Position(0, 0, 0, UnitUtils.Units.MM))
                .build();

        positionPollTimer = createPositionPollTimer();
    }

    /**
     * Create a timer which will execute GRBL's position polling mechanism.
     */
    private Timer createPositionPollTimer() {
        // Action Listener for GRBL's polling mechanism.
        ActionListener actionListener = actionEvent -> java.awt.EventQueue.invokeLater(() -> {
            try {
                if (outstandingPolls == 0) {
                    outstandingPolls++;
                    communicator.sendCommand(new GcodeCommand("M114"));
                } else {
                    // If a poll is somehow lost after 20 intervals,
                    // reset for sending another.
                    outstandingPolls++;
                    if (outstandingPolls >= 20) {
                        outstandingPolls = 0;
                    }
                }
            } catch (Exception ex) {
                messageForConsole(Localization.getString("controller.exception.sendingstatus")
                        + " (" + ex.getMessage() + ")\n");
                ex.printStackTrace();
            }
        });

        return new Timer(getStatusUpdateRate(), actionListener);
    }

    /**
     * Begin issuing GRBL status request commands.
     */
    private void beginPollingPosition() {
        // Start sending '?' commands if supported and enabled.
        if (this.getStatusUpdatesEnabled()) {
            if (!this.positionPollTimer.isRunning()) {
                this.outstandingPolls = 0;
                this.positionPollTimer.start();
            }
        }
    }

    /**
     * Stop issuing GRBL status request commands.
     */
    private void stopPollingPosition() {
        if (this.positionPollTimer.isRunning()) {
            this.positionPollTimer.stop();
        }
    }

    @Override
    public void addListener(ControllerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ControllerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void performHomingCycle() throws Exception {

    }

    @Override
    public void returnToHome() throws Exception {

    }

    @Override
    public void resetCoordinatesToZero() throws Exception {

    }

    @Override
    public void resetCoordinateToZero(Axis coord) throws Exception {

    }

    @Override
    public void setWorkPosition(Axis axis, double position) throws Exception {

    }

    @Override
    public void killAlarmLock() throws Exception {

    }

    @Override
    public void toggleCheckMode() throws Exception {

    }

    @Override
    public void viewParserState() throws Exception {

    }

    @Override
    public void issueSoftReset() throws Exception {

    }

    @Override
    public void jogMachine(int dirX, int dirY, int dirZ, double stepSize, double feedRate, UnitUtils.Units units) throws Exception {
        // Format step size from spinner.
        String formattedStepSize = Utils.formatter.format(stepSize);
        String formattedFeedRate = Utils.formatter.format(feedRate);

        String commandString = GcodeUtils.generateXYZ("G91G0", units,
                formattedStepSize, formattedFeedRate, dirX, dirY, dirZ);

        MarlinGcodeCommand command = new MarlinGcodeCommand(createCommand(commandString));
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);
        restoreParserModalState();
    }

    @Override
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception {

    }

    @Override
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception {

    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {

    }

    @Override
    public boolean getSingleStepMode() {
        return false;
    }

    @Override
    public void setSingleStepMode(boolean enabled) {

    }

    @Override
    public boolean getStatusUpdatesEnabled() {
        return true;
    }

    @Override
    public void setStatusUpdatesEnabled(boolean enabled) {

    }

    @Override
    public int getStatusUpdateRate() {
        return 1000;
    }

    @Override
    public void setStatusUpdateRate(int rate) {

    }

    @Override
    public GcodeCommandCreator getCommandCreator() {
        return commandCreator;
    }

    @Override
    public long getJobLengthEstimate(File gcodeFile) {
        return 0;
    }

    @Override
    public Boolean openCommPort(ConnectionDriver connectionDriver, String port, int portRate) throws Exception {
        return communicator.openCommPort(connectionDriver, port, portRate);
    }

    @Override
    public Boolean closeCommPort() throws Exception {
        communicator.closeCommPort();
        return true;
    }

    @Override
    public Boolean isCommOpen() {
        return communicator.isCommOpen();
    }

    @Override
    public Boolean isReadyToReceiveCommands() throws Exception {
        return communicator.isCommOpen();
    }

    @Override
    public Boolean isReadyToStreamFile() throws Exception {
        return communicator.isCommOpen();
    }

    @Override
    public Boolean isStreaming() {
        return communicator.isStreaming();
    }

    @Override
    public long getSendDuration() {
        return 0;
    }

    @Override
    public int rowsInSend() {
        return communicator.numActiveCommands();
    }

    @Override
    public int rowsSent() {
        return 0;
    }

    @Override
    public int rowsCompleted() {
        return 0;
    }

    @Override
    public int rowsRemaining() {
        return communicator.numActiveCommands();
    }

    @Override
    public Optional<GcodeCommand> getActiveCommand() {
        return Optional.empty();
    }

    @Override
    public GcodeState getCurrentGcodeState() {
        return new GcodeState();
    }

    @Override
    public void beginStreaming() throws Exception {
        communicator.streamCommands();
    }

    @Override
    public void pauseStreaming() throws Exception {
        communicator.pauseSend();
    }

    @Override
    public void resumeStreaming() throws Exception {
        communicator.resumeSend();
    }

    @Override
    public Boolean isPaused() {
        return communicator.isPaused();
    }

    @Override
    public Boolean isIdle() {
        return !communicator.isPaused();
    }

    @Override
    public void cancelSend() throws Exception {
        communicator.cancelSend();
    }

    @Override
    public UGSEvent.ControlState getControlState() {
        return controlState;
    }

    @Override
    public void resetBuffers() {
        communicator.resetBuffers();
    }

    @Override
    public Boolean handlesAllStateChangeEvents() {
        return true;
    }

    @Override
    public GcodeCommand createCommand(String gcode) throws Exception {
        return commandCreator.createCommand(gcode);
    }

    @Override
    public void sendCommandImmediately(GcodeCommand command) throws Exception {
        System.out.println("sendCommandImmediately: " + command);
        communicator.sendCommand(new MarlinGcodeCommand(command));
    }

    @Override
    public void queueCommand(GcodeCommand command) throws Exception {
        throw new UnsupportedOperationException("This method is not implemented");
    }

    @Override
    public void queueStream(GcodeStreamReader gcodeStreamReader) {
        communicator.queueStreamForComm(gcodeStreamReader);
    }

    @Override
    public void cancelCommands() {
        communicator.cancelSend();
    }

    @Override
    public void restoreParserModalState() {

    }

    @Override
    public void updateParserModalState(GcodeCommand command) {

    }

    @Override
    public AbstractCommunicator getCommunicator() {
        return communicator;
    }

    @Override
    public Capabilities getCapabilities() {
        return new Capabilities();
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    @Override
    public String getFirmwareVersion() {
        return null;
    }

    @Override
    public ControllerState getState() {
        return controllerState;
    }

    @Override
    public void messageForConsole(String msg) {

    }

    @Override
    public void onCommandComplete(GcodeCommand command) {
        if (command.getCommandString().equalsIgnoreCase("start")) {
            controlState = UGSEvent.ControlState.COMM_IDLE;
            listeners.forEach(l -> l.controlStateChange(controlState));

            ThreadHelper.invokeLater(() -> {
                try {
                    communicator.sendCommand(new GcodeCommand("M211"));
                    communicator.sendCommand(new GcodeCommand("M503"));
                    communicator.sendCommand(new GcodeCommand("M121"));
                    communicator.sendCommand(new GcodeCommand("M302 S1"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                stopPollingPosition();
                positionPollTimer = createPositionPollTimer();
                beginPollingPosition();
            }, 2000);
        } else if ("M114".equals(command.getCommandString())) {
            listeners.forEach(l -> l.messageForConsole(ControllerListener.MessageType.VERBOSE, command.getCommandString() + " -> " + command.getResponse() + "\n"));
            outstandingPolls = 0;
            try {
                String response = command.getResponse();
                double x = decimalFormatter.parse(StringUtils.substringBetween(response, "X:", " ")).doubleValue();
                double y = decimalFormatter.parse(StringUtils.substringBetween(response, "Y:", " ")).doubleValue();
                double z = decimalFormatter.parse(StringUtils.substringBetween(response, "Z:", " ")).doubleValue();
                position = new Position(x, y, z, position.getUnits());
            } catch (ParseException e) {
            }
        } else {
            listeners.forEach(l -> l.messageForConsole(ControllerListener.MessageType.INFO, command.getCommandString() + " -> " + command.getResponse() + "\n"));
        }
    }

    @Override
    public void onMessageReceived(String message) {
        listeners.forEach(l -> l.messageForConsole(ControllerListener.MessageType.INFO, message + "\n"));
    }

    @Override
    public void onStateChange(final UGSEvent.ControlState controlState) {
        listeners.forEach(l -> l.controlStateChange(controlState));
    }
}

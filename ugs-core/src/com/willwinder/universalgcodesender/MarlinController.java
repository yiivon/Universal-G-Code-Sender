package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.marlin.MarlinFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.TinyGGcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.MarlinGcodeCommand;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;

import javax.swing.Timer;
import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;

public class MarlinController extends AbstractController {
    private static final Logger LOGGER = Logger.getLogger(MarlinController.class.getSimpleName());
    private final DecimalFormat decimalFormatter = new DecimalFormat("0.0000", Localization.dfs);
    private final MarlinFirmwareSettings firmwareSettings;
    private final Capabilities capabilities;
    private final String firmwareVersion;
    private ControllerStatus controllerStatus;
    private ControllerState controllerState;
    private Timer positionPollTimer;
    private int outstandingPolls;

    public MarlinController() {
        this(new MarlinCommunicator());
    }

    public MarlinController(MarlinCommunicator marlinCommunicator) {
        super(marlinCommunicator);
        capabilities = new Capabilities();
        commandCreator = new TinyGGcodeCommandCreator();

        firmwareSettings = new MarlinFirmwareSettings();
        controllerState = ControllerState.UNKNOWN;
        controllerStatus = new ControllerStatus(StringUtils.EMPTY, controllerState, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));
        firmwareVersion = "Marlin unknown version";
        positionPollTimer = createPositionPollTimer();
    }

    @Override
    protected Boolean isIdleEvent() {
        return getControlState() == COMM_IDLE || getControlState() == COMM_CHECK;
    }


    @Override
    protected void closeCommBeforeEvent() {

    }

    @Override
    protected void closeCommAfterEvent() {

    }

    @Override
    protected void cancelSendBeforeEvent() throws Exception {

    }

    @Override
    protected void cancelSendAfterEvent() throws Exception {

    }

    @Override
    protected void pauseStreamingEvent() throws Exception {

    }

    @Override
    protected void resumeStreamingEvent() throws Exception {

    }

    @Override
    protected void isReadyToSendCommandsEvent() throws Exception {

    }

    @Override
    protected void isReadyToStreamCommandsEvent() throws Exception {

    }

    @Override
    protected void rawResponseHandler(String response) {
        if (response.endsWith("start")) {
            dispatchConsoleMessage(MessageType.INFO, "[ready]\n");
            setCurrentState(UGSEvent.ControlState.COMM_IDLE);
            controllerState = ControllerState.IDLE;
            ThreadHelper.invokeLater(() -> {
                try {
                    comm.queueStringForComm("M211");
                    comm.queueStringForComm("M503");
                    comm.queueStringForComm("M121");
                    comm.queueStringForComm("M302 S1");
                    comm.streamCommands();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                stopPollingPosition();
                positionPollTimer = createPositionPollTimer();
                beginPollingPosition();
            }, 2000);
        } else if (getActiveCommand().isPresent()) {
            String commandString = getActiveCommand().get().getCommandString();
            if (commandString.startsWith("M114")) {
                //dispatchConsoleMessage(MessageType.INFO,  commandString + ": " + response + "\n");
                if(response.contains("X:") && response.contains("Y:") && response.contains("Z:")) {
                    try {
                        double x = decimalFormatter.parse(StringUtils.substringBetween(response, "X:", " ")).doubleValue();
                        double y = decimalFormatter.parse(StringUtils.substringBetween(response, "Y:", " ")).doubleValue();
                        double z = decimalFormatter.parse(StringUtils.substringBetween(response, "Z:", " ")).doubleValue();
                        controllerStatus = ControllerStatusBuilder.newInstance(controllerStatus)
                                .setMachineCoord(new Position(x, y, z, UnitUtils.Units.MM))
                                .setWorkCoord(new Position(x, y, z, UnitUtils.Units.MM))
                                .setState(controllerState)
                                .build();

                        dispatchStatusString(controllerStatus);
                    } catch (ParseException e) {
                    }
                }
            } else {
                dispatchConsoleMessage(MessageType.INFO, commandString + ": " + response + "\n");
            }

            if (MarlinGcodeCommand.isOkErrorResponse(response)) {
                try {
                    commandComplete(response);
                } catch (Exception e) {
                    this.dispatchConsoleMessage(MessageType.ERROR, Localization.getString("controller.error.response")
                            + " <" + response + ">: " + e.getMessage());
                }

                this.dispatchConsoleMessage(MessageType.INFO, response + "\n");
            }
        } else if (MarlinGcodeCommand.isEchoResponse(response)) {
            dispatchConsoleMessage(MessageType.INFO, "< " + response + "\n");
        } else if (response.startsWith("FIRMWARE_NAME:")) {

        } else if (MarlinGcodeCommand.isOkErrorResponse(response)) {
            LOGGER.info(response + getActiveCommand().orElse(null));
        } else if (StringUtils.isNotEmpty(response)) {
            LOGGER.info(response + getActiveCommand().orElse(null));
            dispatchConsoleMessage(MessageType.INFO, "Unknown response: " + response + "\n");

            //listeners.forEach(l -> l.messageForConsole(ControllerListener.MessageType.INFO, command.getCommandString() + " -> " + command.getResponse() + "\n"));
        }
    }

    @Override
    protected void statusUpdatesEnabledValueChanged(boolean enabled) {

    }

    @Override
    protected void statusUpdatesRateValueChanged(int rate) {

    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {

    }

    @Override
    public long getJobLengthEstimate(File gcodeFile) {
        return 0;
    }

    @Override
    public Boolean handlesAllStateChangeEvents() {
        return false;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    /**
     * Create a timer which will execute GRBL's position polling mechanism.
     */
    private Timer createPositionPollTimer() {
        // Action Listener for GRBL's polling mechanism.
        ActionListener actionListener = actionEvent -> EventQueue.invokeLater(() -> {
            try {
                if (outstandingPolls == 0) {
                    outstandingPolls++;
                    comm.queueStringForComm("M114\n");
                    comm.streamCommands();
                } else {
                    // If a poll is somehow lost after 20 intervals,
                    // reset for sending another.
                    outstandingPolls++;
                    if (outstandingPolls >= 20) {
                        outstandingPolls = 0;
                    }
                }
            } catch (Exception ex) {
                dispatchConsoleMessage(MessageType.INFO, Localization.getString("controller.exception.sendingstatus")
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
            if (!positionPollTimer.isRunning()) {
                outstandingPolls = 0;
                positionPollTimer.start();
            }
        }
    }

    /**
     * Stop issuing GRBL status request commands.
     */
    private void stopPollingPosition() {
        if (positionPollTimer.isRunning()) {
            positionPollTimer.stop();
        }
    }

    @Override
    public void jogMachine(int dirX, int dirY, int dirZ, double stepSize, double feedRate, UnitUtils.Units units) throws Exception {
        String commandString = GcodeUtils.generateMoveCommand(Code.G1.name(),
                stepSize, feedRate, dirX, dirY, dirZ, null);

        GcodeCommand command = createCommand(Code.G91.name());
        command.setTemporaryParserModalChange(true);
        sendCommandImmediately(command);

        command = createCommand(commandString);
        sendCommandImmediately(command);
        restoreParserModalState();
    }
}

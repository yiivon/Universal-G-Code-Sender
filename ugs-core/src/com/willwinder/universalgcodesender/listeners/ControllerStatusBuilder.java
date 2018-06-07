package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.model.Position;

public class ControllerStatusBuilder {
    private String stateString;
    private ControllerState state;
    private Position machineCoord;
    private Position workCoord;
    private Double feedSpeed = null;
    private Double spindleSpeed = null;
    private ControllerStatus.OverridePercents overrides = null;
    private Position workCoordinateOffset = null;
    private ControllerStatus.EnabledPins pins = null;
    private ControllerStatus.AccessoryStates states = null;

    public ControllerStatusBuilder(ControllerStatus controllerStatus) {
        if (controllerStatus == null) {
            return;
        }
        this.stateString = controllerStatus.getStateString();
        this.state = controllerStatus.getState();
        this.machineCoord = controllerStatus.getMachineCoord();
        this.workCoord = controllerStatus.getWorkCoord();
        this.feedSpeed = controllerStatus.getFeedSpeed();
        this.spindleSpeed = controllerStatus.getSpindleSpeed();
        this.overrides = controllerStatus.getOverrides();
        this.workCoordinateOffset = controllerStatus.getWorkCoordinateOffset();
        this.pins = controllerStatus.getEnabledPins();
        this.states = controllerStatus.getAccessoryStates();
    }

    public ControllerStatusBuilder() {

    }

    public ControllerStatusBuilder setStateString(String stateString) {
        this.stateString = stateString;
        return this;
    }

    public ControllerStatusBuilder setState(ControllerState state) {
        this.state = state;
        return this;
    }

    public ControllerStatusBuilder setMachineCoord(Position machineCoord) {
        this.machineCoord = machineCoord;
        return this;
    }

    public ControllerStatusBuilder setWorkCoord(Position workCoord) {
        this.workCoord = workCoord;
        return this;
    }

    public ControllerStatusBuilder setFeedSpeed(Double feedSpeed) {
        this.feedSpeed = feedSpeed;
        return this;
    }

    public ControllerStatusBuilder setSpindleSpeed(Double spindleSpeed) {
        this.spindleSpeed = spindleSpeed;
        return this;
    }

    public ControllerStatusBuilder setOverrides(ControllerStatus.OverridePercents overrides) {
        this.overrides = overrides;
        return this;
    }

    public ControllerStatusBuilder setWorkCoordinateOffset(Position workCoordinateOffset) {
        this.workCoordinateOffset = workCoordinateOffset;
        return this;
    }

    public ControllerStatusBuilder setPins(ControllerStatus.EnabledPins pins) {
        this.pins = pins;
        return this;
    }

    public ControllerStatusBuilder setStates(ControllerStatus.AccessoryStates states) {
        this.states = states;
        return this;
    }

    public ControllerStatus build() {
        return new ControllerStatus(stateString, state, machineCoord, workCoord, feedSpeed, spindleSpeed, overrides, workCoordinateOffset, pins, states);
    }
}
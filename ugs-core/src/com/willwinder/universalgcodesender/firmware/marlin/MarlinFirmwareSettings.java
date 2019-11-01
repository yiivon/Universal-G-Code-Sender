package com.willwinder.universalgcodesender.firmware.marlin;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.List;
import java.util.Optional;

public class MarlinFirmwareSettings implements IFirmwareSettings {
    @Override
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.empty();
    }

    @Override
    public FirmwareSetting setValue(String key, String value) {
        return null;
    }

    @Override
    public void addListener(IFirmwareSettingsListener listener) {

    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {

    }

    @Override
    public boolean isHomingEnabled() {
        return false;
    }

    @Override
    public void setHomingEnabled(boolean enabled) {

    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        return UnitUtils.Units.MM;
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return null;
    }

    @Override
    public boolean isHardLimitsEnabled() {
        return false;
    }

    @Override
    public void setHardLimitsEnabled(boolean enabled) {

    }

    @Override
    public boolean isSoftLimitsEnabled() {
        return false;
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) {

    }

    @Override
    public boolean isInvertDirection(Axis axis) {
        return false;
    }

    @Override
    public void setInvertDirection(Axis axis, boolean inverted) {

    }

    @Override
    public void setStepsPerMillimeter(Axis axis, int stepsPerMillimeter) {

    }

    @Override
    public int getStepsPerMillimeter(Axis axis) {
        return 0;
    }

    @Override
    public void setSoftLimit(Axis axis, double limit) {

    }

    @Override
    public double getSoftLimit(Axis axis) {
        return 0;
    }

    @Override
    public boolean isHomingDirectionInverted(Axis axis) {
        return false;
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) {

    }

    @Override
    public boolean isHardLimitsInverted() {
        return false;
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) {

    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) {

    }

    @Override
    public double getMaximumRate(Axis axis) {
        return 0;
    }
}
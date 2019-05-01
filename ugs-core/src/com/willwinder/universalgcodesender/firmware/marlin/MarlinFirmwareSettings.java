package com.willwinder.universalgcodesender.firmware.marlin;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
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
    public FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException {
        return null;
    }

    @Override
    public void addListener(IFirmwareSettingsListener listener) {

    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {

    }

    @Override
    public boolean isHomingEnabled() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHomingEnabled(boolean enabled) throws FirmwareSettingsException {

    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        return null;
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return null;
    }

    @Override
    public boolean isHardLimitsEnabled() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHardLimitsEnabled(boolean enabled) throws FirmwareSettingsException {

    }

    @Override
    public boolean isSoftLimitsEnabled() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) throws FirmwareSettingsException {

    }

    @Override
    public boolean isInvertDirection(Axis axis) throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setInvertDirection(Axis axis, boolean inverted) throws FirmwareSettingsException {

    }

    @Override
    public void setStepsPerMillimeter(Axis axis, int stepsPerMillimeter) throws FirmwareSettingsException {

    }

    @Override
    public int getStepsPerMillimeter(Axis axis) throws FirmwareSettingsException {
        return 0;
    }

    @Override
    public void setSoftLimit(Axis axis, double limit) throws FirmwareSettingsException {

    }

    @Override
    public double getSoftLimit(Axis axis) throws FirmwareSettingsException {
        return 0;
    }

    @Override
    public boolean isHomingDirectionInverted(Axis axis) {
        return false;
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) throws FirmwareSettingsException {

    }

    @Override
    public boolean isHardLimitsInverted() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException {

    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException {

    }

    @Override
    public double getMaximumRate(Axis axis) throws FirmwareSettingsException {
        return 0;
    }
}

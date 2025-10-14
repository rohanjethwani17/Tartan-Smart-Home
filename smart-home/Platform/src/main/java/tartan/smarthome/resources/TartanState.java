package tartan.smarthome.resources;


import tartan.smarthome.resources.iotcontroller.IoTValues;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.*;

public class TartanState {
    @Nullable
    Integer tempReading = null; // the current temperature
    @Nullable
    Integer targetTempSetting = null; // the user-desired temperature setting
    @Nullable
    Integer humidityReading = null; // the current humidity
    @Nullable
    Integer alarmDelay = null; // Alarm timer
    @Nullable
    Boolean doorState = null; // the state of the door (true if open, false if closed)
    @Nullable
    Boolean lightState = null; // the state of the light (true if on, false if off)
    @Nullable
    Boolean proximityState = null; // the state of the proximity sensor (true of house occupied, false if vacant)
    @Nullable
    Boolean alarmState = null; // the alarm state (true if enabled, false if disabled)
    @Nullable
    Boolean humidifierState = null; // the humidifier state (true if on, false if off)
    @Nullable
    Boolean heaterOnState = null; // the heater state (true if on, false if off)
    @Nullable
    Boolean chillerOnState = null; // the chiller state (true if on, false if off)
    @Nullable
    Boolean alarmActiveState = null; // the alarm active state (true if alarm sounding, false if alarm not sounding)
    @Nullable
    Boolean awayTimerState = false;  // assume that the away timer did not trigger this evaluation
    @Nullable
    String alarmPassCode = null;
    @Nullable
    String hvacSetting = null; // the HVAC mode setting, either Heater or Chiller
    @Nullable
    String givenPassCode = "";
    @Nullable
    Boolean doorLockState = null; // the state of the door lock (true if locked, false if unlocked)
    @Nullable
    Boolean passcodeRequiredForLock = null; // whether passcode is required for lock/unlock
    @Nullable
    Boolean doorLockRequest = null; // explicit request: true=lock, false=unlock, null=no request
    @Nullable
    Boolean keylessEntryEnabled = null;
    @Nullable
    List<String> knownDevices = null;
    @Nullable
    List<String> detectedDevices = null;

    /**
     * Empty constructor
     */
    public TartanState() {
    }


    public void loadFromStateMap(Map<String, Object> stateMap) {
        for (String key : stateMap.keySet()) {
            if (key.equals(IoTValues.TEMP_READING)) {
                this.tempReading = (Integer) stateMap.get(key);
            } else if (key.equals(IoTValues.HUMIDITY_READING)) {
                this.humidityReading = (Integer) stateMap.get(key);
            } else if (key.equals(IoTValues.TARGET_TEMP)) {
                this.targetTempSetting = (Integer) stateMap.get(key);
            } else if (key.equals(IoTValues.HUMIDIFIER_STATE)) {
                this.humidifierState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.DOOR_STATE)) {
                this.doorState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.LIGHT_STATE)) {
                this.lightState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.PROXIMITY_STATE)) {
                this.proximityState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.ALARM_STATE)) {
                this.alarmState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.HEATER_STATE)) {
                this.heaterOnState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.CHILLER_STATE)) {
                this.chillerOnState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.HVAC_MODE)) {
                this.hvacSetting = (String) stateMap.get(key);
            } else if (key.equals(IoTValues.ALARM_PASSCODE)) {
                this.alarmPassCode = (String) stateMap.get(key);
            } else if (key.equals(IoTValues.GIVEN_PASSCODE)) {
                this.givenPassCode = (String) stateMap.get(key);
            } else if (key.equals(IoTValues.AWAY_TIMER)) {
                // This is a hack!
                this.awayTimerState = (Boolean) stateMap.getOrDefault(key, false);
            } else if (key.equals(IoTValues.ALARM_ACTIVE)) {
                this.alarmActiveState = (Boolean) stateMap.get(key);
            }
        }
    }

    /**
     * Create a TartanState from a String->Object state map.
     * Any value not in the state map is assumed to be null
     * @param stateMap legacy style state map
     * @return TartanState with the same data
     */
    public static TartanState fromStateMap(Map<String, Object> stateMap) {
        TartanState output = new TartanState();
        output.loadFromStateMap(stateMap);
        return output;
    }

    /**
     * Turn this object back into the legacy state map `Map< String, Object>` type
     */
    public Map<String, Object> toStateMap() {
        Hashtable<String, Object> output = new Hashtable<>();
        if (tempReading != null) {
            output.put(IoTValues.TEMP_READING, tempReading);
        }
        if (targetTempSetting != null) {
            output.put(IoTValues.TARGET_TEMP, targetTempSetting);
        }
        if(humidityReading != null){
            output.put(IoTValues.HUMIDITY_READING, humidityReading);
        }
        if(alarmDelay != null){
            output.put(IoTValues.ALARM_DELAY, alarmDelay);
        }
        if (doorState != null) {
            output.put(IoTValues.DOOR_STATE, doorState);
        }
        if (lightState != null) {
            output.put(IoTValues.LIGHT_STATE, lightState);
        }
        if (proximityState != null) {
            output.put(IoTValues.PROXIMITY_STATE, proximityState);
        }
        if (alarmState != null) {
            output.put(IoTValues.ALARM_STATE, alarmState);
        }
        if (humidifierState != null) {
            output.put(IoTValues.HUMIDIFIER_STATE, humidifierState);
        }
        if (heaterOnState != null) {
            output.put(IoTValues.HEATER_STATE, heaterOnState);
        }
        if (chillerOnState != null) {
            output.put(IoTValues.CHILLER_STATE, chillerOnState);
        }
        if (alarmActiveState != null) {
            output.put(IoTValues.ALARM_ACTIVE, alarmActiveState);
        }
        if (awayTimerState != null) {
            output.put(IoTValues.AWAY_TIMER, awayTimerState);
        }
        if (alarmPassCode != null) {
            output.put(IoTValues.ALARM_PASSCODE, alarmPassCode);
        }
        if (hvacSetting != null) {
            output.put(IoTValues.HVAC_MODE, hvacSetting);
        }
        if (givenPassCode != null) {
            output.put(IoTValues.GIVEN_PASSCODE, givenPassCode);
        }
        // TODO Door lock state inside IoTValues and others

        return output;
    }

    /**
     * Update this state by overriding all non-null fields of fromState.
     *
     * @param fromState the state to merge values from
     */
    public void mergeState(TartanState fromState) {
        Map<String, Object> intermediateOverwriteState = fromState.toStateMap();
        Map<String, Object> intermediateSourceState = this.toStateMap();
        intermediateSourceState.putAll(intermediateOverwriteState);
        loadFromStateMap(intermediateSourceState);
    }

    @Nullable
    public Integer getTempReading() {
        return tempReading;
    }

    public void setTempReading(@Nullable Integer tempReading) {
        this.tempReading = tempReading;
    }

    @Nullable
    public Integer getTargetTempSetting() {
        return targetTempSetting;
    }

    public void setTargetTempSetting(@Nullable Integer targetTempSetting) {
        this.targetTempSetting = targetTempSetting;
    }

    @Nullable
    public Integer getHumidityReading() {
        return humidityReading;
    }

    public void setHumidityReading(@Nullable Integer humidityReading) {
        this.humidityReading = humidityReading;
    }

    @Nullable
    public Integer getAlarmDelay() {
        return alarmDelay;
    }

    public void setAlarmDelay(@Nullable Integer alarmDelay) {
        this.alarmDelay = alarmDelay;
    }

    @Nullable
    public Boolean getDoorState() {
        return doorState;
    }

    public void setDoorState(@Nullable Boolean doorState) {
        this.doorState = doorState;
    }

    @Nullable
    public Boolean getLightState() {
        return lightState;
    }

    public void setLightState(@Nullable Boolean lightState) {
        this.lightState = lightState;
    }

    @Nullable
    public Boolean getProximityState() {
        return proximityState;
    }

    public void setProximityState(@Nullable Boolean proximityState) {
        this.proximityState = proximityState;
    }

    @Nullable
    public Boolean getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(@Nullable Boolean alarmState) {
        this.alarmState = alarmState;
    }

    @Nullable
    public Boolean getHumidifierState() {
        return humidifierState;
    }

    public void setHumidifierState(@Nullable Boolean humidifierState) {
        this.humidifierState = humidifierState;
    }

    @Nullable
    public Boolean getHeaterOnState() {
        return heaterOnState;
    }

    public void setHeaterOnState(@Nullable Boolean heaterOnState) {
        this.heaterOnState = heaterOnState;
    }

    @Nullable
    public Boolean getChillerOnState() {
        return chillerOnState;
    }

    public void setChillerOnState(@Nullable Boolean chillerOnState) {
        this.chillerOnState = chillerOnState;
    }

    @Nullable
    public Boolean getAlarmActiveState() {
        return alarmActiveState;
    }

    public void setAlarmActiveState(@Nullable Boolean alarmActiveState) {
        this.alarmActiveState = alarmActiveState;
    }

    @Nullable
    public Boolean getAwayTimerState() {
        return awayTimerState;
    }

    public void setAwayTimerState(@Nullable Boolean awayTimerState) {
        this.awayTimerState = awayTimerState;
    }

    @Nullable
    public String getAlarmPassCode() {
        return alarmPassCode;
    }

    public void setAlarmPassCode(@Nullable String alarmPassCode) {
        this.alarmPassCode = alarmPassCode;
    }

    @Nullable
    public String getHvacSetting() {
        return hvacSetting;
    }

    public void setHvacSetting(@Nullable String hvacSetting) {
        this.hvacSetting = hvacSetting;
    }

    @Nullable
    public String getGivenPassCode() {
        return givenPassCode;
    }

    public void setGivenPassCode(@Nullable String givenPassCode) {
        this.givenPassCode = givenPassCode;
    }

    @Nullable
    public Boolean getDoorLockState() {
        return doorLockState;
    }

    public void setDoorLockState(@Nullable Boolean doorLockState) {
        this.doorLockState = doorLockState;
    }

    @Nullable
    public Boolean getPasscodeRequiredForLock() {
        return passcodeRequiredForLock;
    }

    public void setPasscodeRequiredForLock(@Nullable Boolean passcodeRequiredForLock) {
        this.passcodeRequiredForLock = passcodeRequiredForLock;
    }

    @Nullable
    public Boolean getDoorLockRequest() {
        return doorLockRequest;
    }

    public void setDoorLockRequest(@Nullable Boolean doorLockRequest) {
        this.doorLockRequest = doorLockRequest;
    }

    @Nullable
    public Boolean getKeylessEntryEnabled() {
        return keylessEntryEnabled;
    }

    public void setKeylessEntryEnabled(@Nullable Boolean keylessEntryEnabled) {
        this.keylessEntryEnabled = keylessEntryEnabled;
    }

    @Nullable
    public List<String> getKnownDevices() {
        return knownDevices;
    }

    public void setKnownDevices(@Nullable List<String> knownDevices) {
        this.knownDevices = knownDevices;
    }

    @Nullable
    public List<String> getDetectedDevices() {
        return detectedDevices;
    }

    public void setDetectedDevices(@Nullable List<String> detectedDevices) {
        this.detectedDevices = detectedDevices;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TartanState that = (TartanState) o;
        return Objects.equals(getTempReading(), that.getTempReading()) &&
                Objects.equals(getTargetTempSetting(), that.getTargetTempSetting()) &&
                Objects.equals(getHumidityReading(), that.getHumidityReading()) &&
                Objects.equals(getAlarmDelay(), that.getAlarmDelay()) &&
                Objects.equals(getDoorState(), that.getDoorState()) &&
                Objects.equals(getLightState(), that.getLightState()) &&
                Objects.equals(getProximityState(), that.getProximityState()) &&
                Objects.equals(getAlarmState(), that.getAlarmState()) &&
                Objects.equals(getHumidifierState(), that.getHumidifierState()) &&
                Objects.equals(getHeaterOnState(), that.getHeaterOnState()) &&
                Objects.equals(getChillerOnState(), that.getChillerOnState()) &&
                Objects.equals(getAlarmActiveState(), that.getAlarmActiveState()) &&
                Objects.equals(getAwayTimerState(), that.getAwayTimerState()) &&
                Objects.equals(getAlarmPassCode(), that.getAlarmPassCode()) &&
                Objects.equals(getHvacSetting(), that.getHvacSetting()) &&
                Objects.equals(getGivenPassCode(), that.getGivenPassCode()) &&
                Objects.equals(getDoorLockState(), that.getDoorLockState()) &&
                Objects.equals(getPasscodeRequiredForLock(), that.getPasscodeRequiredForLock()) &&
                Objects.equals(getDoorLockRequest(), that.getDoorLockRequest()) &&
                Objects.equals(getKeylessEntryEnabled(), that.getKeylessEntryEnabled()) &&
                Objects.equals(getKnownDevices(), that.getKnownDevices()) &&
                Objects.equals(getDetectedDevices(), that.getDetectedDevices());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTempReading(),
                getTargetTempSetting(),
                getHumidityReading(),
                getAlarmDelay(),
                getDoorState(),
                getLightState(),
                getProximityState(),
                getAlarmState(),
                getHumidifierState(),
                getHeaterOnState(),
                getChillerOnState(),
                getAlarmActiveState(),
                getAwayTimerState(),
                getAlarmPassCode(),
                getHvacSetting(),
                getGivenPassCode(),
                getDoorLockState(),
                getPasscodeRequiredForLock(),
                getDoorLockRequest(),
                getKeylessEntryEnabled(),
                getKnownDevices(),
                getDetectedDevices());
    }

    @Override
    public String toString() {
        return "TartanState{" +
                "tempReading=" + tempReading +
                ", targetTempSetting=" + targetTempSetting +
                ", humidityReading=" + humidityReading +
                ", alarmDelay=" + alarmDelay +
                ", doorState=" + doorState +
                ", lightState=" + lightState +
                ", proximityState=" + proximityState +
                ", alarmState=" + alarmState +
                ", humidifierState=" + humidifierState +
                ", heaterOnState=" + heaterOnState +
                ", chillerOnState=" + chillerOnState +
                ", alarmActiveState=" + alarmActiveState +
                ", awayTimerState=" + awayTimerState +
                ", alarmPassCode='" + alarmPassCode + '\'' +
                ", hvacSetting='" + hvacSetting + '\'' +
                ", givenPassCode='" + givenPassCode + '\'' +
                ", doorLockState=" + doorLockState +
                ", passcodeRequiredForLock=" + passcodeRequiredForLock +
                ", doorLockRequest=" + doorLockRequest +
                ", keylessEntryEnabled=" + keylessEntryEnabled +
                ", knownDevices=" + knownDevices +
                ", detectedDevices=" + detectedDevices +
                '}';
    }
}

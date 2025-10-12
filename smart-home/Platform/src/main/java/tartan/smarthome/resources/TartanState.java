package tartan.smarthome.resources;


import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.util.Hashtable;
import java.util.Map;

public class TartanState {
    Integer tempReading = null; // the current temperature
    Integer targetTempSetting = null; // the user-desired temperature setting
    Integer humidityReading = null; // the current humidity
    Boolean doorState = null; // the state of the door (true if open, false if closed)
    Boolean lightState = null; // the state of the light (true if on, false if off)
    Boolean proximityState = null; // the state of the proximity sensor (true of house occupied, false if vacant)
    Boolean alarmState = null; // the alarm state (true if enabled, false if disabled)
    Boolean humidifierState = null; // the humidifier state (true if on, false if off)
    Boolean heaterOnState = null; // the heater state (true if on, false if off)
    Boolean chillerOnState = null; // the chiller state (true if on, false if off)
    Boolean alarmActiveState = null; // the alarm active state (true if alarm sounding, false if alarm not sounding)
    Boolean awayTimerState = false;  // assume that the away timer did not trigger this evaluation
    String alarmPassCode = null;
    String hvacSetting = null; // the HVAC mode setting, either Heater or Chiller
    String givenPassCode = "";

    /**
     * Empty constructor
     */
    public TartanState(){
    }

    /**
     * Create a TartanState from a String->Object state map.
     * Any value not in the state map is assumed to be null
     * @param stateMap legacy style state map
     * @return TartanState with the same data
     */
    public static TartanState fromStateMap(Map<String, Object> stateMap) {
        TartanState output = new TartanState();
        for (String key : stateMap.keySet()) {
            if (key.equals(IoTValues.TEMP_READING)) {
                output.tempReading = (Integer) stateMap.get(key);
            } else if (key.equals(IoTValues.HUMIDITY_READING)) {
                output.humidityReading = (Integer) stateMap.get(key);
            } else if (key.equals(IoTValues.TARGET_TEMP)) {
                output.targetTempSetting = (Integer) stateMap.get(key);
            } else if (key.equals(IoTValues.HUMIDIFIER_STATE)) {
                output.humidifierState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.DOOR_STATE)) {
                output.doorState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.LIGHT_STATE)) {
                output.lightState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.PROXIMITY_STATE)) {
                output.proximityState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.ALARM_STATE)) {
                output.alarmState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.HEATER_STATE)) {
                output.heaterOnState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.CHILLER_STATE)) {
                output.chillerOnState = (Boolean) stateMap.get(key);
            } else if (key.equals(IoTValues.HVAC_MODE)) {
                output.hvacSetting = (String) stateMap.get(key);
            } else if (key.equals(IoTValues.ALARM_PASSCODE)) {
                output.alarmPassCode = (String) stateMap.get(key);
            } else if (key.equals(IoTValues.GIVEN_PASSCODE)) {
                output.givenPassCode = (String) stateMap.get(key);
            } else if (key.equals(IoTValues.AWAY_TIMER)) {
                // This is a hack!
                output.awayTimerState = (Boolean) stateMap.getOrDefault(key, false);
            } else if (key.equals(IoTValues.ALARM_ACTIVE)) {
                output.alarmActiveState = (Boolean) stateMap.get(key);
            }
        }
        return output;
    }

    public Map<String, Object> toStateMap(){
        Hashtable<String, Object> output = new Hashtable<>();
        output.put(IoTValues.DOOR_STATE, doorState);
        output.put(IoTValues.AWAY_TIMER, awayTimerState);
        output.put(IoTValues.LIGHT_STATE, lightState);
        output.put(IoTValues.PROXIMITY_STATE, proximityState);
        output.put(IoTValues.ALARM_STATE, alarmState);
        output.put(IoTValues.HUMIDIFIER_STATE, humidifierState);
        output.put(IoTValues.HEATER_STATE, heaterOnState);
        output.put(IoTValues.CHILLER_STATE, chillerOnState);
        output.put(IoTValues.ALARM_ACTIVE, alarmActiveState);
        output.put(IoTValues.HVAC_MODE, hvacSetting);
        output.put(IoTValues.ALARM_PASSCODE, alarmPassCode);
        output.put(IoTValues.GIVEN_PASSCODE, givenPassCode);
        output.put(IoTValues.TARGET_TEMP, targetTempSetting);
        return output;
    }
}

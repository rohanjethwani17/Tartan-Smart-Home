package tartan.smarthome.resources;


import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

public class TartanState {
    Integer tempReading = null; // the current temperature
    Integer targetTempSetting = null; // the user-desired temperature setting
    Integer humidityReading = null; // the current humidity
    Integer alarmDelay = null; // Alarm timer
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

    // IntruderDefense Controller
    boolean lockEngaged = null;
    boolean intruderDetected = null;
    boolean allClear = null;

    /**
     * Empty constructor
     */
    public TartanState(){
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
     * @param removeNull if true, any null fields are not included in the final state map.
     */
    public Map<String, Object> toStateMap(boolean removeNull){
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

        if(removeNull) {
            // https://stackoverflow.com/questions/37664374/java-how-to-remove-all-null-elements-from-a-map
            output.values().removeAll(Collections.singleton(null));
        }
        return output;
    }

    /**
     * Turn this object back into the legacy state map `Map< String, Object>` type.
     * null elements will be included in the map.
     */
    public Map<String, Object> toStateMap(){
        return this.toStateMap(false);
    }

    /**
     * Update this state by overriding all non-null fields of fromState.
     *
     * @param fromState the state to merge values from
     */
    public void mergeState(TartanState fromState){
        Map<String, Object> intermediateOverwriteState = fromState.toStateMap(true);
        Map<String, Object> intermediateSourceState = this.toStateMap();
        intermediateSourceState.putAll(intermediateOverwriteState);
        loadFromStateMap(intermediateSourceState);
    }

    public Integer getTempReading() {
        return tempReading;
    }

    public void setTempReading(Integer tempReading) {
        this.tempReading = tempReading;
    }

    public Integer getTargetTempSetting() {
        return targetTempSetting;
    }

    public void setTargetTempSetting(Integer targetTempSetting) {
        this.targetTempSetting = targetTempSetting;
    }

    public Integer getHumidityReading() {
        return humidityReading;
    }

    public void setHumidityReading(Integer humidityReading) {
        this.humidityReading = humidityReading;
    }

    public Integer getAlarmDelay() {
        return alarmDelay;
    }

    public void setAlarmDelay(Integer alarmDelay) {
        this.alarmDelay = alarmDelay;
    }

    public Boolean getDoorState() {
        return doorState;
    }

    public void setDoorState(Boolean doorState) {
        this.doorState = doorState;
    }

    public Boolean getLightState() {
        return lightState;
    }

    public void setLightState(Boolean lightState) {
        this.lightState = lightState;
    }

    public Boolean getProximityState() {
        return proximityState;
    }

    public void setProximityState(Boolean proximityState) {
        this.proximityState = proximityState;
    }

    public Boolean getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(Boolean alarmState)package tartan.smarthome.resources;


import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

    public class TartanState {
        Integer tempReading = null; // the current temperature
        Integer targetTempSetting = null; // the user-desired temperature setting
        Integer humidityReading = null; // the current humidity
        Integer alarmDelay = null; // Alarm timer
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

        // IntruderDefense Controller
        Boolean lockEngaged = false;
        Boolean intruderDetected = false;
        Boolean allClear = false;
        Boolean motionDetected = false;


        /**
         * Empty constructor
         */
        public TartanState(){
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
         * @param removeNull if true, any null fields are not included in the final state map.
         */
        public Map<String, Object> toStateMap(boolean removeNull){
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

            if(removeNull) {
                // https://stackoverflow.com/questions/37664374/java-how-to-remove-all-null-elements-from-a-map
                output.values().removeAll(Collections.singleton(null));
            }
            return output;
        }

        /**
         * Turn this object back into the legacy state map `Map< String, Object>` type.
         * null elements will be included in the map.
         */
        public Map<String, Object> toStateMap(){
            return this.toStateMap(false);
        }

        /**
         * Update this state by overriding all non-null fields of fromState.
         *
         * @param fromState the state to merge values from
         */
        public void mergeState(TartanState fromState){
            Map<String, Object> intermediateOverwriteState = fromState.toStateMap(true);
            Map<String, Object> intermediateSourceState = this.toStateMap();
            intermediateSourceState.putAll(intermediateOverwriteState);
            loadFromStateMap(intermediateSourceState);
        }

        public Integer getTempReading() {
            return tempReading;
        }

        public void setTempReading(Integer tempReading) {
            this.tempReading = tempReading;
        }

        public Integer getTargetTempSetting() {
            return targetTempSetting;
        }

        public void setTargetTempSetting(Integer targetTempSetting) {
            this.targetTempSetting = targetTempSetting;
        }

        public Integer getHumidityReading() {
            return humidityReading;
        }

        public void setHumidityReading(Integer humidityReading) {
            this.humidityReading = humidityReading;
        }

        public Integer getAlarmDelay() {
            return alarmDelay;
        }

        public void setAlarmDelay(Integer alarmDelay) {
            this.alarmDelay = alarmDelay;
        }

        public Boolean getDoorState() {
            return doorState;
        }

        public void setDoorState(Boolean doorState) {
            this.doorState = doorState;
        }

        public Boolean getLightState() {
            return lightState;
        }

        public void setLightState(Boolean lightState) {
            this.lightState = lightState;
        }

        public Boolean getProximityState() {
            return proximityState;
        }

        public void setProximityState(Boolean proximityState) {
            this.proximityState = proximityState;
        }

        public Boolean getAlarmState() {
            return alarmState;
        }

        public void setAlarmState(Boolean alarmState) {
            this.alarmState = alarmState;
        }

        public Boolean getHumidifierState() {
            return humidifierState;
        }

        public void setHumidifierState(Boolean humidifierState) {
            this.humidifierState = humidifierState;
        }

        public Boolean getHeaterOnState() {
            return heaterOnState;
        }

        public void setHeaterOnState(Boolean heaterOnState) {
            this.heaterOnState = heaterOnState;
        }

        public Boolean getChillerOnState() {
            return chillerOnState;
        }

        public void setChillerOnState(Boolean chillerOnState) {
            this.chillerOnState = chillerOnState;
        }

        public Boolean getAlarmActiveState() {
            return alarmActiveState;
        }

        public void setAlarmActiveState(Boolean alarmActiveState) {
            this.alarmActiveState = alarmActiveState;
        }

        public Boolean getAwayTimerState() {
            return awayTimerState;
        }

        public void setAwayTimerState(Boolean awayTimerState) {
            this.awayTimerState = awayTimerState;
        }

        public String getAlarmPassCode() {
            return alarmPassCode;
        }

        public void setAlarmPassCode(String alarmPassCode) {
            this.alarmPassCode = alarmPassCode;
        }

        public String getHvacSetting() {
            return hvacSetting;
        }

        public void setHvacSetting(String hvacSetting) {
            this.hvacSetting = hvacSetting;
        }

        public String getGivenPassCode() {
            return givenPassCode;
        }

        public void setGivenPassCode(String givenPassCode) {
            this.givenPassCode = givenPassCode;
        }

    }
    {
        this.alarmState = alarmState;
    }

    public Boolean getHumidifierState() {
        return humidifierState;
    }

    public void setHumidifierState(Boolean humidifierState) {
        this.humidifierState = humidifierState;
    }

    public Boolean getHeaterOnState() {
        return heaterOnState;
    }

    public void setHeaterOnState(Boolean heaterOnState) {
        this.heaterOnState = heaterOnState;
    }

    public Boolean getChillerOnState() {
        return chillerOnState;
    }

    public void setChillerOnState(Boolean chillerOnState) {
        this.chillerOnState = chillerOnState;
    }

    public Boolean getAlarmActiveState() {
        return alarmActiveState;
    }

    public void setAlarmActiveState(Boolean alarmActiveState) {
        this.alarmActiveState = alarmActiveState;
    }

    public Boolean getAwayTimerState() {
        return awayTimerState;
    }

    public void setAwayTimerState(Boolean awayTimerState) {
        this.awayTimerState = awayTimerState;
    }

    public String getAlarmPassCode() {
        return alarmPassCode;
    }

    public void setAlarmPassCode(String alarmPassCode) {
        this.alarmPassCode = alarmPassCode;
    }

    public String getHvacSetting() {
        return hvacSetting;
    }

    public void setHvacSetting(String hvacSetting) {
        this.hvacSetting = hvacSetting;
    }

    public String getGivenPassCode() {
        return givenPassCode;
    }

    public void setGivenPassCode(String givenPassCode) {
        this.givenPassCode = givenPassCode;
    }

    public Boolean getLockEngaged(){return lockEngaged;}
    public void setLockEngaged(Boolean lockEngaged){this.lockEngaged = lockEngaged;}

    public Boolean getIntruderDetected(){return intruderDetected;}
    public void setIntruderDetected(Boolean intruderDetected) {this.intruderDetected = intruderDetected;}

    public Boolean getAllClear() {return allClear;}
    public void setAllClear(Boolean allClear){this.allClear = allClear;}
}

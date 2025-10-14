package tartan.smarthome.resources;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class StaticTartanStateEvaluator implements TartanStateEvaluator {
    static final int TARGET_TEMP_MIN_F = 50;
    static final int TARGET_TEMP_MAX_F = 80;

    private String formatLogEntry(String entry) {
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        return "[" + sdf.format(new Date(timeStamp)) + "]: " + entry + "\n";
    }

    /**
     * Ensure the requested state is permitted. This method checks each state
     * variable to ensure that the house remains in a consistent state.
     *
     * @param inState The new state to evaluate
     * @param log The log of state evaluations
     * @return The evaluated state
     */
    @Override
    public TartanState evaluateState(TartanState inState, StringBuffer log) {
        // Enforce target temperature bounds (R16)
        validateTargetTempSetting(inState, log);

        // Ensure light can only be activated if the user is home
        validateLightProximityRules(inState, log);

        if (Boolean.TRUE.equals(inState.doorState)) {
            // if the door is open
            validateOpenedDoorRules(inState, log);
        } else {
            validateClosedDoorRules(inState, log);
        }

        // Auto lock the house
        invokeAwayTimerIfApplicable(inState, log);

        // the user has arrived
        validateHouseNewlyOccupied(inState, log);

        // set the alarm
        if (Boolean.TRUE.equals(inState.alarmState)) {
            log.append(formatLogEntry("Alarm enabled"));
        } else { // attempt to disable alarm
            validateAlarmDisablingAttempt(inState, log);
        }

        if (!inState.alarmState) {
            log.append(formatLogEntry("Alarm disabled"));
            inState.alarmActiveState = false;
        }


        determineHeaterChillerEnabling(inState, log);
        determineHvacSetting(inState, log);
        manageHvacControl(inState, log);

        processDoorLockRequest(inState, log);
        keylessEntry(inState, log);

        return inState;
    }

    /**
     * Ensure that the target temperature is within bounds. Modifies intermediateState
     * @param intermediateState state of house during evaluation
     * @param log The log of state evaluations
     */
    void validateTargetTempSetting(TartanState intermediateState, StringBuffer log) {
        Integer targetTempSetting = intermediateState.targetTempSetting;

        if(targetTempSetting == null){
            return;
        }

        if(targetTempSetting < TARGET_TEMP_MIN_F){
            log.append(formatLogEntry(String.format("Adjusted target temperature to minimum %dF", TARGET_TEMP_MIN_F)));
            intermediateState.targetTempSetting = TARGET_TEMP_MIN_F;
        } else if(targetTempSetting > TARGET_TEMP_MAX_F) {
            log.append(formatLogEntry(String.format("Adjusted target temperature to maximum %dF", TARGET_TEMP_MAX_F)));
            intermediateState.targetTempSetting = TARGET_TEMP_MAX_F;
        }
    }

    /**
     * Ensure that the light can only be activated if the user is home. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void validateLightProximityRules(TartanState intermediateState, StringBuffer log) {
        Boolean lightState = intermediateState.lightState;
        Boolean proximityState = intermediateState.proximityState;

        if(Boolean.TRUE.equals(lightState) && Boolean.FALSE.equals(proximityState)){
            log.append(formatLogEntry("Cannot turn on light because user is not home"));
            intermediateState.lightState = false;
        }
    }

    /**
     * Assuming the door is **open** decide whether to close it or activate the alarm. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void validateOpenedDoorRules(TartanState intermediateState, StringBuffer log) {
        Boolean alarmState = intermediateState.alarmState;
        Boolean proximityState = intermediateState.proximityState;

        if (Boolean.FALSE.equals(proximityState)) {
            if(Boolean.TRUE.equals(alarmState)){
                // door open, nobody's home, and the alarm is set - sound alarm
                log.append(formatLogEntry("Break in detected: Activating alarm"));
                intermediateState.alarmActiveState = true;
            } else {
                // house vacant, close the door
                log.append(formatLogEntry("Closed door because house vacant"));
                intermediateState.doorState = false;
            }
        } else {
            log.append(formatLogEntry("Door open"));
        }
    }

    /**
     * Assuming the door is **closed** decide whether to activate the alarm. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void validateClosedDoorRules(TartanState intermediateState, StringBuffer log) {
        Boolean alarmState = intermediateState.alarmState;
        Boolean proximityState = intermediateState.proximityState;
        // If the house is suddenly occupied this is a break-in
        if (Boolean.TRUE.equals(alarmState) && Boolean.TRUE.equals(proximityState)) {
            log.append(formatLogEntry("Break in detected: Activating alarm"));
            intermediateState.alarmActiveState = true;
        } else {
            log.append(formatLogEntry("Closed door"));
        }
    }

    /**
     * If the away timer is set, turn off the lights, close the door, and turn on the alarm. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void invokeAwayTimerIfApplicable(TartanState intermediateState, StringBuffer log) {
        Boolean awayTimerState = intermediateState.awayTimerState;

        if (Boolean.TRUE.equals(awayTimerState)){
            log.append(formatLogEntry("Away timer set"));
            intermediateState.lightState = false;
            intermediateState.doorState = false;
            intermediateState.alarmState = true;
            intermediateState.awayTimerState = false;
        }
    }

    /**
     * Turn on the light if the house is occupied and the alarm is off. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void validateHouseNewlyOccupied(TartanState intermediateState, StringBuffer log) {
        Boolean proximityState = intermediateState.proximityState;
        Boolean alarmState = intermediateState.alarmState;
        Boolean lightState = intermediateState.lightState;

        if(Boolean.FALSE.equals(proximityState)){
            return;
        }
        // else: house is occupied
        log.append(formatLogEntry("House is occupied"));
        if (Boolean.FALSE.equals(lightState) && Boolean.FALSE.equals(alarmState)) {
            log.append(formatLogEntry("Turning on light"));
            intermediateState.lightState = true;
        }
    }

    /**
     * Run relevant rules on whether an attempt at disabling the alarm is successful. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void validateAlarmDisablingAttempt(TartanState intermediateState, StringBuffer log){
        Boolean proximityState = intermediateState.proximityState;
        Boolean alarmActiveState = intermediateState.alarmActiveState;
        String givenPassCode = intermediateState.givenPassCode;
        String alarmPassCode = intermediateState.alarmPassCode;

        if (Boolean.FALSE.equals(proximityState)) {
            intermediateState.alarmState = true;
            log.append(formatLogEntry("Cannot disable the alarm, house is empty"));
        } else {
            // R13: Require correct passcode only when the alarm is sounding
            if (Boolean.TRUE.equals(alarmActiveState)) {
                boolean hasPasscode = (givenPassCode != null && alarmPassCode != null);
                if (hasPasscode && givenPassCode.equals(alarmPassCode)) {
                    log.append(formatLogEntry("Correct passcode entered, disabled alarm"));
                    intermediateState.alarmActiveState = false;
                    // alarmState remains false
                } else {
                    log.append(formatLogEntry("Cannot disable alarm, invalid passcode given"));
                    intermediateState.alarmState = true; // keep alarm enabled
                }
            } else {
                // Alarm not sounding; allow to disable while occupied
                log.append(formatLogEntry("Alarm disabled"));
            }
        }
    }

    /**
     * Enable the heater or chiller, depending on what is needed. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluations
     */
    void determineHeaterChillerEnabling(TartanState intermediateState, StringBuffer log) {
        Integer tempReading = intermediateState.tempReading;
        Integer targetTempSetting = intermediateState.targetTempSetting;

        if(tempReading == null || targetTempSetting == null){
            return;
        }

        // Heater
        if (tempReading < targetTempSetting) {
            log.append(formatLogEntry(String.format(
                    "Turning on heater, target temperature = %dF, current temperature = %dF",
                    targetTempSetting,
                    tempReading
            )));
            intermediateState.heaterOnState = true;
        } else {
            intermediateState.heaterOnState = false;
        }

        // Chiller
        if (tempReading > targetTempSetting) {
            log.append(formatLogEntry(String.format(
                    "Turning on air conditioner, target temperature = %dF, current temperature = %dF",
                    targetTempSetting,
                    tempReading
            )));
            intermediateState.chillerOnState = true;
        } else {
            intermediateState.chillerOnState = false;
        }

    }

    /**
     * Determine the HVAC setting based on chiller/heater values. Modifies intermediateState
     * @param intermediateState state of the house during evaluation
     * @param log The log of state evaluation
     */
    void determineHvacSetting(TartanState intermediateState, StringBuffer log) {
        Boolean chillerOnState = intermediateState.chillerOnState;
        Boolean heaterOnState = intermediateState.heaterOnState;

        if (Boolean.TRUE.equals(chillerOnState)) {
            intermediateState.hvacSetting = "Chiller";
        } else if (Boolean.TRUE.equals(heaterOnState)) {
            intermediateState.hvacSetting = "Heater";
        }
    }


    void manageHvacControl(TartanState intermediateState, StringBuffer log){
        String hvacSetting = intermediateState.hvacSetting;
        Boolean chillerOnState = intermediateState.chillerOnState;
        Boolean heaterOnState = intermediateState.heaterOnState;


        if ("Heater".equals(hvacSetting)) {

            if (Boolean.TRUE.equals(chillerOnState)) {
                log.append(formatLogEntry("Turning off air conditioner"));
            }

            intermediateState.chillerOnState = false; // can't run AC
            intermediateState.humidifierState = false; // can't run dehumidifier with heater
        } else if ("Chiller".equals(hvacSetting)) {

            if (Boolean.TRUE.equals(heaterOnState)) {
                log.append(formatLogEntry("Turning off heater"));
            }

            intermediateState.heaterOnState = false; // can't run heater when the A/C is on
        }

        if (Boolean.TRUE.equals(intermediateState.humidifierState) && "Chiller".equals(hvacSetting)) {
            log.append(formatLogEntry("Enabled Dehumidifier"));
        } else {
            log.append(formatLogEntry("Automatically disabled dehumidifier when running heater"));
            intermediateState.humidifierState = false;
        }
    }

    // Electronic Operation: process door lock/unlock requests
    /**
     * Process door lock/unlock requests from the access panel, validating passcode if required.
     * Updates doorLockState and appends messages to the log.
     */
    private void processDoorLockRequest(TartanState intermediateState, StringBuffer log) {
        if (intermediateState.getDoorLockRequest() != null) {
            boolean requestLock = Boolean.TRUE.equals(intermediateState.doorLockRequest);
            boolean passcodeRequired = Boolean.TRUE.equals(intermediateState.passcodeRequiredForLock);

            
            boolean passcodeValid;
            if (!passcodeRequired) {
                passcodeValid = true;
            } else {
                String givenPasscode = intermediateState.givenPassCode;
                String alarmPasscode = intermediateState.alarmPassCode;

                if(givenPasscode == null){
                    // do not change doorLockState
                    return;
                }

                passcodeValid = givenPasscode.equals(alarmPasscode);
            }
            

            if (passcodeValid) {
                intermediateState.setDoorLockState(requestLock);
                if (requestLock) {
                    log.append(formatLogEntry("Door locked via access panel."));
                } else {
                    log.append(formatLogEntry("Door unlocked via access panel."));
                }
            } else {
                // Passcode required and invalid
                log.append(formatLogEntry("Door lock/unlock failed: invalid passcode."));
                // Do not change doorLockState
            }
            // Reset request after processing
            intermediateState.setDoorLockRequest(null);
        }
    }

    // Keyless Entry: Unlock door if authorized resident present
    /**
     * Automatically unlocks door if an authorized resident is nearby
     * Updates doorLockState and appends messages to log.
     */
    private void keylessEntry(TartanState intermediateState, StringBuffer log) {
        if (intermediateState.keylessEntryEnabled == null || !intermediateState.keylessEntryEnabled) return;
        if(intermediateState.detectedDevices == null || intermediateState.knownDevices == null) return;

        boolean foundMatch = intermediateState.detectedDevices.stream().anyMatch(intermediateState.knownDevices::contains);


        if (foundMatch) {
            intermediateState.setDoorLockState(false);
            log.append(formatLogEntry("Authorized resident detected, unlocking door"));
        }
    }
}
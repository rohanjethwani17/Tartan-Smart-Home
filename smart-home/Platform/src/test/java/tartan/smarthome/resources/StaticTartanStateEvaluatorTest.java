package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.util.Hashtable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class StaticTartanStateEvaluatorTest {
    private StaticTartanStateEvaluator evaluator;
    private Map<String, Object> state;
    private StringBuffer log;

    public static Map<String, Object> createPlausibleTestState() {
        Map<String, Object> output = new Hashtable<String, Object>();
        output.put(IoTValues.TEMP_READING, 68);
        output.put(IoTValues.HUMIDITY_READING, 50);
        output.put(IoTValues.TARGET_TEMP, 72);
        output.put(IoTValues.HUMIDIFIER_STATE, false);
        output.put(IoTValues.DOOR_STATE, false);
        output.put(IoTValues.LIGHT_STATE, false);
        output.put(IoTValues.PROXIMITY_STATE, false);
        output.put(IoTValues.ALARM_STATE, false);
        output.put(IoTValues.HEATER_STATE, false);
        output.put(IoTValues.CHILLER_STATE, false);
        output.put(IoTValues.HVAC_MODE, "OFF");
        output.put(IoTValues.ALARM_PASSCODE, "passcode");
        output.put(IoTValues.GIVEN_PASSCODE, "");
        output.put(IoTValues.AWAY_TIMER, false);
        output.put(IoTValues.ALARM_ACTIVE, false);
        return output;
    }

    @BeforeEach
    public void setUp() {
        evaluator = new StaticTartanStateEvaluator();
        log = new StringBuffer();
        state = createPlausibleTestState();
    }

    /**
     * R1: If the house is vacant, then the light cannot be turned on.
     * Easy
     */
    @Test
    public void testR1_VacantHouseTurnsLightOff() {
        // house vacant + light on
        state.put(IoTValues.PROXIMITY_STATE, false);
        state.put(IoTValues.LIGHT_STATE, true);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        boolean lightState = (boolean) evaluatedState.get(IoTValues.LIGHT_STATE);

        // lights off
        assertFalse(lightState, "Light should not be on when the house is vacant.");
    }

    /**
     * R3: If the house is vacant, then close the door.
     * Easy
     */
    @Test
    public void testR3_VacantHouseClosesDoor() {
        // house vacant + door open
        state.put(IoTValues.PROXIMITY_STATE, false);
        state.put(IoTValues.DOOR_STATE, true);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        boolean doorState = (boolean) evaluatedState.get(IoTValues.DOOR_STATE);

        // door closed
        assertFalse(doorState, "Door should be closed if house is vacant.");
    }

    /**
     * R8: If the house becomes occupied while the alarm is disabled, then turn on the lights for the legitimate user.
     * Medium
     */
    @Test
    public void testR8_OccupiedWithDisabledAlarmTurnsLightOn() {
        // house occupied + alarm off + light off
        state.put(IoTValues.PROXIMITY_STATE, true);
        state.put(IoTValues.ALARM_STATE, false);
        state.put(IoTValues.LIGHT_STATE, false);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        boolean proximityState = (boolean) evaluatedState.get(IoTValues.PROXIMITY_STATE);
        boolean alarmState = (boolean) evaluatedState.get(IoTValues.ALARM_STATE);
        boolean lightState = (boolean) evaluatedState.get(IoTValues.LIGHT_STATE);

        // house occupied + alarm off + light on
        assertTrue(proximityState, "House should be occupied.");
        assertFalse(alarmState, "Alarm should be off.");
        assertTrue(lightState, "Light should turn on.");
    }

    /**
     * R10: The heater and the dehumidifier cannot be run simultaneously.
     * Medium
     */
    @Test
    public void testR10_NoSimultaneousHeaterDehumidifier() {
        // heater on + humidifier on
        state.put(IoTValues.HEATER_STATE, true);
        state.put(IoTValues.HUMIDIFIER_STATE, true);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        boolean heaterState = (Boolean) evaluatedState.get(IoTValues.HEATER_STATE);
        boolean humidifierState = (Boolean) evaluatedState.get(IoTValues.HUMIDIFIER_STATE);

        // heater and humidifier not on at the same time
        assertFalse(heaterState && humidifierState, "Heater and humidifier cannot be on at the same time.");
    }

     /**
     * R13: The correct passcode is required to disable the alarm.
     * Hard
     */
    @Test
    public void testR13_CannotDisableAlarmWithWrongPasscode() {
        // occupied house, alarm sounding
        state.put(IoTValues.PROXIMITY_STATE, true);
        state.put(IoTValues.ALARM_ACTIVE, true);
        // user attempts to disable (set alarm to false) with wrong passcode
        state.put(IoTValues.GIVEN_PASSCODE, "wrong");
        state.put(IoTValues.ALARM_STATE, false);

        Map<String, Object> evaluated = evaluator.evaluateState(state, log);
        assertTrue((Boolean) evaluated.get(IoTValues.ALARM_STATE),
                "Alarm should remain enabled with an incorrect passcode.");
    }

    @Test
    public void testR13_DisableAlarmWithCorrectPasscode() {
        // occupied house, alarm sounding
        state.put(IoTValues.PROXIMITY_STATE, true);
        state.put(IoTValues.ALARM_ACTIVE, true);
        // user provides correct passcode and requests disable
        state.put(IoTValues.GIVEN_PASSCODE, "passcode");
        state.put(IoTValues.ALARM_STATE, false);

        Map<String, Object> evaluated = evaluator.evaluateState(state, log);
        assertFalse((Boolean) evaluated.get(IoTValues.ALARM_STATE),
                "Alarm should be disabled with the correct passcode.");
    }

    /**
     * R16: The target temperature must be between 50F and 80F.
     * Hard
     */
    @Test
    public void testR16_targetTemperatureMustBeWithin50to80F() {
        // --- Accept lower bound (50°F) ---
        {
            state.put(IoTValues.TARGET_TEMP, 50);
            Map<String, Object> result = evaluator.evaluateState(state, log);
            assertEquals(50, result.get(IoTValues.TARGET_TEMP),
                    "Target temperature 50°F should be accepted (lower bound).");
        }

        // Reject below min (49°F): clamp to 50°F 
        {
            state.put(IoTValues.TARGET_TEMP, 49);
            Map<String, Object> result = evaluator.evaluateState(state, log);
            assertEquals(50, result.get(IoTValues.TARGET_TEMP),
                    "49°F is out of range; value should clamp to 50°F.");
        }

        // Accept upper bound (80°F)
        {
            state.put(IoTValues.TARGET_TEMP, 80);
            Map<String, Object> result = evaluator.evaluateState(state, log);
            assertEquals(80, result.get(IoTValues.TARGET_TEMP),
                    "Target temperature 80°F should be accepted (upper bound).");
        }

        // Reject above max (81°F): clamp to 80°F 
        {
            state.put(IoTValues.TARGET_TEMP, 81);
            Map<String, Object> result = evaluator.evaluateState(state, log);
            assertEquals(80, result.get(IoTValues.TARGET_TEMP),
                    "81°F is out of range; value should clamp to 80°F.");
        }
    }
}

class StaticTartanStateEvaluatorUC13EquivalenceClassesTest {
    private StaticTartanStateEvaluator evaluator;
    private Map<String, Object> state;
    private StringBuffer log;

    @BeforeEach
    public void setUp() {
        evaluator = new StaticTartanStateEvaluator();
        log = new StringBuffer();
        state = StaticTartanStateEvaluatorTest.createPlausibleTestState();
    }

    @Test
    public void test_AC_off_DH_off_valid() {
        state.put(IoTValues.CHILLER_STATE, false);
        state.put(IoTValues.HUMIDIFIER_STATE, false);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        assertFalse((Boolean) evaluatedState.get(IoTValues.CHILLER_STATE));
        assertFalse((Boolean) evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
    }

    @Test
    public void test_AC_on_DH_off_valid() {
        state.put(IoTValues.CHILLER_STATE, true);
        state.put(IoTValues.HUMIDIFIER_STATE, false);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        assertTrue((Boolean) evaluatedState.get(IoTValues.CHILLER_STATE));
        assertFalse((Boolean) evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
    }

    @Test
    public void test_AC_on_DH_on_valid() {
        state.put(IoTValues.CHILLER_STATE, true);
        state.put(IoTValues.HUMIDIFIER_STATE, true);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        assertTrue((Boolean) evaluatedState.get(IoTValues.CHILLER_STATE));
        assertTrue((Boolean) evaluatedState.get(IoTValues.HUMIDIFIER_STATE));
    }

    @Test
    public void test_AC_off_DH_on_invalid() {
        state.put(IoTValues.CHILLER_STATE, false);
        state.put(IoTValues.HUMIDIFIER_STATE, true);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        Boolean chillerState = (Boolean) evaluatedState.get(IoTValues.CHILLER_STATE);
        Boolean humidifierState = (Boolean) evaluatedState.get(IoTValues.HUMIDIFIER_STATE);
        // it should not be the case that things are as they were before (AC off and DH on)
        assertFalse(
                !chillerState && humidifierState
        );
    }


}
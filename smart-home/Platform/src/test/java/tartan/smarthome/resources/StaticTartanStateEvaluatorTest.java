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

    @BeforeEach
    public void setUp() {
        evaluator = new StaticTartanStateEvaluator();
        log = new StringBuffer();
        state = new Hashtable<String, Object>();
        state.put(IoTValues.TEMP_READING, 68);
        state.put(IoTValues.HUMIDITY_READING, 50);
        state.put(IoTValues.TARGET_TEMP, 72);
        state.put(IoTValues.HUMIDIFIER_STATE, false);
        state.put(IoTValues.DOOR_STATE, false);
        state.put(IoTValues.LIGHT_STATE, false);
        state.put(IoTValues.PROXIMITY_STATE, false);
        state.put(IoTValues.ALARM_STATE, false);
        state.put(IoTValues.HEATER_STATE, false);
        state.put(IoTValues.CHILLER_STATE, false);
        state.put(IoTValues.HVAC_MODE, "OFF");
        state.put(IoTValues.ALARM_PASSCODE, "passcode");
        state.put(IoTValues.GIVEN_PASSCODE, "");
        state.put(IoTValues.AWAY_TIMER, false);
        state.put(IoTValues.ALARM_ACTIVE, false);
    }

    /**
     * R1: If the house is vacant, then the light cannot be turned on.
     * Easy
     */
    @Test
    public void testR1() {
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
    public void testR3() {
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
    public void testR8() {
        // house occupied + alarm off + light off
        state.put(IoTValues.PROXIMITY_STATE, true);
        state.put(IoTValues.ALARM_STATE, false);
        state.put(IoTValues.LIGHT_STATE, false);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        boolean proximityState = (boolean) evaluatedState.get(IoTValues.PROXIMITY_STATE);
        boolean alarmState = (boolean) evaluatedState.get(IoTValues.ALARM_STATE);
        boolean lightState = (boolean) evaluatedState.get(IoTValues.LIGHT_STATE);

        // house occupied + alarm off + light on
        assertTrue(proximityState, "House should be occupied should be on.");
        assertFalse(alarmState, "Alarm should be off.");
        assertTrue(lightState, "Light should turn on.");
    }

    /**
     * R10: The heater and the dehumidifier cannot be run simultaneously.
     * Medium
     */
    @Test
    public void testR10() {
        // heater on + humidifier on
        state.put(IoTValues.HEATER_STATE, true);
        state.put(IoTValues.HUMIDIFIER_STATE, true);

        Map<String, Object> evaluatedState = evaluator.evaluateState(state, log);
        boolean heaterState = (Boolean) evaluatedState.get(IoTValues.HEATER_STATE);
        boolean humidifierState = (Boolean) evaluatedState.get(IoTValues.HUMIDIFIER_STATE);

        // heater and humidifier not on at the same time
        assertFalse(heaterState && humidifierState, "Heater and humidifier cannot be on at the same time.");
    }
}
package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.IoTValues;

import java.util.Hashtable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StaticTartanStateEvaluatorTest {
    private StaticTartanStateEvaluator evaluator;
    private TartanState state;
    private StringBuffer log;

    public static TartanState createPlausibleTestState() {
        TartanState output = new TartanState();
        output.setTempReading(68);
        output.setHumidityReading(50);
        output.setTargetTempSetting(72);
        output.setHumidifierState(false);
        output.setDoorState(false);
        output.setLightState(false);
        output.setProximityState(false);
        output.setAlarmState(false);
        output.setHeaterOnState(false);
        output.setChillerOnState(false);
        output.setHvacSetting("OFF");
        output.setAlarmPassCode("passcode");
        output.setGivenPassCode("");
        output.setAwayTimerState(false);
        output.setAlarmActiveState(false);
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
        state.setProximityState(false);
        state.setLightState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // lights off
        assertFalse(evaluatedState.getLightState(), "Light should not be on when the house is vacant.");
    }

    /**
     * R3: If the house is vacant, then close the door.
     * Easy
     */
    @Test
    public void testR3_VacantHouseClosesDoor() {
        // house vacant + door open
        state.setProximityState(false);
        state.setDoorState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // door closed
        assertFalse(evaluatedState.getDoorState(), "Door should be closed if house is vacant.");
    }

    /**
     * R8: If the house becomes occupied while the alarm is disabled, then turn on the lights for the legitimate user.
     * Medium
     */
    @Test
    public void testR8_OccupiedWithDisabledAlarmTurnsLightOn() {
        // house occupied + alarm off + light off
        state.setProximityState(true);
        state.setAlarmState(false);
        state.setLightState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // house occupied + alarm off + light on
        assertTrue(evaluatedState.getProximityState(), "House should be occupied.");
        assertFalse(evaluatedState.getAlarmState(), "Alarm should be off.");
        assertTrue(evaluatedState.getLightState(), "Light should turn on.");
    }

    /**
     * R10: The heater and the dehumidifier cannot be run simultaneously.
     * Medium
     */
    @Test
    public void testR10_NoSimultaneousHeaterDehumidifier() {
        // heater on + humidifier on
        state.setHeaterOnState(true);
        state.setHumidifierState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        boolean heaterState = evaluatedState.getHeaterOnState();
        boolean humidifierState = evaluatedState.getHumidifierState();

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
        state.setProximityState(true);
        state.setAlarmActiveState(true);
        // user attempts to disable (set alarm to false) with wrong passcode
        state.setGivenPassCode("wrong");
        state.setAlarmState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertTrue(evaluatedState.getAlarmState(),
                "Alarm should remain enabled with an incorrect passcode.");
    }

    @Test
    public void testR13_DisableAlarmWithCorrectPasscode() {
        // occupied house, alarm sounding
        state.setProximityState(true);
        state.setAlarmActiveState(true);
        // user provides correct passcode and requests disable
        state.setGivenPassCode("passcode");
        state.setAlarmState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertFalse(evaluatedState.getAlarmState(),
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
            state.setTargetTempSetting(50);
            TartanState evaluatedState = evaluator.evaluateState(state, log);
            assertEquals(50, evaluatedState.getTargetTempSetting(),
                    "Target temperature 50°F should be accepted (lower bound).");
        }

        // Reject below min (49°F): clamp to 50°F 
        {
            state.setTargetTempSetting(49);
            TartanState evaluatedState = evaluator.evaluateState(state, log);
            assertEquals(50, evaluatedState.getTargetTempSetting(),
                    "49°F is out of range; value should clamp to 50°F.");
        }

        // Accept upper bound (80°F)
        {
            state.setTargetTempSetting(80);
            TartanState evaluatedState = evaluator.evaluateState(state, log);
            assertEquals(80, evaluatedState.getTargetTempSetting(),
                    "Target temperature 80°F should be accepted (upper bound).");
        }

        // Reject above max (81°F): clamp to 80°F 
        {
            state.setTargetTempSetting(81);
            TartanState evaluatedState = evaluator.evaluateState(state, log);
            assertEquals(80, evaluatedState.getTargetTempSetting(),
                    "81°F is out of range; value should clamp to 80°F.");
        }
    }
}

class StaticTartanStateEvaluatorUC13EquivalenceClassesTest {
    private StaticTartanStateEvaluator evaluator;
    private TartanState state;
    private StringBuffer log;

    @BeforeEach
    public void setUp() {
        evaluator = new StaticTartanStateEvaluator();
        log = new StringBuffer();
        state = StaticTartanStateEvaluatorTest.createPlausibleTestState();
    }

    @Test
    public void test_AC_off_DH_off_valid() {
        state.setChillerOnState(false);
        state.setHumidifierState(false);

        // set the temperature and target so the AC doesn't automatically enable
        state.setTempReading(60);
        state.setTargetTempSetting(61);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertFalse(evaluatedState.getChillerOnState());
        assertFalse(evaluatedState.getHumidifierState());
    }

    @Test
    public void test_AC_on_DH_off_valid() {
        state.setChillerOnState(true);
        state.setHumidifierState(false);

        // set the temperature and target so the AC doesn't automatically disable
        state.setTempReading(60);
        state.setTargetTempSetting(59);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertTrue(evaluatedState.getChillerOnState());
        assertFalse(evaluatedState.getHumidifierState());
    }

    @Test
    public void test_AC_on_DH_on_valid() {
        state.setChillerOnState(true);
        state.setHumidifierState(true);

        // set the temperature and target so the AC doesn't automatically disable
        state.setTempReading(60);
        state.setTargetTempSetting(59);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertTrue(evaluatedState.getChillerOnState());
        assertTrue(evaluatedState.getHumidifierState());
    }

    @Test
    public void test_AC_off_DH_on_invalid() {
        state.setChillerOnState(false);
        state.setHumidifierState(false);

        // set the temperature and target so the AC doesn't automatically enable
        state.setTempReading(60);
        state.setTargetTempSetting(61);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        Boolean chillerState = evaluatedState.getChillerOnState();
        Boolean humidifierState = evaluatedState.getHumidifierState();
        // it should not be the case that things are as they were before (AC off and DH on)
        assertFalse(
                !chillerState && humidifierState
        );
    }


}
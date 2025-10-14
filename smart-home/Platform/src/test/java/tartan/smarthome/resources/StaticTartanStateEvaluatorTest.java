package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.IoTValues;
import tartan.smarthome.resources.iotcontroller.NightlockController;

import java.time.LocalTime;
import java.util.Hashtable;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StaticTartanStateEvaluatorTest {
    private StaticTartanStateEvaluator evaluator;
    private TartanState state;
    private StringBuffer log;

    @BeforeEach
    public void setUp() {
        evaluator = new StaticTartanStateEvaluator();
        log = new StringBuffer();
        state = new TartanState();
        state.setTempReading(68);
        state.setHumidityReading(50);
        state.setTargetTempSetting(72);
        state.setHumidifierState(false);
        state.setDoorState(false);
        state.setLightState(false);
        state.setProximityState(false);
        state.setAlarmState(false);
        state.setHeaterOnState(false);
        state.setChillerOnState(false);
        state.setHvacSetting("OFF");
        state.setAlarmPassCode("passcode");
        state.setGivenPassCode("");
        state.setAwayTimerState(false);
        state.setAlarmActiveState(false);
        state.setDoorLockedState(false);
    }
    /**
     * R1: If the house is vacant, then the light cannot be turned on.
     * Easy
     */
    @Test
    public void testR1_VacantHouseTurnsLightOff() {
        state.setProximityState(false);
        state.setLightState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertFalse(evaluatedState.getLightState(), "Light should not be on when the house is vacant.");
    }
    /**
     * R3: If the house is vacant, then close the door.
     * Easy
     */
    @Test
    public void testR3_VacantHouseClosesDoor() {
        state.setProximityState(false);
        state.setDoorState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertFalse(evaluatedState.getDoorState(), "Door should be closed if house is vacant.");
    }
    /**
     * R8: If the house becomes occupied while the alarm is disabled, then turn on the lights for the legitimate user.
     * Medium
     */
    @Test
    public void testR8_OccupiedWithDisabledAlarmTurnsLightOn() {
        state.setProximityState(true);
        state.setAlarmState(false);
        state.setLightState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

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
        state.setHeaterOnState(true);
        state.setHumidifierState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertFalse(evaluatedState.getHeaterOnState() && evaluatedState.getHumidifierState(),
                "Heater and humidifier cannot be on at the same time.");
    }
    /**
     * R13: The correct passcode is required to disable the alarm.
     * Hard
     */
    @Test
    public void testR13_CannotDisableAlarmWithWrongPasscode() {
        state.setProximityState(true);
        state.setAlarmActiveState(true);
        state.setGivenPassCode("wrong");
        state.setAlarmState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertTrue(evaluatedState.getAlarmState(),
                "Alarm should remain enabled with an incorrect passcode.");
    }

    @Test
    public void testR13_DisableAlarmWithCorrectPasscode() {
        state.setProximityState(true);
        state.setAlarmActiveState(true);
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
        state.setTargetTempSetting(50);
        assertEquals(50, evaluator.evaluateState(state, log).getTargetTempSetting(),
                "Target temperature 50°F should be accepted (lower bound).");

        state.setTargetTempSetting(49);
        assertEquals(50, evaluator.evaluateState(state, log).getTargetTempSetting(),
                "49°F is out of range; value should clamp to 50°F.");

        state.setTargetTempSetting(80);
        assertEquals(80, evaluator.evaluateState(state, log).getTargetTempSetting(),
                "Target temperature 80°F should be accepted (upper bound).");

        state.setTargetTempSetting(81);
        assertEquals(80, evaluator.evaluateState(state, log).getTargetTempSetting(),
                "81°F is out of range; value should clamp to 80°F.");
    }
    /**
     * test nightlock
     */
    @Test
    public void testNightLockActivatesAtNight() {
        NightlockController nightlock = new NightlockController(state);
        nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));

        LocalTime currentTime = LocalTime.of(23, 0);
        boolean activated = nightlock.checkAndApplyNightLock(currentTime);

        assertTrue(activated, "Nightlock should be active at night.");
        assertTrue(state.getDoorLockedState(), "Door should be locked at night.");
    }

    @Test
    public void testNightLockInactiveDuringDay() {
        NightlockController nightlock = new NightlockController(state);
        nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));

        LocalTime currentTime = LocalTime.of(10, 0);
        boolean activated = nightlock.checkAndApplyNightLock(currentTime);

        assertFalse(activated, "Nightlock should be inactive during the day.");
        assertFalse(state.getDoorLockedState(), "Door should be unlocked during the day.");
    }

    @Test
    public void testNightLockWrapsPastMidnight() {
        NightlockController nightlock = new NightlockController(state);
        nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));

        LocalTime currentTime = LocalTime.of(2, 0);
        boolean activated = nightlock.checkAndApplyNightLock(currentTime);

        assertTrue(activated, "Nightlock should be active past midnight.");
        assertTrue(state.getDoorLockedState(), "Door should remain locked past midnight.");
    }
}

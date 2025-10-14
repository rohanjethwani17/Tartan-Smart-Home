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

    // ============================================================================
    // BLACKBOX TESTS
    // Using equivalence partitioning, boundary value analysis, and edge cases
    // ============================================================================

    /**
     * R1 BLACKBOX TEST: Equivalence Class - Occupied house with light off and alarm enabled
     * Strategy: Equivalence partitioning with rule interaction consideration
     * 
     * Test that when house is occupied with alarm enabled, light can remain off.
     * Note: We enable the alarm to prevent R8 (auto-light-on) from interfering.
     * This validates that R1 doesn't incorrectly prevent lights from being off when occupied.
     */
    @Test
    public void testR1_Blackbox_OccupiedHouseLightCanBeOff() {
        // Setup: house occupied, light off, alarm enabled (to prevent R8 auto-on)
        state.setProximityState(true);  // occupied
        state.setLightState(false);     // light off
        state.setAlarmState(true);      // alarm enabled (prevents R8 auto-light)

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should remain off (alarm enabled prevents auto-on from R8)
        assertFalse(evaluatedState.getLightState(), 
                "Light should be allowed to stay off when house is occupied with alarm enabled.");
        assertTrue(evaluatedState.getProximityState(), 
                "House should remain occupied.");
    }

    /**
     * R1 BLACKBOX TEST: Equivalence Class - Occupied house with light on
     * Strategy: Equivalence partitioning
     * 
     * Test that when house is occupied, light can be on (valid state).
     * This ensures the rule only prevents lights when vacant, not when occupied.
     */
    @Test
    public void testR1_Blackbox_OccupiedHouseLightCanBeOn() {
        // Setup: house occupied, light on
        state.setProximityState(true);  // occupied
        state.setLightState(true);      // light on

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should remain on
        assertTrue(evaluatedState.getLightState(), 
                "Light should be allowed to be on when house is occupied.");
        assertTrue(evaluatedState.getProximityState(), 
                "House should remain occupied.");
    }

    /**
     * R13 BLACKBOX TEST: Empty passcode when alarm is sounding
     * Strategy: Equivalence partitioning - testing invalid passcode input
     * 
     * Test that an empty string passcode is rejected when trying to disable
     * a sounding alarm. This is different from null and tests string validation.
     */
    @Test
    public void testR13_Blackbox_EmptyPasscodeRejected() {
        // Setup: occupied house, alarm enabled and actively sounding
        state.setProximityState(true);
        state.setAlarmState(true);
        state.setAlarmActiveState(true);
        state.setAlarmPassCode("correct123");
        
        // User attempts to disable with empty string passcode
        state.setGivenPassCode("");
        state.setAlarmState(false);  // requesting disable

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: alarm should remain enabled due to incorrect passcode
        assertTrue(evaluatedState.getAlarmState(), 
                "Alarm should remain enabled when empty passcode is provided.");
        assertTrue(evaluatedState.getAlarmActiveState(), 
                "Alarm should continue sounding with empty passcode.");
    }

    /**
     * R13 BLACKBOX TEST: Null passcode when alarm is sounding
     * Strategy: Equivalence partitioning - testing null input handling
     * 
     * Test that a null passcode is rejected when trying to disable
     * a sounding alarm. This tests edge case of missing input.
     */
    @Test
    public void testR13_Blackbox_NullPasscodeRejected() {
        // Setup: occupied house, alarm enabled and actively sounding
        state.setProximityState(true);
        state.setAlarmState(true);
        state.setAlarmActiveState(true);
        state.setAlarmPassCode("correct123");
        
        // User attempts to disable with null passcode
        state.setGivenPassCode(null);
        state.setAlarmState(false);  // requesting disable

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: alarm should remain enabled due to null passcode
        assertTrue(evaluatedState.getAlarmState(), 
                "Alarm should remain enabled when null passcode is provided.");
        assertTrue(evaluatedState.getAlarmActiveState(), 
                "Alarm should continue sounding with null passcode.");
    }

    /**
     * R16 BLACKBOX TEST: Extreme lower boundary - very negative temperature
     * Strategy: Boundary Value Analysis (extreme values)
     * 
     * Test that extremely low temperature values are clamped to minimum (50°F).
     * This ensures the system handles edge cases beyond typical input ranges.
     */
    @Test
    public void testR16_Blackbox_ExtremeNegativeTemperatureClamped() {
        // Setup: attempt to set temperature to Integer.MIN_VALUE
        state.setTargetTempSetting(Integer.MIN_VALUE);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: temperature should be clamped to minimum allowed (50°F)
        assertEquals(50, evaluatedState.getTargetTempSetting(),
                "Extreme negative temperature should be clamped to 50°F minimum.");
    }

    /**
     * R16 BLACKBOX TEST: Extreme upper boundary - very high temperature
     * Strategy: Boundary Value Analysis (extreme values)
     * 
     * Test that extremely high temperature values are clamped to maximum (80°F).
     * This ensures the system handles edge cases beyond typical input ranges.
     */
    @Test
    public void testR16_Blackbox_ExtremeHighTemperatureClamped() {
        // Setup: attempt to set temperature to Integer.MAX_VALUE
        state.setTargetTempSetting(Integer.MAX_VALUE);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: temperature should be clamped to maximum allowed (80°F)
        assertEquals(80, evaluatedState.getTargetTempSetting(),
                "Extreme high temperature should be clamped to 80°F maximum.");
    }

    /**
     * R16 BLACKBOX TEST: Just below lower boundary
     * Strategy: Boundary Value Analysis (boundary - 1)
     * 
     * Test temperature at 49°F, which is one degree below the minimum.
     * Classic BVA test for the lower boundary.
     */
    @Test
    public void testR16_Blackbox_BelowMinimumBoundary() {
        // Setup: set temperature to 49°F (one below minimum)
        state.setTargetTempSetting(49);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: temperature should be clamped to 50°F
        assertEquals(50, evaluatedState.getTargetTempSetting(),
                "49°F should be clamped to 50°F (below minimum boundary).");
    }

    /**
     * R16 BLACKBOX TEST: Just above upper boundary
     * Strategy: Boundary Value Analysis (boundary + 1)
     * 
     * Test temperature at 81°F, which is one degree above the maximum.
     * Classic BVA test for the upper boundary.
     */
    @Test
    public void testR16_Blackbox_AboveMaximumBoundary() {
        // Setup: set temperature to 81°F (one above maximum)
        state.setTargetTempSetting(81);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: temperature should be clamped to 80°F
        assertEquals(80, evaluatedState.getTargetTempSetting(),
                "81°F should be clamped to 80°F (above maximum boundary).");
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
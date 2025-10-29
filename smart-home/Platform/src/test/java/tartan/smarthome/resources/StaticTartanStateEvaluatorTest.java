package tartan.smarthome.resources;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.NightlockController;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalTime;
import java.util.List;


class StaticTartanStateEvaluatorTest extends StaticTartanStateEvaluatorTestBase {

    /**
     * R1: If the house is vacant, then the light cannot be turned on.
     * Easy
     */
    @Test
    void testR1_VacantHouseTurnsLightOff() {
        // house vacant + light on
        state.setProximityState(false);
        state.setLightState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // lights off
        assertNotNull(evaluatedState.getLightState());
        assertFalse(evaluatedState.getLightState(), "Light should not be on when the house is vacant.");
    }

    /**
     * R3: If the house is vacant, then close the door.
     * Easy
     */
    @Test
    void testR3_VacantHouseClosesDoor() {
        // house vacant + door open
        state.setProximityState(false);
        state.setDoorState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // door closed
        assertEquals(Boolean.FALSE, evaluatedState.getDoorState(), "Door should be closed if house is vacant.");
    }

    /**
     * R8: If the house becomes occupied while the alarm is disabled, then turn on the lights for the legitimate user.
     * Medium
     */
    @Test
    void testR8_OccupiedWithDisabledAlarmTurnsLightOn() {
        // house occupied + alarm off + light off
        state.setProximityState(true);
        state.setAlarmState(false);
        state.setLightState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // house occupied + alarm off + light on
        assertEquals(Boolean.TRUE, evaluatedState.getProximityState(), "House should be occupied.");
        assertEquals(Boolean.FALSE, evaluatedState.getAlarmState(), "Alarm should be off.");
        assertEquals(Boolean.TRUE, evaluatedState.getLightState(), "Light should turn on.");
    }

    /**
     * R10: The heater and the dehumidifier cannot be run simultaneously.
     * Medium
     */
    @Test
    void testR10_NoSimultaneousHeaterDehumidifier() {
        // heater on + humidifier on
        state.setHeaterOnState(true);
        state.setHumidifierState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        Boolean heaterState = evaluatedState.getHeaterOnState();
        Boolean humidifierState = evaluatedState.getHumidifierState();

        if (heaterState == null || humidifierState == null){
            fail("Both heater and humidifier states must be non-null");
        }

        // heater and humidifier not on at the same time
        assertFalse(heaterState && humidifierState, "Heater and humidifier cannot be on at the same time.");
    }

    /**
     * R13: The correct passcode is required to disable the alarm.
     * Hard
     */
    @Test
    void testR13_CannotDisableAlarmWithWrongPasscode() {
        // occupied house, alarm sounding
        state.setProximityState(true);
        state.setAlarmActiveState(true);
        // user attempts to disable (set alarm to false) with wrong passcode
        state.setGivenPassCode("wrong");
        state.setAlarmState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getAlarmState(),
                "Alarm should remain enabled with an incorrect passcode.");
    }

    @Test
    void testR13_DisableAlarmWithCorrectPasscode() {
        // occupied house, alarm sounding
        state.setProximityState(true);
        state.setAlarmActiveState(true);
        // user provides correct passcode and requests disable
        state.setGivenPassCode("passcode");
        state.setAlarmState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getAlarmState(),
                "Alarm should be disabled with the correct passcode.");
    }

    // ============================================================================
    // BLACKBOX TESTS
    // Using equivalence partitioning, boundary value analysis, and edge cases
    // ============================================================================

    /**
     * R1 BLACKBOX TEST: Equivalence Class - Occupied house with light off and alarm enabled
     * Strategy: Equivalence partitioning with rule interaction consideration
     * <p>
     * Test that when house is occupied with alarm enabled, light can remain off.
     * Note: We enable the alarm to prevent R8 (auto-light-on) from interfering.
     * This validates that R1 doesn't incorrectly prevent lights from being off when occupied.
     */
    @Test
    void testR1_Blackbox_OccupiedHouseLightCanBeOff() {
        // Setup: house occupied, light off, alarm enabled (to prevent R8 auto-on)
        state.setProximityState(true);  // occupied
        state.setLightState(false);     // light off
        state.setAlarmState(true);      // alarm enabled (prevents R8 auto-light)

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should remain off (alarm enabled prevents auto-on from R8)
        assertEquals(Boolean.FALSE, evaluatedState.getLightState(),
                "Light should be allowed to stay off when house is occupied with alarm enabled.");
        assertEquals(Boolean.TRUE, evaluatedState.getProximityState(),
                "House should remain occupied.");
    }

    /**
     * R1 BLACKBOX TEST: Equivalence Class - Occupied house with light on
     * Strategy: Equivalence partitioning
     * <p>
     * Test that when house is occupied, light can be on (valid state).
     * This ensures the rule only prevents lights when vacant, not when occupied.
     */
    @Test
    void testR1_Blackbox_OccupiedHouseLightCanBeOn() {
        // Setup: house occupied, light on
        state.setProximityState(true);  // occupied
        state.setLightState(true);      // light on

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should remain on
        assertEquals(Boolean.TRUE, evaluatedState.getLightState(),
                "Light should be allowed to be on when house is occupied.");
        assertEquals(Boolean.TRUE, evaluatedState.getProximityState(),
                "House should remain occupied.");
    }

    /**
     * R13 BLACKBOX TEST: Empty passcode when alarm is sounding
     * Strategy: Equivalence partitioning - testing invalid passcode input
     * <p>
     * Test that an empty string passcode is rejected when trying to disable
     * a sounding alarm. This is different from null and tests string validation.
     */
    @Test
    void testR13_Blackbox_EmptyPasscodeRejected() {
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
        assertEquals(Boolean.TRUE, evaluatedState.getAlarmState(),
                "Alarm should remain enabled when empty passcode is provided.");
        assertEquals(Boolean.TRUE, evaluatedState.getAlarmActiveState(),
                "Alarm should continue sounding with empty passcode.");
    }

    /**
     * R13 BLACKBOX TEST: Null passcode when alarm is sounding
     * Strategy: Equivalence partitioning - testing null input handling
     * <p>
     * Test that a null passcode is rejected when trying to disable
     * a sounding alarm. This tests edge case of missing input.
     */
    @Test
    void testR13_Blackbox_NullPasscodeRejected() {
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
        assertEquals(Boolean.TRUE, evaluatedState.getAlarmState(),
                "Alarm should remain enabled when null passcode is provided.");
        assertEquals(Boolean.TRUE, evaluatedState.getAlarmActiveState(),
                "Alarm should continue sounding with null passcode.");
    }

    /**
     * R16 BLACKBOX TEST: Extreme lower boundary - very negative temperature
     * Strategy: Boundary Value Analysis (extreme values)
     * <p>
     * Test that extremely low temperature values are clamped to minimum (50°F).
     * This ensures the system handles edge cases beyond typical input ranges.
     */
    @Test
    void testR16_Blackbox_ExtremeNegativeTemperatureClamped() {
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
     * <p>
     * Test that extremely high temperature values are clamped to maximum (80°F).
     * This ensures the system handles edge cases beyond typical input ranges.
     */
    @Test
    void testR16_Blackbox_ExtremeHighTemperatureClamped() {
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
     * <p>
     * Test temperature at 49°F, which is one degree below the minimum.
     * Classic BVA test for the lower boundary.
     */
    @Test
    void testR16_Blackbox_BelowMinimumBoundary() {
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
     * <p>
     * Test temperature at 81°F, which is one degree above the maximum.
     * Classic BVA test for the upper boundary.
     */
    @Test
    void testR16_Blackbox_AboveMaximumBoundary() {
        // Setup: set temperature to 81°F (one above maximum)
        state.setTargetTempSetting(81);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: temperature should be clamped to 80°F
        assertEquals(80, evaluatedState.getTargetTempSetting(),
                "81°F should be clamped to 80°F (above maximum boundary).");
    }

    /**
     * Kills two mutants
     * Test temperature at 60°F, which is within allowed temperatures.
     */
    @Test
    void mutationKill_Within_Allowed_Space() {
        // Setup: set temperature to 60°F
        state.setTargetTempSetting(60);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: temperature should be clamped to 80°F
        assertEquals(60, evaluatedState.getTargetTempSetting(),
                "60°F should be allowed as it is within boundaries");
    }

    /**
     * R16: The target temperature must be between 50F and 80F.
     * Hard
     */
    @Test
    void testR16_targetTemperatureMustBeWithin50to80F() {
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

    // ============================================================================
    // R3 BLACKBOX TESTS
    // ============================================================================

    /**
     * R3 BLACKBOX TEST: Occupied house with door open
     * Strategy: Equivalence partitioning - valid state
     * <p>
     * Test that door can remain open when house is occupied.
     */
    @Test
    void testR3_Blackbox_OccupiedHouseDoorCanStayOpen() {
        // Setup: house occupied, door open
        state.setProximityState(true);
        state.setDoorState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: door should remain open when occupied
        assertEquals(Boolean.TRUE, evaluatedState.getDoorState(),
                "Door should be allowed to stay open when house is occupied.");
        assertEquals(Boolean.TRUE, evaluatedState.getProximityState(),
                "House should remain occupied.");
    }

    /**
     * R3 BLACKBOX TEST: Vacant house with door closed
     * Strategy: Equivalence partitioning - valid state
     * <p>
     * Test that door remains closed when house is vacant and door already closed.
     */
    @Test
    void testR3_Blackbox_VacantHouseDoorAlreadyClosed() {
        // Setup: house vacant, door already closed
        state.setProximityState(false);
        state.setDoorState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: door should remain closed
        assertEquals(Boolean.FALSE, evaluatedState.getDoorState(),
                "Door should remain closed when house is vacant.");
        assertEquals(Boolean.FALSE, evaluatedState.getProximityState(),
                "House should remain vacant.");
    }

    // ============================================================================
    // R8 BLACKBOX TESTS
    // ============================================================================

    /**
     * R8 BLACKBOX TEST: House already occupied, alarm disabled, light off
     * Strategy: Equivalence partitioning
     * <p>
     * Test that light turns on when house is occupied and alarm is disabled,
     * even if the house was already occupied.
     */
    @Test
    void testR8_Blackbox_AlreadyOccupiedLightOff() {
        // Setup: already occupied, alarm disabled, light off
        state.setProximityState(true);
        state.setAlarmState(false);
        state.setLightState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should turn on
        assertEquals(Boolean.TRUE, evaluatedState.getLightState(),
                "Light should turn on when occupied with alarm disabled.");
    }

    /**
     * R8 BLACKBOX TEST: Light already on when becoming occupied
     * Strategy: Boundary case testing
     * <p>
     * Test that light remains on when house becomes occupied with light already on.
     */
    @Test
    void testR8_Blackbox_LightAlreadyOn() {
        // Setup: house occupied, alarm disabled, light already on
        state.setProximityState(true);
        state.setAlarmState(false);
        state.setLightState(true);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should remain on
        assertEquals(Boolean.TRUE, evaluatedState.getLightState(),
                "Light should remain on when already on with house occupied.");
    }

    /**
     * R8 BLACKBOX TEST: Occupied with alarm enabled - no auto light
     * Strategy: Rule interaction testing
     * <p>
     * Test that light does NOT auto-turn-on when occupied but alarm is enabled
     * (potential intruder scenario).
     */
    @Test
    void testR8_Blackbox_OccupiedWithAlarmEnabledNoLight() {
        // Setup: house occupied, alarm enabled, light off
        state.setProximityState(true);
        state.setAlarmState(true);
        state.setLightState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light should NOT turn on (alarm enabled = potential intruder)
        assertEquals(Boolean.FALSE, evaluatedState.getLightState(),
                "Light should not turn on when alarm is enabled (potential intruder).");
    }

    /**
     * R8 BLACKBOX TEST: Transition from vacant to occupied
     * Strategy: State transition testing
     * <p>
     * Test the specific scenario of transitioning from vacant to occupied.
     */
    @Test
    void testR8_Blackbox_VacantToOccupiedTransition() {
        // Setup: transitioning to occupied, alarm disabled, light off
        state.setProximityState(true);  // now occupied
        state.setAlarmState(false);
        state.setLightState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: light turns on for legitimate user
        assertEquals(Boolean.TRUE, evaluatedState.getLightState(),
                "Light should turn on when house becomes occupied with alarm disabled.");
        assertEquals(Boolean.TRUE, evaluatedState.getProximityState(),
                "House should be occupied.");
    }

    // ============================================================================
    // R10 BLACKBOX TESTS
    // ============================================================================

    /**
     * R10 BLACKBOX TEST: Heater on, dehumidifier off
     * Strategy: Equivalence partitioning - valid state
     * <p>
     * Test that heater and dehumidifier off together is a valid state.
     */
    @Test
    void testR10_Blackbox_HeaterOnDehumidifierOff() {
        // Setup: cold temp requiring heat, dehumidifier off
        state.setTempReading(60);
        state.setTargetTempSetting(70);
        state.setHumidifierState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: heater on, dehumidifier stays off
        assertEquals(Boolean.TRUE, evaluatedState.getHeaterOnState(),
                "Heater should be on when temp is below target.");
        assertEquals(Boolean.FALSE, evaluatedState.getHumidifierState(),
                "Dehumidifier should remain off.");
    }

    /**
     * R10 BLACKBOX TEST: Neither heater nor dehumidifier needed
     * Strategy: Equivalence partitioning - valid state
     * <p>
     * Test that both can be off when neither is needed.
     */
    @Test
    void testR10_Blackbox_BothOff() {
        // Setup: temperature at target, no HVAC needed
        state.setTempReading(70);
        state.setTargetTempSetting(70);
        state.setHeaterOnState(false);
        state.setHumidifierState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: both should be off
        assertEquals(Boolean.FALSE, evaluatedState.getHeaterOnState(),
                "Heater should be off when temp equals target.");
        assertEquals(Boolean.FALSE, evaluatedState.getHumidifierState(),
                "Dehumidifier should be off.");
    }

    /**
     * R10 BLACKBOX TEST: Temperature changes triggering heater
     * Strategy: Boundary value analysis
     * <p>
     * Test state change when temperature drops and heater activates.
     */
    @Test
    void testR10_Blackbox_TemperatureDropActivatesHeater() {
        // Setup: temp just below target
        state.setTempReading(69);
        state.setTargetTempSetting(70);
        state.setHumidifierState(false);

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Verify: heater activates, dehumidifier stays off
        assertEquals(Boolean.TRUE, evaluatedState.getHeaterOnState(),
                "Heater should activate when temp drops below target.");
        assertEquals(Boolean.FALSE, evaluatedState.getHumidifierState(),
                "Dehumidifier should remain off when heater is on.");
    }


    // Electronic operation tests
    @Test
    void testLockWithPasscodeRequiredIncorrectPasscode() {
        state.setDoorLockedState(false); // currently unlocked
        state.setPasscodeRequiredForLock(true);
        state.setGivenPassCode("wrong");
        state.setAlarmPassCode("passcode");
        state.setDoorLockRequest(true); // request lock

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getDoorLockedState(), "Door should remain unlocked with incorrect passcode.");
        assertTrue(log.toString().contains("invalid passcode"), "Log should contain rejection message.");
    }

    @Test
    void testLockWithPasscodeRequiredCorrectPasscode() {
        state.setDoorLockedState(false); // currently unlocked
        state.setPasscodeRequiredForLock(true);
        state.setGivenPassCode("passcode");
        state.setAlarmPassCode("passcode");
        state.setDoorLockRequest(true); // request lock

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getDoorLockedState(), "Door should be locked with correct passcode.");
        assertTrue(log.toString().contains("locked"), "Log should contain success message.");
    }

    @Test
    void testLockWithNoPasscodeRequired() {
        state.setDoorLockedState(false); // currently unlocked
        state.setPasscodeRequiredForLock(false);
        state.setDoorLockRequest(true); // request lock

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getDoorLockedState(), "Door should be locked when no passcode required.");
        assertTrue(log.toString().contains("locked"), "Log should contain success message.");
    }

    @Test
    void testUnlockWithPasscodeRequiredIncorrectPasscode() {
        state.setDoorLockedState(true); // currently locked
        state.setPasscodeRequiredForLock(true);
        state.setGivenPassCode("wrong");
        state.setAlarmPassCode("passcode");
        state.setDoorLockRequest(false); // request unlock

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getDoorLockedState(), "Door should remain locked with incorrect passcode.");
        assertTrue(log.toString().contains("invalid passcode"), "Log should contain rejection message.");
    }

    @Test
    void testUnlockWithPasscodeRequiredCorrectPasscode() {
        state.setDoorLockedState(true); // currently locked
        state.setPasscodeRequiredForLock(true);
        state.setGivenPassCode("passcode");
        state.setAlarmPassCode("passcode");
        state.setDoorLockRequest(false); // request unlock

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getDoorLockedState(), "Door should be unlocked with correct passcode.");
        assertTrue(log.toString().contains("unlocked"), "Log should contain success message.");
    }

    @Test
    void testUnlockWithNoPasscodeRequired() {
        state.setDoorLockedState(true); // currently locked
        state.setPasscodeRequiredForLock(false);
        state.setDoorLockRequest(false); // request unlock

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getDoorLockedState(), "Door should be unlocked when no passcode required.");
        assertTrue(log.toString().contains("unlocked"), "Log should contain success message.");
    }

    // Keyless Entry
    @Test
    void testKeylessEntryAuthorizedUnlock() {
        state.setKeylessEntryEnabled(true);
        state.setDoorLockedState(true); // door is locked
        state.setKnownDevices(List.of("known_phone1",
                "known_phone2", "known_phone3"));
        state.setDetectedDevices(List.of("known_phone2"));
        // No manual unlock request

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getDoorLockedState(), "Door should be unlocked when authorized resident is present.");
        assertTrue(log.toString().contains("Authorized resident detected, unlocking door"), "Log should contain automatica door unlock message");
    }

    @Test
    void testKeylessEntryCannotUnlock() {
        state.setKeylessEntryEnabled(true);
        state.setDoorLockedState(true); // door is locked
        state.setPasscodeRequiredForLock(true);
        state.setKnownDevices(List.of("known_phone1",
                "known_phone2", "known_phone3"));
        state.setDetectedDevices(List.of("bad"));
        // No manual unlock request

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getDoorLockedState(), "Door should be locked when no authorized resident is present.");
        assertFalse(log.toString().contains("Authorized resident detected, unlocking door"), "Log should not contain unlock message");
    }

    /**
     * Intruder Defense Test:
     * -When in-home sensors detect the possible presence of an intruder, lock the door and send "possible intruder detected" messages to the access panels.
     * -Keep the door locked until the sensors provide an "all clear" signal, at which time "all clear" messages are sent to the access panels.
     */
    @Test
    void test_intruderBreakIn() {
        state.setProximityState(false); // user is away
        state.setDoorState(true);       // door is open
        state.setDoorLockedState(false);    // doors not locked

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        assertEquals(Boolean.TRUE, evaluatedState.getDoorLockedState());
        assertTrue(log.toString().contains("possible intruder detected"));
    }

    @Test
    void test_allClearLogs() {
        state.setProximityState(false); // user is away
        state.setDoorState(true);       // door is opened
        state.setDoorLockedState(false);    // doors not locked

        TartanState evaluatedState = evaluator.evaluateState(state, log);

        // Sanity: intruder has engaged lock
        assertEquals(Boolean.TRUE, evaluatedState.getDoorLockedState());

        // 2) Clear the trigger and raise ALL CLEAR
        evaluatedState.setDoorState(false);  // CLOSED
        evaluatedState.allClear = true;      // signal all clear
        evaluator.evaluateState(evaluatedState, log);

        // Must log "Door unlocked after all clear"
        assertTrue(log.toString().contains("Door unlocked after all clear"));
    }
}

class StaticTartanStateEvaluatorUC12EquivalenceClassesTest extends StaticTartanStateEvaluatorTestBase {
    @Test
    void test_hotter_target() {
        state.setTempReading(60);
        state.setTargetTempSetting(61);
        // Hotter temperature target means the AC should be off, and the heater should be on
        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getChillerOnState());
        assertEquals(Boolean.TRUE, evaluatedState.getHeaterOnState());
    }

    @Test
    void test_colder_target() {
        state.setTempReading(60);
        state.setTargetTempSetting(59);
        // Colder temperature target means AC should be on, heater should be off
        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getChillerOnState());
        assertEquals(Boolean.FALSE, evaluatedState.getHeaterOnState());
    }

    @Test
    void test_manual_heater_chiller_setting() {
        // manually enable the heater and chiller, make sure they fix themselves post-evaluation
        state.setChillerOnState(true);
        state.setHeaterOnState(true);
        TartanState evaluatedState = evaluator.evaluateState(state, log);

        Boolean chillerOnState = evaluatedState.getChillerOnState();
        Boolean heaterOnState = evaluatedState.getHeaterOnState();

        if (chillerOnState == null || heaterOnState == null) {
            fail("Both heater and chiller must be non-null");
        }
        assertEquals(Boolean.FALSE,
                chillerOnState && heaterOnState
        );
    }
}

class StaticTartanStateEvaluatorUC06BlackboxTest extends StaticTartanStateEvaluatorTestBase {

    /**
     * UC06: With alarm enabled, opening the door should activate the alarm.
     */
    @Test
    void testUC06_AlarmEnabled_DoorOpened_SoundsAlarm() {
        // Setup: alarm enabled, house vacant, door opened
        state.setAlarmState(true);
        state.setProximityState(false);
        state.setDoorState(true);

        TartanState evaluated = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluated.getAlarmActiveState(),
                "Alarm should sound when door is opened with alarm enabled and house vacant.");
    }

    /**
     * UC06: With alarm enabled, house suddenly occupied should activate the alarm.
     */
    @Test
    void testUC06_AlarmEnabled_NewlyOccupied_SoundsAlarm() {
        // Setup: alarm enabled, door closed, house occupied
        state.setAlarmState(true);
        state.setDoorState(false);
        state.setProximityState(true);

        TartanState evaluated = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluated.getAlarmActiveState(),
                "Alarm should sound when house becomes occupied while alarm is enabled.");
    }
}

class StaticTartanStateEvaluatorUC07BlackboxTest extends StaticTartanStateEvaluatorTestBase {

    /**
     * UC07: When away timer is set, system closes door, turns off light, and enables alarm.
     */
    @Test
    void testUC07_AwayTimer_ArmsAlarm_ClosesDoor_TurnsOffLight() {
        // Setup: away timer set, and set states opposite to expected to verify changes
        state.setAwayTimerState(true);
        state.setLightState(true);
        state.setDoorState(true);
        state.setAlarmState(false);

        TartanState evaluated = evaluator.evaluateState(state, log);

        assertEquals(Boolean.FALSE, evaluated.getLightState(), "Light should be turned off by away timer.");
        assertEquals(Boolean.FALSE, evaluated.getDoorState(), "Door should be closed by away timer.");
        assertEquals(Boolean.TRUE, evaluated.getAlarmState(), "Alarm should be enabled by away timer.");
        assertEquals(Boolean.FALSE, evaluated.getAwayTimerState(), "Away timer flag should reset to false after application.");
    }
}

class StaticTartanStateEvaluatorUC13EquivalenceClassesTest extends StaticTartanStateEvaluatorTestBase {

    @Test
    void test_AC_off_DH_off_valid() {
        state.setChillerOnState(false);
        state.setHumidifierState(false);

        // set the temperature and target so the AC doesn't automatically enable
        state.setTempReading(60);
        state.setTargetTempSetting(61);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.FALSE, evaluatedState.getChillerOnState());
        assertEquals(Boolean.FALSE, evaluatedState.getHumidifierState());
    }

    @Test
    void test_AC_on_DH_off_valid() {
        state.setChillerOnState(true);
        state.setHumidifierState(false);

        // set the temperature and target so the AC doesn't automatically disable
        state.setTempReading(60);
        state.setTargetTempSetting(59);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getChillerOnState());
        assertEquals(Boolean.FALSE, evaluatedState.getHumidifierState());
    }

    @Test
    void test_AC_on_DH_on_valid() {
        state.setChillerOnState(true);
        state.setHumidifierState(true);

        // set the temperature and target so the AC doesn't automatically disable
        state.setTempReading(60);
        state.setTargetTempSetting(59);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        assertEquals(Boolean.TRUE, evaluatedState.getChillerOnState());
        assertEquals(Boolean.TRUE, evaluatedState.getHumidifierState());
    }

    @Test
    void test_AC_off_DH_on_invalid() {
        state.setChillerOnState(false);
        state.setHumidifierState(false);

        // set the temperature and target so the AC doesn't automatically enable
        state.setTempReading(60);
        state.setTargetTempSetting(61);

        TartanState evaluatedState = evaluator.evaluateState(state, log);
        Boolean chillerState = evaluatedState.getChillerOnState();
        Boolean humidifierState = evaluatedState.getHumidifierState();
        if (chillerState == null || humidifierState == null) {
            fail("chiller or humidifier state should not be null");
        }
        // it should not be the case that things are as they were before (AC off and DH on)
        assertEquals(Boolean.FALSE,
                !chillerState && humidifierState
        );
    }

    @Nested
    class NightlockControllerTest {

        private TartanState state;
        private NightlockController nightlock;
        private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        private final PrintStream originalOut = System.out;

        @BeforeEach
        void setUp() {
            state = new TartanState();
            nightlock = new NightlockController(state);
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        // ---------- BASIC FUNCTIONALITY TESTS ----------

        @Test
        void testNightLockActivatesAtNight() {
            nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));
            boolean active = nightlock.checkAndApplyNightLock(LocalTime.of(23, 0));
            assertTrue(active, "Nightlock should activate during night.");
            assertEquals(Boolean.TRUE, state.getDoorLockedState(), "Door should be locked.");
            assertTrue(outContent.toString().contains("Night Lock activated"), "Activation message expected.");
        }


        @Test
        void testNightLockWrapsPastMidnight() {
            nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));
            boolean active = nightlock.checkAndApplyNightLock(LocalTime.of(2, 0));
            assertTrue(active, "Nightlock should activate past midnight.");
            assertEquals(Boolean.TRUE, state.getDoorLockedState());
        }

        // ---------- BOUNDARY VALUE TESTS ----------

        @Test
        void testNightLockAtExactStartTimeInclusive() {
            nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));
            boolean active = nightlock.checkAndApplyNightLock(LocalTime.of(22, 0));
            assertTrue(active, "At exact start time, nightlock should activate.");
            assertEquals(Boolean.TRUE, state.getDoorLockedState());
        }

        @Test
        void testNightLockAtExactEndTimeExclusive() {
            nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));
            boolean active = nightlock.checkAndApplyNightLock(LocalTime.of(6, 0));
            assertFalse(active, "At exact end time, nightlock should deactivate.");
            assertEquals(Boolean.FALSE, state.getDoorLockedState());
        }

        @Test
        void testNightLockAtMidnight() {
            nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));
            boolean active = nightlock.checkAndApplyNightLock(LocalTime.MIDNIGHT);
            assertTrue(active, "Midnight should be within nightlock range.");
            assertEquals(Boolean.TRUE, state.getDoorLockedState());
        }

        // ---------- MANUAL ACTIVATION / DEACTIVATION TESTS ----------

        @Test
        void testActivateNightLockWhenAlreadyLocked() {
            state.setDoorLockedState(true);
            nightlock.activateNightLock();
            assertTrue(outContent.toString().contains("already locked"), "Should detect already locked state.");
        }

        @Test
        void testDeactivateNightLockWhenAlreadyUnlocked() {
            state.setDoorLockedState(false);
            nightlock.deactivateNightLock();
            assertTrue(outContent.toString().contains("already unlocked"), "Should detect already unlocked state.");
        }

        @Test
        void testActivateAndDeactivateManually() {
            nightlock.activateNightLock();
            assertEquals(Boolean.TRUE, state.getDoorLockedState());
            nightlock.deactivateNightLock();
            assertEquals(Boolean.FALSE, state.getDoorLockedState());
        }

        // ---------- RELock BEHAVIOR ----------

        @Test
        void testRelockIfUnlockedDuringNight() {
            nightlock.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));
            nightlock.checkAndApplyNightLock(LocalTime.of(23, 0));
            assertEquals(Boolean.TRUE, state.getDoorLockedState());

            // simulate manual unlock during night
            state.setDoorLockedState(false);
            nightlock.checkAndApplyNightLock(LocalTime.of(23, 30));
            assertEquals(Boolean.TRUE, state.getDoorLockedState(), "Door should relock automatically at night.");
        }

        // ---------- EDGE CONFIGURATION TESTS ----------

        @Test
        void testNightStartAndEndSameTimeMeansAlwaysUnlocked() {
            nightlock.enableNightLock(LocalTime.of(10, 0), LocalTime.of(10, 0));
            boolean active = nightlock.checkAndApplyNightLock(LocalTime.of(10, 0));
            assertFalse(active, "If start == end, assume nightlock never activates.");
            assertEquals(Boolean.FALSE, state.getDoorLockedState());
        }

        @Test
        void testChangeConfigurationTakesEffect() {
            nightlock.enableNightLock(LocalTime.of(20, 0), LocalTime.of(4, 0));
            boolean activeBefore = nightlock.checkAndApplyNightLock(LocalTime.of(19, 0));
            assertFalse(activeBefore);

            nightlock.enableNightLock(LocalTime.of(18, 0), LocalTime.of(4, 0));
            boolean activeAfter = nightlock.checkAndApplyNightLock(LocalTime.of(19, 0));
            assertTrue(activeAfter, "Changing config should update nightlock times.");
        }

        // ---------- STATE QUERIES ----------

        @Test
        void testIsDoorLockedReflectsState() {
            state.setDoorLockedState(true);
            assertTrue(nightlock.isDoorLocked());
            state.setDoorLockedState(false);
            assertFalse(nightlock.isDoorLocked());
        }

    }
}

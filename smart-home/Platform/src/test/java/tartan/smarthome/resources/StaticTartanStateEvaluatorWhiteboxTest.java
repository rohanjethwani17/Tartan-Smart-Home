package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tartan.smarthome.resources.iotcontroller.NightlockController;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Whitebox tests for <code>StaticTartanStateEvaluator.ValidateOpenedDoorRules</code>
 * <p>
 * Strategy: Control flow based
 */
class StaticTartanStateEvaluatorWhiteboxTestValidateOpenedDoorRulesTest extends StaticTartanStateEvaluatorTestBase {
    @Test
    public void test_doorOpenLogged(){
        log = new StringBuffer();
        state.setDoorState(true);
        state.setProximityState(true);
        evaluator.validateOpenedDoorRules(state, log);

        // Opened door with someone home is okay
        assertTrue(log.toString().strip().endsWith("Door open"),
                String.format("%s should end with \"Door open\"", log.toString().strip()));
    }

    @Test
    public void test_breakInDetected(){
        state.setDoorState(true);
        state.setProximityState(false);
        state.setAlarmState(true);
        evaluator.validateOpenedDoorRules(state, log);

        // Opened door with the alarm on and no proximity means a break-in!
        assertTrue(state.getAlarmActiveState());
    }

    @Test
    public void test_DoorAutoCLose(){
        state.setDoorState(true);
        state.setProximityState(false);
        state.setAlarmState(false);
        evaluator.validateOpenedDoorRules(state, log);

        // Opened door with the alarm off and no proximity -> close the door
        assertFalse(state.getDoorState());
    }
}

/**
 * Whitebox tests for <code>StaticTartanStateEvaluator.ValidateClosedDoorRules</code>
 * <p>
 * Strategy: Control flow based
 */
class StaticTartanStateEvaluatorWhiteboxTestValidateClosedDoorRulesTest extends StaticTartanStateEvaluatorTestBase {
    @Test
    public void test_closedDoorLogging(){
        log = new StringBuffer();
        state.setDoorState(false);
        state.setProximityState(false);
        evaluator.validateClosedDoorRules(state, log);

        // No alarm + intruder, therefore just log
        assertTrue(log.toString().strip().endsWith("Closed door"),
                String.format("%s should end with \"Closed door\"", log.toString().strip()));
    }

    @Test
    public void test_BreakInDetected(){
        state.setDoorState(false);
        state.setProximityState(true);
        state.setAlarmState(true);
        evaluator.validateClosedDoorRules(state, log);

        // alarm + intruder = activate alarm
        assertTrue(state.getAlarmActiveState());
    }
}

/**
 * Whitebox tests for <code>StaticTartanStateEvaluator.invokeAwayTimerIfApplicable</code>
 * <p>
 * Strategy: Control flow based
 */
class StaticTartanStateEvaluatorWhiteboxInvokeAwayTimerIfApplicableTest extends StaticTartanStateEvaluatorTestBase {
    @Test
    public void test_doNothing(){
        state.setAwayTimerState(false);
        state.setLightState(true);
        state.setDoorState(true);
        state.setAlarmState(false);

        evaluator.invokeAwayTimerIfApplicable(state, log);

        // nothing should be done
        assertTrue(state.getLightState());
        assertTrue(state.getDoorState());
        assertFalse(state.getAlarmState());
        assertFalse(state.getAwayTimerState());
    }

    @Test
    public void test_setAwayTimer(){
        state.setAwayTimerState(true);
        state.setLightState(true);
        state.setDoorState(true);
        state.setAlarmState(false);

        evaluator.invokeAwayTimerIfApplicable(state, log);

        // away timer should be set
        assertFalse(state.getLightState());
        assertFalse(state.getDoorState());
        assertTrue(state.getAlarmState());
        assertFalse(state.getAwayTimerState());
    }
}

class NightlockControllerWhiteboxTest {

    private TartanState state;
    private NightlockController controller;

    @BeforeEach
    public void setup() {
        state = new TartanState();
        controller = new NightlockController(state);
    }

    /**
     * Tests enabling night lock with custom times
     */
    @Test
    public void test_enableNightLock_customTimes() {
        LocalTime start = LocalTime.of(21, 0);
        LocalTime end = LocalTime.of(5, 0);
        controller.enableNightLock(start, end);

        boolean result = controller.checkAndApplyNightLock(LocalTime.of(22, 0));
        assertTrue(result, "Lock should activate between custom start and end times");
    }

    /**
     * Tests that activateNightLock locks the door only if it’s not already locked
     */
    @Test
    public void test_activateNightLock_alreadyLocked() {
        state.setDoorLockedState(true);
        controller.activateNightLock();
        assertTrue(state.getDoorLockedState(), "Door should remain locked");
    }

    @Test
    public void test_activateNightLock_notLocked() {
        state.setDoorLockedState(false);
        controller.activateNightLock();
        assertTrue(state.getDoorLockedState(), "Door should become locked after activation");
    }

    /**
     * Tests that deactivateNightLock unlocks the door only if it’s currently locked
     */
    @Test
    public void test_deactivateNightLock_alreadyUnlocked() {
        state.setDoorLockedState(false);
        controller.deactivateNightLock();
        assertFalse(state.getDoorLockedState(), "Door should remain unlocked");
    }

    @Test
    public void test_deactivateNightLock_locked() {
        state.setDoorLockedState(true);
        controller.deactivateNightLock();
        assertFalse(state.getDoorLockedState(), "Door should become unlocked after deactivation");
    }

    /**
     * Tests checkAndApplyNightLock for night period without wrapping past midnight
     */
    @Test
    public void test_checkAndApplyNightLock_standardNightPeriod() {
        controller.enableNightLock(LocalTime.of(22, 0), LocalTime.of(6, 0));

        boolean lockActive = controller.checkAndApplyNightLock(LocalTime.of(23, 0));
        assertTrue(lockActive, "Door should be locked during standard night hours");

        lockActive = controller.checkAndApplyNightLock(LocalTime.of(7, 0));
        assertFalse(lockActive, "Door should be unlocked after night hours");
    }

    /**
     * Tests checkAndApplyNightLock for night period wrapping past midnight
     */
    @Test
    public void test_checkAndApplyNightLock_wrapsPastMidnight() {
        controller.enableNightLock(LocalTime.of(23, 0), LocalTime.of(5, 0));

        boolean lockActive = controller.checkAndApplyNightLock(LocalTime.of(1, 0));
        assertTrue(lockActive, "Door should be locked after midnight when wrapping period applies");

        lockActive = controller.checkAndApplyNightLock(LocalTime.of(22, 0));
        assertFalse(lockActive, "Door should be unlocked before night start time");
    }

    /**
     * Tests isDoorLocked() correctness
     */
    @Test
    public void test_isDoorLocked_trueAndFalse() {
        state.setDoorLockedState(true);
        assertTrue(controller.isDoorLocked(), "isDoorLocked should return true when door locked");

        state.setDoorLockedState(false);
        assertFalse(controller.isDoorLocked(), "isDoorLocked should return false when door unlocked");
    }
}
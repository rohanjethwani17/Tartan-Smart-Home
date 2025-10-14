package tartan.smarthome.resources;

import org.junit.jupiter.api.Test;

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
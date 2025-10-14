package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TartanStateTest {
    TartanState state;

    @BeforeEach
    void setUp(){
        state = StaticTartanStateEvaluatorTestBase.createPlausibleTestState();
    }

    @Test
    void test_toAndFromStateMap(){
        Map<String, Object> stateMap = state.toStateMap();
        assertEquals(TartanState.fromStateMap(stateMap), state);
    }

    @Test
    void test_mergeStates(){
        TartanState newState = new TartanState();

        state.setAlarmPassCode("abc");
        state.setProximityState(false);
        state.setTempReading(50);
        state.setGivenPassCode("xyz");

        // newState overrides part of the known values
        newState.setProximityState(true);
        newState.setGivenPassCode("abc");

        state.mergeState(newState);

        // Old value as it wasn't overridden
        assertNotNull(state.getAlarmPassCode());
        assertEquals("abc", state.getAlarmPassCode());

        // New value
        assertNotNull(state.getProximityState());
        assertTrue(state.getProximityState());

        // Old value as it wasn't overridden
        assertNotNull(state.getTempReading());
        assertEquals(50, state.getTempReading());

        // New value
        assertNotNull(state.getGivenPassCode());
        assertEquals("abc", state.getGivenPassCode());
    }

}
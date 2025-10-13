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
     * Intruder Defense Test:
     * -When in-home sensors detect the possible presence of an intruder, lock the door and send "possible intruder detected" messages to the access panels.
     * -Keep the door locked until the sensors provide an "all clear" signal, at which time "all clear" messages are sent to the access panels.
     */
    @Test
    public void test_IntruderDefenseTest() {

    }
}
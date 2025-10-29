package tartan.smarthome.resources;

import org.junit.jupiter.api.BeforeEach;

import java.util.List;

class StaticTartanStateEvaluatorTestBase {
    StaticTartanStateEvaluator evaluator;
    TartanState state;
    StringBuffer log;

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

        output.setDoorLockedState(false);
        output.setPasscodeRequiredForLock(false);
        output.setDoorLockRequest(false);

        output.setKeylessEntryEnabled(false);
        output.setAuthorizedDevices(List.of());
        output.setDetectedDevices(List.of());

        output.setIntruderDetected(false);
        output.setAllClear(false);

        return output;
    }

    @BeforeEach
    public void setUp() {
        evaluator = new StaticTartanStateEvaluator();
        log = new StringBuffer();
        state = createPlausibleTestState();
    }
}
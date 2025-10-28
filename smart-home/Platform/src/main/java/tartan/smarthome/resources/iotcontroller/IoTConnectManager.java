package tartan.smarthome.resources.iotcontroller;

import tartan.smarthome.resources.TartanState;

import java.util.*;

/**
 * Manages connection to the IoT house
 *
 * Project: LG Exec Ed Program
 * Copyright: Copyright (c) 2015 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2015 - initial version
 */
public class IoTConnectManager {
    // Connection to the house
    private IoTConnection connection;

    /**
     * Set up the connection manager with a connection
     * 
     * @param conn the (established) connection
     */
    public IoTConnectManager(IoTConnection conn) {
        connection = conn;
    }

    /**
     * Disconnect from the house
     */
    public void disconnectFromHouse() {
        connection.disconnect();
    }

    /**
     * Get the state from the house
     * 
     * @return the new state of things
     */
    public synchronized TartanState getState() {

        System.out.println("Requesting state");

        synchronized (connection) {
            String update = connection.sendMessageToHouse(IoTValues.GET_STATE + IoTValues.MSG_END);
            if (update == null) {
                return null;
            }

            return handleStateUpdate(update);
        }
    }

    /**
     * Send a state change request to the house
     * 
     * @param state the new state
     * @return true if the state was accepted; false otherwise
     */
    public synchronized Boolean setState(TartanState state) {

        Vector<String> newStateVector = new Vector<>();

        if (state.getDoorState() != null) {
            String doorStateString = state.getDoorState() ? IoTValues.DOOR_OPEN : IoTValues.DOOR_CLOSE;
            newStateVector.add(IoTValues.DOOR_STATE + IoTValues.PARAM_EQ + doorStateString);
        }
        if (state.getLightState() != null) {
            String lightStateString = state.getLightState() ? IoTValues.LIGHT_ON : IoTValues.LIGHT_OFF;
            newStateVector.add(IoTValues.LIGHT_STATE + IoTValues.PARAM_EQ + lightStateString);
        }
        if (state.getAlarmState() != null) {
            String alarmStateString = state.getAlarmState() ? IoTValues.ALARM_ENABLED : IoTValues.ALARM_DISABLED;
            newStateVector.add(IoTValues.ALARM_STATE + IoTValues.PARAM_EQ + alarmStateString);
        }
        if (state.getAlarmActiveState() != null) {
            String alarmActiveStateString = state.getAlarmActiveState() ? IoTValues.ALARM_ON : IoTValues.ALARM_OFF;
            newStateVector.add(IoTValues.ALARM_ACTIVE + IoTValues.PARAM_EQ + alarmActiveStateString);
        }
        if (state.getHumidifierState() != null) {
            String humidifierStateString = state.getHumidifierState() ? IoTValues.HUMIDIFIER_ON
                    : IoTValues.HUMIDIFIER_OFF;
            newStateVector.add(IoTValues.HUMIDIFIER_STATE + IoTValues.PARAM_EQ + humidifierStateString);
        }
        if (state.getChillerOnState() != null) {
            String chillerStateString = state.getChillerOnState() ? IoTValues.CHILLER_ON : IoTValues.CHILLER_OFF;
            newStateVector.add(IoTValues.CHILLER_STATE + IoTValues.PARAM_EQ + chillerStateString);
        }
        if (state.getHeaterOnState() != null) {
            String heaterStateString = state.getHeaterOnState() ? IoTValues.HEATER_ON : IoTValues.HEATER_OFF;
            newStateVector.add(IoTValues.HEATER_STATE + IoTValues.PARAM_EQ + heaterStateString);
        }
        if (state.getDoorLockedState() != null) {
            String doorLockedStateString = state.getDoorLockedState() ? "1" : "0";
            newStateVector.add("doorLockState" + IoTValues.PARAM_EQ + doorLockedStateString);
        }

        StringBuffer newState = new StringBuffer();
        // merge the newStateVector into the newState buffer by separating entries with
        // IoTValues.PARAM_DELIM
        for (int i = 0; i < newStateVector.size(); i++) {
            newState.append(newStateVector.get(i));
            if (i < newStateVector.size() - 1) {
                newState.append(IoTValues.PARAM_DELIM);
            }
        }

        StringBuffer msg = new StringBuffer(
                IoTValues.SET_STATE + IoTValues.MSG_DELIM + newState.toString() + IoTValues.MSG_END);
        System.out.println("ConnectManager newState: " + msg.toString());

        String response = null;
        synchronized (connection) {
            response = connection.sendMessageToHouse(msg.toString());
        }
        if (response == null) {
            System.out.println("No response");
            return false;
        }
        System.out.println("Response: " + response);

        return response.equals(IoTValues.OK);
    }

    /**
     * Process the new state reported by the house
     * 
     * @param stateUpdateMsg the new state message
     * @return the new state
     */
    private TartanState handleStateUpdate(String stateUpdateMsg) {

        if (stateUpdateMsg == null) {
            return null;
        }
        if (stateUpdateMsg.isEmpty()) {
            return null;
        }

        System.out.println("ConnectManager state update: " + stateUpdateMsg);
        TartanState state = new TartanState();

        String[] req = stateUpdateMsg.split(IoTValues.MSG_DELIM);

        // invalid state update
        if (req.length != 2) {
            return null;
        }
        // is this a state update
        String cmd = req[0];
        String body = req[1];

        if (!cmd.equals(IoTValues.STATE_UPDATE)) { // only message that comes from house
            return null;
        }

        if (String.valueOf(body.charAt(body.length() - 1)).equals(IoTValues.MSG_END)) {
            body = body.substring(0, body.length() - 1);
        }
        if (body == null) {
            return null;
        }
        StringTokenizer pt = new StringTokenizer(body, IoTValues.PARAM_DELIM);

        // process the new state
        while (pt.hasMoreTokens()) {
            String param = pt.nextToken();
            String[] data = param.split(IoTValues.PARAM_EQ);
            Integer val = Integer.parseInt(data[1]);

            if (data[0].equals(IoTValues.LIGHT_STATE)) {
                state.setLightState(val == 1);
            } else if (data[0].equals(IoTValues.ALARM_STATE)) {
                state.setAlarmState(val == 1);
            } else if (data[0].equals(IoTValues.DOOR_STATE)) {
                state.setDoorState(val == 1);
            } else if (data[0].equals(IoTValues.HUMIDIFIER_STATE)) {
                state.setHumidifierState(val == 1);
            } else if (data[0].equals(IoTValues.PROXIMITY_STATE)) {
                state.setProximityState(val == 1);
            } else if (data[0].equals(IoTValues.ALARM_ACTIVE)) {
                state.setAlarmActiveState(val == 1);
            } else if (data[0].equals(IoTValues.HEATER_STATE)) {
                state.setHeaterOnState(val == 1);
            } else if (data[0].equals(IoTValues.CHILLER_STATE)) {
                state.setChillerOnState(val == 1);
            } else if (data[0].equals(IoTValues.TEMP_READING)) {
                state.setTempReading(val);
            } else if (data[0].equals(IoTValues.HUMIDITY_READING)) {
                state.setHumidityReading(val);
            } else if (data[0].equals(IoTValues.HVAC_MODE)) {
                if (val == 1) {
                    state.setHvacSetting("Heater");
                } else {
                    state.setHvacSetting("Chiller");
                }
            } else if (data[0].equals("doorLockState")) {
                if (val == 1) {
                    state.setDoorLockedState(true);
                    state.setDoorLockRequest(true);
                } else {
                    state.setDoorLockedState(false);
                    state.setDoorLockRequest(false);
                }
            }
        }
        return state;
    }

    /**
     * Get the connected state
     * 
     * @return true if connected, false otherwise
     */
    public Boolean isConnected() {
        if (connection != null) {
            return connection.isConnected();
        }
        return false;
    }
}

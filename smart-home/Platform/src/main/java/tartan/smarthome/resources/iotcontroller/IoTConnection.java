package tartan.smarthome.resources.iotcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A connection to an IoT-enabled house. This class handles the network connection to the house
 * <p>
 * Project: LG Exec Ed Program
 * Copyright: 2015 Jeffrey S. Gennari
 * Versions:
 * 1.0 November 2015 - initial version
 */
public class IoTConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoTConnection.class);
    private Boolean isConnected = false;
    /**
     * connection settings
     */
    private String address = null;
    private Integer port = 5050; // the default port for the house

    /**
     * The connection is private so it can be controlled
     */
    private Socket houseSocket = null;
    private BufferedWriter out = null;
    private BufferedReader in = null;

    /**
     * Get an existing connection, or make a new one
     *
     * @param addr the house address
     * @return the established connection or null
     */
    public IoTConnection(String addr, Integer port) {
        this.address = addr;
        this.port = port;

    }

    /**
     * Get the house address
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get the house port
     *
     * @return the port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Get connection state
     *
     * @return true if connected, false otherwise
     */
    public Boolean isConnected() {
        return isConnected;
    }

    /**
     * Send a message to the house and get a response
     *
     * @param msg the message to send
     * @return the response
     */
    public String sendMessageToHouse(String msg) {
        try {

            out.write(msg, 0, msg.length());
            out.flush();

            return in.readLine();

        } catch (IOException ioe) {
            LOGGER.error("House @ {}:{} | Error sending message: {}", this.address, this.port, ioe.toString());
        }
        return null;
    }

    /**
     * Disconnect from the house
     */
    public void disconnect() {
        if (houseSocket != null && houseSocket.isConnected()) {
            try {
                houseSocket.close();
            } catch (IOException e) {
                LOGGER.error("House @ {}:{} | Error disconnecting: {}", this.address, this.port, e.toString());
            }
        }
        isConnected = false;
    }

    /**
     * Connect to the house
     *
     * @return true if connection successful, false otherwise
     */
    public Boolean connect() {

        try {
            houseSocket = new Socket(this.address, this.port);

            out = new BufferedWriter(new OutputStreamWriter(houseSocket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(houseSocket.getInputStream()));

        } catch (UnknownHostException uhe) {
            LOGGER.error("Unknown host: {}", address);
            return false;
        } catch (IOException ioe) {
            return false;
        }
        isConnected = true;
        return true;
    }

}

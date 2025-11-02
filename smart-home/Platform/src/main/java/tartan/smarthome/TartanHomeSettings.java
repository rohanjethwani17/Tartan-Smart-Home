package tartan.smarthome;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import jakarta.validation.constraints.NotEmpty;

/**
 * This is Jackson-compatible a configuration class for the initial
 * configuration setting in the primiary
 * YAML confguration file. See that file for definitions
 */
public class TartanHomeSettings {

    @NotEmpty
    @JsonProperty
    private String name;

    @NotEmpty
    @JsonProperty
    private String address;

    @NotEmpty
    @JsonProperty
    private Integer port;

    @NotEmpty
    @JsonProperty
    private String user;

    @NotEmpty
    @JsonProperty
    private String password;

    @NotEmpty
    @JsonProperty
    private String targetTemp;

    @NotEmpty
    @JsonProperty
    private String alarmDelay;

    @NotEmpty
    @JsonProperty
    private String alarmPasscode;

    @NotEmpty
    @JsonProperty
    private Boolean passcodeRequiredForLock;

    @NotEmpty
    @JsonProperty
    private Boolean keylessEntryEnabled;

    @JsonProperty
    private List<String> authorizedDevices = new ArrayList<>();

    @JsonProperty
    private LocalTime nightStart;

    @JsonProperty
    private LocalTime nightEnd;

    @NotEmpty
    @JsonProperty
    private String groupExperiment;

    public String getGroupExperiment() {
        return groupExperiment;
    }

    public void setGroupExperiment(String groupExperiment) {
        this.groupExperiment = groupExperiment;
    }

    public LocalTime getNightStart() {
        return nightStart;
    }

    public void setNightStart(LocalTime nightStart) {
        this.nightStart = nightStart;
    }

    public LocalTime getNightEnd() {
        return nightEnd;
    }

    public void setNightEnd(LocalTime nightEnd) {
        this.nightEnd = nightEnd;
    }

    public List<String> getAuthorizedDevices() {
        return authorizedDevices;
    }

    public void setAuthorizedDevices(List<String> authorizedDevices) {
        this.authorizedDevices = authorizedDevices;
    }

    public Boolean getKeylessEntryEnabled() {
        return keylessEntryEnabled;
    }

    public void setKeylessEntryEnabled(Boolean keylessEntryEnabled) {
        this.keylessEntryEnabled = keylessEntryEnabled;
    }

    public Boolean getPasscodeRequiredForLock() {
        return passcodeRequiredForLock;
    }

    public void setPasscodeRequiredForLock(Boolean passcodeRequiredForLock) {
        this.passcodeRequiredForLock = passcodeRequiredForLock;
    }

    public String getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(String targetTemp) {
        this.targetTemp = targetTemp;
    }

    public String getAlarmDelay() {
        return this.alarmDelay;
    }

    public void setAlarmDelay(String alarmDelay) {
        this.alarmDelay = alarmDelay;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlarmPasscode() {
        return alarmPasscode;
    }

    public void setAlarmPasscode(String alarmPasscode) {
        this.alarmPasscode = alarmPasscode;
    }
}

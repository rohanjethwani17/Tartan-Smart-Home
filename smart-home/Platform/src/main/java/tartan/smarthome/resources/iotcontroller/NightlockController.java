package tartan.smarthome.resources.iotcontroller;

import tartan.smarthome.resources.TartanState;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalTime;

public class NightlockController {
    private final TartanState tartanState;
    private LocalTime nightStart = LocalTime.of(22, 0); // default night start 10 PM
    private LocalTime nightEnd = LocalTime.of(6, 0); // default night end 6 AM
    private StringBuffer log;

    private String formatLogEntry(String entry) {
        long timeStamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        return "[" + sdf.format(new Date(timeStamp)) + "]: " + entry + "\n";
    }

    public NightlockController(TartanState tartanState, StringBuffer log) {
        this.tartanState = tartanState;
        this.log = log;
    }

    /**
     * Enables night lock with specific start and end times
     */
    public void enableNightLock(LocalTime start, LocalTime end) {
        this.nightStart = start;
        this.nightEnd = end;
    }

    /**
     * Activates the night lock (locks the door)
     */
    public void activateNightLock() {
        if (Boolean.TRUE.equals(tartanState.getDoorLockedState())) {
            log.append(formatLogEntry("[ACCESS PANEL] Door already locked."));
            return;
        }
        tartanState.setDoorLockedState(true);
        log.append(formatLogEntry("[ACCESS PANEL] Night Lock activated - door locked."));
    }

    /**
     * Deactivates the night lock (unlocks the door)
     */
    public void deactivateNightLock() {
        if (!tartanState.getDoorLockedState()) {
            // Door already unlocked → do nothing
            log.append(formatLogEntry("[ACCESS PANEL] Door already unlocked."));
            return;
        }
        tartanState.setDoorLockedState(false);
        log.append(formatLogEntry("[ACCESS PANEL] Night Lock ended - door unlocked."));
    }

    /**
     * Returns whether the door is currently locked
     */
    public boolean isDoorLocked() {
        return Boolean.TRUE.equals(tartanState.getDoorLockedState());
    }

    /**
     * Checks current time and applies night lock accordingly.
     * Returns true if the lock is active, false otherwise.
     */
    /**
     * Checks current time and applies night lock accordingly.
     * Returns true if the lock is active, false otherwise.
     */
    public boolean checkAndApplyNightLock(LocalTime currentTime) {
        // If nightStart and nightEnd are equal, nightlock never activates
        if (nightStart.equals(nightEnd)) {
            return false;
        }

        boolean lockActive;

        if (nightStart.isBefore(nightEnd)) {
            // Night period does NOT wrap past midnight (same-day range)
            lockActive = !currentTime.isBefore(nightStart) && currentTime.isBefore(nightEnd);
        } else {
            // Night period wraps past midnight
            lockActive = !currentTime.isBefore(nightStart) || currentTime.isBefore(nightEnd);
        }

        // Apply lock state based on current time
        if (lockActive) {
            activateNightLock();
        }

        return lockActive;
    }

}

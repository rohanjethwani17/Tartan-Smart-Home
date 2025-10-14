package tartan.smarthome.resources.iotcontroller;

import tartan.smarthome.resources.TartanState;

import java.time.LocalTime;

public class NightlockController {
    private final TartanState tartanState;
    private LocalTime nightStart = LocalTime.of(22, 0); // default night start 10 PM
    private LocalTime nightEnd = LocalTime.of(6, 0);    // default night end 6 AM

    public NightlockController(TartanState tartanState) {
        this.tartanState = tartanState;
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
            System.out.println("[ACCESS PANEL] Door already locked.");
            return;
        }
        tartanState.setDoorLockedState(true);
        System.out.println("[ACCESS PANEL] Night Lock activated — door locked.");
    }

    /**
     * Deactivates the night lock (unlocks the door)
     */
    public void deactivateNightLock() {
        if (!tartanState.getDoorLockedState()) {
            // Door already unlocked → do nothing
            System.out.println("[ACCESS PANEL] Door already unlocked.");
            return;
        }
        tartanState.setDoorLockedState(false);
        System.out.println("[ACCESS PANEL] Night Lock ended — door unlocked.");
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
            deactivateNightLock();
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
        } else {
            deactivateNightLock();
        }

        return lockActive;
    }

}

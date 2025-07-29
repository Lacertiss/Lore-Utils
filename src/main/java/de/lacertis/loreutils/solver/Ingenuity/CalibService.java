package de.lacertis.loreutils.solver.Ingenuity;

import de.lacertis.loreutils.MessageManager;

public class CalibService {
    private static final CalibService INSTANCE = new CalibService();
    private ChangeDrivenCalibrator calibrator;
    private boolean running;

    private CalibService() {}

    public static CalibService get() {
        return INSTANCE;
    }

    public boolean start() {
        if (running) return false;
        calibrator = new ChangeDrivenCalibrator(new IngenuityInput());
        calibrator.start();
        running = true;
        if (IngenuityDebug.enabled() && running) IngenuityDebug.chat("calibrate start (running=%s)", running);
        return true;
    }

    public void stop() {
        if (calibrator != null) {
            calibrator.stop();
            calibrator = null;
        }
        running = false;
        MessageManager.sendChatColored("&7Ingenuity: Calibration stopped.");
        if (IngenuityDebug.enabled() && running) IngenuityDebug.chat("calibrate stop");
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        boolean wasRunning = running;
        if (calibrator != null) {
            calibrator.tick();
            if (!calibrator.isRunning()) {
                running = false;
            }
        }
        if (wasRunning && !running) {
            MessageManager.sendChatColored("&aIngenuity: Calibration finished.");
        }
    }

    public String status() {
        IngenuityPerms d = PermutationsStorage.loadOrDefault();
        boolean ready = PermutationsStorage.permsReady(d);
        return "running=" + running + ", permsReady=" + ready + ", stale=" + d.stale;
    }

    public void resetSessions() {
        if (calibrator != null) {
            calibrator.resetSessions();
        }
    }

    public void markStale() {
        IngenuityPerms d = PermutationsStorage.loadOrDefault();
        PermutationsStorage.markStale(d, "manual-reset");
    }
}

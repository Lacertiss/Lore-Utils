package de.lacertis.loreutils.solver.Ingenuity;

import de.lacertis.loreutils.EspRender;
import de.lacertis.loreutils.MessageManager;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class IngenuityPlanRunner {
    private static final BlockPos EAST_INPUT = new BlockPos(-12156, 72, 12579);
    private static final BlockPos LECTERN_INPUT = new BlockPos(-12180, 72, 12579);
    private static final BlockPos WEST_INPUT = new BlockPos(-12204, 72, 12579);

    private final IngenuityInput reader;
    private final ChangeStream changeStream;
    private List<PlanUtils.Run> plan;
    private int currentRunIndex;
    private int currentCountInRun;
    private boolean running;
    private String runId;

    public IngenuityPlanRunner(IngenuityInput reader) {
        this.reader = reader;
        this.changeStream = new ChangeStream(reader, 2, 200, this::onChange);
    }

    public void loadPlan(List<Move> moves) {
        this.plan = PlanUtils.compress(moves);
        this.currentRunIndex = 0;
        this.currentCountInRun = 0;
        this.running = false;
    }

    public void start() {
        if (plan == null || plan.isEmpty()) return;
        running = true;
        runId = java.util.UUID.randomUUID().toString();
        showNextStep();
    }

    public void stop() {
        running = false;
        EspRender.unregisterAllPositions();
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        if (running) changeStream.tick();
    }

    private void onChange(Tile[] before, Tile[] after) {
        if (!running || currentRunIndex >= plan.size()) return;

        PlanUtils.Run currentRun = plan.get(currentRunIndex);
        Move expectedMove = currentRun.move;

        Move actualMove = DeltaUtil.classify(DeltaUtil.diff(before, after));

        if (actualMove == expectedMove) {
            currentCountInRun++;
            if (currentCountInRun >= currentRun.count) {
                currentRunIndex++;
                currentCountInRun = 0;
            }

            if (currentRunIndex >= plan.size()) {
                MessageManager.sendChatColored("&aPlan completed!");
                stop();
            } else {
                showNextStep();
            }
        } else if (actualMove != Move.UNKNOWN) {
            MessageManager.sendChatColored("&cUnexpected move: " + actualMove);
            stop();
        }
    }

    private void showNextStep() {
        if (currentRunIndex >= plan.size()) return;

        PlanUtils.Run currentRun = plan.get(currentRunIndex);
        Move expectedMove = currentRun.move;
        int remaining = currentRun.count - currentCountInRun;

        EspRender.unregisterAllPositions();
        EspRender.registerPosition(inputPos(expectedMove));

        int totalSteps = plan.size();
        int currentStep = currentRunIndex + 1;
        MessageManager.sendActionBarColored(String.format("&bStep %d/%d: %s x%d", currentStep, totalSteps, expectedMove, remaining));
    }

    private BlockPos inputPos(Move m) {
        switch (m) {
            case EAST: return EAST_INPUT;
            case WEST: return WEST_INPUT;
            case LECTERN: return LECTERN_INPUT;
            default: return null;
        }
    }
}

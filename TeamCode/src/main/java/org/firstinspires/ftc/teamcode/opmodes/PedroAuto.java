package org.firstinspires.ftc.teamcode.opmodes;

import com.pedropathing.follower.Follower;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedro.AutoPaths;
import org.firstinspires.ftc.teamcode.pedro.Constants;

import java.util.List;

/**
 * Runs the route in {@link AutoPaths}, one path after another.
 *
 * <p>To change the route, replace {@code pedro/AutoPaths.java} — not this file.
 * To do something at a stop (score, intake), fill in {@link #onArrived}.
 *
 * <p>Iterative OpMode: {@code loop()} never waits. Each call it updates the
 * follower and, once the current path is done, starts the next.
 */
@Autonomous(name = "Pedro Auto", group = "Competition")
public class PedroAuto extends OpMode {

    private Follower follower;
    private List<Path> route;
    private int current = 0;
    private boolean finished = false;

    @Override
    public void init() {
        // Throws with instructions if Pedro hasn't been tuned yet.
        follower = Constants.create(hardwareMap);
        route = AutoPaths.build();

        // Pedro can't know where you put the robot. If this doesn't match
        // reality, every path is off by the same amount.
        follower.setPose(AutoPaths.START);

        telemetry.addData("Route", "%d paths loaded", route.size());
        telemetry.addData("Start", AutoPaths.START);
        telemetry.addLine("Place the robot exactly on its start mark.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (route.isEmpty()) {
            finished = true;
            return;
        }
        follower.follow(route.get(0));
    }

    @Override
    public void loop() {
        follower.update();  // every loop — reads odometry, drives the motors

        if (!finished && !follower.isBusy()) {
            onArrived(current);
            current++;
            if (current < route.size()) {
                follower.follow(route.get(current));
            } else {
                finished = true;
            }
        }

        telemetry.addData("Path", finished ? "done" : (current + 1) + " / " + route.size());
        telemetry.addData("Progress", "%.0f%%", follower.completion() * 100);
        telemetry.addData("Pose", follower.pose());
        telemetry.update();
    }

    @Override
    public void stop() {
        follower.stop();
    }

    /**
     * Called once when path {@code index} (0-based) finishes. Put mechanism
     * actions here — but keep them non-blocking: start the action and return.
     * Something that takes time needs its own state, not a sleep.
     */
    private void onArrived(int index) {
        // e.g. if (index == 0) { scorer.release(); }
    }
}

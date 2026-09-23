package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedro.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.OTOSTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.OctoQuadTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.Tests;
import org.firstinspires.ftc.teamcode.pedro.procedures.ThreeWheelIMUTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.ThreeWheelTuner;
import org.firstinspires.ftc.teamcode.pedro.procedures.TwoWheelTuner;

/**
 * Registers Pedro's autotuners. They are NOT Driver Station OpModes — the
 * tuning library finds these {@code @Tuner} methods at startup and serves them
 * as a web page on the robot:
 *
 * <pre>    http://192.168.43.1:10158</pre>
 *
 * Connect a laptop to the Control Hub's Wi-Fi and open that address.
 *
 * <p>Rules the library enforces (it throws at startup otherwise): each method
 * must be {@code static}, take no parameters, and declare its return type as
 * exactly {@code Procedure} — not a subclass.
 *
 * <p>Pattern follows the Pedro author's own filled-in version of this file in
 * the official Quickstart (commit 20768b3).
 */
public class Tuning {

    // ---- Step 1: drivetrain ------------------------------------------------

    @Tuner
    public static Procedure mecanumTuner() {
        return new MecanumTuner();
    }

    // ---- Step 2: localizer — run the ONE matching your hardware -------------
    // Standalone: they ask for hardware names themselves and print a config.

    @Tuner
    public static Procedure pinpointTuner() {
        return new PinpointTuner();
    }

    @Tuner
    public static Procedure otosTuner() {
        return new OTOSTuner();
    }

    @Tuner
    public static Procedure threeWheelTuner() {
        return new ThreeWheelTuner();
    }

    @Tuner
    public static Procedure threeWheelIMUTuner() {
        return new ThreeWheelIMUTuner();
    }

    @Tuner
    public static Procedure twoWheelTuner() {
        return new TwoWheelTuner();
    }

    @Tuner
    public static Procedure octoQuadTuner() {
        return new OctoQuadTuner();
    }

    // ---- Step 3: path follower ----------------------------------------------
    // Uses the drivetrain and localizer from Constants, so paste steps 1–2
    // into Constants.java and redeploy BEFORE running this.

    @Tuner
    public static Procedure foresightTuner() {
        return new ForesightTuner(Constants::localizer, Constants::drivetrain);
    }

    // ---- Step 4: verify -----------------------------------------------------
    // Before Foresight is tuned the algorithm is passed as null: Localization,
    // Odometry, Pose and Driving tests work, while Hold/Line/Curve/Interpolation
    // abort with a message. Once foresightConfig is filled in, they all run.

    @Tuner
    public static Procedure tests() {
        return new Tests(
                Constants::drivetrain,
                Constants::localizer,
                Constants.isFullyTuned() ? () -> new Foresight(Constants.foresightConfig) : null);
    }
}

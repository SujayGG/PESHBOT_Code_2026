package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.algorithm.ForesightConfig;
import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Localizer;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.revhub.localizers.ThreeWheelIMUConfig;
import com.pedropathing.revhub.localizers.ThreeWheelIMULocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.util.DriveConstants;

/**
 * Pedro Pathing 3 configuration for this robot.
 *
 * <p>Fill this in by running the autotuners, in order, from the tuning web page
 * at <b>http://192.168.43.1:10158</b> (Control Hub Wi-Fi). Each tuner ends by
 * printing a finished Java block — paste it over the matching field below.
 *
 * <ol>
 *   <li><b>Mecanum Tuner</b> → {@link #drivetrainConfig}</li>
 *   <li>Your localizer's tuner (see {@link #ODOMETRY}) →
 *       {@link #localizerConfig} (Pinpoint) or {@link #threeWheelIMUConfig}</li>
 *   <li><b>Foresight Tuner</b> → {@link #foresightConfig}</li>
 *   <li><b>Tests</b> → confirm it all works before writing autonomous</li>
 * </ol>
 *
 * <p>Full walkthrough: docs/guides/03-pedro-setup-and-autotune.md
 */
public class Constants {

    // ------------------------------------------------------------------------
    // STEP 1 — Drivetrain. Replace with Mecanum Tuner output.
    //
    // Starts from util/DriveConstants so the Pedro code and the plain-SDK
    // MecanumTeleOp agree about motor names and directions. The tuner verifies
    // the directions properly; if it disagrees, fix DriveConstants too, or the
    // two drive codes will fight each other.
    // ------------------------------------------------------------------------
    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set(DriveConstants.FRONT_LEFT_NAME);
        c.frontRightName.set(DriveConstants.FRONT_RIGHT_NAME);
        c.backLeftName.set(DriveConstants.BACK_LEFT_NAME);
        c.backRightName.set(DriveConstants.BACK_RIGHT_NAME);

        c.frontLeftDirection.set(direction(DriveConstants.FRONT_LEFT_REVERSED));
        c.frontRightDirection.set(direction(DriveConstants.FRONT_RIGHT_REVERSED));
        c.backLeftDirection.set(direction(DriveConstants.BACK_LEFT_REVERSED));
        c.backRightDirection.set(direction(DriveConstants.BACK_RIGHT_REVERSED));
    });

    // ------------------------------------------------------------------------
    // STEP 2 — Localizer. Pick ONE with ODOMETRY, then run its tuner.
    //
    //   PINPOINT         goBILDA Pinpoint board        → Pinpoint Tuner
    //   THREE_WHEEL_IMU  3 odometry pods + Hub IMU     → Three Wheel + IMU Tuner
    //
    // Other localizers (OTOS, two-wheel, three-wheel without IMU, OctoQuad)
    // follow the same pattern: add a value here, a config below, and a case in
    // localizer().
    // ------------------------------------------------------------------------
    public enum Odometry { PINPOINT, THREE_WHEEL_IMU }

    /** Which localizer the Follower, Foresight Tuner and Tests use. */
    public static Odometry ODOMETRY = Odometry.THREE_WHEEL_IMU;

    // PINPOINT — values below are PLACEHOLDERS, not measurements. Replace with
    // Pinpoint Tuner output.
    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");  // must match the robot configuration
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(0.0);
        c.yPodOffset.set(0.0);
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // THREE_WHEEL_IMU — null until tuned. Paste the Three Wheel + IMU Tuner
    // output over `null`, then:
    //   1. rename the pasted `localizerConfig` to `threeWheelIMUConfig`
    //      (the tuner always prints that name; ours is separate so a Pinpoint
    //      config can live alongside it), and
    //   2. import what it uses: Encoder, RevHubIMU (com.pedropathing.revhub.
    //      localizers) and RevHubOrientationOnRobot (com.qualcomm.hardware.rev)
    //      — Alt+Enter on each red name.
    public static ThreeWheelIMUConfig threeWheelIMUConfig = null;

    // ------------------------------------------------------------------------
    // STEP 3 — Path follower. Replace `null` with Foresight Tuner output.
    //
    // Deliberately null until tuned. Every field is a physical measurement of
    // this robot — top speed, how far it coasts, how hard it can brake — so
    // there is no safe default. Copying another team's numbers (including the
    // ones in Pedro's own example) gives a robot that overshoots into walls.
    //
    // The tuner prints a block starting:
    //     public static ForesightConfig foresightConfig = new ForesightConfig(
    // Paste the whole thing over the line below and add the imports it needs
    // (Controller, Matrix, Vector2D — Android Studio's Alt+Enter will offer them).
    // ------------------------------------------------------------------------
    public static ForesightConfig foresightConfig = null;

    // ------------------------------------------------------------------------
    // Factories. The tuners and every Pedro OpMode build hardware through
    // these, so changing localizer type is a one-line edit here.
    // ------------------------------------------------------------------------

    public static Localizer localizer(HardwareMap h) {
        switch (ODOMETRY) {
            case THREE_WHEEL_IMU:
                if (threeWheelIMUConfig == null) {
                    throw new IllegalStateException(
                            "The Three Wheel + IMU localizer isn't tuned yet: run that tuner at "
                            + "http://192.168.43.1:10158, paste its output into pedro/Constants.java "
                            + "(threeWheelIMUConfig), and redeploy. Using a Pinpoint instead? Set "
                            + "ODOMETRY = Odometry.PINPOINT.");
                }
                return new ThreeWheelIMULocalizer(h, threeWheelIMUConfig);
            case PINPOINT:
            default:
                return new PinpointLocalizer(h, localizerConfig);
        }
    }

    public static Drivetrain drivetrain(HardwareMap h) {
        return new Mecanum(h, drivetrainConfig);
    }

    /** @return true once the Foresight Tuner output has been pasted in. */
    public static boolean isFullyTuned() {
        return foresightConfig != null;
    }

    /**
     * Builds the Follower every Pedro OpMode uses. Argument order is
     * (Localizer, Drivetrain, Algorithm) — verified against the v3.0.1 source.
     *
     * @throws IllegalStateException before the Foresight Tuner has been run,
     *         so an untuned robot fails loudly on INIT instead of driving off.
     */
    public static Follower create(HardwareMap h) {
        if (!isFullyTuned()) {
            throw new IllegalStateException(
                    "Pedro isn't tuned yet: run the Foresight Tuner at "
                    + "http://192.168.43.1:10158 and paste its output into "
                    + "pedro/Constants.java (foresightConfig).");
        }
        return new Follower(localizer(h), drivetrain(h), new Foresight(foresightConfig));
    }

    private static DcMotorSimple.Direction direction(boolean reversed) {
        return reversed ? DcMotorSimple.Direction.REVERSE : DcMotorSimple.Direction.FORWARD;
    }
}

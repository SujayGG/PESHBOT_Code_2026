package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;
import com.pedropathing.paths.Path;

import java.util.Arrays;
import java.util.List;

/**
 * The autonomous route — the ONE file to replace with output from
 * ftc.peshcompsci.org. {@code opmodes/PedroAuto.java} runs whatever is here and
 * never needs editing for a new route.
 *
 * <p>Contract the OpMode relies on:
 * <ul>
 *   <li>{@link #START} — where the robot physically sits at INIT.</li>
 *   <li>{@link #build()} — the paths, in order. Each one starts where the
 *       previous one ended.</li>
 * </ul>
 *
 * <p><b>Pedro 3 syntax only.</b> Code written for Pedro 1/2 — {@code new
 * BezierLine(...)}, {@code follower.pathBuilder()}, {@code PathChain},
 * {@code setLinearHeadingInterpolation(...)} — does not exist in Pedro 3 and
 * will not compile. The Pedro 3 equivalents:
 *
 * <pre>
 *   Paths.line(a, b).linear(a, b)            straight, heading turns a → b
 *   Paths.curve(a, ctrl, b).linear(a, b)      Bézier, ctrl bends it (not visited)
 *   Paths.through(a, mid, b).linear(a, b)     smooth, passes through mid
 *   .tangent()  .constant(heading)  .facingPoint(pose)   other heading modes
 * </pre>
 *
 * <p>Units: inches, field is 144 × 144. Headings in degrees via {@link #FIELD}.
 * The placeholder route below is a shape to replace, not a BIOBUZZ auto.
 */
public final class AutoPaths {

    private AutoPaths() {}

    /** Write headings in degrees; Pedro converts to radians internally. */
    public static final PoseFactory FIELD = PoseFactory.degrees();

    // ===================== REPLACE FROM HERE ... ============================

    public static final Pose START = FIELD.of(9, 60, 0);

    public static final Pose SCORE = FIELD.of(36, 84, 45);
    public static final Pose PICKUP_CONTROL = FIELD.of(20, 110, 0);
    public static final Pose PICKUP = FIELD.of(40, 120, 90);
    public static final Pose PARK = FIELD.of(60, 96, 90);

    public static List<Path> build() {
        return Arrays.asList(
                Paths.line(START, SCORE).linear(START, SCORE),
                Paths.curve(SCORE, PICKUP_CONTROL, PICKUP).linear(SCORE, PICKUP),
                Paths.line(PICKUP, SCORE).linear(PICKUP, SCORE),
                Paths.line(SCORE, PARK).linear(SCORE, PARK)
        );
    }

    // ===================== ... TO HERE ======================================
}

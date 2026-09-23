# Guide 2 — Odometry and Pedro Pathing 3

**Prerequisite:** [Guide 1](01-teleop-mecanum.md) finished, all four exercises
working on the robot.

**What you'll have at the end:** a robot that knows where it is on the field
and drives smooth curved paths to coordinates you name.

> **Accuracy note.** Everything here was read from the Pedro Pathing **v3.0.0/3.0.1**
> source and the official Pedro Quickstart, not from memory — Pedro 3 is a
> substantial rewrite and older tutorials (and AI answers) describe Pedro 1/2,
> which will not compile. The official docs are at
> [pedropathing.com](https://pedropathing.com/).
>
> Nobody has compiled the snippets on this page against a real robot yet. Treat
> the first build as part of the work.

---

## 1. Why timed autonomous fails

The obvious first autonomous:

```java
leftDrive.setPower(0.5);
rightDrive.setPower(0.5);
sleep(1000);              // "drive forward about 2 feet"
```

This works in the pit and fails at competition, because `setPower(0.5)` is not
a speed — it's a fraction of battery voltage. A fresh battery drives further
than a drained one. Carpet differs from tile. A wheel scuffs the wall.

And errors **accumulate**: drift 2 inches on the first move and every later
move starts 2 inches wrong. By the fourth you've missed entirely.

The fix is to stop measuring time and start measuring **position**.

---

## 2. What odometry actually is

**Odometry** means tracking your position by measuring wheel or encoder
rotation. Instead of "drive for 1 second" you say "drive to (36, 24)", and the
robot corrects itself as it goes.

Your position is a **pose** — three numbers:

- **x** — inches along the field
- **y** — inches across the field
- **heading** — which way you're facing

The BIOBUZZ field is about **144 × 144 inches** (12 ft square), so coordinates
run 0–144 in each direction. Pick a corner as (0, 0) and be consistent; every
pose in your autonomous is relative to that choice.

### Where the numbers come from

| Method | How | Accuracy |
|---|---|---|
| **Drive encoders** | Built into the drive motors | Poor — wheels slip under acceleration, and slip reads as motion |
| **Dead-wheel odometry** | Unpowered wheels on light spring pressure | Good — they don't drive, so they don't slip |
| **goBILDA Pinpoint** | Two dead wheels + built-in IMU on one board | Good, and much less to configure |
| **SparkFun OTOS** | Optical sensor reading the floor | Good, no moving parts |

Most teams use **Pinpoint** now: two odometry pods plug into one board that
does the math and fuses in its own IMU. Pedro supports all of the above.

---

## 3. What Pedro Pathing gives you

Odometry tells you where you are. **Pedro Pathing** uses that to actually drive
somewhere — you hand it a path and it works out wheel powers, correcting
continuously.

Its paths are **Bezier curves**, so the robot flows through a turn instead of
stopping, rotating, and starting again. On a 30-second autonomous that's worth
several seconds.

### Pedro 3's three pieces

Pedro 3 is built from three objects you construct and hand to a `Follower`:

| Piece | What it is | Class |
|---|---|---|
| **Localizer** | Where am I? | `PinpointLocalizer`, `OTOSLocalizer`, `ThreeWheelLocalizer`, … |
| **Drivetrain** | How do I move? | `Mecanum`, `Swerve` |
| **Algorithm** | How do I follow a path? | `Foresight` |

```java
Follower follower = new Follower(localizer, drivetrain, algorithm);
```

> **Argument order matters and is easy to get wrong.** The constructor is
> `Follower(Localizer, Drivetrain, Algorithm)` — verified in the v3.0.0 and v3.0.1 source.
> A comment in the official Quickstart's `Constants.java` lists them as
> `(Drivetrain, Localizer, Foresight)`, which is misleading. Trust the
> compiler: both are objects, so a swap is a type error, not a silent bug.

`Foresight` is Pedro 3's **predictive braking** follower. Rather than reacting
to error with a PID, it predicts how far the robot will coast and starts
braking early — smoother and less twitchy, but it needs to know your robot's
real acceleration, which is what tuning measures.

---

## 4. Gradle setup — already done

This repo is already configured to match the official
[Pedro Quickstart](https://github.com/Pedro-Pathing/Quickstart):

```groovy
// build.dependencies.gradle
maven { url 'https://repo.dairy.foundation/releases/' }

implementation 'com.pedropathing:revhub:3.0.1'   // Pedro 3 (pulls in core)
implementation 'com.pedropathing:tuning:1.0.1'   // the autotuners
```

plus `compileSdk 34` in both `build.common.gradle` and
`FtcRobotController/build.gradle` — Pedro needs API 34 and the Quickstart sets
it in both places.

Note the repository: Pedro 3 lives on `repo.dairy.foundation`, **not** the
Maven repo Pedro 2 used. Old setup instructions will point you at the wrong one.

---

## 5. Tune before you write paths

**You cannot skip this, and you cannot copy another team's numbers.** Pedro
needs to know how *your* robot accelerates, decelerates, and drifts — those
depend on its weight, wheels, motors and gearing.

Pedro 3 ships interactive autotuners. They aren't Driver Station OpModes: the
robot serves them as a web page at **http://192.168.43.1:10158** (connect a
laptop to the Control Hub's Wi-Fi). Each walks you through with prompts, drives
the robot itself where needed, and prints the exact Java to paste into
`pedro/Constants.java`.

| Order | Tuner | Finds |
|---|---|---|
| 1 | Mecanum Tuner | Which way each drive motor spins |
| 2 | Pinpoint Tuner (or OTOS / three-wheel / …) | Pod directions and offsets |
| 3 | Foresight Tuner | Top speed, natural deceleration, braking |
| 4 | Tests | Confirms localization and path following work |

They're registered in `pedro/Tuning.java` with the `@Tuner` annotation. The
library is strict: each method must be `static`, take no arguments, and return
exactly `Procedure`, or the app throws at startup.

**The step-by-step checklist for tuning day is
[Guide 3](03-pedro-setup-and-autotune.md).**

---

## 6. Constants.java — the shape of it

Everything Pedro needs lives in one class. The pattern is a **config lambda**:
you receive a config object and `.set()` its fields.

```java
package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.drivetrains.Mecanum;
import com.pedropathing.revhub.drivetrains.MecanumConfig;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {

    public static MecanumConfig drivetrainConfig = new MecanumConfig(c -> {
        c.frontLeftName.set("frontLeft");
        c.frontRightName.set("frontRight");
        c.backLeftName.set("backLeft");
        c.backRightName.set("backRight");
        c.frontLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.frontRightDirection.set(DcMotorSimple.Direction.FORWARD);
        c.backLeftDirection.set(DcMotorSimple.Direction.REVERSE);
        c.backRightDirection.set(DcMotorSimple.Direction.FORWARD);
    });

    public static PinpointConfig localizerConfig = new PinpointConfig(c -> {
        c.name.set("pinpoint");
        c.podType.set(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        c.xPodOffset.set(0.0);       // ← from PinpointTuner
        c.yPodOffset.set(0.0);       // ← from PinpointTuner
        c.xPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.yPodDirection.set(GoBildaPinpointDriver.EncoderDirection.FORWARD);
        c.globalDistanceUnit.set(DistanceUnit.INCH);
        c.offsetUnits.set(DistanceUnit.INCH);
    });

    // ForesightConfig is NOT shown filled in, because every required value
    // here must come from ForesightTuner on your robot. See section 7.

    public static Follower create(HardwareMap h) {
        return new Follower(
                new PinpointLocalizer(h, localizerConfig),
                new Mecanum(h, drivetrainConfig),
                new Foresight(foresightConfig));
    }
}
```

The real file is
`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/Constants.java`.
It differs from the sketch above in one useful way: it also exposes
`localizer()` and `drivetrain()` factories, which the tuners share, and
`create()` throws a clear message until Foresight is tuned.

---

## 7. Foresight config — why it's blank here

`ForesightConfig` has a dozen **required** fields with no defaults, including:

- `maxAchievableForwardVelocity`, `maxAchievableStrafeVelocity`
- `naturalForwardDeceleration`, `naturalStrafeDeceleration`
- `linearBrakeCoefficients`, `quadraticBrakeCoefficients`, `headingBrakeCoefficients`
- `forwardTranslational`, `strafeTranslational`, `headingFeedback`, `brake`, `coast`

**Inventing these would give you a robot that drives into walls.** They are
physical measurements of your robot. `ForesightTuner` measures them and prints
the config; paste that in verbatim.

For orientation, the shape the tuner emits looks like:

```java
public static ForesightConfig foresightConfig = new ForesightConfig(c -> {
    Controller primaryForward   = Controller.proportional(/* tuned */);
    Controller secondaryForward = Controller.proportional(/* tuned */);

    c.forwardTranslational.set(Controller.piecewise(secondaryForward).put(2.5, primaryForward));
    c.strafeTranslational.set(/* same shape, lateral values */);

    c.coast.set(Controller.proportionalFeedforward(/* tuned */));
    c.brake.set(Controller.proportionalFeedforward(/* tuned */));

    c.headingFeedback.set(Controller.proportional(/* tuned */));
    c.headingBrakeCoefficients.set(Vector2D.cartesian(/* tuned */, /* tuned */));

    c.linearBrakeCoefficients.set(Matrix.diag(/* tuned */, /* tuned */));
    c.quadraticBrakeCoefficients.set(Matrix.diag(/* tuned */, /* tuned */));

    c.maxAchievableForwardVelocity.set(/* tuned */);
    c.maxAchievableStrafeVelocity.set(/* tuned */);
    c.naturalForwardDeceleration.set(/* tuned */);
    c.naturalStrafeDeceleration.set(/* tuned */);
});
```

The `piecewise` controller is worth understanding: it uses a gentler gain when
far from the target and a stronger one within 2.5 inches, so the robot doesn't
creep the last two inches or oscillate on arrival.

---

## 8. Poses and paths

```java
import com.pedropathing.api.PoseFactory;
import com.pedropathing.math.Pose;

PoseFactory field = PoseFactory.degrees();      // headings in degrees

Pose start  = field.of(9, 60, 0);
Pose sample = field.of(36, 84, 45);
Pose basket = field.of(12, 128, 135);
```

`PoseFactory.degrees()` lets you write headings in degrees while Pedro works
internally in radians — fewer conversion mistakes. `PoseFactory.radians()`
exists if you prefer.

### Building paths

```java
import com.pedropathing.api.Paths;
import com.pedropathing.paths.Path;

Path straight = Paths.line(start, sample);        // A to B
Path curved   = Paths.curve(start, control, end); // Bezier through control points
Path visiting = Paths.through(start, mid, end);   // smooth path hitting each pose
Path whole    = Paths.path(straight, curved);     // chain them
```

`Paths.curve` treats the middle poses as **control points** — magnets the curve
bends toward but doesn't pass through. `Paths.through` passes through each pose.
Use `through` when you must hit a position, `curve` when you're shaping a
trajectory around an obstacle.

### Mirroring for the other alliance

```java
PoseFactory blue = PoseFactory.degrees();
PoseFactory red  = PoseFactory.degrees().mirrorX(72);   // 72 = half of 144
```

Write your autonomous once against one factory, swap the factory, and you have
the other alliance's auto. This alone saves hours every season — and removes a
whole category of copy-paste sign errors.

---

## 9. A TeleOp that knows where it is

```java
@TeleOp(name = "Pedro TeleOp", group = "Competition")
public class PedroTeleOp extends OpMode {
    private Follower follower;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(0, 0, 0));
    }

    @Override
    public void loop() {
        follower.manual(
                -gamepad1.left_stick_y,     // forward
                -gamepad1.left_stick_x,     // lateral
                -gamepad1.right_stick_x);   // heading

        follower.update();                  // ← never skip this

        telemetry.addData("x", "%.1f", follower.pose().x());
        telemetry.addData("y", "%.1f", follower.pose().y());
        telemetry.addData("heading", "%.1f", Math.toDegrees(follower.pose().heading()));
        telemetry.update();
    }
}
```

`follower.manual(...)` replaces all the kinematics from Guide 1 — Pedro's
`Mecanum` drivetrain does it. You still needed Guide 1: when the robot behaves
oddly you have to know what's happening underneath.

**`follower.update()` every loop.** It reads odometry and sets motor powers.
Skip it and nothing moves, with no error — the same failure mode as a subsystem
whose `update()` you forgot.

Check the signs against your robot on the first run; which way is positive
depends on your pod directions and mounting.

---

## 10. Autonomous as a state machine

Autonomous can't block — you can't `sleep()` until the robot arrives. So use a
state machine: each `loop()` checks "am I done?" and advances if so.

```java
@Autonomous(name = "BIOBUZZ Auto", group = "Competition")
public class PedroAuto extends OpMode {

    private Follower follower;
    private int state = 0;

    private final PoseFactory field = PoseFactory.degrees();
    private Pose start, scorePose, parkPose;

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);

        start     = field.of(9, 60, 0);
        scorePose = field.of(36, 84, 45);
        parkPose  = field.of(60, 96, 90);

        follower.setPose(start);        // tell Pedro where you physically are
    }

    @Override
    public void loop() {
        follower.update();

        switch (state) {
            case 0:
                follower.follow(Paths.line(start, scorePose));
                state = 1;
                break;

            case 1:
                if (!follower.isBusy()) {
                    // arrived — score here, then move on
                    state = 2;
                }
                break;

            case 2:
                follower.follow(Paths.line(scorePose, parkPose));
                state = 3;
                break;

            case 3:
                if (!follower.isBusy()) {
                    state = 4;
                }
                break;
        }

        telemetry.addData("State", state);
        telemetry.addData("Pose", follower.pose());
        telemetry.addData("Completion", "%.0f%%", follower.completion() * 100);
        telemetry.update();
    }
}
```

The pattern: **even states start an action, odd states wait for it.** Each case
returns immediately, so `loop()` never blocks.

`follower.setPose(start)` is critical — Pedro has no idea where you placed the
robot. Get this wrong and every path is offset by the same error. Use a
physical alignment jig so the robot starts in the same spot every match.

### Useful Follower methods

| Method | Use |
|---|---|
| `follow(Path)` | Start following |
| `isBusy()` | Still going? |
| `pose()` | Current pose |
| `completion()` | Fraction of path done, 0–1 |
| `hold(Pose)` | Actively hold a position |
| `manual(f, l, h)` | Direct driver control |
| `stop()` | Stop now |
| `setPose(Pose)` | Declare where you are |
| `distanceToEndpoint()` | Inches remaining |

---

## 11. When it goes wrong

| Symptom | Likely cause |
|---|---|
| Robot doesn't move at all | `follower.update()` not called every loop |
| Position drifts steadily | Pod offsets wrong, or pods not touching the floor |
| Position jumps | Loose pod, or a pod wheel slipping |
| Robot drives the wrong way | Pod direction reversed — rerun the localizer tuner |
| Oscillates around the target | Translational gain too high; rerun `ForesightTuner` |
| Stops short every time | Braking coefficients tuned on a different battery/weight |
| Path curves oddly | Control points aren't where you think — draw it on paper |
| Wrong by a constant offset | `setPose()` doesn't match where the robot physically starts |
| Won't compile, imports missing | Gradle not synced, or Pedro 2 imports from an old tutorial |

**Push the robot by hand first.** Before trusting any path, run a TeleOp that
only prints `follower.pose()`, push the robot a measured 24 inches, and check
the number says 24. If odometry is wrong, nothing built on it can work.

---

## 12. Becoming advanced

Once paths work reliably:

- **Path callbacks** — run a mechanism partway through a path instead of
  waiting for the end. Overlapping driving and scoring is where the seconds are.
- **Tune your Foresight config properly** and re-tune when the robot's weight
  changes. Most teams tune once and wonder why autonomous degrades.
- **AprilTag vision** — the SDK's `VisionPortal` and `AprilTagProcessor` read
  the field's tags. Worth knowing before you rely on it: BIOBUZZ's tags are
  mounted on **moving** field elements, so — unlike past seasons — they are
  *not* a valid reference for correcting odometry drift (per the SDK v12.0
  release notes). They're still useful for aiming and detecting game-element
  state, just not for "where am I on the field". See the `ConceptAprilTag*`
  samples in `FtcRobotController/.../samples/`.
- **Subsystem state machines** — give each mechanism its own states so the arm
  can move while the robot drives.
- **Write the BIOBUZZ auto you actually want**, then work backwards. Read the
  [Competition Manual](https://ftc-resources.firstinspires.org/ftc/game/manual)
  and count what each cycle is worth before optimising anything.

### Reference

- [Pedro Pathing docs](https://pedropathing.com/)
- [Pedro Pathing source](https://github.com/Pedro-Pathing/PedroPathing) — the
  most reliable reference; read the class you're using
- [Pedro Quickstart](https://github.com/Pedro-Pathing/Quickstart) — tuners and
  the canonical Gradle setup
- [Game Manual 0](https://gm0.org/) — best general FTC resource there is
- [BIOBUZZ Competition Manual](https://ftc-resources.firstinspires.org/ftc/game/manual)

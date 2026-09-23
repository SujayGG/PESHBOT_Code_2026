# Guide 3 — Pedro Pathing 3: setup day and autotune

The checklist for getting Pedro running on the real robot, start to finish.
[Guide 2](02-odometry-pedro-pathing.md) explains *why*; this is *what to do*.

**Already done in the repo** — you don't need to touch Gradle:

| | |
|---|---|
| Pedro Pathing | `com.pedropathing:revhub:3.0.1` (latest) |
| Autotuner | `com.pedropathing:tuning:1.0.1` |
| Maven repo | `https://repo.dairy.foundation/releases/` |
| `compileSdk` | 34, in `build.common.gradle` **and** `FtcRobotController/build.gradle` |
| FTC SDK | 12.0.0 |

These match the official [Pedro Quickstart](https://github.com/Pedro-Pathing/Quickstart)
exactly. Code lives in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/pedro/`.

> **Don't trust the `ftc` MCP server or AI answers about Pedro.** The MCP's
> knowledge stops at Pedro 2.1, and most AI training data is Pedro 1/2. Pedro 3
> is a rewrite: `pathBuilder()`, `PathChain`, `BezierLine`,
> `FollowerConstants` and `setLinearHeadingInterpolation` **don't exist** in it.
> The source of truth is the code in `pedro/` and the
> [Pedro 3 source](https://github.com/Pedro-Pathing/PedroPathing).

---

## Before you start

- [ ] Gradle synced in Android Studio (elephant icon) with no errors — the first
      sync downloads Pedro, needs internet
- [ ] Robot at **match weight**, on a **fully charged battery**
- [ ] Competition-style foam tiles, with at least 4–5 ft clear in every direction
- [ ] Odometry pods (or OTOS) installed and **touching the floor**
- [ ] Robot configuration on the Driver Station includes the drivetrain motors
      and the localizer (Pinpoint is named `pinpoint` by default)
- [ ] `util/DriveConstants.java` motor names match that configuration

Tuning on a half-dead battery or a bare chassis produces numbers that are wrong
the moment conditions change. Retune if the robot gains or loses significant
weight.

---

## Opening the tuner

The tuners are **not** on the Driver Station OpMode list. They're a web page
the robot serves:

1. Deploy the code (`./gradlew installDebug` or the Run button).
2. Connect your **laptop** to the Control Hub's Wi-Fi.
3. Open **<http://192.168.43.1:10158>** in a browser.

You'll see the procedures registered in `pedro/Tuning.java`. Each one walks you
through with prompts and diagrams, drives the robot itself where needed, and
**ends by printing a block of Java** to paste into `pedro/Constants.java`.

If the page doesn't load: check the laptop is on the robot's Wi-Fi (not school
Wi-Fi), the app is running, and the deploy succeeded.

---

## The loop you'll repeat

Every tuner step is the same cycle:

**run tuner → copy printed Java → paste into `Constants.java` → redeploy → next tuner**

The redeploy matters. Later tuners read earlier results out of `Constants.java`,
so skipping it means tuning against placeholder values.

---

## Step 1 — Mecanum Tuner

Spins each wheel one at a time; you tell it which way it went.

- [ ] Enter the four motor names (the defaults in `Constants.java` come from
      `DriveConstants`)
- [ ] Answer forward/reversed for each wheel, using the on-screen diagram
- [ ] Paste the printed `drivetrainConfig` over the one in `Constants.java`
- [ ] **Also update the `*_REVERSED` flags in `util/DriveConstants.java`** to
      match, so `MecanumTeleOp` agrees with Pedro
- [ ] Redeploy

If it says the wrong motor spun, fix the robot configuration or wiring before
continuing — every later step depends on this.

## Step 2 — Localizer tuner

Run **only the one for your hardware**. For a goBILDA Pinpoint:

- [ ] **Pinpoint Tuner**: pod type, then push the robot forward, push it left,
      then spin it 180° counter-clockwise, when asked
- [ ] Paste the printed `localizerConfig` over the one in `Constants.java`
- [ ] Redeploy

Using something else (OTOS, three-wheel, two-wheel, OctoQuad)? Run that tuner,
then in `Constants.java` change `localizerConfig`'s type to the matching
`…Config` class and `localizer()` to the matching `…Localizer`. The rest of the
codebase goes through `Constants.localizer()`, so nothing else changes.

### Check it before going further

Run **Tests → Localization** (or **Odometry**) and push the robot a measured
24 inches. If the reported position isn't about 24, stop and fix the localizer
now. Every path you ever run is built on this number.

## Step 3 — Foresight Tuner

The long one. It measures top speed, how far the robot coasts, and how hard it
brakes, driving itself forward/back, sideways and spinning.

- [ ] Clear plenty of space — it will overshoot during the velocity tests
- [ ] Work through every sub-step
- [ ] Paste the printed block over `public static ForesightConfig foresightConfig = null;`
- [ ] Alt+Enter on the red names to import `Controller`, `Matrix`, `Vector2D`
- [ ] Redeploy

Until this is pasted in, `Constants.create()` throws a message telling you
exactly this — on purpose, so an untuned robot fails at INIT instead of driving
into a wall.

**Never paste someone else's Foresight numbers**, including the example in
Pedro's own repo. They're measurements of a specific robot.

## Step 4 — Tests

- [ ] **Line** — drives a straight line and back. Should stop close to the mark
      without oscillating
- [ ] **Curve** — follows a curve
- [ ] **Hold** — push the robot; it should fight back to its position

Oscillating or overshooting → re-run the Foresight Tuner on a charged battery.
Consistently wrong distance → the localizer is the problem, go back to Step 2.

---

## Building an autonomous with ftc.peshcompsci.org

Pedro's code is split so the route is **one file**:

| File | Role | Edit it? |
|---|---|---|
| `pedro/AutoPaths.java` | Start pose + the list of paths | **Yes — paste your route here** |
| `opmodes/PedroAuto.java` | Runs the paths in order, one after another | Only for mechanism actions |
| `pedro/Constants.java` | Tuned robot values | Only when retuning |

1. Design the route on ftc.peshcompsci.org.
2. Replace the section between `REPLACE FROM HERE` and `TO HERE` in
   `AutoPaths.java` with the site's output. It must provide a `START` pose and a
   `build()` method returning the paths in order.
3. Deploy, set the robot **exactly** on its start mark, pick **Pedro Auto**
   under Autonomous.

The site's output has to be **Pedro 3** code. The shapes `AutoPaths` expects:

```java
public static final Pose START = FIELD.of(9, 60, 0);        // x, y inches; heading degrees
public static final Pose SCORE = FIELD.of(36, 84, 45);

public static List<Path> build() {
    return Arrays.asList(
        Paths.line(START, SCORE).linear(START, SCORE),                 // straight
        Paths.curve(SCORE, CONTROL, PICKUP).linear(SCORE, PICKUP),     // Bézier
        Paths.through(PICKUP, MID, PARK).linear(PICKUP, PARK)          // passes through MID
    );
}
```

Heading options on any path: `.linear(a, b)` (turn from a's heading to b's),
`.tangent()` (face along the path), `.constant(Math.toRadians(90))` (radians!),
`.facingPoint(pose)`.

If the site produces `follower.pathBuilder()`, `new BezierLine(...)`,
`PathChain` or `setLinearHeadingInterpolation`, that's **Pedro 2** output and
won't compile — the site's generator needs updating to the forms above.

### Doing something at each stop

`PedroAuto.onArrived(index)` runs once when path `index` (0-based) finishes.
Start mechanism actions there, but don't wait inside it — no `sleep()`. If an
action takes time (say, a 0.5 s release), give it its own state so `loop()`
keeps returning.

### Field coordinates

Inches, 144 × 144, headings in degrees through `FIELD`. Pick a corner as
(0, 0) and use the same convention as your website — if the site's origin or
heading direction differs from Pedro's, every pose is wrong in a consistent,
recognisable way (mirrored, rotated). Check the first path on the real field
before building a long route.

For the other alliance, `PoseFactory.degrees().mirrorX(72)` flips every pose
built from it across the field's midline.

---

## When it goes wrong

| Symptom | Likely cause |
|---|---|
| `IllegalStateException: Pedro isn't tuned yet` on INIT | Foresight output not pasted into `Constants.java` |
| Tuner page won't load | Laptop not on the robot's Wi-Fi, or app not running |
| Startup error mentioning `@Tuner` | A `Tuning.java` method isn't `static`, has parameters, or returns something other than `Procedure` |
| Robot doesn't move in auto | Missed a redeploy after pasting, or odometry reading 0 |
| Every path offset the same way | `START` doesn't match where the robot really sat |
| Route looks mirrored/rotated | Site's coordinate convention differs from Pedro's |
| Won't compile after pasting route | Pedro 2 syntax from the site or an old tutorial |
| Worked yesterday, overshoots today | Battery, or the robot's weight changed — retune Foresight |
| Restart loop: *Fatal class locating error occurred while running Sloth* … `Invalid name: org/firstinspires/…/SomeName` | An old **OnBot Java** program is on the hub. Sloth 0.3.2 (pulled in by Pedro's tuner) crashes on any compiled OnBot Java class. Back up and delete it — see below |

### OnBot Java and the Sloth crash

Pedro's tuning library brings in Sloth, and Sloth 0.3.2 has a bug: when it
reads compiled OnBot Java classes it passes names like
`org/firstinspires/ftc/teamcode/X` (slashes) instead of `…teamcode.X`, which
Java rejects, and the robot stops on every restart. Code built in Android
Studio is unaffected — only OnBot Java programs stored on the hub trigger it.

Fix, with the laptop on the robot's Wi-Fi:

```powershell
adb connect 192.168.43.1:5555
adb shell ls -R /sdcard/FIRST/java/src           # see what's there
adb pull /sdcard/FIRST/java/src .\onbotjava-backup   # keep a copy
adb shell rm -r /sdcard/FIRST/java/build         # compiled OnBot Java output
adb shell rm -r /sdcard/FIRST/java/src/org       # the OnBot Java sources
```

Then power-cycle the hub. Don't build anything in OnBot Java while Pedro's
tuner is installed — write code in Android Studio instead.

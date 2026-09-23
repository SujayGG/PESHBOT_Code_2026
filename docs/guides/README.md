# PESH Robotics — programmer learning path

**BIOBUZZ (2026–2027)**

Zero experience to writing competition autonomous, in order. Each stage
assumes the one before it and ends with something running on the robot.

Don't skip ahead. Every stage exists because the next one breaks confusingly
without it.

---

## The route

| Stage | You learn | Read | Do | Time |
|---|---|---|---|---|
| **0** | Install Android Studio, deploy to the robot | [README](../../README.md) §2–4 | Get a build onto the robot | 1 session |
| **1** | What an OpMode is, run a FIRST sample | [README](../../README.md) §5–9 | Copy `BasicOpMode_Linear`, drive it | 1 session |
| **2** | Motors, gamepads, tank drive | [Guide 1](01-teleop-mecanum.md) §1–4 | `Exercise1_TankDrive` | 1–2 sessions |
| **3** | Mecanum kinematics, strafing | [Guide 1](01-teleop-mecanum.md) §5–7 | `Exercise2_MecanumDrive` | 1–2 sessions |
| **4** | IMU, field-centric drive | [Guide 1](01-teleop-mecanum.md) §8–10 | `Exercise3_FieldCentric` | 1–2 sessions |
| **5** | Subsystems, non-blocking loops, P control | [Guide 1](01-teleop-mecanum.md) §11–12 | `Exercise4_Subsystem` + TeleOp | 2–3 sessions |
| **6** | Why timed autonomous fails; what odometry is | [Guide 2](02-odometry-pedro-pathing.md) §1–3 | Nothing — just read | 1 session |
| **7** | Pedro Pathing 3 setup and tuning | [Guide 3](03-pedro-setup-and-autotune.md) | Run the autotuners at `192.168.43.1:10158` | 1–2 sessions |
| **8** | Poses, paths, heading modes | [Guide 2](02-odometry-pedro-pathing.md) §8–9 | Tests → Line/Curve on the tuner page | 1 session |
| **9** | Autonomous from ftc.peshcompsci.org | [Guide 3](03-pedro-setup-and-autotune.md) + Guide 2 §10–11 | Route in `pedro/AutoPaths.java`, run **Pedro Auto** | 3+ sessions |
| **10** | Callbacks, AprilTag vision, real optimisation | [Guide 2](02-odometry-pedro-pathing.md) §12 | Build the auto you actually want | Rest of season |

Stages 0–5 make you useful to the team. 6–9 make you a programmer the team
depends on. Stage 10 is the rest of your FTC career.

---

## The two guides

### [Guide 1 — TeleOp and the mecanum drivetrain](01-teleop-mecanum.md)

From "what is an OpMode" to a field-centric mecanum TeleOp with a working arm
subsystem. Covers motors, gamepads, the mecanum maths and why it needs
normalising, the IMU, and how to structure code so it doesn't become one
400-line file.

### [Guide 2 — Odometry and Pedro Pathing 3](02-odometry-pedro-pathing.md)

Why `sleep()`-based autonomous fails at competition, what odometry is, and how
to set up, tune and use **Pedro Pathing 3** to drive to field coordinates.

> Pedro 3 is a rewrite — its API differs substantially from Pedro 1 and 2.
> Guides 2 and 3 were written from the v3.0.x source, so prefer it over any older
> tutorial you find, and be sceptical of AI answers about Pedro: most were
> trained on Pedro 2 and will confidently hand you code that doesn't compile.


### [Guide 3 — Pedro setup day and autotune](03-pedro-setup-and-autotune.md)

The hands-on checklist: open the tuner page, run each autotuner, paste its
output, verify with Tests, then build an autonomous on ftc.peshcompsci.org and
drop it into `pedro/AutoPaths.java`.
---

## Exercises

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/exercises/`

Real OpModes with the interesting parts left blank. They **compile and deploy
as-is** — the robot just won't do much until you fill in the TODOs. That's
intentional: get an empty OpMode onto the Driver Station first, then make it
work.

| File | Builds |
|---|---|
| `Exercise1_TankDrive.java` | Two-motor tank drive |
| `Exercise2_MecanumDrive.java` | Four-motor mecanum, robot-centric |
| `Exercise3_FieldCentric.java` | Mecanum with IMU heading |
| `Exercise4_Subsystem.java` | An arm subsystem (not an OpMode) |
| `Exercise4_SubsystemTeleOp.java` | The OpMode that drives it |

They appear on the Driver Station under the group **Exercises**.

## Competition drivetrain — already built

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/opmodes/MecanumTeleOp.java`
and `MecanumAutoBasic.java`, backed by
`subsystems/MecanumDrive.java` — a finished field-centric mecanum TeleOp and a
basic encoder-and-IMU autonomous (drive, strafe, turn, park), no odometry or
Pedro Pathing required. This is the drivetrain code a new team actually starts
the season on; Guide 1 and Guide 2 explain the technique behind it in depth,
and the exercises let you build it yourself once to understand it, but you
don't have to reinvent it to compete.

**Before it will drive correctly, fill in
`TeamCode/.../util/DriveConstants.java`** — hardware names, your motor's
encoder resolution, your wheel diameter, and which sides are reversed. Every
value that's specific to a physical robot lives in that one file; nothing
about a smaller or larger chassis requires touching `MecanumDrive.java` or
either OpMode, only different numbers in `DriveConstants`. Verify wheel
directions and the strafe correction on the real robot before trusting any
autonomous distance — see the guide sections on tuning by measurement, not by
guessing.

`MecanumAutoBasic` dead-reckons off encoder ticks, so small errors accumulate
across several moves — good for a short routine, not for a long multi-part
path. That accuracy ceiling is exactly why Guide 2 exists: once you outgrow
this, Pedro Pathing 3 is the upgrade.

Each TODO explains *why*, not just what to type. Read that part — the guides
and exercises are trying to make you someone who can debug a robot at 11pm
before a competition, not someone who has a working file.

## Pedro Pathing — already installed

Pedro Pathing 3.0.1 and its autotuner are in the build, with the tuners
registered and a route-runner autonomous ready. Files, in
`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`:

| File | Role |
|---|---|
| `pedro/Constants.java` | Tuned robot values — filled in from the autotuners |
| `pedro/Tuning.java` | Registers the tuners served at `192.168.43.1:10158` |
| `pedro/procedures/` | The official tuner procedures, unmodified from Pedro's Quickstart |
| `pedro/AutoPaths.java` | The autonomous route — paste from ftc.peshcompsci.org |
| `opmodes/PedroAuto.java` | Runs `AutoPaths` path by path |

Start at [Guide 3](03-pedro-setup-and-autotune.md).

---

## Rules of the road

**Test on the robot before you push.** Code that compiles but was never run
isn't finished, and a broken `main` blocks everyone at the next meeting.

**Telemetry is your debugger.** No breakpoints on a moving robot. Print what
you think is true and check.

**`adb logcat -s RobotCore:V System.err:V`** gives you the real stack trace
when an OpMode dies. Use it instead of guessing.

**Hardware names must match the robot config exactly.** The most common
beginner crash by a wide margin.

**Ask after twenty minutes stuck.** Everyone here has lost an afternoon to
`@Disabled` or a mismatched motor name. Asking is the right call, not a
failure.

---

## Reference

- [Game Manual 0](https://gm0.org/) — community-written, better than the
  official docs for most things
- [Official FTC programming docs](https://ftc-docs.firstinspires.org/)
- [BIOBUZZ Competition Manual](https://ftc-resources.firstinspires.org/ftc/game/manual)
- [Pedro Pathing](https://pedropathing.com/) · [source](https://github.com/Pedro-Pathing/PedroPathing) · [quickstart](https://github.com/Pedro-Pathing/Quickstart)
- 63 sample OpModes ship with the SDK in
  `FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/`

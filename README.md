# PESH Robotics — BioBuzz 2026

This is our team's robot code for the FTC **BIOBUZZ (2026–2027)** season.

It's the official [FTC SDK](https://github.com/FIRST-Tech-Challenge/FtcRobotController)
(version **12.0**) with a `TeamCode` folder where we write our own code. If
you've never done this before, start at the top and work down — this guide
assumes you know nothing about FTC programming yet.

---

## 1. What you're actually building

The robot has a **Control Hub** (or a phone) bolted to it. That hub runs an
Android app called the **Robot Controller**. Everything in this repo compiles
into that app.

You write **OpModes** — small Java classes that say what the robot does. Two kinds:

- **TeleOp** — the driver-controlled part of the match. Reads the gamepad, moves motors.
- **Autonomous** — the first 30 seconds, where the robot drives itself with no driver input.

On the **Driver Station** (the tablet the drivers hold), your OpModes show up in
a dropdown list. Pick one, press start, the robot does the thing.

So the loop is: **write Java → build → send it to the robot → pick it on the tablet → test.**

---

## 2. Install these first

| What | Why | Notes |
|---|---|---|
| [Android Studio](https://developer.android.com/studio) | Where you write and build the code | Must be **Narwhal 3 Feature Drop or newer** — the FTC SDK v12.0 requirement. Older versions will not work. |
| [Git](https://git-scm.com/downloads) | Downloads the code and shares your changes | |
| [GitHub Desktop](https://desktop.github.com/) | Optional, but much easier than git commands | Recommended if you're new |

Android Studio is a big download (several GB) and the install takes a while.
Start it before a meeting, not during one.

You do **not** need to install Java separately — Android Studio brings its own.

---

## 3. Get the code onto your laptop

Open a terminal (Command Prompt on Windows) and run:

```bash
git clone https://github.com/SujayGG/Panther_Robotics_Biobuzz_Code_2026.git
```

**Put it somewhere with a short path and no spaces.** `C:\FTC\` on Windows is
ideal. A path like `C:\Users\My Name\OneDrive\School Stuff\Robotics\` will cause
strange build errors that are painful to diagnose. This matters more than it
sounds like it should.

---

## 4. Open it in Android Studio

1. Launch Android Studio.
2. **File → Open** (not "Import Project" — that breaks things).
3. Select the `Panther_Robotics_Biobuzz_Code_2026` folder — the one that has
   `settings.gradle` inside it. Click OK.
4. If it asks whether you trust the project, say yes.
5. **Wait.** Android Studio now downloads Gradle and the FTC libraries. The
   first time takes 5–15 minutes depending on your internet. There's a progress
   bar at the bottom. Don't start editing until it finishes.
6. If it offers to install a missing SDK component, accept.

You'll know it worked when the left sidebar shows `FtcRobotController` and
`TeamCode` as folders, and there are no red error banners at the top.

**If the sync fails:** File → Invalidate Caches → Invalidate and Restart. That
fixes most first-time problems. If it still fails, check you're not on a school
Wi-Fi network that blocks downloads.

---

## 5. Where your code goes

Only ever write code inside **`TeamCode`**:

```
TeamCode/src/main/java/org/firstinspires/ftc/teamcode/
├── opmodes/       ← your TeleOp and Autonomous classes
├── subsystems/    ← one class per mechanism (drivetrain, intake, arm, …)
└── util/          ← constants, math helpers, shared settings
```

**Do not edit anything inside `FtcRobotController/`.** That's the official FIRST
code. If you change it, updating the SDK next season becomes a nightmare, and
your changes will get wiped out anyway. It's read-only as far as we're concerned.

### What's a subsystem?

A **subsystem** is one physical mechanism wrapped in a class — the drivetrain,
the arm, the intake. It knows how to control that one thing and nothing else.

Your OpMode then just says `arm.raise()` instead of containing forty lines of
motor math. It keeps OpModes short and means two people can work on different
mechanisms without colliding.

Don't worry about this on day one. Write a working TeleOp first, then pull the
mechanism code out into a subsystem once it works.

---

## 6. Your first OpMode

**Don't write one from scratch.** FIRST ships 63 working examples with the SDK,
and copying one is the normal way to start.

They're in:

```
FtcRobotController/src/main/java/org/firstinspires/ftc/robotcontroller/external/samples/
```

Good ones to start from:

| Sample | What it does |
|---|---|
| `BasicOpMode_Linear.java` | Simplest possible TeleOp — two motors, tank drive |
| `BasicOmniOpMode_Linear.java` | Mecanum/omni drive with strafing — closest to our robot |
| `RobotAutoDriveByTime_Linear.java` | Dead-simple autonomous: drive forward for N seconds |
| `SensorColor.java` | Reading a sensor and printing the values |

### The copy steps

1. Find the sample in the sidebar, right-click it → **Copy**.
2. Right-click our `opmodes` folder → **Paste**.
3. Android Studio asks for a new name. Give it a real one, like `PeshTeleOp`.
4. Open your new file. Two things to fix:

   **a. The package line** at the very top must match where the file now lives:

   ```java
   package org.firstinspires.ftc.teamcode.opmodes;
   ```

   **b. Delete the `@Disabled` line.** Every sample has one. It's what keeps 63
   examples from flooding the Driver Station dropdown. If you leave it in, your
   OpMode will not appear on the tablet, and this confuses everybody the first
   time it happens.

5. Change the name that shows on the tablet:

   ```java
   @TeleOp(name = "PESH TeleOp", group = "Competition")
   ```

### Motor names have to match the robot

This line asks the robot for a motor by name:

```java
leftDrive = hardwareMap.get(DcMotor.class, "left_drive");
```

That string — `"left_drive"` — must **exactly** match the name in the robot's
configuration file on the Driver Station. Not "leftDrive", not "Left_Drive".
Exact match, capitals and underscores included.

If they don't match, the OpMode crashes the instant you press init, and the
tablet shows an error naming the device it couldn't find. This is the single
most common beginner error. Whoever configured the robot knows the real names —
go ask them, and write them down somewhere the whole team can see.

---

## 7. Build it

Building = turning your Java into an app. Do this often; it catches typos.

In Android Studio: **Build → Make Project**, or press **Ctrl+F9** (**Cmd+F9** on Mac).

Or from a terminal in the project folder:

```bash
./gradlew assembleDebug        # Mac / Linux
gradlew.bat assembleDebug      # Windows
```

Errors appear in the **Build** panel at the bottom. Click a red line and it
jumps to the problem. Read the first error, not the last — later ones are often
just fallout from the first.

**A successful build does not mean your robot works.** It means the code is
valid Java. Whether the arm actually goes up is a separate question, answered
only by testing on the robot.

---

## 8. Put it on the robot

### Control Hub (over Wi-Fi)

1. Turn on the robot.
2. On your laptop, connect to the Control Hub's Wi-Fi network. The name is
   printed on the hub; the password is in the Robot Controller app.
3. In a terminal:

   ```bash
   adb connect 192.168.43.1:5555
   adb devices                    # should list the hub
   ./gradlew installDebug
   ```

### Phone as Robot Controller (over USB)

1. On the phone: enable **Developer options**, then **USB debugging**.
2. Plug it into your laptop. Accept the prompt that appears on the phone.
3. `adb devices` to confirm it's listed, then `./gradlew installDebug`.

Or just press the green **Run** button in Android Studio with `TeamCode`
selected — same result.

### If `adb` isn't found

It ships with Android Studio but isn't on your PATH by default. Either add this
folder to your PATH, or `cd` into it and run `adb` from there:

- **Windows:** `%LOCALAPPDATA%\Android\Sdk\platform-tools`
- **macOS:** `~/Library/Android/sdk/platform-tools`
- **Linux:** `~/Android/Sdk/platform-tools`

---

## 9. Run it

On the Driver Station tablet, pick your OpMode from the TeleOp or Autonomous
dropdown, press **INIT**, then **▶**.

**Your OpMode isn't in the list?** In order of likelihood:

1. You left `@Disabled` in the file.
2. The class has no `@TeleOp` or `@Autonomous` annotation.
3. The install didn't actually succeed — scroll back and check for errors.
4. The Robot Controller app didn't restart. Restart it manually.

---

## 10. When it breaks

Robots fail in ways that compile perfectly. Here's what to check.

| What you see | What it usually means |
|---|---|
| Crash at INIT, error names a device | A `hardwareMap.get(...)` name doesn't match the robot config |
| Robot spins instead of driving straight | One motor's direction needs reversing (`setDirection(REVERSE)`) |
| One wheel goes the wrong way | Same — that motor's direction is wrong |
| Robot drifts when sticks are neutral | Gamepad stick drift; ignore inputs below ~0.05 (a "deadzone") |
| OpMode stops mid-match, no error | Something threw an exception — check logcat below |
| Pushing stick forward goes backward | Gamepad Y axes are inverted. Negate it: `-gamepad1.left_stick_y` |
| Changes didn't take effect | You built but didn't install, or installed to the wrong device |

### Reading the actual error

```bash
adb logcat -s RobotCore:V System.err:V
```

This prints the robot's log live, including the full stack trace when your
OpMode crashes. When something breaks mysteriously, this is the fastest way to
find out why — much better than guessing. Run it, reproduce the problem, read
what appears.

---

## 11. Two rules that will save you

**Never block in an iterative OpMode.** If your class extends `OpMode` (with
`init()` and `loop()` methods), never use `Thread.sleep()` or a `while` loop
that waits for something. The `loop()` method must return quickly, every time.
If it doesn't, the robot stops responding and the match is over.

If you need to wait, use `LinearOpMode` instead (with a single `runOpMode()`
method) — there, `sleep()` and `opModeIsActive()` are fine and expected.

**Negate the gamepad Y axis.** Pushing a stick forward gives you a *negative*
number. Almost every OpMode wants `-gamepad1.left_stick_y`. Forgetting this is a
rite of passage; now you can skip it.

---

## 12. Sharing your work with the team

Never commit straight to `main`. Work on a branch:

```bash
git checkout main
git pull                              # get everyone else's latest work
git checkout -b arm-subsystem         # your own branch, named for what you're doing

# ... write code, build it, test it on the robot ...

git add .
git commit -m "Add arm subsystem with preset heights"
git push -u origin arm-subsystem
```

Then open a Pull Request on GitHub so someone can look it over before it merges.

GitHub Desktop does all of this with buttons if the commands feel like a lot.

**Test on the robot before you push.** Code that compiles but was never run is
not finished, and a broken `main` blocks everyone at the next meeting.

---

## 13. Words people will use at you

| Term | What it means |
|---|---|
| **OpMode** | One program the robot can run. Shows up on the tablet. |
| **TeleOp** | Driver-controlled period |
| **Autonomous** | Robot drives itself, first 30 seconds |
| **Control Hub** | The computer on the robot |
| **Driver Station** | The tablet the drivers hold |
| **hardwareMap** | How code asks for a specific motor or sensor by name |
| **Gradle** | The build tool. When it "syncs", it's fetching libraries. |
| **adb** | Command-line tool for talking to the robot |
| **Telemetry** | Printing text to the tablet screen — how you debug |
| **Encoder** | Sensor that counts motor rotation, so you know how far it turned |
| **Subsystem** | A class wrapping one mechanism |
| **SDK** | The official FIRST code this project is built on |

---

## 14. Keep going — the learning path

Sections 1–13 get you driving. Everything after that — mecanum kinematics,
field-centric drive, subsystems, odometry, and autonomous with Pedro Pathing 3
— is laid out as a stage-by-stage route with exercises you fill in:

**→ [docs/guides/README.md](docs/guides/README.md)**

| Guide | Covers |
|---|---|
| [Guide 1](docs/guides/01-teleop-mecanum.md) | TeleOp, motors, gamepads, mecanum maths, IMU, subsystems |
| [Guide 2](docs/guides/02-odometry-pedro-pathing.md) | Odometry, Pedro Pathing 3, tuning, autonomous |
| [Guide 3](docs/guides/03-pedro-setup-and-autotune.md) | Pedro setup day: autotuners, then autonomous from ftc.peshcompsci.org |

The exercises live in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/exercises/`
and appear on the Driver Station under the group **Exercises**. They compile and
deploy as-is; you fill in the blanks.

---

## 15. Where to learn more

- [Official FTC Programming Docs](https://ftc-docs.firstinspires.org/) — start here
- [Android Studio Tutorial (FIRST)](https://ftc-docs.firstinspires.org/programming_resources/android_studio_java/Android-Studio-Tutorial.html)
- [Game Manual 0](https://gm0.org/) — community-written, genuinely excellent, better than the official docs for most things
- [`docs/FTC_SDK_README.md`](docs/FTC_SDK_README.md) — the official SDK readme and this season's release notes
- [`docs/ANDROID_STUDIO_SETUP.md`](docs/ANDROID_STUDIO_SETUP.md) — longer setup walkthrough and troubleshooting

**And ask.** Every person on this team got stuck on `@Disabled` or a mismatched
motor name at some point. Asking after twenty minutes of being stuck is the
right call, not a failure.

---

## For Sujay — AI tooling

This repo is wired for Claude Code with the `ftc-mcp` server (`.mcp.json`,
`.claude/settings.json`, `CLAUDE.md`). Nobody else needs to install or think
about any of it — it doesn't affect the normal Android Studio workflow above.

Setup and usage: [`docs/MCP_SETUP.md`](docs/MCP_SETUP.md).

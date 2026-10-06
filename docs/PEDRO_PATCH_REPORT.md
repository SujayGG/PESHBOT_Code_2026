# Pedro Pathing 3 — patch report

Every difference between this repo and the official sources, why it exists, and
how it was checked. Written 2026-10-06.

## 1. Official versions

| Component | This repo | Latest official | Source checked |
|---|---|---|---|
| FTC SDK | `12.0.0` | `v12.0` | FIRST-Tech-Challenge/FtcRobotController tags |
| Pedro Pathing (`revhub`, pulls `core`) | `3.0.1` | `v3.0.1` | Pedro-Pathing/PedroPathing tags |
| Pedro autotuner (`tuning`) | `1.0.1` | `1.0.1` | Pedro-Pathing/Quickstart `build.dependencies.gradle` |
| Maven repo for Pedro | `repo.dairy.foundation/releases` | same | Quickstart |
| Sloth (pulled in by `tuning`) | `0.3.2` | `0.3.2` | Dairy-Foundation/Sloth tags |
| Tuner procedures | Quickstart `2df9646` (2026-09-30) | same | Pedro-Pathing/Quickstart |

Our build files (`build.gradle`, `build.common.gradle`, `build.dependencies.gradle`,
`settings.gradle`, `gradle.properties`, the Gradle wrapper, `FtcRobotController/`
and both manifests) are **byte-identical to Quickstart `2df9646`**, apart from
comments and a trailing newline in `build.dependencies.gradle`.

## 2. Patches, in order of importance

### 2.1 Official patches pulled in (Quickstart, 2026-09-30)

| Quickstart commit | File | What it fixes |
|---|---|---|
| `f13d1cf` | `ThreeWheelIMUTuner` | The tuner printed `c.imuOrientation.set(...)`, a field that doesn't exist in Pedro 3.0.1's `ThreeWheelIMUConfig`, so its output **would not compile** when pasted. Now prints `c.imu.set(new RevHubIMU(...))`. |
| `2df9646` | `ForesightTuner` | Several tests stopped sending drive power partway through (forward/strafe deceleration, heading, forward/strafe translational), so measurements could be wrong. They now keep driving until they intend to stop. |

### 2.2 Local patch: hard-coded motor names (the autotune crash)

**Symptom:** `IllegalArgumentException: Unable to find a hardware device with
name "lf" and type DcMotorEx` at `ThreeWheelIMUTuner.localizer(...)`, as soon as
a measuring step started.

**Cause:** before each push-the-robot step, `ThreeWheelTuner` and
`ThreeWheelIMUTuner` look up the four drive motors from a hard-coded list of the
Pedro author's own names (`lf`, `lr`, `rf`, `rr`) and ignore what you type on the
tuner page. Any robot that names its motors differently crashes.

**Fix** (`ThreeWheelTuner`, `ThreeWheelIMUTuner`): read the four names and
directions from `Constants.drivetrainConfig` (which reads `util/DriveConstants`).
`TwoWheelTuner` and the two three-wheel tuners also pre-fill their encoder-name
boxes from `DriveConstants`. Search for `PESH change` to find it. A scan of the
tuner procedures, the tuning library and Pedro's `revhub` found **no other
hard-coded hardware names**.

**If you re-copy `procedures/` from the Quickstart**, re-apply this change or the
crash returns. Upstream hasn't fixed it as of `2df9646`.

### 2.3 Local: `Constants.java` and `Tuning.java`

The Quickstart ships both as empty stubs. Ours:

- `Tuning.java` registers all nine tuners, following the Pedro author's own
  filled-in version (Quickstart commit `20768b3`). The tuning library enforces
  that each `@Tuner` method is `static`, takes no arguments, and returns exactly
  `Procedure`.
- `Constants.java` builds the drivetrain from `DriveConstants`, supports **either
  Pinpoint or Three-Wheel + IMU** through `ODOMETRY` (default `THREE_WHEEL_IMU`,
  since this team has no Pinpoint), and **refuses to run until tuned**:
  `foresightConfig` and `threeWheelIMUConfig` are `null`, and `create()` /
  `localizer()` throw a message saying what to do. Better a clear stop at INIT
  than a robot driving on invented numbers.

### 2.4 Repo-level

| Item | Detail |
|---|---|
| `compileSdk 34` | In both `build.common.gradle` **and** `FtcRobotController/build.gradle`, as the Quickstart does. Re-apply after any SDK upgrade. |
| AprilTag samples | The v12.0 upgrade left six v11.2.1 sample files that no longer match the v12 AprilTag API; replaced with the official v12.0 copies. |
| `subsystems/autonomous.java` (teammate's push) | Written for **Pedro 1.x** and does not compile (see `docs/drafts/README.md`). One uncompilable file stops the whole build, and with it the autotuner, so it was moved to `docs/drafts/` with its history intact. Nothing was deleted. |

## 3. Known upstream issue we can only work around

**Sloth 0.3.2 crashes the app at startup if an OnBot Java program exists on the
Control Hub** (`Fatal class locating error ... Invalid name:
org/firstinspires/ftc/teamcode/BasicTeleOP`). Sloth is pulled in by Pedro's
tuner, so it can't be removed. Workaround, with the laptop on the robot's Wi-Fi:

```powershell
adb pull /sdcard/FIRST/java/src .\onbotjava-backup
adb shell rm -r /sdcard/FIRST/java/build
adb shell rm -r /sdcard/FIRST/java/src/org
```

Then power-cycle the hub. Don't use OnBot Java while the tuner is installed.

## 4. What was verified, and what wasn't

**Verified (2026-10-06)**

- A full compile of Pedro `core` + `revhub` 3.0.1, the tuning library, the
  official FTC SDK 12.0.0 libraries, a real Android framework jar, and **all of
  `TeamCode/`**: **0 errors**. Before the fixes, one file broke the build.
- A plain-Java run of the startup rules the tuning library enforces on
  `Tuning.java`: all nine tuners register and construct. Untuned `Constants`
  stops with clear instructions. The drivetrain config resolves its names from
  `DriveConstants`. 40 of 40 checks pass.
- The patched tuner differs from the official source only in the lines in 2.2.

**Not verified**

- Nothing was run on a real Control Hub or robot. Whether a tuner *finishes*
  depends on your hardware, wiring and floor.
- Small stand-ins replaced Dairy's Sinister scanner classes and gson-extras'
  `RuntimeTypeAdapterFactory` (both on hosts blocked from where this was built),
  so a real Gradle sync is the final check.

## 5. Before you tune: hardware reality check

- Each encoder box on the tuner page wants **the name of the motor port that
  pod's encoder is plugged into**, not a name you invent. Your drive-motor names
  must be right in `util/DriveConstants.java` or the lookup still fails, now
  naming *your* motor.
- Three-wheel odometry needs **two forward pods and one sideways pod**. Mecanum
  drive wheels are not pods: they slip, and none points sideways.
- Pedro 3 has no drive-wheel-encoder localizer. Without pods, a Pinpoint or an
  OTOS, use `MecanumAutoBasic`.

## 6. Re-syncing with the Quickstart later

1. Clone `Pedro-Pathing/Quickstart`; diff `TeamCode/.../pedro/procedures` and the
   build files against ours.
2. Copy `procedures/*.java` over ours, then redo section 2.2.
3. Update versions in `build.dependencies.gradle` to whatever the Quickstart pins.
4. Keep `compileSdk 34` in both places.
5. Sync Gradle, build, and deploy.

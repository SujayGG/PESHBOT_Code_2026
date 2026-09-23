# PESH Robotics — BioBuzz 2026

FTC robot code for the BIOBUZZ (2026–2027) season. This repo is the FTC SDK
project (`FtcRobotController` v12.0) plus our team module, `TeamCode`.

## Where our code goes

All team code lives in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/`:

| Package      | Contents                                                        |
|--------------|-----------------------------------------------------------------|
| `opmodes/`   | `@TeleOp` / `@Autonomous` OpModes — driver-facing entry points   |
| `subsystems/`| Hardware wrappers (drivetrain, intake, lift, …) — no OpMode logic|
| `util/`      | Constants, tuning configs, math/helper classes                   |

**Never edit `FtcRobotController/`, `build.common.gradle`, or `libs/`** — those
are the stock SDK, and editing them makes future SDK upgrades painful. The one
exception is `compileSdk 34` in both `build.common.gradle` and
`FtcRobotController/build.gradle`, which Pedro Pathing requires; re-apply it
after any SDK upgrade. Module
customisations belong in `TeamCode/build.gradle`; new library dependencies go in
`build.dependencies.gradle`.

## Use the `ftc` MCP server

This project ships an MCP server (`ftc-mcp`) that carries verified, current FTC
API docs — FTC Dashboard, Panels, Road Runner, FTCLib, and the full hardware
API. **Its Pedro Pathing knowledge is 2.1 and wrong for this repo**, which uses
Pedro Pathing 3 (see the Pedro section below). Model training data for these libraries is outdated, so
**prefer the MCP over recalled API knowledge**:

- Call `scan_project` with this repo's path at the start of a session — it
  reports the SDK version, which libraries are actually installed, and the
  existing OpModes, hardware device names, and `@Config` classes.
- Call `search_knowledge` before writing against any library or hardware API
  (motors, servos, IMU, Pinpoint, OTOS, VisionPortal, Limelight, Pedro paths,
  dashboard config).
- Call `validate_ftc_code` on new or edited OpModes before saying they're done.
- The server also exposes `ftc://…` resources and workflow prompts
  (`create-autonomous`, `create-subsystem`, `tune-pid`, `setup-gradle`, …).

If a `search_knowledge` result contradicts what you remember about an API,
the MCP is right.

## Build and deploy

```bash
./gradlew assembleDebug                 # compile — the fast correctness check
./gradlew :TeamCode:assembleDebug       # compile TeamCode only
./gradlew installDebug                  # build + push to a connected Robot Controller
adb devices                             # confirm the RC phone / Control Hub is attached
```

Deploying to a Control Hub over Wi-Fi: connect to the hub's network, then
`adb connect 192.168.43.1:5555`.

There is no unit-test suite — a clean `assembleDebug` plus a check on the robot
is our verification. Always compile before reporting an OpMode as finished.

## FTC conventions that bite

- Hardware names in `hardwareMap.get(...)` must match the Robot Controller's
  configuration file exactly. If you introduce a new device, say what the
  config entry needs to be named.
- Iterative `OpMode` (`init`/`loop`) must never block — no `Thread.sleep`, no
  `while` loops waiting on hardware. Use a state machine. `LinearOpMode` may
  block, via `sleep()` and `opModeIsActive()`.
- Gamepad Y axes are inverted (pushing forward gives negative). Negate them.
- `@Config` fields for live dashboard tuning must be `public static` and must
  **not** be `final`; read them fresh each loop rather than copying into a
  local at init, or tuning changes won't take effect.
- Prefer bulk reads (`LynxModule` `BULK_CACHE_MODE.MANUAL`, cleared once per
  loop) when loop times matter.
- Every OpMode needs `@TeleOp` or `@Autonomous` to appear on the Driver Station.

## Adding a library

Add dependencies to `build.dependencies.gradle`, never to `build.common.gradle`.
Ask the `ftc` MCP (`setup-gradle` prompt, or `ftc://gradle/all-library-coords`)
for the exact Maven coordinates and repositories — except for Pedro, below.

## Pedro Pathing 3

Installed: `com.pedropathing:revhub:3.0.1` and `com.pedropathing:tuning:1.0.1`
from `repo.dairy.foundation`, matching the official Pedro Quickstart. Code is in
`TeamCode/.../pedro/`.

- Pedro 3 is a rewrite. `pathBuilder()`, `PathChain`, `BezierLine`,
  `FollowerConstants`, `setLinearHeadingInterpolation` do **not** exist. Read
  the Pedro 3 source (github.com/Pedro-Pathing/PedroPathing) or the existing
  files in `pedro/` rather than recalling APIs — and don't use the `ftc` MCP
  for Pedro.
- `new Follower(Localizer, Drivetrain, Algorithm)` — in that order. Build it
  only through `Constants.create()`.
- Paths: `Paths.line/curve/through(...)` then a heading mode,
  e.g. `.linear(a, b)`. Poses via `PoseFactory.degrees().of(x, y, deg)`.
- Tuners are registered in `pedro/Tuning.java` with `@Tuner` on `static`,
  zero-arg methods returning exactly `Procedure`; served at
  http://192.168.43.1:10158, not on the Driver Station.
- Never invent or copy values into `Constants.foresightConfig` — they come only
  from the Foresight Tuner on this robot.
- The autonomous route lives in `pedro/AutoPaths.java` (pasted from
  ftc.peshcompsci.org); `opmodes/PedroAuto.java` runs it and shouldn't need
  editing for a new route.
- `pedro/procedures/` is copied unmodified from the Quickstart — update by
  re-copying, not by editing.

## Git

Work on a branch, commit with a clear message, and open a PR against `main`.
Don't commit `local.properties`, build outputs, or `.idea/`.

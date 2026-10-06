# Drafts that are not part of the build

Files here are saved work-in-progress that **does not compile** against this
repo, so they're kept out of `TeamCode/` where one broken file stops the whole
Android Studio build (and with it, the Pedro autotuner).

## `autonomous-pedro1-draft.java.txt`

Added by Ahmad in commit `35e4957` as `subsystems/autonomous.java`. It's a
start on a Pedro Pathing autonomous, but it's written for **Pedro 1.x**, which
doesn't exist in this repo (we're on Pedro Pathing 3):

| In the draft | Problem | Pedro 3 equivalent |
|---|---|---|
| `import pedroPathing.follower.Follower` (and `pathGeneration.*`, `util.Timer`) | Those packages are Pedro 1; Pedro 3 uses `com.pedropathing.*` | `com.pedropathing.follower.Follower` |
| `follower.pathBuilder().addPath(new BezierLine(...))` | `pathBuilder()` and `BezierLine` were removed | `Paths.line(a, b)` / `Paths.curve(...)` |
| `.setLinearHeadingInterpolation(a, b)` | Removed | `.linear(a, b)` on the path |
| `pathChain` | Removed | `Path` |
| `import com.qualcomm.robotcore.eventloop.Autonomous` | Wrong package | `...eventloop.opmode.Autonomous` |
| `public class Autonomous` | Clashes with the `@Autonomous` annotation | Any other name |
| `private final staartingPos = ...` | No type | `private final Pose ...` |
| File `autonomous.java` in `subsystems/` | File name must match the class; it's an OpMode, not a subsystem | `opmodes/` |

## What to use instead

The route-runner is already built:

- Put the poses and paths in `TeamCode/.../pedro/AutoPaths.java`
- `opmodes/PedroAuto.java` follows them one after another

Both are in Pedro 3 syntax, and the Pedro 3 forms are in
`docs/guides/03-pedro-setup-and-autotune.md`. Pedro needs tuning first
(`pedro/Constants.java`), so an autonomous only drives once that's done.

To keep working on the draft, copy it into `AutoPaths.java` as Pedro 3 paths,
or ask for it to be ported. Nothing was deleted — it's all in this file.

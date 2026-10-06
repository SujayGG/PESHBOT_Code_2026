package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.Autonomous;

import pedroPathing.follower.Follower;
import pedroPathing.pathGeneration.BezierCurve;
import pedroPathing.pathGeneration.pathChain;
import pedroPathing.pathGeneration.Point;
import pedroPathing.util.Timer;

//Import constants file here

@Autonomous(name = "Pedro Pathing Autonomous", group = "Autonomous")
//Extend OpMode
public class Autonomous extends OpMode {
	
	private Follower follower;
	private Timer pathTimer, actionTimer;
	private int pathState;
	
	//Setup positions
	private final staartingPos = new Pose(9, 111, Math.toRadians(0));
	private final parkingPos = new Pose(9, 111, Math.toRadians(0));
	private final shoot1Pos = new Pose(9, 111, Math.toRadians(0)); //Going under
	private final shoot2Pos = new Pose(9, 111, Math.toRadians(0)); //Going Left
	private final shoot3Pos = new Pose(9, 111, Math.toRadians(0)); //Going Right
	
	//Paths
	private pathChain driveToShoot, driveToPark;
	
	
	//Path Calculations
	private void calcPaths(Pose start, Pose shoot, Path park){
		driveToShoot = follower.pathBuilder().addPath(new BezierLine(start, shoot)).setLinearHeadingInterpolation(start.getHeading, score.getHeading).build();
		driveToPark = follower.pathBuilder().addPath(new BezierLine(shoot, park)).setLinearHeadingInterpolation(shoot.getHeading, park.getHeading).build();
	}
}

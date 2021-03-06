package org.firstinspires.ftc.teamcode;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import com.qualcomm.robotcore.hardware.Servo;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

@Autonomous(name="Auto Marker Only", group="States")

public class Auto_Marker_Only extends LinearOpMode {

    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;
    private DcMotor hookMotor = null;
    private DcMotor elevator = null;
    private Servo left_scoop = null;
    private ElapsedTime runtime = new ElapsedTime();

    BNO055IMU imu;
    Orientation angles;

    // Drive robot in straight line for given time with given power
    private void driveStraight(double power, double seconds) {
        rightDrive.setPower(power);
        leftDrive.setPower(power);
        runtime.reset();
        
        while (opModeIsActive() && (runtime.seconds() < seconds)) {}
    }

    // Turn robot with given power and angle
    private void turnRobot(double power, double angle) {
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        leftDrive.setPower(power);
        rightDrive.setPower(-power);

        while (opModeIsActive() && Math.abs(angles.firstAngle) < angle) {
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            telemetry.addData("angle ", Math.abs(angles.firstAngle));
            telemetry.update();
        }
    }

    @Override
    public void runOpMode() {
        leftDrive   = hardwareMap.get(DcMotor.class, "left_wheel");
        rightDrive  = hardwareMap.get(DcMotor.class, "right_wheel");
        hookMotor   = hardwareMap.get(DcMotor.class, "hook_motor");
        elevator    = hardwareMap.get(DcMotor.class, "elevator");
        left_scoop  = hardwareMap.get(Servo.class, "left_scoop");

        leftDrive.setDirection(DcMotor.Direction.REVERSE);
        rightDrive.setDirection(DcMotor.Direction.FORWARD);
        hookMotor.setDirection(DcMotor.Direction.FORWARD);

        hookMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        hookMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        elevator.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        elevator.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.mode = BNO055IMU.SensorMode.IMU;

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        waitForStart();
        runtime.reset();

        // Lowers robot from lander
        hookMotor.setTargetPosition(14000);
        hookMotor.setPower(0.75);
        
        while (hookMotor.getCurrentPosition() < 14000 && opModeIsActive()) {
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

            telemetry.addData("imu", angles.firstAngle);
            telemetry.addData("hook position", hookMotor.getCurrentPosition());
            telemetry.update();
        }

        // Robot is on the ground and hook isn't moving or disengaged
        hookMotor.setPower(0);

        // Needs to disengage from the lander
        driveStraight(-1, 0.3);

        // Turns towards depot after disengaging
        turnRobot(0.4, 90);

        // Goes to depot
        driveStraight(0.8, 0.3);

        // Raise elevator
        elevator.setPower(-1);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < .75)) {}
        elevator.setPower(0);
        
        // Throw marker into depot
        left_scoop.setPosition(1);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 1)) {}
    }
}

package ste.stella.erarc.stelladrive;

public class ControlManager {
    static ControlManager controlManager = new ControlManager();
    private float leftSpeed = 0;
    private float rightSpeed = 0;
    private float motorPowerLimit = (float) 1.0;

    private ControlManager() {}

    public void setMotorPowerLimit(float motorPower) {
        this.motorPowerLimit  = (float) (motorPower * 0.01);
    }

    public void setLeftSpeed(float speed) {
        this.leftSpeed = speed * motorPowerLimit * (float)2.55;
    }

    public void setRightSpeed(float speed) {

        this.rightSpeed = speed * motorPowerLimit * (float)2.55;
    }

    public void setEqualSpeed(float speed)  {
        float speedLimit = speed * motorPowerLimit * (float)2.55;
        this.rightSpeed = speedLimit;
        this.leftSpeed = speedLimit;
    }

    public void stopBothMotors() {
        this.rightSpeed = 1;
        this.leftSpeed = 1;
    }

    public float getLeftSpeed() {
        return leftSpeed;
    }

    public float getRightSpeed() {
        return rightSpeed;
    }

    public float getMotorPowerLimit() {
        return motorPowerLimit;
    }

    public static ControlManager getInstance() {
        return controlManager;
    }

}



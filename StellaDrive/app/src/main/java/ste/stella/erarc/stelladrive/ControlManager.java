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
        this.leftSpeed = speed * motorPowerLimit;
    }

    public void setRightSpeed(float speed) {
        this.rightSpeed = speed * motorPowerLimit;
    }

    public void setEqualSpeed(float speed)  {
        float speedLimit = speed * motorPowerLimit;
        this.rightSpeed = speedLimit;
        this.leftSpeed = speedLimit;
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



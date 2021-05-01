package ste.stella.erarc.stelladrive;

public class ConnectivityKing {
    static ConnectivityKing connectivityKing = new ConnectivityKing();
    private String macAddress;
    private String bleName;
    private boolean isConnected = false;

    private ConnectivityKing() {}

    public void updateMacAddress(String mac) {
        this.macAddress = mac;
    }

    public void updateBleName(String ble) {
        this.bleName = ble;
    }

    public void updateConnectivityStatus(boolean status) {
        this.isConnected = status;
    }

    public String getMacAddress() {
        return this.macAddress;
    }

    public String getBleName() {
        return this.bleName;
    }

    public boolean getConnectedStatus() {
        return this.isConnected;
    }

    public static ConnectivityKing getInstance() {
        return connectivityKing;
    }
}

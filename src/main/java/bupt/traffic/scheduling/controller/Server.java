package bupt.traffic.scheduling.controller;


public class Server {
	private float load;
	private String nodeID;
	private String ip;
	private String mac;
	private String connectedSwitch;
	private String connectedSwitchPort;
	

	public Server(String nodeID,String ip,String mac,String connectedSwitch,String connectedSwitchPort){
		this.nodeID = nodeID;
		this.ip = ip;
		this.mac = mac;
		this.nodeID = nodeID;
		this.connectedSwitch = connectedSwitch;
		this.connectedSwitchPort = connectedSwitchPort;
		
	}
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public double getLoad() {
		return load;
	}

	public void setLoad(float load) {
		this.load = load;
	}

	public String getConnectedSwitch() {
		return connectedSwitch;
	}

	public void setConnectedSwitch(String connectedSwitch) {
		this.connectedSwitch = connectedSwitch;
	}

	public String getConnectedSwitchPort() {
		return connectedSwitchPort;
	}

	public void setConnectedSwitchPort(String connectedSwitchPort) {
		this.connectedSwitchPort = connectedSwitchPort;
	}
}

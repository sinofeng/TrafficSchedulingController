package bupt.traffic.scheduling.controller;

public class FlowEntry {

	private String nodeID;
	private int tableID = 0;
	private int cookie;
	private int priority = 1000;
	private int inPort;
	private String srcIP;
	private String dstIP;
	
	private int outputNodeConnector;
	private String nwSrcIP;
	private String nwDstIP;
	private String nwSrcMac;
	private String nwDstMac;
	
	public FlowEntry(){
		this.srcIP = "";
		this.dstIP = "";
	}

	public FlowEntry(String nodeID, int tableID, int cookie, int priority, String dstIP, int outputNodeConnector, String nwDstIP, String nwDstMac){
		this.nodeID = nodeID;
		this.tableID = tableID;
		this.cookie = cookie;
		this.priority = priority;
		this.dstIP = dstIP;
		this.outputNodeConnector = outputNodeConnector;
		this.nwDstIP = nwDstIP;
		this.nwDstMac = nwDstMac;
	}
	
	public FlowEntry(String nodeID, int tableID, int cookie, int priority, int inPort, int outputNodeConnector, String nwSrcIP, String nwSrcMac){
		this.nodeID = nodeID;
		this.tableID = tableID;
		this.cookie = cookie;
		this.priority = priority;
		this.inPort = inPort;
		this.outputNodeConnector = outputNodeConnector;
		this.nwSrcIP = nwSrcIP;
		this.nwSrcMac = nwSrcMac;
		this.dstIP = " ";
	}
	
	public String getNwSrcIP() {
		return nwSrcIP;
	}

	public void setNwSrcIP(String nwSrcIP) {
		this.nwSrcIP = nwSrcIP;
	}

	public String getNwDstIP() {
		return nwDstIP;
	}

	public void setNwDstIP(String nwDstIP) {
		this.nwDstIP = nwDstIP;
	}

	public int getOutputNodeConnector() {
		return outputNodeConnector;
	}

	public void setOutputNodeConnector(int outputNodeConnector) {
		this.outputNodeConnector = outputNodeConnector;
	}

	public int getTableID() {
		return tableID;
	}

	public void setTableID(int tableID) {
		this.tableID = tableID;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getSrcIP() {
		return srcIP;
	}

	public void setSrcIP(String srcIP) {
		this.srcIP = srcIP;
	}

	public String getDstIP() {
		return dstIP;
	}

	public void setDstIP(String dstIP) {
		this.dstIP = dstIP;
	}
	
	public String getNwSrcMac() {
		return nwSrcMac;
	}

	public void setNwSrcMac(String nwSrcMac) {
		this.nwSrcMac = nwSrcMac;
	}
	
	public String getNwDstMac() {
		return nwDstMac;
	}

	public void setNwDstMac(String nwDstMac) {
		this.nwDstMac = nwDstMac;
	}

	public int getInPort() {
		return inPort;
	}

	public void setInPort(int inPort) {
		this.inPort = inPort;
	}

	public int getCookie() {
		return cookie;
	}

	public void setCookie(int cookie) {
		this.cookie = cookie;
	}
	
	
}

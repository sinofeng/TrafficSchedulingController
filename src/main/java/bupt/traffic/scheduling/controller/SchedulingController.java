package bupt.traffic.scheduling.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

public class SchedulingController {
	private String controllerIP = "localhost";
	private List<Server> serverList;
	private Map<String,Integer> connectedSwitchMap;
	private Map<String,Server> serverMap;
	private FlowEntry changeInputPacket;
	private List<FlowEntry> changeOutputPacket;
	private String collectiveSwitchNodeID;
	
	private Server mainServer;
	
	public SchedulingController(String controllerIP){
		this.controllerIP = controllerIP;
		this.serverList = new ArrayList<Server>();
		this.connectedSwitchMap = new HashMap<String,Integer>();
		this.serverMap = new HashMap<String,Server>();
		this.changeInputPacket = new FlowEntry();
		this.changeOutputPacket = new ArrayList<FlowEntry>();
		this.collectiveSwitchNodeID = "openflow:1";
		
		getTopoInfo();
		
		mainServer = serverList.get(0);
		
		System.out.println("Please visit " + mainServer.getIp() + "to get service!");
		
	}
	
	public void getTopoInfo(){
		HttpClient httpClient = new DefaultHttpClient();
		
		String url = "http://" + controllerIP + ":8181/restconf/operational/network-topology:network-topology/";
		HttpGet getResponce = new HttpGet(url);
		getResponce.addHeader("Accept","application/json");
		getResponce.addHeader("Authorization","Basic YWRtaW46YWRtaW4=");
		
		try {
			HttpResponse httpResponse = httpClient.execute(getResponce);
			
			if (httpResponse.getStatusLine().getStatusCode() != 200){
				throw new RuntimeException("Failed : Http error code : "
						+ httpResponse.getStatusLine().getStatusCode());
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			StringBuilder strBuilder = new StringBuilder();
			String line = null;
			while((line = br.readLine())!=null){
				strBuilder.append(line+"\n");
			}
			
			JSONObject jsonObject = new JSONObject(strBuilder.toString());
			JSONArray topoArr = jsonObject.getJSONObject("network-topology").getJSONArray("topology");
			JSONArray linkArr = topoArr.getJSONObject(0).getJSONArray("link");
			JSONArray nodeArr = topoArr.getJSONObject(0).getJSONArray("node"); 
			
			for(int i=0;i<linkArr.length();i++){
				JSONObject tempLink= linkArr.getJSONObject(i);
				if(tempLink.getString("link-id").contains("/openflow") && !tempLink.getString("link-id").contains("/openflow:1")){
					String serverID = tempLink.getJSONObject("source").getString("source-node");
					String connectedSwitch = tempLink.getJSONObject("destination").getString("dest-node");
					String connectedSwitchPort = tempLink.getJSONObject("destination").getString("dest-tp");
					
					
					for(int j=0;j<nodeArr.length();j++){
						JSONObject tempNode = nodeArr.getJSONObject(j);
						if(tempNode.getString("node-id").equals(serverID)){
							String serverMAC = tempNode.getJSONArray("host-tracker-service:addresses").getJSONObject(0).getString("mac");
							String serverIP = tempNode.getJSONArray("host-tracker-service:addresses").getJSONObject(0).getString("ip");
							
							Server s = new Server(serverID, serverIP, serverMAC, connectedSwitch, connectedSwitchPort);
							serverList.add(s);
							serverMap.put(s.getIp(), s);
						}
					}
				}
				
				if(tempLink.getString("link-id").contains(collectiveSwitchNodeID)&&!tempLink.getString("link-id").contains("host")){
					String switchNodeID = tempLink.getJSONObject("destination").getString("dest-node");
					String tpPort = tempLink.getJSONObject("source").getString("source-tp");
					
					connectedSwitchMap.put(switchNodeID,(int) tpPort.getBytes()[ tpPort.getBytes().length-1]-48);
				}
			}
			
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public boolean scheduling() throws IOException{
		
		//new Thread(new SocketServer()).start();
		
		Set entries = connectedSwitchMap.entrySet();
		if(entries!=null){
			Iterator iterator =entries.iterator();
			int cookie = 600;
			while(iterator.hasNext()){
				Entry entry = (Entry)iterator.next();
				FlowEntry fe = new FlowEntry(collectiveSwitchNodeID, 0, cookie, 1000, (Integer)entry.getValue(), 1, mainServer.getIp(), mainServer.getMac());
				changeOutputPacket.add(fe);
				modifyFlowTable(fe, OPERATION.ADD);
			}
		}
		
		Server minLoadServer = serverList.get(0);
		
		boolean f =true;
		while(f){
			
			for(int i=0; i<serverList.size(); i++){
				File file = new File("/home/ylf/"+serverList.get(i).getIp());
				if (!file.exists()) {
		            file.createNewFile();  
				} 
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String temp="",str;
				do{
					str = temp;
				}while((temp = br.readLine())!= null);
				
				if("".equals(str)){
					serverList.get(i).setLoad(0);
				}else{
					String temp1 = (String) str.subSequence(str.indexOf("is:")+3, str.length());
					int load = Integer.parseInt(temp1);
					System.out.println(serverList.get(i).getIp() + " " + load);
					serverList.get(i).setLoad(load);
				}
			}
			
			for(int i=0; i<serverList.size(); i++){
				if(serverList.get(i).getLoad()<minLoadServer.getLoad()){
					minLoadServer = serverList.get(i);
				}
			}
			//System.out.println(minLoadServer.getMac());
			
			if(!minLoadServer.getIp().equals(changeInputPacket.getNwDstIP())){
				if(!"".equals(changeInputPacket.getDstIP())){
					modifyFlowTable(changeInputPacket, OPERATION.REMOVE);
					changeInputPacket =  new FlowEntry(collectiveSwitchNodeID, 0 , 599, 1000, mainServer.getIp(),connectedSwitchMap.get(minLoadServer.getConnectedSwitch()), minLoadServer.getIp(), minLoadServer.getMac());
					modifyFlowTable(changeInputPacket, OPERATION.ADD);	
				}else{
					changeInputPacket =  new FlowEntry(collectiveSwitchNodeID, 0 , 599, 1000, mainServer.getIp(),connectedSwitchMap.get(minLoadServer.getConnectedSwitch()), minLoadServer.getIp(), minLoadServer.getMac());
					System.out.println(changeInputPacket.getNwDstMac());
					modifyFlowTable(changeInputPacket, OPERATION.ADD);
				}

			}
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block				
				e.printStackTrace();
			}
			
		}
		
		return true;
	}

	public void modifyFlowTable(FlowEntry fe,OPERATION op){
		String nodeID = fe.getNodeID();
		int tableID = fe.getTableID();
		int cookie = fe.getCookie();
		int priority = fe.getPriority();
		String body = "";
		System.out.println("fe" + fe.getDstIP() + " server" + mainServer.getIp());
	   
		if(fe.getDstIP().equals(mainServer.getIp())){
			String dstIP = fe.getDstIP();
			
			String nwDstIP = fe.getNwDstIP();
			String nwDstMac = fe.getNwDstMac();
			int outputNodeConnector = fe.getOutputNodeConnector();
			
			body = String.format( 
						"<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                        "<input xmlns='urn:opendaylight:flow:service'>\n" +
                        "   <barrier>false</barrier>\n" +
                        "   <node xmlns:inv='urn:opendaylight:inventory'>/inv:nodes/inv:node[inv:id='%s']</node>\n" +
                        "   <cookie>%d</cookie>\n" +
                        "   <hard-timeout>0</hard-timeout>\n" +
                        "   <idle-timeout>0</idle-timeout>\n" +
                        "   <installHw>false</installHw>\n" +
                        "   <match>\n" +
                        "    <ethernet-match>\n" +
                        "     <ethernet-type>\n" +
                        "       <type>2048</type>\n" +
                        "     </ethernet-type>\n" +
                        "    </ethernet-match>\n" +
                        "    <ipv4-destination>%s/32</ipv4-destination>\n" +
                        "   </match>\n" +
                        "   <instructions>\n" +
                        "    <instruction>\n" +
                        "     <order>0</order>\n" +
                        "     <apply-actions>\n" +
                        "       <action>\n" +
                        "        <order>0</order>\n" +
                        "        <set-nw-dst-action>\n" +
                        "          <ipv4-address>%s/32</ipv4-address>" +
                        "        </set-nw-dst-action>\n" +
                        "       </action>\n" +
                        "       <action>\n" +
                        "        <order>1</order>\n" +
                        "        <set-dl-dst-action>\n" +
                        "          <address>%s</address>" +
                        "        </set-dl-dst-action>\n" +
                        "       </action>\n" +
                        "       <action>\n" +
                        "        <order>2</order>\n" +
                        "        <output-action>\n" +
                        "          <output-node-connector>%d</output-node-connector>\n" +
                        "          <max-length>60</max-length>\n" +
                        "        </output-action>\n" +
                        "       </action>\n" +
                        "     </apply-actions>\n" +
                        "    </instruction>\n" +
                        "   </instructions>\n" +
                        "   <priority>%d</priority>\n" +
                        "   <strict>false</strict>\n" +
                        "   <table_id>%d</table_id>\n" +
                        "</input>", nodeID, cookie, dstIP, nwDstIP, nwDstMac, outputNodeConnector, priority, tableID);
			//System.out.println(body);

		}else{
			int inPort = fe.getInPort();
			
			String nwSrcIP = fe.getNwSrcIP();
			String nwSrcMac = fe.getNwSrcMac();
			int outputNodeConnector = fe.getOutputNodeConnector();
			
			body = String.format( 
					"<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                    "<input xmlns='urn:opendaylight:flow:service'>\n" +
                    "   <barrier>false</barrier>\n" +
                    "   <node xmlns:inv='urn:opendaylight:inventory'>/inv:nodes/inv:node[inv:id='%s']</node>\n" +
                    "   <cookie>%d</cookie>\n" +
                    "   <hard-timeout>0</hard-timeout>\n" +
                    "   <idle-timeout>0</idle-timeout>\n" +
                    "   <installHw>false</installHw>\n" +
                    "   <match>\n" +
                    "    <in-port>%d</in-port>\n" +
                    "    <ethernet-match>\n" +
                    "     <ethernet-type>\n" +
                    "       <type>2048</type>\n" +
                    "     </ethernet-type>\n" +
                    "    </ethernet-match>\n" +
                    "   </match>\n" +
                    "   <instructions>\n" +
                    "    <instruction>\n" +
                    "     <order>0</order>\n" +
                    "     <apply-actions>\n" +
                    "       <action>\n" +
                    "        <order>0</order>\n" +
                    "        <set-nw-src-action>\n" +
                    "          <ipv4-address>%s/32</ipv4-address>\n" +
                    "        </set-nw-src-action>\n" +
                    "       </action>\n" +
                    "       <action>\n" +
                    "        <order>1</order>\n" +
                    "        <set-dl-src-action>\n" +
                    "          <address>%s</address>\n" +
                    "        </set-dl-src-action>\n" +
                    "       </action>\n" +
                    "       <action>\n" +
                    "        <order>2</order>\n" +
                    "        <output-action>\n" +
                    "          <output-node-connector>%d</output-node-connector>\n" +
                    "          <max-length>60</max-length>\n" +
                    "        </output-action>\n" +
                    "       </action>\n" +
                    "     </apply-actions>\n" +
                    "    </instruction>\n" +
                    "   </instructions>\n" +
                    "   <priority>%d</priority>\n" +
                    "   <strict>false</strict>\n" +
                    "   <table_id>%d</table_id>\n" +
                    "</input>", nodeID, cookie, inPort, nwSrcIP, nwSrcMac, outputNodeConnector, priority, tableID);
		}
		
		String url;
		if (op == OPERATION.ADD){
            url = String.format("http://%s:8181/restconf/operations/sal-flow:add-flow", controllerIP);
        }else {
            url = String.format("http://%s:8181/restconf/operations/sal-flow:remove-flow", controllerIP);
        }
       
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(url);

			//System.out.println(url);
			StringEntity input = new StringEntity(body);
			input.setContentType("application/xml");
			postRequest.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
		        
			postRequest.setEntity(input);


			HttpResponse response = httpClient.execute(postRequest);

			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatusLine().getStatusCode());
			}else{
				if(fe.getDstIP().equals(mainServer.getIp())){

					System.out.println(op+String.format(": cookie=%d, dstIP=%s, nwDstIP=%s, nwDstMac=%s, outputNodeConnector=%d \nSuccess",
											fe.getCookie(), fe.getDstIP(), fe.getNwDstIP(), fe.getNwDstMac(), fe.getOutputNodeConnector()));
					
				}else{
					System.out.println(op+String.format(": cookie=%d, inPort=%d, nwSrcIP=%s, nwSrcMac=%s, outputNodeConnector=%d \nSuccess",
											fe.getCookie(), fe.getInPort(), fe.getNwSrcIP(), fe.getNwSrcMac(), fe.getOutputNodeConnector()));
				}
			}

			BufferedReader br = new BufferedReader(
		                       new InputStreamReader((response.getEntity().getContent())));

			String output;
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			httpClient.getConnectionManager().shutdown();

			 } catch (MalformedURLException e) {

				 e.printStackTrace();
			
			 } catch (IOException e) {

				e.printStackTrace();

			 }
		
	}
}

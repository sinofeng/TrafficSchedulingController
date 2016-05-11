package bupt.traffic.scheduling.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ServerThread implements Runnable{
	private Socket client = null;  
    public ServerThread(Socket client){  
        this.client = client;  
    }  

    public void run() {  
        try{  
        	 InputStream input = client.getInputStream();
             OutputStream output = client.getOutputStream();
             byte[] arry = new byte[1024];
             int length = input.read(arry);
             if (length!=-1){
            	 output.write("Success".getBytes());
            	 output.flush();
             }
             
             int load = arry[0];
             byte[] temp = new byte[length-4];
             for(int i=0; i<temp.length; i++){
            	 temp[i] = arry[i+4];
             }
             
             String serverIP = new String(temp);
             System.out.println(serverIP + ":" + load);
             
             
             
             output.close();
             client.close();
             
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  
    
	public static int bytesToInt(byte[] bytes){
		int n = (int) ((bytes[0]&0xFF)   
	            | ((bytes[1]<<8) & 0xFF00)  
	            | ((bytes[2]<<16)& 0xFF0000)   
	            | ((bytes[3]<<24) & 0xFF000000));  
		
		return n;
		
	}
}


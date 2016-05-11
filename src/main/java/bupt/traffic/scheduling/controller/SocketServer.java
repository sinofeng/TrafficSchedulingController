package bupt.traffic.scheduling.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketServer implements Runnable {

	public void run(){
		// TODO Auto-generated method stub
        ServerSocket server;
		try {
			server = new ServerSocket(20006);
			Socket client = null;  
			boolean f = true;
			while(f){  
				//等待客户端的连接，如果没有获取连接  
				client = server.accept();  
				System.out.println("与客户端连接成功！");  
				//为每个客户端连接开启一个线程  
				new Thread(new ServerThread(client)).start();
			}
			server.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}

}

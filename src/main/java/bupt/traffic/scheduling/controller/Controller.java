package bupt.traffic.scheduling.controller;

import java.io.IOException;

public class Controller {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		SchedulingController sc = new SchedulingController("localhost");
		sc.scheduling();
	}

}

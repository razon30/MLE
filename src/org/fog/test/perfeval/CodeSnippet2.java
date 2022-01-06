package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.List;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Tuple;

public class CodeSnippet2 {

	public static Application createApplication(String appId, int brokerid) {
		
		Application application = Application.createApplication(appId, brokerid);
		application.addAppModule("MasterModule", 1000);
		application.addAppModule("WorkerModule-1", 1000);
		application.addAppModule("WorkerModule-2", 1000);
		application.addAppModule("WorkerModule-3", 1000);

		application.addAppEdge("Sensor", "MasterModule", 3000, 500, "Sensor", Tuple.UP, AppEdge.SENSOR);
		
		application.addAppEdge("MasterModule", "WorkerModule-1", 100, 1000, "Task-1", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("MasterModule", "WorkerModule-2", 100, 1000, "Task-2", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("MasterModule", "WorkerModule-3", 100, 1000, "Task-3", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-1", "MasterModule", 20, 50, "Response-1", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-2", "MasterModule", 20, 50, "Response-2", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-2", "MasterModule", 20, 50, "Response-3", Tuple.DOWN, AppEdge.MODULE);
		
		application.addAppEdge("MasterModule", "Actuators", 100, 50, "OutputData", Tuple.DOWN, AppEdge.ACTUATOR);
		
		
		application.addTupleMapping("MasterModule", "Sensor", "Task-1", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", "Sensor", "Task-2", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", "Sensor", "Task-3", new FractionalSelectivity(0.3));
		
		application.addTupleMapping("WorkerModule-1", "Task-1", "Response-1", new FractionalSelectivity(1.0));
		application.addTupleMapping("WorkerModule-2", "Task-2", "Response-2", new FractionalSelectivity(1.0));
		application.addTupleMapping("WorkerModule-3", "Task-3", "Response-3", new FractionalSelectivity(1.0));
		
		application.addTupleMapping("MasterModule", "Response-1", "OutputData", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", "Response-2", "OutputData", new FractionalSelectivity(0.3));
		application.addTupleMapping("MasterModule", "Response-3", "OutputData", new FractionalSelectivity(0.3));
		
		
		AppLoop loop1 = new AppLoop(new ArrayList<String>() {{add("Sensor"); add("MasterModule"); add("WorkerModule-1"); add("MasterModule"); add("Actuator");}});
		AppLoop loop2 = new AppLoop(new ArrayList<String>() {{add("Sensor"); add("MasterModule"); add("WorkerModule-2"); add("MasterModule"); add("Actuator");}});
		AppLoop loop3 = new AppLoop(new ArrayList<String>() {{add("Sensor"); add("MasterModule"); add("WorkerModule-3"); add("MasterModule"); add("Actuator");}});
		
		List<AppLoop> loops = new ArrayList<AppLoop>() {
			{
				add(loop1);
				add(loop2);
				add(loop3);
			}
		};
		application.setLoops(loops);
		
		return application;
	}

}

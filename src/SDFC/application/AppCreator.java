package SDFC.application;

import java.util.ArrayList;
import java.util.List;

import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Tuple;

import SDFC.utils.Constants;

public class AppCreator {

	public static Application createApplication(String appId, int brokerid) {
		
		Application application = Application.createApplication(appId, brokerid);
		
		
		application.addAppEdge(Constants.AUTH_SENSOR_NAME, Constants.MAIN_MODULE, 3000, 500, Constants.AUTH_SENSOR_NAME, Tuple.UP, AppEdge.SENSOR);
		
		application.addAppEdge(Constants.MAIN_MODULE, "WorkerModule-2", 100, 1000, "Task-1", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge(Constants.MAIN_MODULE, "WorkerModule-2", 100, 1000, "Task-2", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge(Constants.MAIN_MODULE, "WorkerModule-3", 100, 1000, "Task-3", Tuple.UP, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-1", Constants.MAIN_MODULE, 20, 50, "Response-1", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-2", Constants.MAIN_MODULE, 20, 50, "Response-2", Tuple.DOWN, AppEdge.MODULE);
		application.addAppEdge("WorkerModule-2", Constants.MAIN_MODULE, 20, 50, "Response-3", Tuple.DOWN, AppEdge.MODULE);
		
		application.addAppEdge(Constants.MAIN_MODULE, "Actuators", 100, 50, "OutputData", Tuple.DOWN, AppEdge.ACTUATOR);
		
		
		application.addTupleMapping(Constants.MAIN_MODULE, Constants.AUTH_SENSOR_NAME, "Task-1", new FractionalSelectivity(0.3));
		application.addTupleMapping(Constants.MAIN_MODULE, Constants.AUTH_SENSOR_NAME, "Task-2", new FractionalSelectivity(0.3));
		application.addTupleMapping(Constants.MAIN_MODULE, Constants.AUTH_SENSOR_NAME, "Task-3", new FractionalSelectivity(0.3));
		
		application.addTupleMapping("WorkerModule-1", "Task-1", "Response-1", new FractionalSelectivity(1.0));
		application.addTupleMapping("WorkerModule-2", "Task-2", "Response-2", new FractionalSelectivity(1.0));
		application.addTupleMapping("WorkerModule-3", "Task-3", "Response-3", new FractionalSelectivity(1.0));
		
		application.addTupleMapping(Constants.MAIN_MODULE, "Response-1", "OutputData", new FractionalSelectivity(0.3));
		application.addTupleMapping(Constants.MAIN_MODULE, "Response-2", "OutputData", new FractionalSelectivity(0.3));
		application.addTupleMapping(Constants.MAIN_MODULE, "Response-3", "OutputData", new FractionalSelectivity(0.3));
		
		
		AppLoop loop1 = new AppLoop(new ArrayList<String>() {{add(Constants.AUTH_SENSOR_NAME); add(Constants.MAIN_MODULE); add("WorkerModule-1"); add(Constants.MAIN_MODULE); add("Actuator");}});
		AppLoop loop2 = new AppLoop(new ArrayList<String>() {{add(Constants.AUTH_SENSOR_NAME); add(Constants.MAIN_MODULE); add("WorkerModule-2"); add(Constants.MAIN_MODULE); add("Actuator");}});
		AppLoop loop3 = new AppLoop(new ArrayList<String>() {{add(Constants.AUTH_SENSOR_NAME); add(Constants.MAIN_MODULE); add("WorkerModule-3"); add(Constants.MAIN_MODULE); add("Actuator");}});
		
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

package SDFCNew;

import java.util.*;

import MSDFC.GeneralPurposeFog;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import SDFC.utils.Utils;

public class SimulateSDFC {

	public static Map<Integer, SDFCFogDevice> CFbyIDHealthStatusTable = new HashMap<>();
	public static List<SDFCFogDevice> SDFCFogDevices = new ArrayList<SDFCFogDevice>();
	static List<SDFCSensor> sensors = new ArrayList<SDFCSensor>();
	static List<SDFCActuator> actuators = new ArrayList<SDFCActuator>();
	static int numOfAreas = 1;
	static int numOfCamerasPerArea = 4;

	public static Set<Integer> totalVM = new HashSet<>();
	public static Set<Integer> totalVMFaile = new HashSet<>();

	private static boolean CLOUD = false;

	public static void main(String[] args) {

		Log.printLine("Starting SDFC...");

		try {
			Log.disable();
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);

			String appId = "sdfc"; // identifier of the application

			FogBroker broker = new FogBroker("broker");

			SDFCApplication application = createSDFCApplication(appId, broker.getId());
			application.setUserId(broker.getId());

			// createSDFCFogDevices(broker.getId(), appId);

			SDFCModuleMapping moduleMapping = SDFCModuleMapping.createModuleMapping(); // initializing a module mapping
			for (int i = 0; i < Constant.NumberOfCF; i++) {
				createCFDevices(broker.getId(), appId, i, application, moduleMapping, Constant.CITIZEN_FOG,
						Constant.CITIZEN_MODULE);
			}
			createCFDevices(broker.getId(), appId, 0, application, moduleMapping, Constant.MASTER_FOG,
					Constant.MASTER_MODULE);

			completeApplicationsettings(application);

			SDFCController controller = new SDFCController("master-SDFCController", SDFCFogDevices, sensors, actuators);
			controller.submitApplication(application,
					(new SDFCModulePlacement(SDFCFogDevices, sensors, actuators, application, moduleMapping)));

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("VRGame finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the fog devices in the physical topology of the simulation.
	 * 
	 * @param userId
	 * @param appId
	 * @param application
	 * @param moduleMapping
	 * @param fogName
	 * @param moduleName
	 * @param CFId
	 */

	private static void createCFDevices(int userId, String appId, int cfId, SDFCApplication application,
			SDFCModuleMapping moduleMapping, String fogName, String moduleName) {

		long mips = Utils.getValue(1024 * 3, 1024 * 5);
		int rams = Utils.getValue(1024 * 3, 1024 * 5);
		long upBW = Utils.getValue(1024 * 6, 1024 * 8);
		long dwnBW = Utils.getValue(1024 * 6, 1024 * 8);
		long storage = Utils.getValue(1024 * 100 * 8, 1024 * 100 * 10); // 1000000; // host storage
		int bw = Utils.getValue(1024 * 8, 1024 * 11);
		int level = 2;

		SDFCFogDevice CF = createSDFCFogDevice(fogName + cfId, mips, rams, upBW, dwnBW, 0, 0.01, 107.339, 83.4333,
				storage, bw);

//		long storage = 1000000; // host storage
//		int bw = 10000;
//		SDFCFogDevice CF = createSDFCFogDevice(fogName + cfId, 500, 1000, 10000, 10000, 3, 0, 87.53, 82.44, storage,
//				bw);

		// moduleMapping.addModuleToDevice(moduleName, CF.getName());
		application.addAppModule(moduleName, rams, mips, bw, storage);
		// application.addAppModule(moduleName);

		CF.setMips((int) mips);
		CFbyIDHealthStatusTable.put(CF.getId(), CF);
		SDFCFogDevices.add(CF);

		if (fogName.equalsIgnoreCase(Constant.CITIZEN_FOG)) {
			for (int i = 0; i < Constant.SenorAtEachCF; i++) {
				setSensorActuator(CF, cfId + "_" + i, userId, appId);
			}
		}

		// printCFInfo(CF, moduleName);

	}

	private static void setSensorActuator(SDFCFogDevice cF, String id, int userId, String appId) {

		SDFCSensor sensor = new SDFCSensor(Constant.SENSOR + id, Constant.SENSOR, userId, appId,
				new DeterministicDistribution(5));
		sensor.setGatewayDeviceId(cF.getId());
		sensors.add(sensor);

		SDFCActuator actuator = new SDFCActuator(Constant.ACTUATOR + id, userId, appId, Constant.ACTUATOR);
		actuator.setGatewayDeviceId(cF.getId());
		actuators.add(actuator);

	}

	private static void printCFInfo(SDFCFogDevice cF, String moduleName) {

		print("\n\n========================");
		print("Fog Name: " + cF.getName());
		print("Module Name: " + moduleName);
		print("Fog RAM: " + cF.getHostList().get(0).getRam());
		print("Fog MIPS: " + cF.getHost().getPeList().get(0).getMips() * cF.getHost().getPeList().size());
		print("Fog BW: " + cF.getHost().getBw());
		print("Fog Execution Time: " + cF.getExecutionTime());
		print("========================\n\n");

	}

	private static void print(String string) {
		System.out.println(string);
	}

	/**
	 * Creates a vanilla fog device
	 * 
	 * @param nodeName    name of the device to be used in simulation
	 * @param mips        MIPS
	 * @param ram         RAM
	 * @param upBw        uplink bandwidth
	 * @param downBw      downlink bandwidth
	 * @param level       hierarchy level of the device
	 * @param ratePerMips cost rate per MIPS used
	 * @param busyPower
	 * @param idlePower
	 * @return
	 */
	private static SDFCFogDevice createSDFCFogDevice(String nodeName, long mips, int ram, long upBw, long downBw,
			int level, double ratePerMips, double busyPower, double idlePower, long storage, int bw) {

		List<Pe> peList = new ArrayList<Pe>();

		int numOfPe = Utils.getValue(1, 5);

		// 3. Create PEs and add these into a list.
		for (int i = 0; i < numOfPe; i++) {
			peList.add(new Pe(i, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating
		}

		// peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));

		int hostId = FogUtils.generateEntityId();

		PowerHost host = new PowerHost(hostId, new RamProvisionerSimple(ram), new BwProvisionerOverbooking(bw), storage,
				peList, new StreamOperatorScheduler(peList), new FogLinearPowerModel(busyPower, idlePower));

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
										// resource
		double costPerBw = 0.0; // the cost of using bw in this resource

		float executionTime = (float) (Math.pow(10, 3) / (mips * numOfPe));
		// Execution Time to execute 10^9 MIPS = (Instruction count / (totalMIPS *
		// 10^6))

		LinkedList<Storage> storageList = new LinkedList<Storage>();
		// we are not adding SAN devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch, os, vmm, host, time_zone, cost,
				costPerMem, costPerStorage, costPerBw);

		SDFCFogDevice SDFCFogDevice = null;
		try {
			SDFCFogDevice = new SDFCFogDevice(nodeName, characteristics, new AppModuleAllocationPolicy(hostList),
					storageList, 10, upBw, downBw, 0, ratePerMips, executionTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SDFCFogDevice.setLevel(level);
		return SDFCFogDevice;
	}

	private static void completeApplicationsettings(SDFCApplication application) {

//		application.addAppModule(Constant.MASTER_MODULE);
//		for (int i = 0; i < Constant.NumberOfCF; i++) {
//			application.addAppModule(Constant.CITIZEN_MODULE);
//		}

		application.addAppEdge(Constant.SENSOR, Constant.CITIZEN_MODULE, 1000, 20000, Constant.TASK, Tuple.UP,
				AppEdge.SENSOR);

		application.addAppEdge(Constant.CITIZEN_MODULE, Constant.MASTER_MODULE, 2000, 2000, Constant.TASK, Tuple.UP,
				AppEdge.MODULE);

		application.addAppEdge(Constant.MASTER_MODULE, Constant.CITIZEN_MODULE, 500, 2000, Constant.TASK, Tuple.UP,
				AppEdge.MODULE);

		application.addAppEdge(Constant.CITIZEN_MODULE, Constant.MASTER_MODULE, 1000, 100, Constant.RESPONSE,
				Tuple.DOWN, AppEdge.MODULE);

		application.addAppEdge(Constant.MASTER_MODULE, Constant.CITIZEN_MODULE, 500, 2000, Constant.RESPONSE,
				Tuple.DOWN, AppEdge.MODULE);

		application.addAppEdge(Constant.CITIZEN_MODULE, Constant.ACTUATOR, 500, 2000, Constant.RESPONSE, Tuple.DOWN,
				AppEdge.ACTUATOR);

		/*
		 * Defining the input-output relationships (represented by selectivity) of the
		 * application modules.
		 */
		application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.TASK, Constant.TASK,
				new FractionalSelectivity(1.0));
		application.addTupleMapping(Constant.MASTER_MODULE, Constant.TASK, Constant.TASK,
				new FractionalSelectivity(1.0));
		application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.TASK, Constant.RESPONSE,
				new FractionalSelectivity(0.05));
		application.addTupleMapping(Constant.MASTER_MODULE, Constant.RESPONSE, Constant.RESPONSE,
				new FractionalSelectivity(0.05));
		application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.RESPONSE, Constant.RESPONSE,
				new FractionalSelectivity(0.05));

		/*
		 * Defining application loops (maybe incomplete loops) to monitor the latency
		 * of. Here, we add two loops for monitoring : Motion Detector -> Object
		 * Detector -> Object Tracker and Object Tracker -> PTZ Control
		 */
		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
			{
				add(Constant.SENSOR);
				add(Constant.CITIZEN_MODULE);
				add(Constant.MASTER_MODULE);
				add(Constant.CITIZEN_MODULE);
				add(Constant.ACTUATOR);
			}
		});

		final AppLoop loop2 = new AppLoop(new ArrayList<String>() {
			{
				add(Constant.SENSOR);
				add(Constant.CITIZEN_MODULE);
				add(Constant.ACTUATOR);
			}
		});

		List<AppLoop> loops = new ArrayList<AppLoop>() {
			{
				add(loop1);
				add(loop2);
			}
		};

		application.setLoops(loops);

	}

	@SuppressWarnings({ "serial" })
	private static SDFCApplication createSDFCApplication(String appId, int userId) {

		SDFCApplication application = SDFCApplication.createApplication(appId, userId);

		/*
		 * Connecting the application modules (vertices) in the application model
		 * (directed graph) with edges
		 */

		return application;
	}

//	private static void createSDFCFogDevices(int userId, String appId) {
//		SDFCFogDevice cloud = createSDFCFogDevice("cloud", 44800, 40000, 100, 10000, 0, 0.01, 16 * 103, 16 * 83.25);
//		cloud.setParentId(-1);
//		SDFCFogDevices.add(cloud);
//		SDFCFogDevice proxy = createSDFCFogDevice("proxy-server", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
//		proxy.setParentId(cloud.getId());
//		proxy.setUplinkLatency(100); // latency of connection between proxy server and cloud is 100 ms
//		SDFCFogDevices.add(proxy);
//		for (int i = 0; i < numOfAreas; i++) {
//			addArea(i + "", userId, appId, proxy.getId());
//		}
//	}
//
//	private static SDFCFogDevice addArea(String id, int userId, String appId, int parentId) {
//		SDFCFogDevice router = createSDFCFogDevice("d-" + id, 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
//		SDFCFogDevices.add(router);
//		router.setUplinkLatency(2); // latency of connection between router and proxy server is 2 ms
//		for (int i = 0; i < numOfCamerasPerArea; i++) {
//			String mobileId = id + "-" + i;
//			SDFCFogDevice camera = addCamera(mobileId, userId, appId, router.getId()); // adding a smart camera to the
//																						// physical topology. Smart
//																						// cameras
//																						// have been modeled as fog
//																						// devices
//																						// as well.
//			camera.setUplinkLatency(2); // latency of connection between camera and router is 2 ms
//			SDFCFogDevices.add(camera);
//		}
//		router.setParentId(parentId);
//		return router;
//	}
//
//	private static SDFCFogDevice addCamera(String id, int userId, String appId, int parentId) {
//		SDFCFogDevice camera = createSDFCFogDevice("m-" + id, 500, 1000, 10000, 10000, 3, 0, 87.53, 82.44);
//		camera.setParentId(parentId);
//		SDFCSensor sensor = new SDFCSensor("s-" + id, "CAMERA", userId, appId, new DeterministicDistribution(5)); // inter-transmission
//		// time of
//		// camera
//		// (sensor)
//		// follows a
//		// deterministic
//		// distribution
//		sensors.add(sensor);
//		SDFCActuator ptz = new SDFCActuator("ptz-" + id, userId, appId, "PTZ_CONTROL");
//		actuators.add(ptz);
//		sensor.setGatewayDeviceId(camera.getId());
//		sensor.setLatency(1.0); // latency of connection between camera (sensor) and the parent Smart Camera is
//								// 1 ms
//		ptz.setGatewayDeviceId(camera.getId());
//		ptz.setLatency(1.0); // latency of connection between PTZ Control and the parent Smart Camera is 1 ms
//		return camera;
//	}

//	int mips = 1000;
//	long size = 10000;
//	long bw = 1000;
//	String vmm = "Xen";

	/**
	 * Function to create the Intelligent Surveillance application in the DDF model.
	 * 
	 * @param appId  unique identifier of the application
	 * @param userId identifier of the user of the application
	 * @return
	 */
//	@SuppressWarnings({ "serial" })
//	private static SDFCApplication createSDFCApplication(String appId, int userId) {
//
//		SDFCApplication application = SDFCApplication.createApplication(appId, userId);
//		/*
//		 * Adding modules (vertices) to the application model (directed graph)
//		 * 
//		 */
//
//		// application.addAppModule(Constant.CITIZEN_MODULE, ram, mips, size, bw);
//
////		application.addAppModule("citizen_fog", 10);
////		application.addAppModule("motion_detector", 10);
////		application.addAppModule("object_tracker", 10);
////		application.addAppModule("user_interface", 10);
//
//		/*
//		 * Connecting the application modules (vertices) in the application model
//		 * (directed graph) with edges
//		 */
//		application.addAppEdge("CAMERA", "motion_detector", 1000, 20000, "CAMERA", Tuple.UP, AppEdge.SENSOR); // adding
//																												// edge
//																												// from
//																												// CAMERA
//																												// (sensor)
//																												// to
//																												// Motion
//																												// Detector
//																												// module
//																												// carrying
//																												// tuples
//																												// of
//																												// type
//																												// CAMERA
//		application.addAppEdge("motion_detector", "object_detector", 2000, 2000, "MOTION_VIDEO_STREAM", Tuple.UP,
//				AppEdge.MODULE); // adding edge from Motion Detector to Object Detector module carrying tuples of
//									// type MOTION_VIDEO_STREAM
//		application.addAppEdge("object_detector", "user_interface", 500, 2000, "DETECTED_OBJECT", Tuple.UP,
//				AppEdge.MODULE); // adding edge from Object Detector to User Interface module carrying tuples of
//									// type DETECTED_OBJECT
//		application.addAppEdge("object_detector", "object_tracker", 1000, 100, "OBJECT_LOCATION", Tuple.UP,
//				AppEdge.MODULE); // adding edge from Object Detector to Object Tracker module carrying tuples of
//									// type OBJECT_LOCATION
//		application.addAppEdge("object_tracker", "PTZ_CONTROL", 100, 28, 100, "PTZ_PARAMS", Tuple.DOWN,
//				AppEdge.ACTUATOR); // adding edge from Object Tracker to PTZ CONTROL (actuator) carrying tuples of
//									// type PTZ_PARAMS
//
//		/*
//		 * Defining the input-output relationships (represented by selectivity) of the
//		 * application modules.
//		 */
//		application.addTupleMapping("motion_detector", "CAMERA", "MOTION_VIDEO_STREAM", new FractionalSelectivity(1.0)); // 1.0
//																															// tuples
//																															// of
//																															// type
//																															// MOTION_VIDEO_STREAM
//																															// are
//																															// emitted
//																															// by
//																															// Motion
//																															// Detector
//																															// module
//																															// per
//																															// incoming
//																															// tuple
//																															// of
//																															// type
//																															// CAMERA
//		application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "OBJECT_LOCATION",
//				new FractionalSelectivity(1.0)); // 1.0 tuples of type OBJECT_LOCATION are emitted by Object Detector
//													// module per incoming tuple of type MOTION_VIDEO_STREAM
//		application.addTupleMapping("object_detector", "MOTION_VIDEO_STREAM", "DETECTED_OBJECT",
//				new FractionalSelectivity(0.05)); // 0.05 tuples of type MOTION_VIDEO_STREAM are emitted by Object
//													// Detector module per incoming tuple of type MOTION_VIDEO_STREAM
//
//		/*
//		 * Defining application loops (maybe incomplete loops) to monitor the latency
//		 * of. Here, we add two loops for monitoring : Motion Detector -> Object
//		 * Detector -> Object Tracker and Object Tracker -> PTZ Control
//		 */
//		final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
//			{
//				add("motion_detector");
//				add("object_detector");
//				add("object_tracker");
//			}
//		});
//		final AppLoop loop2 = new AppLoop(new ArrayList<String>() {
//			{
//				add("object_tracker");
//				add("PTZ_CONTROL");
//			}
//		});
//		List<AppLoop> loops = new ArrayList<AppLoop>() {
//			{
//				add(loop1);
//				add(loop2);
//			}
//		};
//
//		application.setLoops(loops);
//		return application;
//	}
}
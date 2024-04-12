package org.fog.placement;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SDFCNew.SimulateSDFC;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.MyApplication;
import org.fog.entities.MyActuator;
import org.fog.entities.MyFogDevice;
import org.fog.entities.MySensor;
import org.fog.test.perfeval.TestApplication;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

public class MyController extends SimEntity{
	
	public static boolean ONLY_CLOUD = false;
		
	private List<MyFogDevice> fogDevices;
	private List<MySensor> sensors;
	private List<MyActuator> actuators;
	
	private Map<String, MyApplication> applications;
	private Map<String, Integer> appLaunchDelays;

	private Map<String, MyModulePlacement> appModulePlacementPolicy;
	
	public MyController(String name, List<MyFogDevice> fogDevices, List<MySensor> sensors, List<MyActuator> actuators) {
		super(name);
		this.applications = new HashMap<String, MyApplication>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setAppModulePlacementPolicy(new HashMap<String, MyModulePlacement>());
		for(MyFogDevice fogDevice : fogDevices){
			fogDevice.setControllerId(getId());
		}
		setMyFogDevices(fogDevices);
		setMyActuators(actuators);
		setMySensors(sensors);
		connectWithLatencies();
	}

	private MyFogDevice getMyFogDeviceById(int id){
		for(MyFogDevice fogDevice : getMyFogDevices()){
			if(id==fogDevice.getId())
				return fogDevice;
		}
		return null;
	}
	
	private void connectWithLatencies(){
		for(MyFogDevice fogDevice : getMyFogDevices()){
			MyFogDevice parent = getMyFogDeviceById(fogDevice.getParentId());
			if(parent == null)
				continue;
			double latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());
		}
	}
	
	@Override
	public void startEntity() {
		for(String appId : applications.keySet()){
			if(getAppLaunchDelays().get(appId)==0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
		}

		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
		
		send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);
		
		for(MyFogDevice dev : getMyFogDevices())
			sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);

	}

	@Override
	public void processEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.TUPLE_FINISHED:
			processTupleFinished(ev);
			break;
		case FogEvents.CONTROLLER_RESOURCE_MANAGE:
			manageResources();
			break;
		case FogEvents.STOP_SIMULATION:
			CloudSim.stopSimulation();
			printResourceDetails();
			printTimeDetails();
			printPowerDetails();
			printCostDetails();
			printNetworkUsageDetails();
			printFailedVM();
			System.exit(0);
			break;
			
		}
	}

	private void printFailedVM() {

		float totalVM = SimulateSDFC.placedVM.size() + SimulateSDFC.totalVMFaile.size();
		float failed = SimulateSDFC.totalVMFaile.size() / totalVM;

		System.out.println("VM Failure rate = " + (failed * 100));
	}

	private void printResourceDetails() {
		System.out.println("=========================================");
		System.out.println("Original vs Available resource");
		System.out.println("=========================================");

		int ram = 0;
		int mips = 0;
		long bw = 0;
		long storage = 0;

		for (MyFogDevice mainCitizenFog: TestApplication.CFbyIDHealthStatusTable.values()){

			int aram = mainCitizenFog.getHost().getRamProvisioner().getAvailableRam();
			int amips = mainCitizenFog.getHost().getPeList().get(0).getMips() * mainCitizenFog.getHost().getPeList().size();
			long abw = mainCitizenFog.getHost().getBwProvisioner().getAvailableBw();
			long astorage = mainCitizenFog.getHost().getStorage();

		//	if (mainCitizenFog.getName().contains("g")) {

				System.out.println("Name: " + mainCitizenFog.getName());
				System.out.println("Available MIPS: " + mainCitizenFog.getMips());
				System.out.println("Available RAM: " + mainCitizenFog.getHost().getRamProvisioner().getAvailableRam());
				System.out.println("Available BW: " + mainCitizenFog.getHost().getBwProvisioner().getAvailableBw());
				System.out.println("Available Storage: " + mainCitizenFog.getHost().getStorage());
				System.out.println("------------------------------------------------");

		//	}


//			//if (aram>0){
//				ram += aram;
//			//}
//
//			//if (amips>0){
//				mips += amips;
//			//}
//
//			//if (abw>0){
//				bw += abw;
//			//}
//
//			//if (astorage>0){
//				storage += astorage;
//		//	}
//

		}

//		//System.out.println("CF Name: "+mainCitizenFog.getName());
//		System.out.println("Available MIPS: "+mips);
//		System.out.println("Available RAM: "+ram);
//		System.out.println("Available BW: "+bw);
//		System.out.println("Available Storage: "+storage);
//		System.out.println("-------------------------------------------------------------");

	}

//	private void printResourceDetails() {
//		System.out.println("=========================================");
//		System.out.println("Original vs Available resource");
//		System.out.println("=========================================");
//		for (MyFogDevice mainCitizenFog: TestApplication.fogDevices){
//
//			MyFogDevice availableCF = TestApplication.deviceById.get(mainCitizenFog.getId());
//
//			System.out.println("CF Name: "+mainCitizenFog.getName());
//			System.out.println("Total MIPS: "+mainCitizenFog.getMips()+"  Available MIPS: "+availableCF.getMips());
//			System.out.println("Total RAM: "+mainCitizenFog.getHost().getRamProvisioner().getAvailableRam()+"  Available RAM: "+availableCF.getHost().getRamProvisioner().getAvailableRam());
//			System.out.println("Total BW: "+mainCitizenFog.getHost().getBwProvisioner().getAvailableBw()+"  Available BW: "+availableCF.getHost().getBwProvisioner().getAvailableBw());
//			System.out.println("Total Storage: "+mainCitizenFog.getHost().getStorage()+"  Available Storage: "+availableCF.getHost().getStorage());
//			System.out.println("-------------------------------------------------------------");
//
//		}
//	}
	
	private void printNetworkUsageDetails() {
		System.out.println("Total network usage = "+NetworkUsageMonitor.getNetworkUsage()/Config.MAX_SIMULATION_TIME);		
	}

	private MyFogDevice getCloud(){
		for(MyFogDevice dev : getMyFogDevices())
			if(dev.getName().equals("cloud"))
				return dev;
		return null;
	}
	
	private void printCostDetails(){
		System.out.println("Cost of execution in cloud = "+getCloud().getTotalCost());
	}
	
	private void printPowerDetails() {
		double fogDeviceEnergyConsumption = 0.0;
		for (MyFogDevice fogDevice : getMyFogDevices()) {
			System.out.println(fogDevice.getName() + " : Energy Consumed = " + fogDevice.getEnergyConsumption());
			//fogDeviceEnergyConsumption += fogDevice.getEnergyConsumption();
		}
		//System.out.println("Total Energy Consumed by all CF = " + fogDeviceEnergyConsumption);
	}

	private String getStringForLoopId(int loopId){
		for(String appId : getMyApplications().keySet()){
			MyApplication app = getMyApplications().get(appId);
			for(AppLoop loop : app.getLoops()){
				if(loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}
	private void printTimeDetails() {
		System.out.println("=========================================");
		System.out.println("============== RESULTS ==================");
		System.out.println("Number of modules placed: "+TestApplication.placedNumberOfModule+" ==================");
		//System.out.println("============== RESULTS "+MainApplication.tupleEventCounter+" ==================");
		//System.out.println("============== RESULTS "+MainApplication.tupleCounter+" ==================");
		System.out.println("=========================================");
		System.out.println("EXECUTION TIME : "+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
		System.out.println("=========================================");
		System.out.println("APPLICATION LOOP DELAYS");
		System.out.println("=========================================");
		for(Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()){
		
			System.out.println(getStringForLoopId(loopId) + " ---> "+TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
		}
		System.out.println("=========================================");
		System.out.println("TUPLE CPU EXECUTION DELAY");
		System.out.println("=========================================");
		
		for(String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()){
			System.out.println(tupleType + " ---> "+TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
		}
		
		System.out.println("=========================================");
	}

	protected void manageResources(){
		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}
	
	private void processTupleFinished(SimEvent ev) {
	}
	
	@Override
	public void shutdownEntity() {	
	}
	
	public void submitApplication(MyApplication application, int delay, MyModulePlacement modulePlacement){
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getMyApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
		getAppModulePlacementPolicy().put(application.getAppId(), modulePlacement);
		
		for(MySensor sensor : sensors){
			sensor.setApp(getMyApplications().get(sensor.getAppId()));
		}
		for(MyActuator ac : actuators){
			ac.setApp(getMyApplications().get(ac.getAppId()));
		}
		
		for(AppEdge edge : application.getEdges()){
			if(edge.getEdgeType() == AppEdge.ACTUATOR){
				String moduleName = edge.getSource();
				for(MyActuator actuator : getMyActuators()){
					if(actuator.getMyActuatorType().equalsIgnoreCase(edge.getDestination()))
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(), edge.getTupleType());
				}
			}
		}	
	}
	
	public void submitApplication(MyApplication application, MyModulePlacement modulePlacement){
		submitApplication(application, 0, modulePlacement);
	}
	
	
	private void processAppSubmit(SimEvent ev){
		MyApplication app = (MyApplication) ev.getData();
		processAppSubmit(app);
	}
	
	private void processAppSubmit(MyApplication application){
		System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getMyApplications().put(application.getAppId(), application);
		
		MyModulePlacement modulePlacement = getAppModulePlacementPolicy().get(application.getAppId());
		for(MyFogDevice fogDevice : fogDevices){
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
		}
		
		Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
		
		
		for(Integer deviceId : deviceToModuleMap.keySet()){
			for(AppModule module : deviceToModuleMap.get(deviceId)){
				
				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				System.out.println(CloudSim.clock()+" Trying to Launch "+ module.getName() + " in "+getMyFogDeviceById(deviceId).getName());
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
			}
		}
		
	}

	public List<MyFogDevice> getMyFogDevices() {
		return fogDevices;
	}

	public void setMyFogDevices(List<MyFogDevice> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, MyApplication> getMyApplications() {
		return applications;
	}

	public void setMyApplications(Map<String, MyApplication> applications) {
		this.applications = applications;
	}

	public List<MySensor> getMySensors() {
		return sensors;
	}

	public void setMySensors(List<MySensor> sensors) {
		for(MySensor sensor : sensors)
			sensor.setControllerId(getId());
		this.sensors = sensors;
	}

	public List<MyActuator> getMyActuators() {
		return actuators;
	}

	public void setMyActuators(List<MyActuator> actuators) {
		this.actuators = actuators;
	}

	public Map<String, MyModulePlacement> getAppModulePlacementPolicy() {
		return appModulePlacementPolicy;
	}

	public void setAppModulePlacementPolicy(Map<String, MyModulePlacement> appModulePlacementPolicy) {
		this.appModulePlacementPolicy = appModulePlacementPolicy;
	}
}
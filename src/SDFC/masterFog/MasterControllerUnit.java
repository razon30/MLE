package SDFC.masterFog;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

import SDFC.application.Microservice;
import SDFC.application.SocialMediaApplication;
import SDFC.endDevice.ActuatorEndDevice;
import SDFC.endDevice.SensorEndDevice;

public class MasterControllerUnit extends SimEntity {

	public static boolean ONLY_CLOUD = false;

	private List<MasterComputingUnit> fogDevices;
	private List<SensorEndDevice> sensors;
	private List<ActuatorEndDevice> actuators;

	private Map<String, SocialMediaApplication> applications;
	private Map<String, Integer> appLaunchDelays;

	private Map<String, MasterPlacementUnit> appModulePlacementPolicy;

	public MasterControllerUnit(String name, List<MasterComputingUnit> fogDevices, List<SensorEndDevice> sensors,
			List<ActuatorEndDevice> actuators) {
		super(name);
		this.applications = new HashMap<String, SocialMediaApplication>();
		setAppLaunchDelays(new HashMap<String, Integer>());
		setAppModulePlacementPolicy(new HashMap<String, MasterPlacementUnit>());
		for (MasterComputingUnit fogDevice : fogDevices) {
			fogDevice.setControllerId(getId());
		}
		setMyFogDevices(fogDevices);
		setMyActuators(actuators);
		setMySensors(sensors);
		connectWithLatencies();
	}

	private MasterComputingUnit getMyFogDeviceById(int id) {
		for (MasterComputingUnit fogDevice : getMyFogDevices()) {
			if (id == fogDevice.getId())
				return fogDevice;
		}
		return null;
	}

	private void connectWithLatencies() {
		for (MasterComputingUnit fogDevice : getMyFogDevices()) {
			MasterComputingUnit parent = getMyFogDeviceById(fogDevice.getParentId());
			if (parent == null)
				continue;
			double latency = fogDevice.getUplinkLatency();
			parent.getChildToLatencyMap().put(fogDevice.getId(), latency);
			parent.getChildrenIds().add(fogDevice.getId());
		}
	}

	@Override
	public void startEntity() {
		for (String appId : applications.keySet()) {
			if (getAppLaunchDelays().get(appId) == 0)
				processAppSubmit(applications.get(appId));
			else
				send(getId(), getAppLaunchDelays().get(appId), FogEvents.APP_SUBMIT, applications.get(appId));
		}

		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);

		send(getId(), Config.MAX_SIMULATION_TIME, FogEvents.STOP_SIMULATION);

		for (MasterComputingUnit dev : getMyFogDevices())
			sendNow(dev.getId(), FogEvents.RESOURCE_MGMT);

	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
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
			printTimeDetails();
			printPowerDetails();
			printCostDetails();
			printNetworkUsageDetails();
			System.exit(0);
			break;

		}
	}

	private void printNetworkUsageDetails() {
		System.out
				.println("Total network usage = " + NetworkUsageMonitor.getNetworkUsage() / Config.MAX_SIMULATION_TIME);
	}

	private MasterComputingUnit getCloud() {
		for (MasterComputingUnit dev : getMyFogDevices())
			if (dev.getName().equals("cloud"))
				// if (dev.getName().equals(Constants.CITIZEN_FOG + "-2"))
				return dev;
		return null;
	}

	private void printCostDetails() {
		if (getCloud() == null) {
			System.out.println("Cost of execution in MainModule = Cloud wasn't required");
		} else {
			System.out.println("Cost of execution in MainModule = " + getCloud().getTotalCost());
		}
	}

	private void printPowerDetails() {
		for (MasterComputingUnit fogDevice : getMyFogDevices()) {
			System.out.println(fogDevice.getName() + " : Energy Consumed = " + fogDevice.getEnergyConsumption());
		}
	}

	private String getStringForLoopId(int loopId) {
		for (String appId : getMyApplications().keySet()) {
			SocialMediaApplication app = getMyApplications().get(appId);
			for (AppLoop loop : app.getLoops()) {
				if (loop.getLoopId() == loopId)
					return loop.getModules().toString();
			}
		}
		return null;
	}

	private void printTimeDetails() {
		System.out.println("=========================================");
		System.out.println("============== RESULTS ==================");
		System.out.println("=========================================");
		System.out.println("EXECUTION TIME : "
				+ (Calendar.getInstance().getTimeInMillis() - TimeKeeper.getInstance().getSimulationStartTime()));
		System.out.println("=========================================");
		System.out.println("APPLICATION LOOP DELAYS");
		System.out.println("=========================================");
		for (Integer loopId : TimeKeeper.getInstance().getLoopIdToTupleIds().keySet()) {

			System.out.println(getStringForLoopId(loopId) + " ---> "
					+ TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loopId));
		}
		System.out.println("=========================================");
		System.out.println("TUPLE CPU EXECUTION DELAY");
		System.out.println("=========================================");

		for (String tupleType : TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().keySet()) {
			System.out.println(
					tupleType + " ---> " + TimeKeeper.getInstance().getTupleTypeToAverageCpuTime().get(tupleType));
		}

		System.out.println("=========================================");
	}

	protected void manageResources() {
		send(getId(), Config.RESOURCE_MANAGE_INTERVAL, FogEvents.CONTROLLER_RESOURCE_MANAGE);
	}

	private void processTupleFinished(SimEvent ev) {

	}

	@Override
	public void shutdownEntity() {
	}

	public void submitApplication(SocialMediaApplication application, int delay, MasterPlacementUnit modulePlacement) {
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getMyApplications().put(application.getAppId(), application);
		getAppLaunchDelays().put(application.getAppId(), delay);
		getAppModulePlacementPolicy().put(application.getAppId(), modulePlacement);

		for (SensorEndDevice sensor : sensors) {
			sensor.setApp(getMyApplications().get(sensor.getAppId()));
		}
		for (ActuatorEndDevice ac : actuators) {
			ac.setApp(getMyApplications().get(ac.getAppId()));
		}

		for (AppEdge edge : application.getEdges()) {
			if (edge.getEdgeType() == AppEdge.ACTUATOR) {
				String moduleName = edge.getSource();
				for (ActuatorEndDevice actuator : getMyActuators()) {
					if (actuator.getMyActuatorType().equalsIgnoreCase(edge.getDestination()))
						application.getModuleByName(moduleName).subscribeActuator(actuator.getId(),
								edge.getTupleType());
				}
			}
		}
	}

	public void submitApplication(SocialMediaApplication application, MasterPlacementUnit modulePlacement) {
		submitApplication(application, 0, modulePlacement);
	}

	private void processAppSubmit(SimEvent ev) {
		SocialMediaApplication app = (SocialMediaApplication) ev.getData();
		processAppSubmit(app);
	}

	private void processAppSubmit(SocialMediaApplication application) {
		System.out.println(CloudSim.clock() + " Submitted application " + application.getAppId());
		FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
		getMyApplications().put(application.getAppId(), application);

		MasterPlacementUnit modulePlacement = getAppModulePlacementPolicy().get(application.getAppId());
		for (MasterComputingUnit fogDevice : fogDevices) {
			sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
		}

		Map<Integer, List<Microservice>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();

		for (Integer deviceId : deviceToModuleMap.keySet()) {
			for (Microservice module : deviceToModuleMap.get(deviceId)) {

				sendNow(deviceId, FogEvents.APP_SUBMIT, application);
				System.out.println(CloudSim.clock() + " Trying to Launch " + module.getName() + " in "
						+ getMyFogDeviceById(deviceId).getName());
				sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
			}
		}

	}

	public List<MasterComputingUnit> getMyFogDevices() {
		return fogDevices;
	}

	public void setMyFogDevices(List<MasterComputingUnit> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public Map<String, Integer> getAppLaunchDelays() {
		return appLaunchDelays;
	}

	public void setAppLaunchDelays(Map<String, Integer> appLaunchDelays) {
		this.appLaunchDelays = appLaunchDelays;
	}

	public Map<String, SocialMediaApplication> getMyApplications() {
		return applications;
	}

	public void setMyApplications(Map<String, SocialMediaApplication> applications) {
		this.applications = applications;
	}

	public List<SensorEndDevice> getMySensors() {
		return sensors;
	}

	public void setMySensors(List<SensorEndDevice> sensors) {
		for (SensorEndDevice sensor : sensors)
			sensor.setControllerId(getId());
		this.sensors = sensors;
	}

	public List<ActuatorEndDevice> getMyActuators() {
		return actuators;
	}

	public void setMyActuators(List<ActuatorEndDevice> actuators) {
		this.actuators = actuators;
	}

	public Map<String, MasterPlacementUnit> getAppModulePlacementPolicy() {
		return appModulePlacementPolicy;
	}

	public void setAppModulePlacementPolicy(Map<String, MasterPlacementUnit> appModulePlacementPolicy) {
		this.appModulePlacementPolicy = appModulePlacementPolicy;
	}
}
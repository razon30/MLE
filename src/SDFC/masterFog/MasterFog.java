package SDFC.masterFog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.fog.entities.FogBroker;
import org.fog.placement.ModuleMapping;

import SDFC.application.Microservice;
import SDFC.application.SocialMediaApplication;
import SDFC.endDevice.ActuatorEndDevice;
import SDFC.endDevice.SensorEndDevice;
import SDFC.utils.Constants;
import SDFC.utils.MicroserviceSorter;

/**
 * @author Razon
 *
 */
public class MasterFog {

	static List<MasterComputingUnit> fogDevices = new ArrayList<MasterComputingUnit>();
	static Map<Integer, MasterComputingUnit> deviceById = new HashMap<Integer, MasterComputingUnit>();
	static List<SensorEndDevice> sensors = new ArrayList<SensorEndDevice>();
	static List<ActuatorEndDevice> actuators = new ArrayList<ActuatorEndDevice>();
	static List<Integer> idOfEndDevices = new ArrayList<Integer>();
	static List<Microservice> modules = new ArrayList<Microservice>();
	static Map<Integer, List<Integer>> moduleDependencyMap = new HashMap<Integer, List<Integer>>();
	static Map<Integer, Integer> numberOfRequestToModuleMap = new HashMap<Integer, Integer>();
	static Map<String, List<MasterComputingUnit>> serviceTypeToCFMap = new HashMap<String, List<MasterComputingUnit>>();
	static Map<Integer, String> tupleToExecutingModuleNameMap = new HashMap<Integer, String>();
	static MasterComputingUnit gateWayDevice;
	static FogBroker broker;
	static SocialMediaApplication socialMediaApplication;
	static ModuleMapping moduleMapping;
	static MasterControllerUnit controller;
	static MasterPlacementUnit modulePlacement;

	public static void init() {
		moduleMapping = ModuleMapping.createModuleMapping();
		controller = new MasterControllerUnit(Constants.MASTER_CONTROLLER, fogDevices, sensors, actuators);
		modulePlacement = new MasterPlacementUnit(fogDevices, sensors, actuators, socialMediaApplication, moduleMapping,
				Constants.MAIN_MODULE);
		controller.submitApplication(socialMediaApplication, 0, modulePlacement);
	}

	public static void createGateway() {
		gateWayDevice = MasterComputingUnit.createFogDevice(Constants.MASTER_FOG, 2800, 4096, 10240, 10240, 1, 0.0,
				107.339, 83.433, 4096, 10240);
		gateWayDevice.setParentId(-1);
		gateWayDevice.setUplinkBandwidth(2);
		idOfEndDevices.add(gateWayDevice.getId());
		fogDevices.add(gateWayDevice);
		deviceById.put(gateWayDevice.getId(), gateWayDevice);

	}

	public static void MNH_ADD_CF(MasterComputingUnit citizenFog) {

		// citizenFog.setParentId(0);
		citizenFog.setParentId(-1);
		citizenFog.setUplinkLatency(4);
		fogDevices.add(citizenFog);
		deviceById.put(citizenFog.getId(), citizenFog);

	}

	public static void MNH_ADD_SENSOR(SensorEndDevice sensor) {

		sensor.setUserId(broker.getId());
		sensor.setGatewayDeviceId(gateWayDevice.getId());
		sensor.setLatency(6.0);

		sensors.add(sensor);

	}

	public static void MNH_ADD_ACTUATOR(ActuatorEndDevice actuator) {

		actuator.setUserId(broker.getId());
		actuator.setGatewayDeviceId(gateWayDevice.getId());
		actuator.setLatency(1.0);

		actuators.add(actuator);

	}

	public static void placeApplication(SocialMediaApplication application) {
		socialMediaApplication = application;
		socialMediaApplication.setUserId(broker.getId());
		addModules();
		addEdges();
	}

	private static void addModules() {
		Microservice gatewayModule = socialMediaApplication.addMicroservice(Constants.MAIN_MODULE, 2048, 2000, 1000,
				1024, Constants.TUPLE_TYPE_GATEWAY);
		modules.add(gatewayModule);
		numberOfRequestToModuleMap.put(gatewayModule.getId(), 0);

		Microservice authModule = socialMediaApplication.addMicroservice(Constants.AUTH_MODULE, 2048 * 2, 4000, 4000,
				1024 * 4, Constants.TUPLE_TYPE_AUTH);
		modules.add(authModule);
		numberOfRequestToModuleMap.put(authModule.getId(), 0);

		Microservice profileModule = socialMediaApplication.addMicroservice(Constants.PROFILE_MODULE, 2048 * 1, 4000,
				4000, 1024 * 3, Constants.TUPLE_TYPE_PROFILE);
		modules.add(profileModule);
		numberOfRequestToModuleMap.put(profileModule.getId(), 0);

		addDependency(authModule.getId(), profileModule.getId());
		addDependency(gatewayModule.getId(), authModule.getId());
		addDependency(gatewayModule.getId(), profileModule.getId());
	}

	private static void addDependency(Integer onDepend, Integer toDepend) {
		List<Integer> dependants = new ArrayList<Integer>();
		if (moduleDependencyMap.keySet().contains(onDepend)) {
			dependants = moduleDependencyMap.get(onDepend);
		}
		dependants.add(toDepend);
		moduleDependencyMap.put(onDepend, dependants);
	}

	private static void addEdges() {

	}

	public static List<MasterComputingUnit> getFogDevices() {
		return fogDevices;
	}

	public static void setFogDevices(List<MasterComputingUnit> fogDevices) {
		MasterFog.fogDevices = fogDevices;
	}

	public static Map<Integer, MasterComputingUnit> getDeviceById() {
		return deviceById;
	}

	public static void setDeviceById(Map<Integer, MasterComputingUnit> deviceById) {
		MasterFog.deviceById = deviceById;
	}

	public static List<SensorEndDevice> getSensors() {
		return sensors;
	}

	public static void setSensors(List<SensorEndDevice> sensors) {
		MasterFog.sensors = sensors;
	}

	public static List<ActuatorEndDevice> getActuators() {
		return actuators;
	}

	public static void setActuators(List<ActuatorEndDevice> actuators) {
		MasterFog.actuators = actuators;
	}

	public static List<Integer> getIdOfEndDevices() {
		return idOfEndDevices;
	}

	public static void setIdOfEndDevices(List<Integer> idOfEndDevices) {
		MasterFog.idOfEndDevices = idOfEndDevices;
	}

	public static List<Microservice> getModules() {
		return modules;
	}

	public static void setModules(List<Microservice> modules) {
		MasterFog.modules = modules;
	}

	public static Map<Integer, List<Integer>> getModuleDependencyMap() {
		return moduleDependencyMap;
	}

	public static void setModuleDependencyMap(Map<Integer, List<Integer>> moduleDependencyMap) {
		MasterFog.moduleDependencyMap = moduleDependencyMap;
	}

	public static MasterComputingUnit getGateWayDevice() {
		return gateWayDevice;
	}

	public static void setGateWayDevice(MasterComputingUnit gateWayDevice) {
		MasterFog.gateWayDevice = gateWayDevice;
	}

	public static FogBroker getBroker() {
		return broker;
	}

	public static Map<Integer, Integer> getNumberOfCallToModuleMap() {
		return numberOfRequestToModuleMap;
	}

	public static void setNumberOfCallToModuleMap(Map<Integer, Integer> numberOfCallToModuleMap) {
		MasterFog.numberOfRequestToModuleMap = numberOfCallToModuleMap;
	}

	public static void setBroker(FogBroker broker) {
		MasterFog.broker = broker;
	}

	public static SocialMediaApplication getSocialMediaApplication() {
		return socialMediaApplication;
	}

	public static void setSocialMediaApplication(SocialMediaApplication socialMediaApplication) {
		MasterFog.socialMediaApplication = socialMediaApplication;
	}

	public static ModuleMapping getModuleMapping() {
		return moduleMapping;
	}

	public static void setModuleMapping(ModuleMapping moduleMapping) {
		MasterFog.moduleMapping = moduleMapping;
	}

	public static MasterControllerUnit getController() {
		return controller;
	}

	public static void setController(MasterControllerUnit controller) {
		MasterFog.controller = controller;
	}

	public static MasterPlacementUnit getModulePlacement() {
		return modulePlacement;
	}

	public static void setModulePlacement(MasterPlacementUnit modulePlacement) {
		MasterFog.modulePlacement = modulePlacement;
	}

	public static Map<String, List<MasterComputingUnit>> getServiceTypeToCFMap() {
		return serviceTypeToCFMap;
	}

	public static void setServiceTypeToCFMap(Map<String, List<MasterComputingUnit>> serviceTypeToCFMap) {
		MasterFog.serviceTypeToCFMap = serviceTypeToCFMap;
	}

	public static Microservice getEligibleMicroservice(int cpuLength, int nwLength, long outputSize,
			String serviceType) {

		List<MasterComputingUnit> fogList = getServiceTypeToCFMap().get(serviceType);

		MasterComputingUnit selectedMasterComputingUnit = fogList.get(0);

		for (MasterComputingUnit masterComputingUnit : fogList) {

			int totalMips = 0;

			for (Host host : masterComputingUnit.getFogCharacteristics().getHostList()) {

				for (Pe pe : host.getPeList()) {

					totalMips += pe.getMips();

				}

			}

			if (cpuLength < totalMips) {
				selectedMasterComputingUnit = masterComputingUnit;
				gateWayDevice.setParentId(masterComputingUnit.getId());

				break;
			}

		}

		List<Microservice> microserviceList = modulePlacement.getDeviceToModuleMap()
				.get(selectedMasterComputingUnit.getId());
		Microservice[] microservices = new Microservice[microserviceList.size()];
		for (int i = 0; i < microserviceList.size(); i++) {
			microservices[i] = microserviceList.get(i);
		}
		MicroserviceSorter microserviceSorter = new MicroserviceSorter();
		List<Microservice> sortedMicroservices = microserviceSorter.mergerAndReturn(microservices,
				MasterFog.getModuleDependencyMap(), MasterFog.getNumberOfCallToModuleMap());
		Microservice selectedMicroservice = sortedMicroservices.get(0);

		if (selectedMicroservice.getMips() <= (cpuLength + nwLength + outputSize)) {
			selectedMicroservice.setMips(cpuLength + nwLength + outputSize);
		}

		gateWayDevice.setParentId(selectedMasterComputingUnit.getId());

		// Find the Microservice from the selectedMasterComputingUnit fog that has
		// higher MIPS than require. else provide extra mips to hioghest MS from CF.
		// Create appEdge fromMaster module to selectedModule, one to TupleType.Up and
		// another TupleType.Down
		// Create module mapping for
		// sensor-mainmodule-selectedmodule-mainmodule-actuator

		return selectedMicroservice;
	}

	public static Map<Integer, Integer> getNumberOfRequestToModuleMap() {
		return numberOfRequestToModuleMap;
	}

	public static void setNumberOfRequestToModuleMap(Map<Integer, Integer> numberOfRequestToModuleMap) {
		MasterFog.numberOfRequestToModuleMap = numberOfRequestToModuleMap;
	}

	public static Map<Integer, String> getTupleToExecutingModuleNameMap() {
		return tupleToExecutingModuleNameMap;
	}

	public static void setTupleToExecutingModuleNameMap(Map<Integer, String> tupleToExecutingModuleNameMap) {
		MasterFog.tupleToExecutingModuleNameMap = tupleToExecutingModuleNameMap;
	}

}

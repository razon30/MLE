package SDFC.masterFog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.placement.ModuleMapping;

import SDFC.application.Microservice;
import SDFC.application.SocialMediaApplication;
import SDFC.endDevice.ActuatorEndDevice;
import SDFC.endDevice.SensorEndDevice;
import SDFC.utils.Constants;
import SDFC.utils.FogSorter;
import SDFC.utils.MicroserviceSorter;

public class MasterPlacementUnit extends MasterPlacement {

	protected ModuleMapping moduleMapping;
	protected List<SensorEndDevice> sensors;
	protected List<ActuatorEndDevice> actuators;
	protected String moduleToPlace;
	protected Map<Integer, Integer> deviceMipsInfo;

	public MasterPlacementUnit(List<MasterComputingUnit> fogDevices, List<SensorEndDevice> sensors,
			List<ActuatorEndDevice> actuators, SocialMediaApplication application, ModuleMapping moduleMapping,
			String moduleToPlace) {
		this.setMasterComputingUnits(fogDevices);
		this.setMyApplication(application);
		this.setModuleMapping(moduleMapping);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<Microservice>>());
		setMySensors(sensors);
		setMyActuators(actuators);
		this.moduleToPlace = moduleToPlace;
		this.deviceMipsInfo = new HashMap<Integer, Integer>();
		mapModules();
	}

	@Override
	protected void mapModules() {

		setMasterComputingUnits(FogSorter.cfScheduler(getMasterComputingUnits()));

		List<Microservice> microserviceList = getMyApplication().getModules();
		Microservice[] microservices = new Microservice[microserviceList.size()];
		for (int i = 0; i < microserviceList.size(); i++) {
			microservices[i] = microserviceList.get(i);
		}
		MicroserviceSorter microserviceSorter = new MicroserviceSorter();
		List<Microservice> sortedMicroservices = microserviceSorter.mergerAndReturn(microservices,
				MasterFog.getModuleDependencyMap(), MasterFog.getNumberOfCallToModuleMap());
		getMyApplication().setModules(sortedMicroservices);

		// Setting the MainModule/MainMicroservice/Gateway into MAsterfog/GatewayFog
//		List<Microservice> placedModules = new ArrayList<Microservice>();
//		placedModules.add(appModule);
//		getDeviceToModuleMap().put(deviceId, placedModules);

		List<Integer> placedModule = new ArrayList<Integer>();

		for (MasterComputingUnit device : getMasterComputingUnits()) {
			String deviceName = device.getName();
			int deviceId = CloudSim.getEntityId(deviceName);
			for (Microservice microservice : getMyApplication().getModules()) {
				String microServiceName = microservice.getName();

				if (deviceName.equalsIgnoreCase(Constants.MASTER_FOG)
						&& microServiceName.equalsIgnoreCase(Constants.MAIN_MODULE)) {
					List<Microservice> placedModules = new ArrayList<Microservice>();
					placedModules.add(microservice);
					getDeviceToModuleMap().put(deviceId, placedModules);
					getModuleMapping().addModuleToDevice(microServiceName, deviceName);

					List<MasterComputingUnit> deviceList = new ArrayList<MasterComputingUnit>();
					deviceList.add(device);
					MasterFog.getServiceTypeToCFMap().put(microservice.getServiceType(), deviceList);

				} else {

					if (!deviceName.equalsIgnoreCase(Constants.MASTER_FOG)
							&& !microServiceName.equalsIgnoreCase(Constants.MAIN_MODULE)) {
						if (!getDeviceToModuleMap().containsKey(device.getId())
								&& !placedModule.contains(microservice.getId())) {
							List<Microservice> placedModules = new ArrayList<Microservice>();
							placedModules.add(microservice);
							getDeviceToModuleMap().put(deviceId, placedModules);
							getModuleMapping().addModuleToDevice(microServiceName, deviceName);
							placedModule.add(microservice.getId());

							List<MasterComputingUnit> deviceList = new ArrayList<MasterComputingUnit>();
							deviceList.add(device);
							MasterFog.getServiceTypeToCFMap().put(microservice.getServiceType(), deviceList);

						}
					}

				}

			}

		}

		/*
		 * ToDo: 1. Sort the CFs 2. Sort the modules 3. Assigning modules to the CFs 4.
		 * Adding corresponding i. AppEdges ii. TupleMapping iii. Apploop 5. Set data
		 * from 4 to application 6. Add Parent to gateway fog.
		 */

		/*
		 * for (String deviceName : getModuleMapping().getModuleMapping().keySet()) {
		 * for (String moduleName :
		 * getModuleMapping().getModuleMapping().get(deviceName)) { int deviceId =
		 * CloudSim.getEntityId(deviceName); Microservice appModule =
		 * getMyApplication().getModuleByName(moduleName); if
		 * (!getDeviceToModuleMap().containsKey(deviceId)) { List<Microservice>
		 * placedModules = new ArrayList<Microservice>(); placedModules.add(appModule);
		 * getDeviceToModuleMap().put(deviceId, placedModules);
		 * 
		 * } else { List<Microservice> placedModules =
		 * getDeviceToModuleMap().get(deviceId); placedModules.add(appModule);
		 * getDeviceToModuleMap().put(deviceId, placedModules); } } }
		 * 
		 * for (MasterComputingUnit device : getMasterComputingUnits()) { int
		 * deviceParent = -1; List<Integer> children = new ArrayList<Integer>();
		 * 
		 * if (device.getLevel() == 1) { if
		 * (!deviceMipsInfo.containsKey(device.getId()))
		 * deviceMipsInfo.put(device.getId(), 0); deviceParent = device.getParentId();
		 * for (MasterComputingUnit deviceChild : getMasterComputingUnits()) { if
		 * (deviceChild.getParentId() == device.getId()) {
		 * children.add(deviceChild.getId()); } }
		 * 
		 * Map<Integer, Double> childDeadline = new HashMap<Integer, Double>(); for (int
		 * childId : children) childDeadline.put(childId,
		 * getMyApplication().getDeadlineInfo().get(childId).get(moduleToPlace));
		 * 
		 * List<Integer> keys = new ArrayList<Integer>(childDeadline.keySet());
		 * 
		 * for (int i = 0; i < keys.size() - 1; i++) { for (int j = 0; j < keys.size() -
		 * i - 1; j++) { if (childDeadline.get(keys.get(j)) >
		 * childDeadline.get(keys.get(j + 1))) { int tempJ = keys.get(j); int tempJn =
		 * keys.get(j + 1); keys.set(j, tempJn); keys.set(j + 1, tempJ); } } }
		 * 
		 * int baseMipsOfPlacingModule = (int)
		 * getMyApplication().getModuleByName(moduleToPlace).getMips(); for (int key :
		 * keys) { int currentMips = deviceMipsInfo.get(device.getId()); Microservice
		 * appModule = getMyApplication().getModuleByName(moduleToPlace); int
		 * additionalMips =
		 * getMyApplication().getAdditionalMipsInfo().get(key).get(moduleToPlace); if
		 * (currentMips + baseMipsOfPlacingModule + additionalMips < device.getMips()) {
		 * currentMips = currentMips + baseMipsOfPlacingModule + additionalMips;
		 * deviceMipsInfo.put(device.getId(), currentMips); if
		 * (!getDeviceToModuleMap().containsKey(device.getId())) { List<Microservice>
		 * placedModules = new ArrayList<Microservice>(); placedModules.add(appModule);
		 * getDeviceToModuleMap().put(device.getId(), placedModules);
		 * 
		 * } else { List<Microservice> placedModules =
		 * getDeviceToModuleMap().get(device.getId()); placedModules.add(appModule);
		 * getDeviceToModuleMap().put(device.getId(), placedModules); } } else {
		 * List<Microservice> placedModules = getDeviceToModuleMap().get(deviceParent);
		 * placedModules.add(appModule); getDeviceToModuleMap().put(deviceParent,
		 * placedModules); } }
		 * 
		 * }
		 * 
		 * }
		 */

	}

	public ModuleMapping getModuleMapping() {
		return moduleMapping;
	}

	public void setModuleMapping(ModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	public List<SensorEndDevice> getMySensors() {
		return sensors;
	}

	public void setMySensors(List<SensorEndDevice> sensors) {
		this.sensors = sensors;
	}

	public List<ActuatorEndDevice> getMyActuators() {
		return actuators;
	}

	public void setMyActuators(List<ActuatorEndDevice> actuators) {
		this.actuators = actuators;
	}

}

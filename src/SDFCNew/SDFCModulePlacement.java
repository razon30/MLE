package SDFCNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MSDFC.GeneralPurposeFog;
import MSDFC.MicroserviceContainer;
import org.fog.application.AppModule;

public class SDFCModulePlacement extends SDFCPlacement {

	protected SDFCModuleMapping moduleMapping;
	protected List<SDFCSensor> sensors;
	protected List<SDFCActuator> actuators;
	protected String moduleToPlace;
	protected Map<Integer, Integer> deviceMipsInfo;

	protected Map<Integer, Map<String, Double>> currentModuleLoadMap;
	protected Map<Integer, Map<String, Integer>> currentModuleInstanceNum;

	public SDFCModulePlacement(List<SDFCFogDevice> fogDevices, List<SDFCSensor> sensors, List<SDFCActuator> actuators,
			SDFCApplication application, SDFCModuleMapping moduleMapping, String moduleToPlace) {
		this.setSDFCFogDevices(fogDevices);
		this.setSDFCAplication(application);
		this.setSDFCModuleMapping(moduleMapping);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		setSDFCSensors(sensors);
		setSDFCActuators(actuators);
		this.moduleToPlace = moduleToPlace;
		this.deviceMipsInfo = new HashMap<Integer, Integer>();
		mapModules();
	}

	public SDFCModulePlacement(List<SDFCFogDevice> fogDevices, List<SDFCSensor> sensors, List<SDFCActuator> actuators,
			SDFCApplication application, SDFCModuleMapping moduleMapping) {
		this.setSDFCFogDevices(fogDevices);
		this.setSDFCAplication(application);
		this.setSDFCModuleMapping(moduleMapping);
		this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
		this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
		setSDFCSensors(sensors);
		setSDFCActuators(actuators);
		this.deviceMipsInfo = new HashMap<Integer, Integer>();
		mapModules();
	}

	@Override
	protected void mapModules() {

		setSDFCFogDevices(CFSorter.cfScheduler(getSDFCFogDevices()));
		List<AppModule> moduleNames = getSDFCApplication().getModules();// getModuleToDeviceMap().keySet();//
		MNH.sortedDevices.addAll(getSDFCFogDevices());

		/**
		 * Following code places each module in each device.
		 */
		createModuleInstanceOnDevice(getSDFCApplication().getModuleByName(Constant.MASTER_MODULE),
				getDeviceByName(Constant.MASTER_FOG + "0"));

		MNH.updatedDeviceToModule.put(Constant.MASTER_FOG + "0", new ArrayList<String>());
		MNH.updatedDeviceToModule.get(Constant.MASTER_FOG + "0").add(Constant.MASTER_MODULE);

		for (int i = 0; i < moduleNames.size(); i++) {
			AppModule module = moduleNames.get(i);
			if (module.getName().equalsIgnoreCase(Constant.MASTER_MODULE)) {
				return;
			}

			for (SDFCFogDevice device : getSDFCFogDevices()) {

				// if (!device.getName().equalsIgnoreCase(Constant.MASTER_FOG + "0")) {

				if (!MNH.updatedDeviceToModule.containsKey(device.getName())) {
					if (createModuleInstanceOnDevice(module, device)) {
						MNH.updatedDeviceToModule.put(device.getName(), new ArrayList<String>());
						MNH.updatedDeviceToModule.get(device.getName()).add(module.getName());
						updateResourceOfCF(device.getId(), module);
						break;
					}

				}

				// }

			}

		}


	}


	public void updateResourceOfCF(int id, AppModule container) {

		SDFCFogDevice citizenFog = SimulateSDFC.CFbyIDHealthStatusTable.get(id);

//        System.out.println("==================================================================");
//        System.out.println("Before Compare CF Name: " + citizenFog.getName() + " Module name: " + container.getModule().getName());
//        System.out.println(citizenFog.getMips() + " " + container.getModule().getMips());
//        System.out.println(citizenFog.getHost().getRamProvisioner().getAvailableRam() + " " + container.getModule().getRam());
//        System.out.println(citizenFog.getHost().getBwProvisioner().getAvailableBw() + " " + container.getModule().getBw());
//        System.out.println(citizenFog.getHost().getStorage() + " " + container.getModule().getSize());
//        System.out.println("==================================================================");

		citizenFog.setMips((int) (citizenFog.getMips() - container.getMips()));
		citizenFog.getHost().getRamProvisioner().setAvailableRam(
				citizenFog.getHost().getRamProvisioner().getAvailableRam() - container.getRam());
		citizenFog.getHost().getBwProvisioner().setAvailableBw(
				citizenFog.getHost().getBwProvisioner().getAvailableBw() - container.getBw());
		citizenFog.getHost().setStorage(
				citizenFog.getHost().getStorage() - container.getSize());
		SimulateSDFC.CFbyIDHealthStatusTable.put(citizenFog.getId(), citizenFog);
		//setPlacedMSIDbyCFID(citizenFog.getId(), container.getId());

	}


	public SDFCModuleMapping getSDFCModuleMapping() {
		return moduleMapping;
	}

	public void setSDFCModuleMapping(SDFCModuleMapping moduleMapping) {
		this.moduleMapping = moduleMapping;
	}

	public List<SDFCSensor> getSDFCSensors() {
		return sensors;
	}

	public void setSDFCSensors(List<SDFCSensor> sensors) {
		this.sensors = sensors;
	}

	public List<SDFCActuator> getSDFCActuators() {
		return actuators;
	}

	public void setSDFCActuators(List<SDFCActuator> actuators) {
		this.actuators = actuators;
	}

}

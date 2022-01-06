package SDFC.masterFog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;

import SDFC.application.Microservice;
import SDFC.application.SocialMediaApplication;

public abstract class MasterPlacement {

	public static int ONLY_CLOUD = 1;
	public static int EDGEWARDS = 2;
	public static int USER_MAPPING = 3;

	private List<MasterComputingUnit> fogDevices;
	private SocialMediaApplication application;
	private Map<String, List<Integer>> moduleToDeviceMap;
	private Map<Integer, List<Microservice>> deviceToModuleMap;
	private Map<Integer, Map<String, Integer>> moduleInstanceCountMap;

	protected abstract void mapModules();

	protected boolean canBeCreated(MasterComputingUnit fogDevice, Microservice module) {
		return fogDevice.getVmAllocationPolicy().allocateHostForVm(module);
	}

	protected int getParentDevice(int fogDeviceId) {
		return ((MasterComputingUnit) CloudSim.getEntity(fogDeviceId)).getParentId();
	}

	protected MasterComputingUnit getMasterComputingUnitById(int fogDeviceId) {
		return (MasterComputingUnit) CloudSim.getEntity(fogDeviceId);
	}

	protected boolean createModuleInstanceOnDevice(Microservice _module, final MasterComputingUnit device,
			int instanceCount) {
		return false;
	}

	protected boolean createModuleInstanceOnDevice(Microservice _module, final MasterComputingUnit device) {
		Microservice module = null;
		if (getModuleToDeviceMap().containsKey(_module.getName()))
			module = new Microservice(_module);
		else
			module = _module;

		if (canBeCreated(device, module)) {
			System.out.println("Creating " + module.getName() + " on device " + device.getName());

			if (!getDeviceToModuleMap().containsKey(device.getId()))
				getDeviceToModuleMap().put(device.getId(), new ArrayList<Microservice>());
			getDeviceToModuleMap().get(device.getId()).add(module);

			if (!getModuleToDeviceMap().containsKey(module.getName()))
				getModuleToDeviceMap().put(module.getName(), new ArrayList<Integer>());
			getModuleToDeviceMap().get(module.getName()).add(device.getId());
			return true;
		} else {
			System.err.println("Module " + module.getName() + " cannot be created on device " + device.getName());
			System.err.println("Terminating");
			return false;
		}
	}

	protected MasterComputingUnit getDeviceByName(String deviceName) {
		for (MasterComputingUnit dev : getMasterComputingUnits()) {
			if (dev.getName().equals(deviceName))
				return dev;
		}
		return null;
	}

	protected MasterComputingUnit getDeviceById(int id) {
		for (MasterComputingUnit dev : getMasterComputingUnits()) {
			if (dev.getId() == id)
				return dev;
		}
		return null;
	}

	public List<MasterComputingUnit> getMasterComputingUnits() {
		return fogDevices;
	}

	public void setMasterComputingUnits(List<MasterComputingUnit> fogDevices) {
		this.fogDevices = fogDevices;
	}

	public SocialMediaApplication getMyApplication() {
		return application;
	}

	public void setMyApplication(SocialMediaApplication application) {
		this.application = application;
	}

	public Map<String, List<Integer>> getModuleToDeviceMap() {
		return moduleToDeviceMap;
	}

	public void setModuleToDeviceMap(Map<String, List<Integer>> moduleToDeviceMap) {
		this.moduleToDeviceMap = moduleToDeviceMap;
	}

	public Map<Integer, List<Microservice>> getDeviceToModuleMap() {
		return deviceToModuleMap;
	}

	public void setDeviceToModuleMap(Map<Integer, List<Microservice>> deviceToModuleMap) {
		this.deviceToModuleMap = deviceToModuleMap;
	}

	public Map<Integer, Map<String, Integer>> getModuleInstanceCountMap() {
		return moduleInstanceCountMap;
	}

	public void setModuleInstanceCountMap(Map<Integer, Map<String, Integer>> moduleInstanceCountMap) {
		this.moduleInstanceCountMap = moduleInstanceCountMap;
	}

}
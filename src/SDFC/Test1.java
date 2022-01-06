package SDFC;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import SDFC.application.SocialMediaApplication;
import SDFC.endDevice.ActuatorEndDevice;
import SDFC.endDevice.SensorEndDevice;
import SDFC.masterFog.MasterComputingUnit;
import SDFC.masterFog.MasterFog;
import SDFC.utils.Constants;
import SDFC.utils.Utils;

public class Test1 {

	public static void main(String[] args) {

		Log.printLine("Starting Social ...");

		try {

			Log.enable();
			Logger.ENABLED = true;
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;
			CloudSim.init(num_user, calendar, trace_flag);

			MasterFog.createGateway();

			MasterFog.setBroker(new FogBroker(Constants.SOCIAL_MEDIA_BROKER_NAME));
			MasterFog.MNH_ADD_SENSOR(createSensor(Constants.AUTH_SENSOR_NAME + "-1", 5));
			MasterFog.MNH_ADD_ACTUATOR(createActuator(Constants.ACTUATOR + "-1"));

			MasterComputingUnit citizenFogDevice1 = createFogDevice(Constants.CITIZEN_FOG + "-1", 3200, 1024 * 3, 10240,
					540, 2, 0, 87.53, 82.44, 1000000, 10000);

			MasterComputingUnit citizenFogDevice2 = createFogDevice(Constants.CITIZEN_FOG + "-2", 4000, 1024 * 5, 10000,
					270, 2, 0, 187.53, 182.44, 1000000, 10000);

			MasterFog.MNH_ADD_CF(citizenFogDevice1);
			MasterFog.MNH_ADD_CF(citizenFogDevice2);

			MasterFog.placeApplication(SocialMediaApplication.createApplication(Constants.SOCIAL_MEDIA_APP_ID));

			MasterFog.init();

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("TestApplication finished!");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.printLine("Unwanted errors happen " + e.getLocalizedMessage());
		}

	}

	public static ActuatorEndDevice createActuator(String name) {

		ActuatorEndDevice actuator = new ActuatorEndDevice(name, Constants.SOCIAL_MEDIA_APP_ID,
				Constants.ACTUATOR_TUPLE_TYPE);

		return actuator;

	}

	public static SensorEndDevice createSensor(String name, double sensingInterval) {
		SensorEndDevice sensor = new SensorEndDevice(name, Constants.SENSOR_TUPLE_TYPE, Constants.SOCIAL_MEDIA_APP_ID,
				new DeterministicDistribution(sensingInterval));
		return sensor;
	}

	public static MasterComputingUnit createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw,
			int level, double ratePerMips, double busyPower, double idlePower, int storage, int bw) {
		List<Pe> peList = new ArrayList<Pe>();

		int numOfPe = Utils.getValue(1, 5);

		for (int i = 0; i < numOfPe; i++) {
			peList.add(new Pe(i, new PeProvisionerOverbooking(mips)));
		}

		int hostId = FogUtils.generateEntityId();

		PowerHost host = new PowerHost(hostId, new RamProvisionerSimple(ram), new BwProvisionerOverbooking(bw), storage,
				peList, new StreamOperatorScheduler(peList), new FogLinearPowerModel(busyPower, idlePower));
		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch, os, vmm, host, time_zone, cost,
				costPerMem, costPerStorage, costPerBw);

		MasterComputingUnit fogdevice = null;
		try {
			fogdevice = new MasterComputingUnit(nodeName, characteristics, new AppModuleAllocationPolicy(hostList),
					storageList, 5, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fogdevice.setLevel(level);
		fogdevice.setMips((int) mips);
		return fogdevice;
	}

}

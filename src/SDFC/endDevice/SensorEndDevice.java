package SDFC.endDevice;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoLocation;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.Distribution;

import SDFC.application.Microservice;
import SDFC.application.SocialMediaApplication;
import SDFC.masterFog.MasterFog;
import SDFC.utils.Constants;

public class SensorEndDevice extends SimEntity {

	private int gatewayDeviceId;
	private GeoLocation geoLocation;
	private long outputSize;
	private String appId;
	private int userId;
	private String tupleType;
	private String sensorName;
	private String destModuleName;
	private Distribution transmitDistribution;
	private int controllerId;
	private double latency;
	private SocialMediaApplication app;

	public SensorEndDevice(String name, int userId, String appId, int gatewayDeviceId, double latency,
			GeoLocation geoLocation, Distribution transmitDistribution, int cpuLength, int nwLength, String tupleType,
			String destModuleName) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setDestModuleName(destModuleName);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
	}

	public SensorEndDevice(String name, int userId, String appId, int gatewayDeviceId, double latency,
			GeoLocation geoLocation, Distribution transmitDistribution, String tupleType) {
		super(name);
		this.setAppId(appId);
		this.gatewayDeviceId = gatewayDeviceId;
		this.geoLocation = geoLocation;
		this.outputSize = 3;
		this.setTransmitDistribution(transmitDistribution);
		setUserId(userId);
		setTupleType(tupleType);
		setSensorName(sensorName);
		setLatency(latency);
	}

	/**
	 * This constructor is called from the code that generates PhysicalTopology from
	 * JSON
	 * 
	 * @param name
	 * @param tupleType
	 * @param string
	 * @param userId
	 * @param appId
	 * @param transmitDistribution
	 */
	public SensorEndDevice(String name, String tupleType, int userId, String appId, Distribution transmitDistribution) {
		super(name);
		this.setAppId(appId);
		this.setTransmitDistribution(transmitDistribution);
		setTupleType(tupleType);
		setSensorName(tupleType);
		setUserId(userId);
	}

	public SensorEndDevice(String name, String tupleType, String appId, Distribution transmitDistribution) {
		super(name);
		this.setAppId(appId);
		this.setTransmitDistribution(transmitDistribution);
		setTupleType(tupleType);
		setSensorName(tupleType);
		// setUserId(userId);
	}

	public void transmit(Tuple tuple) {

		// Can Work in HERE to module Dynamically

		AppEdge _edge = null;
		for (AppEdge edge : getApp().getEdges()) {
			if (edge.getSource().equals(getTupleType()))
				_edge = edge;
		}
		// long cpuLength = (long) _edge.getTupleCpuLength();
		// long nwLength = (long) _edge.getTupleNwLength();

		tuple.setUserId(getUserId());

		tuple.setDestModuleName(_edge.getDestination());
		tuple.setSrcModuleName(getSensorName());
		Logger.debug(getName(), "Sending tuple with tupleId = " + tuple.getCloudletId());

		int actualTupleId = updateTimings(getSensorName(), tuple.getDestModuleName());
		tuple.setActualTupleId(actualTupleId);

		send(gatewayDeviceId, getLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
	}

	private int updateTimings(String src, String dest) {
		SocialMediaApplication application = getApp();
		for (AppLoop loop : application.getLoops()) {
			if (loop.hasEdge(src, dest)) {

				int tupleId = TimeKeeper.getInstance().getUniqueId();
				if (!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
					TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
				TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
				TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
				return tupleId;
			}
		}
		return -1;
	}

	@Override
	public void startEntity() {
		send(gatewayDeviceId, CloudSim.getMinTimeBetweenEvents(), FogEvents.SENSOR_JOINED, geoLocation);
		send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
	}

	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		case FogEvents.TUPLE_ACK:
			// transmit(transmitDistribution.getNextValue());
			break;
		case FogEvents.EMIT_TUPLE:

			int cpuLength = 3000;
			int nwLength = 500;
			int peNumber = 1;

			Tuple tuple = new Tuple(getAppId(), FogUtils.generateTupleId(), Tuple.UP, cpuLength, peNumber, nwLength,
					outputSize, new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());

			if (tuple.getCloudletId() > 20) {
				return;
			}

			tuple.setTupleType(Constants.TUPLE_TYPE_AUTH);

			Microservice executingModule = MasterFog.getEligibleMicroservice(cpuLength, nwLength, outputSize,
					Constants.TUPLE_TYPE_AUTH);

			// Resetting the edges
			// getApp().getEdges().clear();

			if (!MasterFog.getTupleToExecutingModuleNameMap().containsKey(tuple.getCloudletId())) {
				MasterFog.getTupleToExecutingModuleNameMap().put(tuple.getCloudletId(), executingModule.getName());
			}

			getApp().addAppEdge(Constants.SENSOR_TUPLE_TYPE, Constants.MAIN_MODULE, 100, 200,
					Constants.SENSOR_TUPLE_TYPE, Tuple.UP, AppEdge.SENSOR);

			getApp().addAppEdge(Constants.MAIN_MODULE, executingModule.getName(), 6000, 600, Constants.TUPLE_TYPE_AUTH,
					Tuple.UP, AppEdge.SENSOR);

			getApp().addAppEdge(executingModule.getName(), Constants.ACTUATOR_TUPLE_TYPE, 100, 50,
					Constants.ACTUATOR_TUPLE_TYPE, Tuple.DOWN, AppEdge.ACTUATOR);

			// Resetting the Tuple Mapping
			getApp().addTupleMapping(Constants.MAIN_MODULE, Constants.SENSOR_TUPLE_TYPE, Constants.TUPLE_TYPE_AUTH,
					new FractionalSelectivity(1.0));
			getApp().addTupleMapping(executingModule.getName(), Constants.TUPLE_TYPE_AUTH,
					Constants.ACTUATOR_TUPLE_TYPE, new FractionalSelectivity(1.0));

			final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
				{
					// add(getName());
					add(Constants.SENSOR_TUPLE_TYPE);
					add(Constants.MAIN_MODULE);
					add(executingModule.getName());
					// add(Constants.MAIN_MODULE);
					add(Constants.ACTUATOR_TUPLE_TYPE);
				}
			});
			List<AppLoop> loops = new ArrayList<AppLoop>() {
				{
					add(loop1);
				}
			};
			getApp().setLoops(loops);

			for (AppEdge edge : getApp().getEdges()) {
				if (edge.getEdgeType() == AppEdge.ACTUATOR) {
					String moduleName = edge.getSource();
					for (ActuatorEndDevice actuator : MasterFog.getActuators()) {
						if (actuator.getMyActuatorType().equalsIgnoreCase(edge.getDestination()))
							getApp().getModuleByName(moduleName).subscribeActuator(actuator.getId(),
									edge.getTupleType());
					}
				}
			}

			transmit(tuple);
			send(getId(), getTransmitDistribution().getNextValue(), FogEvents.EMIT_TUPLE);
			break;
		}

	}

	@Override
	public void shutdownEntity() {

	}

	public int getGatewayDeviceId() {
		return gatewayDeviceId;
	}

	public void setGatewayDeviceId(int gatewayDeviceId) {
		this.gatewayDeviceId = gatewayDeviceId;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTupleType() {
		return tupleType;
	}

	public void setTupleType(String tupleType) {
		this.tupleType = tupleType;
	}

	public String getSensorName() {
		return sensorName;
	}

	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getDestModuleName() {
		return destModuleName;
	}

	public void setDestModuleName(String destModuleName) {
		this.destModuleName = destModuleName;
	}

	public Distribution getTransmitDistribution() {
		return transmitDistribution;
	}

	public void setTransmitDistribution(Distribution transmitDistribution) {
		this.transmitDistribution = transmitDistribution;
	}

	public int getControllerId() {
		return controllerId;
	}

	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}

	public SocialMediaApplication getApp() {
		return app;
	}

	public void setApp(SocialMediaApplication app) {
		this.app = app;
	}

	public Double getLatency() {
		return latency;
	}

	public void setLatency(Double latency) {
		this.latency = latency;
	}

}
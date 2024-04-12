package MLE;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import SDFC.utils.Utils;
import org.apache.commons.math3.util.Pair;
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
import org.fog.application.AppModule;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.placement.ModuleMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

public class MainApplication {
    public static int numberOfTuplePerEvent = 3;
    public static int tupleEventCounter = 0;
    public static int tupleCounter = 0;
    public static int tupleReCounter = 0;
    public static MasterFog masterFog;
    public static AppModule masterAppModule;
    public static CloudDevice cloudDevice;
    public static AppModule cloudAppModule;
    public static List<GeneralPurposeFog> fogDevices = new ArrayList<GeneralPurposeFog>();
    static Map<Integer, GeneralPurposeFog> deviceById = new HashMap<Integer, GeneralPurposeFog>();
    static List<SDFCSensor> sensors = new ArrayList<SDFCSensor>();
    static List<SDFCActuator> actuators = new ArrayList<SDFCActuator>();
    //static List<Integer> idOfEndDevices = new ArrayList<Integer>();
    static Map<Integer, Map<String, Double>> deadlineInfo = new HashMap<Integer, Map<String, Double>>();
    static Map<Integer, Map<String, Integer>> additionalMipsInfo = new HashMap<Integer, Map<String, Integer>>();

    static boolean CLOUD = false;

    static int numOfCitizenFogs = 5;
    static int numOfEndDevPerGateway = 2;
    static int numberOfMS = 5;
    static double sensingInterval = 5;

    public static double maxTupleNumber = 500;

    public static void main(String[] args) {

        Log.printLine("Starting TestApplication...");

        try {
            Log.disable();
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;
            CloudSim.init(num_user, calendar, trace_flag);
            String appId = "test_app";
            FogBroker broker = new FogBroker("broker");

            createFogDevices(broker.getId(), appId);

            SDFCApplication application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
            moduleMapping.addModuleToDevice(Constant.CLOUD_MODULE, Constant.CLOUD);


            masterFog.CFbyIDHealthStatusTable.putAll(masterFog.getCFbyID()); //masterFog.getCFbyID();
            masterFog.startPreProcessingCFandMS();

//            for (int i = 0; i < fogDevices.size(); i++) {
//                GeneralPurposeFog fogDevice = fogDevices.get(i);
//
//            }


            SDFCController controller = new SDFCController("master-controller", fogDevices, sensors, actuators);
            controller.submitApplication(application, 0,
                    new MicroservicePlacement(fogDevices, sensors, actuators, application, moduleMapping, "mainModule"));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("TestApplication finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    private static double getvalue(double min, double max) {
        Random r = new Random();
        double randomValue = min + (max - min) * r.nextDouble();
        return randomValue;
    }

    private static int getvalue(int min, int max) {
        Random r = new Random();
        int randomValue = min + r.nextInt() % (max - min);
        return randomValue;
    }

    private static void createFogDevices(int userId, String appId) {

        long storage = 1000000;
        int bw = 10000;

        GeneralPurposeFog cloud = createFogDevice(Constant.CLOUD, 44800, 40000, 100, 10000,
                0, 0.01, 16 * 103, 16 * 83.25, storage, bw);
        cloud.setParentId(-1);
        cloudAppModule = createAppModule(appId, userId, Constant.CLOUD_MODULE, 40000, 44800, storage, bw);
        try {
            cloudDevice = new CloudDevice(cloud);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // cloud.mod

        long mips = Utils.getValue(1024 * 3, 1024 * 5);
        int rams = Utils.getValue(1024 * 3, 1024 * 5);
        long upBW = Utils.getValue(1024 * 6, 1024 * 8);
        long dwnBW = Utils.getValue(1024 * 6, 1024 * 8);
        storage = Utils.getValue(1024 * 100 * 8, 1024 * 100 * 10); // 1000000; // host storage
        bw = Utils.getValue(1024 * 8, 1024 * 11);

        masterAppModule = createAppModule(appId, userId, Constant.MASTER_MODULE, 10, 1000, 1000, 100);
        MicroserviceContainer masterMicroserviceContainer = new MicroserviceContainer(masterAppModule, -1);
        GeneralPurposeFog masterGeneralPurposeFog = createFogDevice(Constant.MASTER_FOG, mips, rams, upBW, dwnBW, 1, 0.0, 107.339, 83.4333,
                storage, bw);
        try {
            masterFog = new MasterFog(masterMicroserviceContainer, masterGeneralPurposeFog);
        } catch (Exception e) {
            e.printStackTrace();
        }

        masterFog.setUplinkLatency(150);
        fogDevices.add(cloudDevice);
        fogDevices.add(masterFog);
        deviceById.put(cloud.getId(), cloudDevice);
        deviceById.put(masterFog.getId(), masterFog);

        for (int i = 0; i < numOfCitizenFogs; i++) {
            addCF(i + "", userId, appId, masterFog.getId());
        }

    }

    private static void addCF(String gwPartialName, int userId, String appId, int parentId) {

        long mips = Utils.getValue(1024 * 3, 1024 * 5);
        int rams = Utils.getValue(1024 * 3, 1024 * 5);
        long upBW = Utils.getValue(1024 * 6, 1024 * 8);
        long dwnBW = Utils.getValue(1024 * 6, 1024 * 8);
        long storage = Utils.getValue(1024 * 100 * 8, 1024 * 100 * 10); // 1000000; // host storage
        int bw = Utils.getValue(1024 * 8, 1024 * 11);

        GeneralPurposeFog cf = createFogDevice(Constant.CITIZEN_FOG + "-" + gwPartialName, mips, rams, upBW, dwnBW,
                2, 0.0, 107.339, 83.4333,
                storage, bw);
        fogDevices.add(cf);
        deviceById.put(cf.getId(), cf);
        cf.setParentId(parentId);
        cf.setUplinkLatency(5);


        double throughput = maxTupleNumber;//200;
        SDFCSensor sensor = new SDFCSensor(Constant.SENSOR + "-" + Constant.CITIZEN_FOG + "-" + gwPartialName,
                Constant.SENSOR, userId, appId,
                new DeterministicDistribution(1000 / (throughput / 9 * 10))); // inter-transmission time of EEG sensor follows a
        // deterministic distribution
        sensors.add(sensor);
        sensor.setGatewayDeviceId(cf.getId());
        sensor.setLatency(6.0); // latency of connection between EEG sensors and the parent Smartphone is 6 ms

        SDFCActuator actuator = new SDFCActuator(Constant.ACTUATOR + "-" + Constant.CITIZEN_FOG + "-" + gwPartialName, userId, appId, Constant.ACTUATOR);
        actuators.add(actuator);
        actuator.setGatewayDeviceId(cf.getId());
        actuator.setLatency(1.0);

        masterFog.setCFbyID(cf.getId(), cf);


//        for (int i = 0; i < numOfEndDevPerGateway; i++) {
//            String endPartialName = gwPartialName + "-" + i;
//            GeneralPurposeFog end = addEnd(endPartialName, userId, appId, cf.getId());
//            end.setUplinkLatency(2);
//            fogDevices.add(end);
//            deviceById.put(end.getId(), end);
//        }

    }

    private static GeneralPurposeFog addEnd(String endPartialName, int userId, String appId, int parentId) {

        long storage = 1000000;
        int bw = 10000;

        GeneralPurposeFog end = createFogDevice("e-" + endPartialName, 3200, 1000, 10000, 270, 2, 0, 87.53, 82.44, storage, bw);
        end.setParentId(parentId);
        // idOfEndDevices.add(end.getId());

        SDFCSensor sensor = new SDFCSensor("s-" + endPartialName, "IoTSensor", userId, appId,
                new DeterministicDistribution(sensingInterval)); // inter-transmission time of EEG sensor follows a
        // deterministic distribution
        sensors.add(sensor);
        sensor.setGatewayDeviceId(end.getId());
        sensor.setLatency(6.0); // latency of connection between EEG sensors and the parent Smartphone is 6 ms

        SDFCActuator actuator = new SDFCActuator("a-" + endPartialName, userId, appId, "IoTActuator");
        actuators.add(actuator);
        actuator.setGatewayDeviceId(end.getId());
        actuator.setLatency(1.0); // latency of connection between Display actuator and the parent Smartphone is 1
        // ms

        return end;
    }

    private static GeneralPurposeFog createFogDevice(String nodeName, long mips, int ram,
                                                     long upBw, long downBw, int level,
                                                     double ratePerMips,
                                                     double busyPower, double idlePower,
                                                     long storage, int bw) {
        List<Pe> peList = new ArrayList<Pe>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));
        int hostId = FogUtils.generateEntityId();


        PowerHost host = new PowerHost(hostId, new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw), storage,
                peList, new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower));
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
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch, os, vmm, host,
                time_zone, cost,
                costPerMem, costPerStorage, costPerBw);

        GeneralPurposeFog fogdevice = null;
        try {
            fogdevice = new GeneralPurposeFog(nodeName, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList,
                    5, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);
        fogdevice.setMips((int) mips);
        return fogdevice;
    }

    public static AppModule createAppModule(String appId, int userId, String moduleName, int ram, int mips, long size, long bw) {
        String vmm = "Xen";
        AppModule module = new AppModule(FogUtils.generateEntityId(), moduleName, appId, userId, mips, ram, bw, size,
                vmm, new TupleScheduler(mips, 1), new HashMap<Pair<String, String>, SelectivityModel>());

        return module;
    }

    @SuppressWarnings({"serial"})
    private static SDFCApplication createApplication(String appId, int userId) {

        /*
        TODO:
        1. Create the modules.
        2. Prepare the placement map
        3. According to placement, create application appedges dynamically.
         */


        List<AppLoop> loops = new ArrayList<>();
        SDFCApplication application = SDFCApplication.createApplication(appId, userId);

        application.getModules().add(cloudAppModule);
        application.getModules().add(masterAppModule);

        for (int i = 0; i < numberOfMS; i++) {

            int mips = Utils.getValue(512, 512 * 3);
            int rams = Utils.getValue(10, 10 * 3);
            long size = Utils.getValue(512, 512 * 3);
            int bw = Utils.getValue(128, 128 * 3);

            AppModule appModule = createAppModule(appId, userId, Constant.CITIZEN_MODULE + i, rams, mips, size, bw);
            application.getModules().add(appModule);

            application.addAppEdge(Constant.SENSOR, Constant.CITIZEN_MODULE + i, 100, 200, Constant.SENSOR, Tuple.UP, AppEdge.SENSOR);
            application.addAppEdge(Constant.CITIZEN_MODULE + i, Constant.ACTUATOR, 100, 50, Constant.RESPONSE, Tuple.DOWN, AppEdge.ACTUATOR);
            application.addAppEdge(Constant.CITIZEN_MODULE + i, Constant.MASTER_MODULE, 6000, 600, Constant.RAWDATA, Tuple.UP, AppEdge.MODULE);
            application.addAppEdge(Constant.MASTER_MODULE, Constant.CITIZEN_MODULE + i, 1000, 300, Constant.RAWDATA, Tuple.DOWN, AppEdge.MODULE);
            application.addAppEdge(Constant.CLOUD_MODULE, Constant.CITIZEN_MODULE + i, 100, 50, Constant.RESPONSE, Tuple.DOWN, AppEdge.MODULE);


            application.addTupleMapping(Constant.CITIZEN_MODULE + i, Constant.SENSOR, Constant.RAWDATA, new FractionalSelectivity(1.0));
            application.addTupleMapping(Constant.CITIZEN_MODULE + i, Constant.RAWDATA, Constant.RAWDATA, new FractionalSelectivity(1.0));
            application.addTupleMapping(Constant.CITIZEN_MODULE + i, Constant.RAWDATA, Constant.RESPONSE, new FractionalSelectivity(1.0));
            application.addTupleMapping(Constant.CITIZEN_MODULE + i, Constant.RESPONSE, Constant.RESPONSE, new FractionalSelectivity(1.0));
            application.addTupleMapping(Constant.CITIZEN_MODULE + i, Constant.SENSOR, Constant.RESPONSE, new FractionalSelectivity(1.0));


            int finalI = i;
            AppLoop loop1 = new AppLoop(new ArrayList<String>() {
                {
                    add(Constant.SENSOR);
                    add(Constant.CITIZEN_MODULE + finalI);
                    add(Constant.MASTER_MODULE);
                    add(Constant.CLOUD_MODULE);
                    add(Constant.CITIZEN_MODULE + finalI);
                    add(Constant.ACTUATOR);
                }
            });

            AppLoop loop2 = new AppLoop(new ArrayList<String>() {
                {
                    add(Constant.SENSOR);
                    add(Constant.CITIZEN_MODULE + finalI);
                    add(Constant.MASTER_MODULE);
                    add(Constant.CITIZEN_MODULE + finalI);
                    add(Constant.ACTUATOR);
                }
            });

            AppLoop loop3 = new AppLoop(new ArrayList<String>() {
                {
                    add(Constant.SENSOR);
                    add(Constant.CITIZEN_MODULE + finalI);
                    add(Constant.ACTUATOR);
                }
            });

            loops.add(loop3);
            loops.add(loop2);
            loops.add(loop1);


            MicroserviceContainer microserviceContainer = new MicroserviceContainer(appModule, i);

            microserviceContainer.setPriority(new Random().nextInt(3));

            int conditionalDependsOnMSNumber = Utils.getValue(0, numberOfMS - 1);
            for (int j = 0; j < conditionalDependsOnMSNumber; j++) {
                int id = Utils.getValue(0, numberOfMS - 1);
                if (!microserviceContainer.getConditionalDependsOnMSList().contains(id) && id != i) {
                    microserviceContainer.setConditionalDependsOnMSList(id);
                }

            }
            int dependantMSNumber = Utils.getValue(0, numberOfMS - 1);
            for (int j = 0; j < dependantMSNumber; j++) {
                int id = Utils.getValue(0, numberOfMS - 1);
                if (!microserviceContainer.getConditionalDependsOnMSList().contains(id)
                        && !microserviceContainer.getDependantMSList().contains(id) && id != i) {
                    microserviceContainer.setDependantMSList(id);
                }

            }
            int obviouslyDependsOnMSNumber = Utils.getValue(0, numberOfMS - 1);
            for (int j = 0; j < obviouslyDependsOnMSNumber; j++) {
                int id = Utils.getValue(0, numberOfMS - 1);
                if (!microserviceContainer.getConditionalDependsOnMSList().contains(id)
                        && !microserviceContainer.getDependantMSList().contains(id)
                        && !microserviceContainer.getObviouslyDependsOnMSList().contains(id)
                        && id != i
                       // && !masterFog.isAlreadyNotRelated(id, i)
                ) {
                    microserviceContainer.setObviouslyDependsOnMSList(id);
                }
                //String moduleName = Constant.CITIZEN_MODULE+id;
            }

            masterFog.setMSbyID(i, microserviceContainer);

        }


//        application.addAppEdge(Constant.SENSOR, Constant.CITIZEN_MODULE, 100, 200, Constant.SENSOR, Tuple.UP, AppEdge.SENSOR);
//        application.addAppEdge(Constant.CITIZEN_MODULE, Constant.MASTER_MODULE, 6000, 600, Constant.RAWDATA, Tuple.UP, AppEdge.MODULE);
        application.addAppEdge(Constant.MASTER_MODULE, Constant.CLOUD_MODULE, 1000, 300, Constant.RAWDATA, Tuple.UP, AppEdge.MODULE);
//        application.addAppEdge(Constant.MASTER_MODULE, Constant.CITIZEN_MODULE, 1000, 300, Constant.RAWDATA, Tuple.DOWN, AppEdge.MODULE);
//        application.addAppEdge(Constant.CLOUD_MODULE, Constant.CITIZEN_MODULE, 100, 50, Constant.RESPONSE, Tuple.DOWN, AppEdge.MODULE);
//        application.addAppEdge(Constant.CITIZEN_MODULE, Constant.ACTUATOR, 100, 50, Constant.RESPONSE, Tuple.DOWN, AppEdge.ACTUATOR);

//        application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.SENSOR, Constant.RAWDATA, new FractionalSelectivity(1.0));
//        application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.RAWDATA, Constant.RAWDATA, new FractionalSelectivity(1.0));
        application.addTupleMapping(Constant.MASTER_MODULE, Constant.RAWDATA, Constant.RAWDATA, new FractionalSelectivity(1.0));
        application.addTupleMapping(Constant.CLOUD_MODULE, Constant.RAWDATA, Constant.RESPONSE, new FractionalSelectivity(1.0));
//        application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.RAWDATA, Constant.RESPONSE, new FractionalSelectivity(1.0));
//        application.addTupleMapping(Constant.CITIZEN_MODULE, Constant.RESPONSE, Constant.RESPONSE, new FractionalSelectivity(1.0));


//        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {
//            {
//                add(Constant.SENSOR);
//                add(Constant.CITIZEN_MODULE);
//                add(Constant.MASTER_MODULE);
//                add(Constant.CLOUD_MODULE);
//                add(Constant.CITIZEN_MODULE);
//                add(Constant.ACTUATOR);
//            }
//        });
//
//        final AppLoop loop2 = new AppLoop(new ArrayList<String>() {
//            {
//                add(Constant.SENSOR);
//                add(Constant.CITIZEN_MODULE);
//                add(Constant.MASTER_MODULE);
//                add(Constant.CITIZEN_MODULE);
//                add(Constant.ACTUATOR);
//            }
//        });
//
//        final AppLoop loop3 = new AppLoop(new ArrayList<String>() {
//            {
//                add(Constant.SENSOR);
//                add(Constant.CITIZEN_MODULE);
//                add(Constant.ACTUATOR);
//            }
//        });
//
//        List<AppLoop> loops = new ArrayList<AppLoop>() {
//            {
//                add(loop1);
//                add(loop2);
//                add(loop3);
//            }
//        };

     //   System.out.println("Number of loo " + loops.size());
        application.setLoops(loops);

        return application;
    }
}
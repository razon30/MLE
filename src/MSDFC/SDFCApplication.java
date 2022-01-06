package MSDFC;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Tuple;
import org.fog.scheduler.TupleScheduler;
import org.fog.utils.FogUtils;
import org.fog.utils.GeoCoverage;

/**
 * Class represents an application in the Distributed Dataflow Model.
 *
 * @author Harshit Gupta
 */
public class SDFCApplication {

    private String appId;
    private int userId;
    private GeoCoverage geoCoverage;

    /**
     * List of application modules in the application
     */
    private List<AppModule> modules;

    /**
     * List of application edges in the application
     */
    private List<AppEdge> edges;

    /**
     * List of application loops to monitor for delay
     */
    private List<AppLoop> loops;

    private Map<String, AppEdge> edgeMap;

    /**
     * Creates a plain vanilla application with no modules and edges.
     *
     * @param appId
     * @param userId
     * @return
     */
    public static SDFCApplication createApplication(String appId, int userId) {
        return new SDFCApplication(appId, userId);
    }

    public static SDFCApplication createApplication(String appId) {
        return new SDFCApplication(appId);
    }

    /**
     * Adds an application module to the application.
     *
     * @param moduleName
     * @param ram
     */

    /**
     * Adds a non-periodic edge to the application model.
     *
     * @param source
     * @param destination
     * @param tupleCpuLength
     * @param tupleNwLength
     * @param tupleType
     * @param direction
     * @param edgeType
     */
    public void addAppEdge(String source, String destination, double tupleCpuLength, double tupleNwLength,
                           String tupleType, int direction, int edgeType) {
        AppEdge edge = new AppEdge(source, destination, tupleCpuLength, tupleNwLength, tupleType, direction, edgeType);
        getEdges().add(edge);
        getEdgeMap().put(edge.getTupleType(), edge);
    }

    /**
     * Adds a periodic edge to the application model.
     *
     * @param source
     * @param destination
     * @param tupleCpuLength
     * @param tupleNwLength
     * @param tupleType
     * @param direction
     * @param edgeType
     */
    public void addAppEdge(String source, String destination, double periodicity, double tupleCpuLength,
                           double tupleNwLength, String tupleType, int direction, int edgeType) {
        AppEdge edge = new AppEdge(source, destination, periodicity, tupleCpuLength, tupleNwLength, tupleType,
                direction, edgeType);
        getEdges().add(edge);
        getEdgeMap().put(edge.getTupleType(), edge);
    }

    /**
     * Define the input-output relationship of an application module for a given
     * input tuple type.
     *
     * @param moduleName       Name of the module
     * @param inputTupleType   Type of tuples carried by the incoming edge
     * @param outputTupleType  Type of tuples carried by the output edge
     * @param selectivityModel Selectivity model governing the relation between the
     *                         incoming and outgoing edge
     */
    public void addTupleMapping(String moduleName, String inputTupleType, String outputTupleType,
                                SelectivityModel selectivityModel) {
        AppModule module = getModuleByName(moduleName);
        module.getSelectivityMap().put(new Pair<String, String>(inputTupleType, outputTupleType), selectivityModel);
    }

    /**
     * Get a list of all periodic edges in the application.
     *
     * @param srcModule
     * @return
     */
    public List<AppEdge> getPeriodicEdges(String srcModule) {
        List<AppEdge> result = new ArrayList<AppEdge>();
        for (AppEdge edge : edges) {
            if (edge.isPeriodic() && edge.getSource().equals(srcModule))
                result.add(edge);
        }
        return result;
    }

    public SDFCApplication(String appId, int userId) {
        setAppId(appId);
        setUserId(userId);
        setModules(new ArrayList<AppModule>());
        setEdges(new ArrayList<AppEdge>());
        setGeoCoverage(null);
        setLoops(new ArrayList<AppLoop>());
        setEdgeMap(new HashMap<String, AppEdge>());
    }

    public SDFCApplication(String appId) {
        setAppId(appId);
        // setUserId(userId);
        setModules(new ArrayList<AppModule>());
        setEdges(new ArrayList<AppEdge>());
        setGeoCoverage(null);
        setLoops(new ArrayList<AppLoop>());
        setEdgeMap(new HashMap<String, AppEdge>());
    }

    public SDFCApplication(String appId, List<AppModule> modules, List<AppEdge> edges, List<AppLoop> loops,
                           GeoCoverage geoCoverage) {
        setAppId(appId);
        setModules(modules);
        setEdges(edges);
        setGeoCoverage(geoCoverage);
        setLoops(loops);
        setEdgeMap(new HashMap<String, AppEdge>());
        for (AppEdge edge : edges) {
            getEdgeMap().put(edge.getTupleType(), edge);
        }
    }

    /**
     * Search and return an application module by its module name
     *
     * @param name the module name to be returned
     * @return
     */
    public AppModule getModuleByName(String name) {
        for (AppModule module : modules) {
            if (module.getName().equals(name))
                return module;
        }
        return null;
    }

    /**
     * Get the tuples generated upon execution of incoming tuple <i>inputTuple</i>
     * by module named <i>moduleName</i>
     *
     * @param moduleName     name of the module performing execution of incoming
     *                       tuple and emitting resultant tuples
     * @param inputTuple     incoming tuple, whose execution creates resultant
     *                       tuples
     * @param sourceDeviceId
     * @return
     */
    public List<Tuple> getResultantTuples(String moduleName, Tuple inputTuple, int sourceDeviceId, int sourceModuleId) {
        List<Tuple> tuples = new ArrayList<Tuple>();
        AppModule destModule = getModuleByName(moduleName);
        AppModule srcModule = getModuleByName(inputTuple.getSrcModuleName());

        if (inputTuple.getSrcModuleName().contains(Constant.CITIZEN_MODULE)) {

           // if (inputTuple.getTupleType().contains(srcModule.getName()) || inputTuple.getTupleType().contains(destModule.getName())){}


            int srcContainerId = Integer.parseInt(inputTuple.getSrcModuleName().replace(Constant.CITIZEN_MODULE, ""));
            MicroserviceContainer container = MainApplication.masterFog.getMSbyID().get(srcContainerId);

            List<Integer> relatedModuleIDs = container.getObviouslyDependsOnMSList();

            if (inputTuple.getTupleType().equals("feed") || inputTuple.getTupleType().equals("feed-")) {
                AppEdge edge = new AppEdge(inputTuple.getSrcModuleName(), Constant.ACTUATOR, 100, 50, Constant.RESPONSE, Tuple.DOWN
                        , AppEdge.ACTUATOR);
                getEdges().add(edge);
                getEdgeMap().put(edge.getTupleType(), edge);
                Tuple tuple = getTupleForActuator(inputTuple, sourceDeviceId, sourceModuleId, edge);
                tuples.add(tuple);
            } else if (inputTuple.getTupleType().contains("feed")) {
                String[] destinationList = inputTuple.getTupleType().split("-");
                String desModuleName = destinationList[destinationList.length - 1];
                String newInputTupleType = inputTuple.getTupleType().replace("-" + desModuleName, "");
                String newOutputTupleType = "feed";

                if (destinationList.length > 2) {
                    String furtherDestModuleName = destinationList[destinationList.length - 2];
                    newOutputTupleType = newInputTupleType.replace("-" + furtherDestModuleName, "");
                }

                AppEdge edge = new AppEdge(inputTuple.getSrcModuleName(), desModuleName, 100, 50, newInputTupleType, Tuple.DOWN
                        , AppEdge.MODULE);
                getEdges().add(edge);
                getEdgeMap().put(edge.getTupleType(), edge);
                addTupleMapping(desModuleName, newInputTupleType, newOutputTupleType, new FractionalSelectivity(1.0));

                Tuple tuple = getTupleForOtherCF(inputTuple, sourceModuleId, edge);
                tuples.add(tuple);

            } else if (relatedModuleIDs.size() > 0) {
                for (int id : relatedModuleIDs) {
                    String destModuleName = Constant.CITIZEN_MODULE + id;
                    MicroserviceContainer destContainer = MainApplication.masterFog.getMSbyID().get(id);
                    if (inputTuple.getTupleType().contains("dep") &&
                            (!inputTuple.getTupleType().contains(destModuleName)) &&
                            (!inputTuple.getTupleType().contains(srcModule.getName()))) {

                        String newInputTupleType = inputTuple.getTupleType() + "-" + srcModule.getName();
                        String newOutputTupleType = "";

                        if (destContainer.getObviouslyDependsOnMSList().size() == 0) {
                            newOutputTupleType = inputTuple.getTupleType().replace("dep", "feed");//.replace(src);
                        } else {
                            newOutputTupleType = newInputTupleType + "-" + destModuleName;
                        }

                        AppEdge edge = new AppEdge(inputTuple.getSrcModuleName(), destModuleName, 100, 50, newInputTupleType, Tuple.DOWN
                                , AppEdge.MODULE);
                        getEdges().add(edge);
                        getEdgeMap().put(edge.getTupleType(), edge);
                        addTupleMapping(destModuleName, newInputTupleType, newOutputTupleType, new FractionalSelectivity(1.0));

                        Tuple tuple = getTupleForOtherCF(inputTuple, sourceModuleId, edge);
//                        if (tuple.getTupleType().equals("dep")) {
//                            System.out.println("No related module with dep: " + inputTuple.getTupleType());
//                        }
                        tuples.add(tuple);
                    } else if (!inputTuple.getTupleType().contains("dep") &&
                            (!inputTuple.getTupleType().contains(destModuleName)) &&
                            (!inputTuple.getTupleType().contains(srcModule.getName()))) {
                        String newInputTupleType = "dep-" + srcModule.getName();
                        String newOutputTupleType = newInputTupleType + "-" + destModuleName;
                        AppEdge edge = new AppEdge(inputTuple.getSrcModuleName(), destModuleName, 100, 50, newInputTupleType, Tuple.DOWN
                                , AppEdge.MODULE);
                        getEdges().add(edge);
                        getEdgeMap().put(edge.getTupleType(), edge);

                        addTupleMapping(destModuleName, newInputTupleType, newOutputTupleType, new FractionalSelectivity(1.0));

                        Tuple tuple = getTupleForOtherCF(inputTuple, sourceModuleId, edge);
//
//                        if (tuple.getTupleType().equals("dep")) {
//                            System.out.println("No related module with dep: " + inputTuple.getTupleType());
//                        }

                        tuples.add(tuple);
                    }

                }

            } else if (inputTuple.getTupleType().contains("dep")) {

                String[] destinationList = inputTuple.getTupleType().split("-");
               // System.out.println("No related module with dep: " + inputTuple.getTupleType());
                String desModuleName = destinationList[destinationList.length - 1];

                String newInputTupleType = inputTuple.getTupleType().replace("-" + desModuleName, "");
                String newOutputTupleType = "feed";

                if (destinationList.length > 2) {
                    String furtherDestModuleName = destinationList[destinationList.length - 2];
                    newOutputTupleType = newInputTupleType.replace("-" + furtherDestModuleName, "");
                } else {
                    newInputTupleType = Constant.RAWDATA;
                    newOutputTupleType = Constant.RAWDATA;
                }

                AppEdge edge = new AppEdge(inputTuple.getSrcModuleName(), desModuleName, 100, 50, newInputTupleType, Tuple.DOWN
                        , AppEdge.MODULE);
                getEdges().add(edge);
                getEdgeMap().put(edge.getTupleType(), edge);
                addTupleMapping(desModuleName, newInputTupleType, newOutputTupleType, new FractionalSelectivity(1.0));

                Tuple tuple = getTupleForOtherCF(inputTuple, sourceModuleId, edge);
//                if (tuple.getTupleType().equals("dep")) {
//                    System.out.println("No related module with dep: " + inputTuple.getTupleType());
//                }
                tuples.add(tuple);

            }
//                else {
//                    String newInputTupleType = "dep-" + srcModule.getName();
//                    String newOutputTupleType = newInputTupleType + "-" + destModule.getName();
//                    AppEdge edge = new AppEdge(inputTuple.getSrcModuleName(), destModule.getName(), 100, 50, newInputTupleType, Tuple.DOWN
//                            , AppEdge.MODULE);
//                    getEdges().add(edge);
//                    getEdgeMap().put(edge.getTupleType(), edge);
//                    addTupleMapping(destModule.getName(), newInputTupleType, newOutputTupleType, new FractionalSelectivity(1.0));
//
//                    Tuple tuple = getTupleForOtherCF(inputTuple, sourceModuleId, edge);
//                    if (tuple.getTupleType().equals("dep")){
//                        System.out.println("No related module with dep: "+inputTuple.getTupleType());
//                    }
//                    tuples.add(tuple);
//                }


        } else {


            for (AppEdge edge : getEdges()) {
                if (edge.getSource().equals(moduleName)) {
                    Pair<String, String> pair = new Pair<String, String>(inputTuple.getTupleType(), edge.getTupleType());

                    if (destModule.getSelectivityMap().get(pair) == null)
                        continue;
                    SelectivityModel selectivityModel = destModule.getSelectivityMap().get(pair);
                    if (selectivityModel.canSelect()) {
                        // TODO check if the edge is ACTUATOR, then create multiple tuples
                        if (edge.getEdgeType() == AppEdge.ACTUATOR) {
                            // for(Integer actuatorId :
                            // module.getActuatorSubscriptions().get(edge.getTupleType())){
                            Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),
                                    (long) (edge.getTupleCpuLength()), inputTuple.getNumberOfPes(),
                                    (long) (edge.getTupleNwLength()), inputTuple.getCloudletOutputSize(),
                                    inputTuple.getUtilizationModelCpu(), inputTuple.getUtilizationModelRam(),
                                    inputTuple.getUtilizationModelBw());
                            tuple.setActualTupleId(inputTuple.getActualTupleId());
                            tuple.setUserId(inputTuple.getUserId());
                            tuple.setAppId(inputTuple.getAppId());
                            tuple.setDestModuleName(edge.getDestination());
                            tuple.setSrcModuleName(edge.getSource());
                            tuple.setDirection(Tuple.ACTUATOR);
                            tuple.setTupleType(edge.getTupleType());
                            tuple.setSourceDeviceId(sourceDeviceId);
                            tuple.setSourceModuleId(sourceModuleId);
                            // tuple.setActuatorId(actuatorId);

                            tuples.add(tuple);
                            // }
                        } else {


                            Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),
                                    (long) (edge.getTupleCpuLength()), inputTuple.getNumberOfPes(),
                                    (long) (edge.getTupleNwLength()), inputTuple.getCloudletOutputSize(),
                                    inputTuple.getUtilizationModelCpu(), inputTuple.getUtilizationModelRam(),
                                    inputTuple.getUtilizationModelBw());
                            tuple.setActualTupleId(inputTuple.getActualTupleId());
                            tuple.setUserId(inputTuple.getUserId());
                            tuple.setAppId(inputTuple.getAppId());
                            tuple.setDestModuleName(edge.getDestination());
                            tuple.setSrcModuleName(edge.getSource());
                            tuple.setDirection(edge.getDirection());
                            tuple.setTupleType(edge.getTupleType());
                            tuple.setSourceModuleId(sourceModuleId);

                            tuples.add(tuple);
                        }
                    }
                }
            }

        }




        return tuples;
    }

    private Tuple getTupleForOtherCF(Tuple inputTuple, int sourceModuleId, AppEdge edge) {

        Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),
                (long) (edge.getTupleCpuLength()), inputTuple.getNumberOfPes(),
                (long) (edge.getTupleNwLength()), inputTuple.getCloudletOutputSize(),
                inputTuple.getUtilizationModelCpu(), inputTuple.getUtilizationModelRam(),
                inputTuple.getUtilizationModelBw());
        tuple.setActualTupleId(inputTuple.getActualTupleId());
        tuple.setUserId(inputTuple.getUserId());
        tuple.setAppId(inputTuple.getAppId());
        tuple.setDestModuleName(edge.getDestination());
        tuple.setSrcModuleName(edge.getSource());
        tuple.setDirection(edge.getDirection());
        tuple.setTupleType(edge.getTupleType());
        tuple.setSourceModuleId(sourceModuleId);

        return tuple;
    }

    private Tuple getTupleForActuator(Tuple inputTuple, int sourceDeviceId, int sourceModuleId, AppEdge edge) {
        Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), AppEdge.ACTUATOR,
                (long) (edge.getTupleCpuLength()), inputTuple.getNumberOfPes(),
                (long) (edge.getTupleNwLength()), inputTuple.getCloudletOutputSize(),
                inputTuple.getUtilizationModelCpu(), inputTuple.getUtilizationModelRam(),
                inputTuple.getUtilizationModelBw());
        tuple.setActualTupleId(inputTuple.getActualTupleId());
        tuple.setUserId(inputTuple.getUserId());
        tuple.setAppId(inputTuple.getAppId());
        tuple.setDestModuleName(edge.getDestination());
        tuple.setSrcModuleName(edge.getSource());
        tuple.setDirection(Tuple.ACTUATOR);
        tuple.setTupleType(edge.getTupleType());
        tuple.setSourceDeviceId(sourceDeviceId);
        tuple.setSourceModuleId(sourceModuleId);

        return tuple;
    }

    /**
     * Create a tuple for a given application edge
     *
     * @param edge
     * @param sourceDeviceId
     * @return
     */
    public Tuple createTuple(AppEdge edge, int sourceDeviceId, int sourceModuleId) {
        AppModule module = getModuleByName(edge.getSource());
        if (edge.getEdgeType() == AppEdge.ACTUATOR) {
            for (Integer actuatorId : module.getActuatorSubscriptions().get(edge.getTupleType())) {
                Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),
                        (long) (edge.getTupleCpuLength()), 1, (long) (edge.getTupleNwLength()), 100,
                        new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
                tuple.setUserId(getUserId());
                tuple.setAppId(getAppId());
                tuple.setDestModuleName(edge.getDestination());
                tuple.setSrcModuleName(edge.getSource());
                tuple.setDirection(Tuple.ACTUATOR);
                tuple.setTupleType(edge.getTupleType());
                tuple.setSourceDeviceId(sourceDeviceId);
                tuple.setActuatorId(actuatorId);
                tuple.setSourceModuleId(sourceModuleId);

                return tuple;
            }
        } else {
            Tuple tuple = new Tuple(appId, FogUtils.generateTupleId(), edge.getDirection(),
                    (long) (edge.getTupleCpuLength()), 1, (long) (edge.getTupleNwLength()), 100,
                    new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            // tuple.setActualTupleId(inputTuple.getActualTupleId());
            tuple.setUserId(getUserId());
            tuple.setAppId(getAppId());
            tuple.setDestModuleName(edge.getDestination());
            tuple.setSrcModuleName(edge.getSource());
            tuple.setDirection(edge.getDirection());
            tuple.setTupleType(edge.getTupleType());
            tuple.setSourceModuleId(sourceModuleId);

            return tuple;
        }
        return null;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public List<AppModule> getModules() {
        return modules;
    }

    public void setModules(List<AppModule> modules) {
        this.modules = modules;
    }

    public List<AppEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<AppEdge> edges) {
        this.edges = edges;
    }

    public GeoCoverage getGeoCoverage() {
        return geoCoverage;
    }

    public void setGeoCoverage(GeoCoverage geoCoverage) {
        this.geoCoverage = geoCoverage;
    }

    public List<AppLoop> getLoops() {
        return loops;
    }

    public void setLoops(List<AppLoop> loops) {
        this.loops = loops;
    }

    public void addLoops(AppLoop loop) {
        if (this.loops == null) {
            loops = new ArrayList<>();
        }
        this.loops.add(loop);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Map<String, AppEdge> getEdgeMap() {
        return edgeMap;
    }

    public void setEdgeMap(Map<String, AppEdge> edgeMap) {
        this.edgeMap = edgeMap;
    }

    ////////////////////////////////////////////////////////////// inclusion
    private Map<Integer, Map<String, Double>> deadlineInfo;
    private Map<Integer, Map<String, Integer>> additionalMipsInfo;

    public Map<Integer, Map<String, Integer>> getAdditionalMipsInfo() {
        return additionalMipsInfo;
    }

    public void setAdditionalMipsInfo(Map<Integer, Map<String, Integer>> additionalMipsInfo) {
        this.additionalMipsInfo = additionalMipsInfo;
    }

    public void setDeadlineInfo(Map<Integer, Map<String, Double>> deadlineInfo) {
        this.deadlineInfo = deadlineInfo;
    }

    public Map<Integer, Map<String, Double>> getDeadlineInfo() {
        return deadlineInfo;
    }

    public AppModule addAppModule(String moduleName, int ram, int mips, long size, long bw) {
        String vmm = "Xen";
        AppModule module = new AppModule(FogUtils.generateEntityId(), moduleName, appId, userId, mips, ram, bw, size,
                vmm, new TupleScheduler(mips, 1), new HashMap<Pair<String, String>, SelectivityModel>());

        getModules().add(module);

        return module;

    }

}


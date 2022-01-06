package MSDFC;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;

import java.util.*;

public class MasterFog extends GeneralPurposeFog {

    private Map<Integer, GeneralPurposeFog> CFbyID = new HashMap<>();
    public Map<Integer, GeneralPurposeFog> CFbyIDHealthStatusTable = new HashMap<>();
    private List<GeneralPurposeFog> sortedCFList = new ArrayList<>();

    private Map<Integer, MicroserviceContainer> MSbyID = new HashMap<>();
    private List<MicroserviceContainer> sortedMSList = new ArrayList<>();
    Set<MicroserviceContainer> containers = new LinkedHashSet<>();

    private Map<Integer, List<AppModule>> deviceToModuleMap = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> placedMSIDbyCFID = new HashMap<>();

    private MicroserviceContainer masterMicroserviceContainer;

    public double interModuleCOmmunicationCost = 0.0;
    public double fogDeviceEnergyConsumption = 0.0;

    public MasterFog(MicroserviceContainer masterMicroserviceContainer, GeneralPurposeFog generalPurposeFog) throws Exception {
        super(generalPurposeFog.getName(), generalPurposeFog.getFogCharacteristics(), generalPurposeFog.getVmAllocationPolicy(),
                generalPurposeFog.getStorageList(), generalPurposeFog.getSchedulingInterval(),
                generalPurposeFog.getUplinkBandwidth(), generalPurposeFog.getDownlinkBandwidth(),
                generalPurposeFog.getUplinkLatency(), generalPurposeFog.getRatePerMips());
        setMasterMicroserviceContainer(masterMicroserviceContainer);
    }

    public void startPreProcessingCFandMS() {
        setSortedCFList(CFSorter.cfScheduler(getCFList()));

        MicroserviceContainer[] microservicesContainer = new MicroserviceContainer[MSbyID.size()];
        int i = 0;
        for (MicroserviceContainer value : MSbyID.values()) {
            microservicesContainer[i] = value;
            i++;
        }
        setSortedMSList(new MSContainerSort().mergerAndReturn(microservicesContainer));

      //  System.out.println("==========================Starting Clustering========================================");


        List<MicroserviceContainer> tempContainerList = MainApplication.masterFog.getSortedMSList();
        List<MicroserviceContainer> tempContainerList2 = MainApplication.masterFog.getSortedMSList();

        Map<Integer, List<Integer>> clusterList = new HashMap<>();

        for (MicroserviceContainer leftContainer : tempContainerList) {
            containers.add(leftContainer);
        //    System.out.println("Parent: " + leftContainer.getName() + " Related Module IDs: " + leftContainer.getObviouslyDependsOnMSList().toString());
            for (MicroserviceContainer rightContainer : tempContainerList2) {
                if (leftContainer.getObviouslyDependsOnMSList().contains(rightContainer.getId()) ||
                        rightContainer.getObviouslyDependsOnMSList().contains(leftContainer.getId())){

                    //System.out.println("Parent: " + rightContainer.getName() + " Related Module IDs: " + rightContainer.getObviouslyDependsOnMSList().toString());


                    containers.add(rightContainer);

                    if (clusterList.containsKey(leftContainer.getId())){
                        List<Integer> items = clusterList.get(leftContainer.getId());

                        if (!items.contains(leftContainer.getId())) {
                            items.add(leftContainer.getId());
                        }
                        if (!items.contains(rightContainer.getId())) {
                            items.add(rightContainer.getId());
                        }
                        clusterList.put(leftContainer.getId(), items);

                    }else  if (clusterList.containsKey(rightContainer.getId())){
                        List<Integer> items = clusterList.get(rightContainer.getId());//new ArrayList<>();
                        if (!items.contains(leftContainer.getId())) {
                            items.add(leftContainer.getId());
                        }
                        if (!items.contains(rightContainer.getId())) {
                            items.add(rightContainer.getId());
                        }
                        clusterList.put(rightContainer.getId(), items);
                    }else {
                        List<Integer> items = new ArrayList<>();
                        items.add(leftContainer.getId());
                        items.add(rightContainer.getId());
                        clusterList.put(leftContainer.getId(), items);
                    }

                }
            }
        }

     //   System.out.println(clusterList.toString());
        containers.addAll(getSortedMSList());
      //  System.out.print("Finalised Container serial is: ");
//        for(MicroserviceContainer container: containers){
//            System.out.print(container.getId()+" ");
//        }


      //  System.out.println("\n===========================Ending Clustering=======================================");

        placeMicroservices();
    }

    private void placeMicroservices() {

        prepareCloudToWithMS();
        placeMasterMStoMF();
        placeCitizenMStoCF();

    }

    private void placeCitizenMStoCF() {
        Set<MicroserviceContainer> tempContainerList = containers;
       // List<MicroserviceContainer> tempContainerList2 = MainApplication.masterFog.getSortedMSList();


        for (GeneralPurposeFog citizenFog : MainApplication.masterFog.getSortedCFList()) {
            Iterator<MicroserviceContainer> itr = tempContainerList.iterator();
        //    System.out.println("Before entering WHILE CF Name: " + citizenFog.getName());

            while (itr.hasNext()){
                MicroserviceContainer container = itr.next();
           //     System.out.println("CF Name: " + citizenFog.getName() + " Module name: " + container.getModule().getName());
                if (isModulePlacable(citizenFog.getId(), container)) {
                    updateResourceOfCF(citizenFog.getId(), container);
                    placeTheModule(citizenFog, container);
                    itr.remove();
           //         System.out.println("CF Name: " + citizenFog.getName() + " Placed Module name: " + container.getModule().getName());
                } else {
          //          System.out.println("CF Name: " + citizenFog.getName() + " UnPlaced Module name: " + container.getModule().getName());
                }
            }
        }








//        for (GeneralPurposeFog citizenFog : MainApplication.masterFog.getSortedCFList()) {
//            //  Iterator<MicroserviceContainer> itr = tempContainerList.iterator();
//            System.out.println("Before entering WHILE CF Name: " + citizenFog.getName());
//            for (int i = 0; i < tempContainerList.size() && tempContainerList2.size() > 0; i++) {
//                MicroserviceContainer container = tempContainerList.get(i);//itr.next();
//                System.out.println("CF Name: " + citizenFog.getName() + " Module name: " + container.getModule().getName());
//
//                checkAndStartPlacementProcessing(citizenFog, container, tempContainerList2);
//
////                if (isModulePlacable(citizenFog.getId(), container)) {
////                    updateResourceOfCF(citizenFog.getId(), container);
////                    placeTheModule(citizenFog, container);
////                    itr.remove();
////                    tempContainerList2.remove(container);
////
////                    List<Integer> relativeList = container.getObviouslyDependsOnMSList();
////
////                    for (Integer id : relativeList) {
////                        for (MicroserviceContainer relatedMS : tempContainerList2) {
////                            if (relatedMS.getId() == id) {
////
////                            }
////                        }
////                    }
////
////                    //      System.out.println("CF Name: "+citizenFog.getName()+" Placed Module name: "+container.getModule().getName());
////                } else {
////                    System.out.println("CF Name: " + citizenFog.getName() + " UnPlaced Module name: " + container.getModule().getName());
////                }
//            }
//        }
    }

    private void checkAndStartPlacementProcessing(GeneralPurposeFog citizenFog, MicroserviceContainer container, List<MicroserviceContainer> tempContainerList2) {

        if (isModulePlacable(citizenFog.getId(), container)) {
            updateResourceOfCF(citizenFog.getId(), container);
            placeTheModule(citizenFog, container);
            //itr.remove();
            tempContainerList2.remove(container);

            List<Integer> relativeList = container.getObviouslyDependsOnMSList();
            //System.out.println("Parent: " + container.getName() + " Related Module IDs: " + relativeList.toString());
            List<MicroserviceContainer> tempContainerList3 = new ArrayList<>(tempContainerList2);
            for (Integer id : relativeList) {
                for (MicroserviceContainer relatedMS : tempContainerList3) {
                    if (relatedMS.getId() == id) {
                        checkAndStartPlacementProcessing(citizenFog, relatedMS, tempContainerList2);
                    }
                }
            }

        //    System.out.println("CF Name: " + citizenFog.getName() + " Placed Module name: " + container.getModule().getName());
        } else {
         //   System.out.println("CF Name: " + citizenFog.getName() + " UnPlaced Module name: " + container.getModule().getName());
        }

    }

    private void placeTheModule(GeneralPurposeFog citizenFog, MicroserviceContainer container) {

        int deviceId = CloudSim.getEntityId(citizenFog.getName());
        AppModule appModule = container.getModule();

        List<AppModule> placedModules;
        if (!getDeviceToModuleMap().containsKey(deviceId)) {
            placedModules = new ArrayList<AppModule>();
        } else {
            placedModules = getDeviceToModuleMap().get(deviceId);
        }
        placedModules.add(appModule);
        getDeviceToModuleMap().put(deviceId, placedModules);

    }

    private void placeMasterMStoMF() {
        // Master Microservice placement
        List<AppModule> placeMasterModules;
        int masterDeviceId = CloudSim.getEntityId(MainApplication.masterFog.getName());
        if (!getDeviceToModuleMap().containsKey(masterDeviceId)) {
            placeMasterModules = new ArrayList<AppModule>();
        } else {
            placeMasterModules = getDeviceToModuleMap().get(masterDeviceId);
        }
        placeMasterModules.add(MainApplication.masterAppModule);
        getDeviceToModuleMap().put(masterDeviceId, placeMasterModules);
    }

    private void prepareCloudToWithMS() {
        // Cloud Microservice placement
        List<AppModule> placeCloudModules;
        int cloudDeviceId = CloudSim.getEntityId(MainApplication.cloudDevice.getName());
        if (!getDeviceToModuleMap().containsKey(cloudDeviceId)) {
            placeCloudModules = new ArrayList<AppModule>();
        } else {
            placeCloudModules = getDeviceToModuleMap().get(cloudDeviceId);
        }
        placeCloudModules.add(MainApplication.masterAppModule);
        getDeviceToModuleMap().put(cloudDeviceId, placeCloudModules);
    }

    public boolean isModulePlacable(int id, MicroserviceContainer container) {

        GeneralPurposeFog citizenFog = CFbyIDHealthStatusTable.get(id);
        return ((citizenFog.getMips() / 3.0) * 2 > container.getModule().getMips()) &&
                ((citizenFog.getHost().getRamProvisioner().getAvailableRam() / 3.0) * 2 > container.getModule().getRam()) &&
                ((citizenFog.getHost().getBwProvisioner().getAvailableBw() / 3.0) * 2 > container.getModule().getBw()) &&
                ((citizenFog.getHost().getStorage() / 3.0) * 2 > container.getModule().getSize());

    }


    public void updateResourceOfCF(int id, MicroserviceContainer container) {

        GeneralPurposeFog citizenFog = CFbyIDHealthStatusTable.get(id);

//        System.out.println("==================================================================");
//        System.out.println("Before Compare CF Name: " + citizenFog.getName() + " Module name: " + container.getModule().getName());
//        System.out.println(citizenFog.getMips() + " " + container.getModule().getMips());
//        System.out.println(citizenFog.getHost().getRamProvisioner().getAvailableRam() + " " + container.getModule().getRam());
//        System.out.println(citizenFog.getHost().getBwProvisioner().getAvailableBw() + " " + container.getModule().getBw());
//        System.out.println(citizenFog.getHost().getStorage() + " " + container.getModule().getSize());
//        System.out.println("==================================================================");

        citizenFog.setMips((int) (citizenFog.getMips() - container.getModule().getMips()));
        citizenFog.getHost().getRamProvisioner().setAvailableRam(
                citizenFog.getHost().getRamProvisioner().getAvailableRam() - container.getModule().getRam());
        citizenFog.getHost().getBwProvisioner().setAvailableBw(
                citizenFog.getHost().getBwProvisioner().getAvailableBw() - container.getModule().getBw());
        citizenFog.getHost().setStorage(
                citizenFog.getHost().getStorage() - container.getModule().getSize());
        CFbyIDHealthStatusTable.put(citizenFog.getId(), citizenFog);
        setPlacedMSIDbyCFID(citizenFog.getId(), container.getId());

    }

    public int provideGateWayDeviceId(String moduleName) {

        final int[] gateWay = {0};
        int moduleId = Integer.parseInt(moduleName.replace(Constant.CITIZEN_MODULE, ""));

        placedMSIDbyCFID.forEach((key, value) -> {

            if (value.contains(moduleId)) {
                gateWay[0] = key;
            }

        });

        return gateWay[0];
    }


    public List<GeneralPurposeFog> getCFList() {
        ArrayList<GeneralPurposeFog> CFList = new ArrayList<>();

        CFbyID.forEach((key, value) -> CFList.add(value));

        return CFList;
    }

    public List<GeneralPurposeFog> getSortedCFList() {
        return sortedCFList;
    }

    public void setSortedCFList(List<GeneralPurposeFog> sortedCFList) {
        this.sortedCFList = sortedCFList;
    }

    public List<MicroserviceContainer> getSortedMSList() {
        return sortedMSList;
    }

    public void setSortedMSList(List<MicroserviceContainer> sortedMSList) {
        this.sortedMSList = sortedMSList;
    }

    public Map<Integer, GeneralPurposeFog> getCFbyID() {
        return CFbyID;
    }

    public void setCFbyID(Integer id, GeneralPurposeFog CF) {
        if (!this.CFbyID.containsKey(id)) {
            this.CFbyID.put(id, CF);
        }
    }

    public Map<Integer, MicroserviceContainer> getMSbyID() {
        return MSbyID;
    }

    public void setMSbyID(Integer id, MicroserviceContainer MS) {
        if (!this.MSbyID.containsKey(id)) {
            this.MSbyID.put(id, MS);
        }
    }

    public Map<Integer, List<AppModule>> getDeviceToModuleMap() {
        return deviceToModuleMap;
    }

    public void setDeviceToModuleMap(Integer cfId, AppModule module) {
        List<AppModule> moduleList;
        if (!this.deviceToModuleMap.containsKey(cfId)) {
            moduleList = new ArrayList<>();
        } else {
            moduleList = deviceToModuleMap.get(cfId);
        }
        moduleList.add(module);
        this.deviceToModuleMap.put(cfId, moduleList);
    }

    public Map<Integer, ArrayList<Integer>> getPlacedMSIDbyCFID() {
        return placedMSIDbyCFID;
    }

    public void setPlacedMSIDbyCFID(Integer cfId, Integer placedMSID) {

        ArrayList<Integer> msList;
        if (!this.placedMSIDbyCFID.containsKey(cfId)) {
            msList = new ArrayList<>();
        } else {
            msList = placedMSIDbyCFID.get(cfId);
        }
        msList.add(placedMSID);
        this.placedMSIDbyCFID.put(cfId, msList);

    }

    public MicroserviceContainer getMasterMicroserviceContainer() {
        return masterMicroserviceContainer;
    }

    public void setMasterMicroserviceContainer(MicroserviceContainer masterMicroserviceContainer) {
        this.masterMicroserviceContainer = masterMicroserviceContainer;
    }

    public boolean isAlreadyNotRelated(int id, int i) {
        for (MicroserviceContainer container: MSbyID.values()){
            if (container.getId() == id && container.getObviouslyDependsOnMSList().contains(i)) {
                return false;
            }
        }
        return true;
    }
}

package MSDFC;


import java.util.*;

import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.placement.ModuleMapping;



public class MicroservicePlacement extends SDFCPlacement {

    protected ModuleMapping moduleMapping;
    protected List<SDFCSensor> sensors;
    protected List<SDFCActuator> actuators;
    protected String moduleToPlace;
    protected Map<Integer, Integer> deviceMipsInfo;

    public MicroservicePlacement(List<GeneralPurposeFog> fogDevices, List<SDFCSensor> sensors, List<SDFCActuator> actuators,
                                 SDFCApplication application, ModuleMapping moduleMapping, String moduleToPlace) {
        this.setGeneralPurposeFogs(fogDevices);
        this.setSDFCApplication(application);
        this.setModuleMapping(moduleMapping);
        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        setSDFCSensors(sensors);
        setSDFCActuators(actuators);
        this.moduleToPlace = moduleToPlace;
        this.deviceMipsInfo = new HashMap<Integer, Integer>();
        mapModules();
    }


    @Override
    protected void mapModules() {

        setDeviceToModuleMap(MainApplication.masterFog.getDeviceToModuleMap());


//        // Cloud Microservice placement
//        List<AppModule> placeCloudModules;
//        int cloudDeviceId = CloudSim.getEntityId(MainApplication.cloudDevice.getName());
//        if (!getDeviceToModuleMap().containsKey(cloudDeviceId)) {
//            placeCloudModules = new ArrayList<AppModule>();
//        } else {
//            placeCloudModules = getDeviceToModuleMap().get(cloudDeviceId);
//        }
//        placeCloudModules.add(MainApplication.masterAppModule);
//        getDeviceToModuleMap().put(cloudDeviceId, placeCloudModules);
//
//        // Master Microservice placement
//        List<AppModule> placeMasterModules;
//        int masterDeviceId = CloudSim.getEntityId(MainApplication.masterFog.getName());
//        if (!getDeviceToModuleMap().containsKey(masterDeviceId)) {
//            placeMasterModules = new ArrayList<AppModule>();
//        } else {
//            placeMasterModules = getDeviceToModuleMap().get(masterDeviceId);
//        }
//        placeMasterModules.add(MainApplication.masterAppModule);
//        getDeviceToModuleMap().put(masterDeviceId, placeMasterModules);
//
//
//
//        // Citizen Microservice placement
//        List<MicroserviceContainer> tempContainerList = MainApplication.masterFog.getSortedMSList();
//        //Iterator<MicroserviceContainer> itr = tempContainerList.iterator();
//
//        for (GeneralPurposeFog citizenFog: MainApplication.masterFog.getSortedCFList()) {
//            Iterator<MicroserviceContainer> itr = tempContainerList.iterator();
//            //System.out.println("Before entering WHILE CF Name: "+citizenFog.getName()+" "+itr.next().getName());
//
//            while (itr.hasNext()) {
//
//                MicroserviceContainer container = itr.next();
//
//              //  System.out.println("CF Name: "+citizenFog.getName()+" Module name: "+container.getModule().getName());
//
//                if (MainApplication.masterFog.updateResourceOfCF(citizenFog.getId(), container)) {
//                    int deviceId = CloudSim.getEntityId(citizenFog.getName());
//                    AppModule appModule = container.getModule();
//
//                    List<AppModule> placedModules;
//                    if (!getDeviceToModuleMap().containsKey(deviceId)) {
//                        placedModules = new ArrayList<AppModule>();
//                    } else {
//                        placedModules = getDeviceToModuleMap().get(deviceId);
//                    }
//                    placedModules.add(appModule);
//                    getDeviceToModuleMap().put(deviceId, placedModules);
//                    itr.remove();
//              //      System.out.println("CF Name: "+citizenFog.getName()+" Placed Module name: "+container.getModule().getName());
//
//                }else {
//             //       System.out.println("CF Name: "+citizenFog.getName()+" UnPlaced Module name: "+container.getModule().getName());
//                }
//            }
//        }


//        for (String deviceName : getModuleMapping().getModuleMapping().keySet()) {
//            for (String moduleName : getModuleMapping().getModuleMapping().get(deviceName)) {
//                int deviceId = CloudSim.getEntityId(deviceName);
//                AppModule appModule = getSDFCApplication().getModuleByName(moduleName);
//                if (!getDeviceToModuleMap().containsKey(deviceId)) {
//                    List<AppModule> placedModules = new ArrayList<AppModule>();
//                    placedModules.add(appModule);
//                    getDeviceToModuleMap().put(deviceId, placedModules);
//
//                } else {
//                    List<AppModule> placedModules = getDeviceToModuleMap().get(deviceId);
//                    placedModules.add(appModule);
//                    getDeviceToModuleMap().put(deviceId, placedModules);
//                }
//            }
//        }
//
//
//        for (GeneralPurposeFog device : getGeneralPurposeFogs()) {
//            int deviceParent = -1;
//            List<Integer> children = new ArrayList<Integer>();
//
//            if (device.getLevel() == 1) {
//                if (!deviceMipsInfo.containsKey(device.getId()))
//                    deviceMipsInfo.put(device.getId(), 0);
//                deviceParent = device.getParentId();
//                for (GeneralPurposeFog deviceChild : getGeneralPurposeFogs()) {
//                    if (deviceChild.getParentId() == device.getId()) {
//                        children.add(deviceChild.getId());
//                    }
//                }
//
//                Map<Integer, Double> childDeadline = new HashMap<Integer, Double>();
//                for (int childId : children)
//                    childDeadline.put(childId, getSDFCApplication().getDeadlineInfo().get(childId).get(moduleToPlace));
//
//                List<Integer> keys = new ArrayList<Integer>(childDeadline.keySet());
//
//                for (int i = 0; i < keys.size() - 1; i++) {
//                    for (int j = 0; j < keys.size() - i - 1; j++) {
//                        if (childDeadline.get(keys.get(j)) > childDeadline.get(keys.get(j + 1))) {
//                            int tempJ = keys.get(j);
//                            int tempJn = keys.get(j + 1);
//                            keys.set(j, tempJn);
//                            keys.set(j + 1, tempJ);
//                        }
//                    }
//                }
//
//
//                int baseMipsOfPlacingModule = (int) getSDFCApplication().getModuleByName(moduleToPlace).getMips();
//                for (int key : keys) {
//                    int currentMips = deviceMipsInfo.get(device.getId());
//                    AppModule appModule = getSDFCApplication().getModuleByName(moduleToPlace);
//                    int additionalMips = getSDFCApplication().getAdditionalMipsInfo().get(key).get(moduleToPlace);
//                    if (currentMips + baseMipsOfPlacingModule + additionalMips < device.getMips()) {
//                        currentMips = currentMips + baseMipsOfPlacingModule + additionalMips;
//                        deviceMipsInfo.put(device.getId(), currentMips);
//                        if (!getDeviceToModuleMap().containsKey(device.getId())) {
//                            List<AppModule> placedModules = new ArrayList<AppModule>();
//                            placedModules.add(appModule);
//                            getDeviceToModuleMap().put(device.getId(), placedModules);
//
//                        } else {
//                            List<AppModule> placedModules = getDeviceToModuleMap().get(device.getId());
//                            placedModules.add(appModule);
//                            getDeviceToModuleMap().put(device.getId(), placedModules);
//                        }
//                    } else {
//                        List<AppModule> placedModules = getDeviceToModuleMap().get(deviceParent);
//                        placedModules.add(appModule);
//                        getDeviceToModuleMap().put(deviceParent, placedModules);
//                    }
//                }
//
//
//            }
//
//        }

    }

    public ModuleMapping getModuleMapping() {
        return moduleMapping;
    }

    public void setModuleMapping(ModuleMapping moduleMapping) {
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

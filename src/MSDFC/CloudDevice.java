package MSDFC;

public class CloudDevice extends GeneralPurposeFog{

    public CloudDevice(GeneralPurposeFog generalPurposeFog) throws Exception {
        super(generalPurposeFog.getName(), generalPurposeFog.getFogCharacteristics(), generalPurposeFog.getVmAllocationPolicy(),
                generalPurposeFog.getStorageList(), generalPurposeFog.getSchedulingInterval(),
                generalPurposeFog.getUplinkBandwidth(), generalPurposeFog.getDownlinkBandwidth(),
                generalPurposeFog.getUplinkLatency(), generalPurposeFog.getRatePerMips());
    }

}

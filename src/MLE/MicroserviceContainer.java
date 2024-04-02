package MSDFC;

import org.fog.application.AppModule;
import java.util.ArrayList;

public class MicroserviceContainer extends AppModule {

    private int id;
    private int priority;
    private ArrayList<Integer> obviouslyDependsOnMSList = new ArrayList<>();
    private ArrayList<Integer> conditionalDependsOnMSList = new ArrayList<>();
    private ArrayList<Integer> dependantMSList = new ArrayList<>();
    private int numberOfRequest = 0;
    private AppModule module;

    public MicroserviceContainer(AppModule operator,
                                 int id) {
        super(operator);
        this.id = id;
        this.module = operator;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public AppModule getModule() {
        return module;
    }

    public void setModule(AppModule module) {
        this.module = module;
    }

    public int getNumberOfRequest() {
        return numberOfRequest;
    }

    public void incrementNumberOfRequest() {
        this.numberOfRequest += 1;
    }

    public ArrayList<Integer> getObviouslyDependsOnMSList() {
        return obviouslyDependsOnMSList;
    }

    public void setObviouslyDependsOnMSList(Integer obviouslyDependsOnMS) {
        if (!obviouslyDependsOnMSList.contains(obviouslyDependsOnMS)){
            obviouslyDependsOnMSList.add(obviouslyDependsOnMS);
        }
    }

    public ArrayList<Integer> getConditionalDependsOnMSList() {
        return conditionalDependsOnMSList;
    }

    public void setConditionalDependsOnMSList(Integer conditionalDependsOnMS) {
        if (!conditionalDependsOnMSList.contains(conditionalDependsOnMS)){
            conditionalDependsOnMSList.add(conditionalDependsOnMS);
        }
    }

    public ArrayList<Integer> getDependantMSList() {
        return dependantMSList;
    }

    public void setDependantMSList(Integer dependantMS) {
        if (!dependantMSList.contains(dependantMS)){
            dependantMSList.add(dependantMS);
        }
    }
}

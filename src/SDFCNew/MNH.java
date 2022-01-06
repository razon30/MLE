package SDFCNew;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MNH {

	public static Map<String, List<String>> requestedCFToTaskMap = new HashMap<String, List<String>>();
	public static Map<String, List<String>> updatedDeviceToModule = new HashMap<String, List<String>>();
	public static List<SDFCFogDevice> sortedDevices = new ArrayList<SDFCFogDevice>();

	public static void addRequester(String cfName, String taskName) {

		if (requestedCFToTaskMap.containsKey(cfName)) {
			requestedCFToTaskMap.get(cfName).add(taskName);
		} else {
			List<String> taskList = new ArrayList<String>();
			taskList.add(taskName);
			requestedCFToTaskMap.put(cfName, taskList);
		}

	}

	public static String getRequester(String taskName) {

		String cfName = "";
		for (String key : requestedCFToTaskMap.keySet()) {
			if (requestedCFToTaskMap.get(key).contains(taskName)) {
				cfName = key;
			}
		}
		return cfName;
	}

	public static void removeRequester(String cfName, String taskName) {

		if (requestedCFToTaskMap.containsKey(cfName)) {
			requestedCFToTaskMap.get(cfName).remove(taskName);
		}

	}

}

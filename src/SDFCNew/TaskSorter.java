package SDFCNew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import SDFC.application.Microservice;

public class TaskSorter {

	Map<Integer, List<Integer>> dependencyMap = new HashMap<Integer, List<Integer>>();
	Map<Integer, Integer> numberOfRequestMap = new HashMap<Integer, Integer>();
	List<Integer> keySet = new ArrayList<Integer>();

	void merge(Microservice arr[], int l, int m, int r) {
		// Find sizes of two subarrays to be merged
		int n1 = m - l + 1;
		int n2 = r - m;

		/* Create temp arrays */
		Microservice L[] = new Microservice[n1];
		Microservice R[] = new Microservice[n2];

		/* Copy data to temp arrays */
		for (int i = 0; i < n1; ++i)
			L[i] = arr[l + i];
		for (int j = 0; j < n2; ++j)
			R[j] = arr[m + 1 + j];

		/* Merge the temp arrays */

		// Initial indexes of first and second subarrays
		int i = 0, j = 0;

		// Initial index of merged subarry array
		int k = l;

//        System.out.println("===========================Left============================");
//        printArray(L);
//        System.out.println("===========================Left============================");
//        System.out.println("===========================Right============================");
//        printArray(R);
//        System.out.println("===========================Right============================");

		while (i < n1 && j < n2) {

			// main sorting will be here.
			Microservice left = L[i];
			Microservice right = R[j];

			List<Integer> leftDependantIds = new ArrayList<Integer>();
			List<Integer> rightDependantIds = new ArrayList<Integer>();
			int leftNumberOfRequest = 0;
			int rightNumberOfRequest = 0;
			double leftWorklength = left.getMips();
			double rightWorklength = right.getMips();

			for (int n = 0; n < keySet.size(); n++) {

				if (keySet.get(n) == left.getId()) {
					leftDependantIds = dependencyMap.get(left.getId());
					leftNumberOfRequest = numberOfRequestMap.get(left.getId());
				}

				if (keySet.get(n) == right.getId()) {
					leftDependantIds = dependencyMap.get(right.getId());
					leftNumberOfRequest = numberOfRequestMap.get(right.getId());
				}

			}

			if (leftDependantIds.contains(right.getId())) { // isDependent(leftDependantIds, right.getId())) {
				arr[k] = left;
				i++;
			} else if (rightDependantIds.contains(left.getId())) { // isDependent(rightDependantIds, left.getId())) {
				arr[k] = right;
				j++;
			} else {

				if (leftDependantIds.size() > rightDependantIds.size()) {
					arr[k] = left;
					i++;
				} else if (rightDependantIds.size() > leftDependantIds.size()) {
					arr[k] = right;
					j++;
				} else {

					if (leftNumberOfRequest > rightNumberOfRequest) {
						arr[k] = left;
						i++;
					} else if (rightNumberOfRequest > leftNumberOfRequest) {
						arr[k] = right;
						j++;

					} else {

						if (rightWorklength < leftWorklength) {
							arr[k] = right;
							j++;
						} else {
							arr[k] = left;
							i++;
						}

					}

				}

			}

			k++;
		}

		/* Copy remaining elements of L[] if any */
		while (i < n1) {
			arr[k] = L[i];
			i++;
			k++;
		}

		/* Copy remaining elements of R[] if any */
		while (j < n2) {
			arr[k] = R[j];
			j++;
			k++;
		}

//        System.out.println("===========================ARR============================");
//        printArray(arr);
//        System.out.println("===========================ARR============================");

	}

//	private boolean isDependent(List<Integer> dependentCFIDs, int cloudletId) {
//
//		boolean isDependent = false;
//
//		for (int l = 0; l < dependentCFIDs.length; l++) {
//
//			if (dependentCFIDs[l] == cloudletId) {
//				isDependent = true;
//				break;
//			}
//
//		}
//
//		return isDependent;
//	}

	// Main function that sorts arr[l..r] using
	// merge()
	void sort(Microservice arr[], int l, int r) {
		if (l < r) {
			// Find the middle point
			int m = (l + r) / 2;

			// Sort first and second halves
			sort(arr, l, m);
			sort(arr, m + 1, r);

			// Merge the sorted halves
			merge(arr, l, m, r);
		}
	}

	/* A utility function to print array of size n */
	void printArray(Microservice arr[]) {
		int n = arr.length;
		for (int i = 0; i < n; ++i) {

			Microservice microservice = arr[i];

			List<Integer> dependantIds = new ArrayList<Integer>();
			int numberOfRequest = 0;
			double worklength = microservice.getMips();
			int ram = microservice.getRam();

			for (int j = 0; j < keySet.size(); j++) {
				if (microservice.getId() == keySet.get(j)) {

					dependantIds = dependencyMap.get(microservice.getId());
					numberOfRequest = numberOfRequestMap.get(microservice.getId());

				}
			}

			System.out.println("=======================================================");
			System.out.println("ID: " + microservice.getId() + " ");
			System.out.println(
					"Dependent CF ID size: " + dependantIds.size() + " And dependent on: " + printList(dependantIds));
			System.out.println("Number of request: " + numberOfRequest + " ");
			System.out.println("Work length: " + worklength + " ");
			System.out.println("Ram: " + ram + " ");
			System.out.println("=======================================================");

		}
		System.out.println();
	}

	private static String printList(List<Integer> dependentCFID) {
		// TODO Auto-generated method stub

		String string = "";

		for (int i : dependentCFID) {

			string = string + " " + i + " ";

		}

		return string;
	}

	// Driver method
	public List<Microservice> mergerAndReturn(Microservice tasks[], Map<Integer, List<Integer>> dependencyMap,
			Map<Integer, Integer> numberOfRequestMap) {

		this.dependencyMap = dependencyMap;
		this.numberOfRequestMap = numberOfRequestMap;
		this.keySet = new ArrayList<Integer>(dependencyMap.keySet());

		// System.out.println("Given Array");
		// printArray(tasks);

		// MicroserviceSorter ob = new MicroserviceSorter();
		sort(tasks, 0, tasks.length - 1);

		// System.out.println("\nSorted array");
		// printArray(tasks);

		List<Microservice> taskList = new ArrayList<>(Arrays.asList(tasks));

		return taskList;
	}

}
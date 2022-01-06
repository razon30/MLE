package SDFC.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.Host;

import SDFC.masterFog.MasterComputingUnit;

public class FogSorter {

	void merge(MasterComputingUnit[] arr, int l, int m, int r, List<Double> IRList, List<Double> PRList,
			List<Double> RRList, List<Double> BRList) {
		// Find sizes of two subarrays to be merged
		int n1 = m - l + 1;
		int n2 = r - m;

		/* Create temp arrays */
		MasterComputingUnit L[] = new MasterComputingUnit[n1];
		MasterComputingUnit R[] = new MasterComputingUnit[n2];

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
			MasterComputingUnit left = L[i];
			MasterComputingUnit right = R[j];

			int numOfPeleft = 0;
			int ramleft = 0;
			int bwleft = 0;
			for (Host host : left.getFogCharacteristics().getHostList()) {
				numOfPeleft += host.getNumberOfPes();
				ramleft += host.getRam();
				bwleft += host.getBw();
			}

			double leftIR = getIR(left.getMips(), IRList);
			double leftPR = getPR(numOfPeleft, PRList);
			double leftRR = getRR(ramleft, RRList);
			double leftBR = getBR(bwleft, BRList);

			int numOfPeright = 0;
			int ramright = 0;
			int bwright = 0;
			for (Host host : right.getFogCharacteristics().getHostList()) {
				numOfPeright += host.getNumberOfPes();
				ramright += host.getRam();
				bwright += host.getBw();
			}

			double rightIR = getIR(right.getMips(), IRList);
			double rightPR = getPR(numOfPeright, PRList);
			double rightRR = getRR(ramright, RRList);
			double rightBR = getBR(bwright, BRList);

			double leftTotal = leftBR + leftIR + leftPR + leftRR;
			double rightTotal = rightBR + rightIR + rightPR + rightRR;

			if (leftTotal > rightTotal) {
				arr[k] = left;
				i++;
			} else if (leftTotal < rightTotal) {
				arr[k] = right;
				j++;
			} else {

				if (left.getMips() >= right.getMips()) {
					arr[k] = left;
					i++;
				} else if (left.getMips() < right.getMips()) {
					arr[k] = right;
					j++;
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

	private double getBR(long bw, List<Double> BRList) {
		// System.out.println(BRList.toArray());

		return bw / Collections.min(BRList);
	}

	private double getRR(int ram, List<Double> RRList) {
		// System.out.println(RRList.toArray());

		return ram / Collections.min(RRList);
	}

	private double getPR(int numberOfPes, List<Double> PRList) {
		// System.out.println(PRList.toArray());

		return numberOfPes / Collections.min(PRList);
	}

	private double getIR(double mips, List<Double> IRList) {

		System.out.println(Collections.min(IRList));

		return mips / Collections.min(IRList);

	}

	private boolean isDependent(int[] dependentCFID, int cloudletId) {

		boolean isDependent = false;

		for (int l = 0; l < dependentCFID.length; l++) {

			if (dependentCFID[l] == cloudletId) {
				isDependent = true;
				break;
			}

		}

		return isDependent;
	}

	// Main function that sorts arr[l..r] using
	// merge()
	void sort(MasterComputingUnit[] arr, int l, int r, List<Double> IRList, List<Double> PRList, List<Double> RRList,
			List<Double> BRList) {
		if (l < r) {
			// Find the middle point
			int m = (l + r) / 2;

			// Sort first and second halves
			sort(arr, l, m, IRList, PRList, RRList, BRList);
			sort(arr, m + 1, r, IRList, PRList, RRList, BRList);

			// Merge the sorted halves
			merge(arr, l, m, r, IRList, PRList, RRList, BRList);
		}
	}

	/* A utility function to print array of size n */
	static void printArray(MasterComputingUnit arr[]) {
		int n = arr.length;
		for (int i = 0; i < n; ++i) {

			int numOfPe = 0;
			int ram = 0;
			int bw = 0;
			for (Host host : arr[i].getFogCharacteristics().getHostList()) {
				numOfPe += host.getNumberOfPes();
				ram += host.getRam();
				bw += host.getBw();
			}

			System.out.println("=======================================================");
			System.out.println("ID: " + arr[i].getId() + " ");
			System.out.println("MIPS: " + arr[i].getMips());
			System.out.println("Number of Processor: " + numOfPe + " ");
			System.out.println("RAM: " + ram + " ");
			System.out.println("Bandwidth: " + bw + " ");
			System.out.println("=======================================================");

		}
		System.out.println();
	}

	// List<ChildFog> childFogList2;
	private static List<Double> IRList;
	private static List<Double> PRList;
	private static List<Double> RRList;
	private static List<Double> BRList;

	public static List<MasterComputingUnit> cfScheduler(List<MasterComputingUnit> childFogList) {

		// childFogList2 = new ArrayList<>();
		IRList = new ArrayList<>();
		PRList = new ArrayList<>();
		RRList = new ArrayList<>();
		BRList = new ArrayList<>();

		// this.childFogList2.addAll(childFogList);
		List<MasterComputingUnit> childFogList1 = new ArrayList<>();
		// List<ChildFogSchedulerHelperModel> childFogSchedulerHelperModels = new
		// ArrayList<>();

		MasterComputingUnit[] childFogs = new MasterComputingUnit[childFogList.size()];

		int p = 0;
		for (MasterComputingUnit childFog : childFogList) {

			IRList.add((double) childFog.getMips());

			int numOfPe = 0;
			int ram = 0;
			int bw = 0;
			for (Host host : childFog.getFogCharacteristics().getHostList()) {
				numOfPe += host.getNumberOfPes();
				ram += host.getRam();
				bw += host.getBw();
			}

			PRList.add((double) numOfPe);
			RRList.add((double) ram);
			BRList.add((double) bw);

			childFogs[p] = childFog;

//            System.out.println(IRList.get(p));
//            System.out.println(PRList.get(p));
//            System.out.println(RRList.get(p));
//            System.out.println(BRList.get(p));

			p++;

		}

		// System.out.println("Fogs before Sorting");
		// printArray(childFogs);

		FogSorter ob = new FogSorter();
		ob.sort(childFogs, 0, childFogs.length - 1, IRList, PRList, RRList, BRList);

		// System.out.println("Fogs After Sorting");
		// printArray(childFogs);

		childFogList1.addAll(Arrays.asList(childFogs));

		return childFogList1;
	}

}

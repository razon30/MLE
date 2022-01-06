package SDFC.utils;

import java.util.Random;

public class Utils {

	public static double getValue(double min, double max) {
		Random rn = new Random();
		return min + (max - min) * rn.nextDouble();
	}

	public static Long getValue(Long min, Long max) {
		Random rn = new Random();
		return min + (max - min) * rn.nextLong();
	}

	public static int getValue(int min, int max) {
		// Random rn = new Random();
		return (int) (min + (Math.random() * (max - min)));// min + (max - min) * rn.nextInt();
	}

	public static void print(Object object) {
		System.out.println(object);
	}

	public static double getValue(double min) {
		Random rn = new Random();
		return rn.nextDouble() * 10 + min;
	}

	public static int getValue(int min) {
		Random rn = new Random();
		return rn.nextInt() * 10 + min;
	}

}

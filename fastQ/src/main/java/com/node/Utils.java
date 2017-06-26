package main.java.com.node;

import java.io.File;


public final class Utils {

	private static int OBJ_HEADER;
	private static int ARR_HEADER;
	private static int INT_FIELDS = 12;
	private static int OBJ_REF;
	private static int OBJ_OVERHEAD;
	private static boolean IS_64_BIT_JVM;


	private Utils() {
	}

	
	static {
		IS_64_BIT_JVM = true;
		String arch = System.getProperty("sun.arch.data.model");
		if (arch != null) {
			if (arch.contains("32")) {
				// If exists and is 32 bit then we assume a 32bit JVM
				IS_64_BIT_JVM = false;
			}
		}
		OBJ_HEADER = IS_64_BIT_JVM ? 16 : 8;
		ARR_HEADER = IS_64_BIT_JVM ? 24 : 12;
		OBJ_REF = IS_64_BIT_JVM ? 8 : 4;
		OBJ_OVERHEAD = OBJ_HEADER + INT_FIELDS + OBJ_REF + ARR_HEADER;

	}

	public static long getStringSize(String s) {
		return (s.length() * 2) + OBJ_OVERHEAD;
	}

	public static long getAvailableMemory() {
		System.gc();
		// http://stackoverflow.com/questions/12807797/java-get-available-memory
		Runtime r = Runtime.getRuntime();
		long allocatedMemory = r.totalMemory() - r.freeMemory();
		long presFreeMemory = r.maxMemory() - allocatedMemory;
		return presFreeMemory;
	}

	public static long getBlockSize(final long sizeoffile, final int maxtmpfiles, final long maxMemory) {// girdi

		long blocksize = sizeoffile / maxtmpfiles + (sizeoffile % maxtmpfiles == 0 ? 0 : 1);

		if (blocksize < maxMemory / 2) {
			blocksize = maxMemory / 2;
		}
		return blocksize;
	}
	
	public static long getFileSize(String filePath){
		
		return new File(filePath).length();
	}
	
	final static class WordFreq implements Comparable<WordFreq> {
		String word;
		int freq;

		public WordFreq(final String w, final int c) {
			word = w;
			freq = c;
		}

		@Override
		public int compareTo(final WordFreq other) {
			return Integer.compare(this.freq, other.freq);
		}
	}
}

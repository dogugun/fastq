package main.java.com.node;

import java.io.File;

public class Main {

	private static String tempFolder = "data";
	private static long externalSortThreshold = 75000000;
	private static int defaultTopCount = 25;
	private static int defaultSeqLength = 30;
	private static long defaultBlockSize = 0;

	public static void main(String[] args) {
		// default values
		int topCount = 0;
		int seqLength = 0;
		long blockSize = 0;

		// runtime parameters
		String filePath = System.getProperty("filepath");
		String topCountStr = System.getProperty("top");
		String seqLengthStr = System.getProperty("length");
		String blockSizeStr = System.getProperty("block");

		//file path
		if (filePath == null || filePath.equals("")) {
			System.out.println("Please enter a valid file path");
			return;
		}
		// number of top k-mers to be listed
		if (topCountStr != null && topCountStr.equals("") == false) {
			topCount = Integer.parseInt(topCountStr);
		} else {
			topCount=defaultTopCount;
		}
		//length of the k-mer sequences. default is 30
		if (seqLengthStr != null && seqLengthStr.equals("") == false) {
			seqLength = Integer.parseInt(seqLengthStr);
		} else {
			seqLength = defaultSeqLength;
		}
		/*There is already a calculation of blocksize for the temporary files in Utils class: getBlockSize 
		 * This block size is used for temporary file sizes in external sorting
		 * For different computers and different memory sizes, user may want to set it by themselves.
		 * This may cause problems like opening too many files or longer execution time. 
		 * So, the advised blocksize is around half of the available memory for the JVM.  
		 */
		if (blockSizeStr != null && blockSizeStr.equals("") == false) {
			blockSize = Integer.parseInt(blockSizeStr);
		} else {
			blockSize=defaultBlockSize;
		}

		/* The threshold is needed to avoid the unnecessary run of 
		 * external sorting for files smaller than 75MB*/
		String seqPrefix = "@" + filePath.split("\\.")[0];
		long fileSize = Utils.getFileSize(filePath);
		if (fileSize > externalSortThreshold) {
			long start = System.currentTimeMillis();
			FastQReader.prepareOutputFile(filePath, seqPrefix, seqLength);
			long outputTime = System.currentTimeMillis();
			System.out.println("Generating k-mers completed in: " + (outputTime-start) + " msecs");
			ExternalSorting.externalSort(blockSize);
			long esTime = System.currentTimeMillis();
			System.out.println("External sorting is completed in: " + (esTime-outputTime) + " msecs");
			TopKHeap.topKHeap(topCount);
			long topKHeapTime = System.currentTimeMillis();
			System.out.println("Top-K Heap sort is completed in: " + (topKHeapTime-esTime) + " msecs");
			System.out.println("Overall completed in: " + (topKHeapTime-start) + " msecs");
			new File(tempFolder).delete();
		} else {
			long start = System.currentTimeMillis();
			FastQReader.calculateDirectly(filePath, seqPrefix, seqLength, topCount);
			long end = System.currentTimeMillis();
			System.out.println("Overall completed in: " + (end-start) + " msecs");
		}

	}

}

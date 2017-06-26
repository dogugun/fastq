package main.java.com.node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.PriorityQueue;



public class TopKHeap {
	private static String FINAL_FILE = "data/FINAL";

	public static void topKHeap(int topCount) {
		final PriorityQueue<Utils.WordFreq> topKHeap = new PriorityQueue<Utils.WordFreq>(topCount);
		readFile(FINAL_FILE, topKHeap, topCount);
		displayTopKHeap(topKHeap, topCount);
	}

	private static void displayTopKHeap(PriorityQueue<Utils.WordFreq> topKHeap, int topCount) {
		final String[] topK = new String[topCount];
		int i = 0;
		while (topKHeap.size() > 0) {
			Utils.WordFreq wf = topKHeap.remove();
			String sequence = wf.word;
			int count = wf.freq;
			topK[i++] = sequence;
			System.out.println(sequence + " - " + count);
		}

	}

	private static void readFile(String filePath, PriorityQueue<Utils.WordFreq> topKHeap, int topCount) {
		try {
			InputStream in = new FileInputStream(new File(filePath));
			InputStreamReader inR = new InputStreamReader(in);
			BufferedReader buf = new BufferedReader(inR, 1024);
			String line;
			String prev = buf.readLine();
			//
			
			int itemCount = 1;
			while ((line = buf.readLine()) != null) {
				if (line.equals(prev)) {
					itemCount++;
				} else {
					topKSequences(topCount, topKHeap, prev, itemCount);
					itemCount = 1;

				}
				prev = line;

			}
			topKSequences(topCount, topKHeap, prev, itemCount);
			in.close();
			inR.close();
			buf.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}

	public static void topKSequences(final int k, PriorityQueue<Utils.WordFreq> topKHeap, String kmer, int itemCount) {

		if (topKHeap.size() < k) {
			topKHeap.add(new Utils.WordFreq(kmer, itemCount));
		} else if (itemCount > topKHeap.peek().freq) {
			topKHeap.remove();
			topKHeap.add(new Utils.WordFreq(kmer, itemCount));
		}
	}

}

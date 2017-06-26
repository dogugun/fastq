package main.java.com.node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import main.java.com.node.Utils.WordFreq;



public class FastQReader {

	
	private static String OUTPUT_FILE = "data/OUTPUT";
		
	public static void calculateDirectly(String filePath, String seqPrefix, int seqLength, int topCount) {
		try {

			InputStream in = new FileInputStream(new File(filePath));
			InputStreamReader inR = new InputStreamReader(in);
			BufferedReader buf = new BufferedReader(inR, 1024);

			String line;

			boolean ready = false;
			long rowcount = 0;
			List<String> sequenceBuffer = new ArrayList<String>();
			List<String> globalBuffer = new ArrayList<String>();
			while ((line = buf.readLine()) != null) {
				String sequence = "";

				if (line.startsWith(seqPrefix)) {
					ready = true;
				}
				if (ready) {

					sequence = buf.readLine();
					sequenceBuffer.add(sequence);
					rowcount++;
					if (rowcount == 100) {
						List<String> kmers = new ArrayList<>();
						kmers = generate_kmer(sequenceBuffer, seqLength);
						globalBuffer.addAll(kmers);
						sequenceBuffer.clear();
						rowcount = 0;
					}
					ready = false;

				}
			}

			List<String> kmers = new ArrayList<>();
			kmers = generate_kmer(sequenceBuffer, seqLength);
			globalBuffer.addAll(kmers);
			sequenceBuffer.clear();
			rowcount = 0;

			in.close();
			inR.close();
			buf.close();
			topKSequences(convertToFreqMap(globalBuffer), topCount);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}
	
	public static void prepareOutputFile(String filePath, String seqPrefix, int seqLength) {
		try {

			File file = new File("data");
	        if (!file.exists()) {
	            if (file.mkdir()) {
	                System.out.println("data folder is created!");
	            } 
	        }
			
			InputStream in = new FileInputStream(new File(filePath));
			InputStreamReader inR = new InputStreamReader(in);
			BufferedReader buf = new BufferedReader(inR, 1024);

			String line;

			boolean ready = false;
			long rowcount = 0;
			List<String> sequenceBuffer = new ArrayList<String>();
			while ((line = buf.readLine()) != null) {
				String sequence = "";

				if (line.startsWith(seqPrefix)) {
					ready = true;
				}
				if (ready) {

					sequence = buf.readLine();
					sequenceBuffer.add(sequence);
					rowcount++;
					if (rowcount == 100) {
						List<String> kmers = new ArrayList<>();
						kmers = generate_kmer(sequenceBuffer, seqLength);
						writeToOutputFile(kmers);
						sequenceBuffer.clear();
						rowcount = 0;
					}
					ready = false;

				}
			}

			List<String> kmers = new ArrayList<>();
			kmers = generate_kmer(sequenceBuffer, seqLength);
			writeToOutputFile(kmers);
			sequenceBuffer.clear();
			rowcount = 0;

			in.close();
			inR.close();
			buf.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}


	private static void writeToOutputFile(List<String> kmers) {
		try {
			FileWriter fw = new FileWriter(OUTPUT_FILE, true);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String kmer : kmers) {
				bw.write(kmer + '\n');

			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<String, Long> convertToFreqMap(List<String> kmers) {

		Map<String, Long> subMap = kmers.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));

		return subMap;
	}

	private static List<String> generate_kmer(List<String> sequenceBuffer, int k_merLength) {

		List<String> kmers = new ArrayList<String>();
		for (String sequence : sequenceBuffer) {
			for (int i = 0; i <= sequence.length() - k_merLength; i++) {
				String substr = sequence.substring(i, i + k_merLength);
				kmers.add(substr);
			}
		}
		return kmers;
	}

	public static String[] topKSequences(Map<String, Long> map, final int k) {
		final PriorityQueue<WordFreq> topKHeap = new PriorityQueue<WordFreq>(k);
		for (String key : map.keySet()) {
			String sequence = key;
			int count = map.get(key).intValue();
			if (topKHeap.size() < k) {
				topKHeap.add(new WordFreq(sequence, count));
			} else if (count > topKHeap.peek().freq) {
				topKHeap.remove();
				topKHeap.add(new WordFreq(sequence, count));
			}
		}

		// extract the top K
		final String[] topK = new String[k];
		int i = 0;
		while (topKHeap.size() > 0) {
			WordFreq wf = topKHeap.remove();
			String sequence = wf.word;
			int count = wf.freq;
			topK[i++] = sequence;
			System.out.println(sequence + " - " + count);
		}
		return topK;
	}

	
}

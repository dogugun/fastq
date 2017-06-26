package main.java.com.node;

// filename: ExternalSort.java
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class ExternalSorting {

	public static final int DEFAULTMAXTEMPFILES = 1024;

	public static void externalSort(long blockSize) {
		try {

		int maxtmpfiles = DEFAULTMAXTEMPFILES;
		Charset cs = Charset.defaultCharset();
		String inputfile = "data/OUTPUT";
		String outputfile = "data/FINAL";
		File tempFileStore = null;

		Comparator<String> comparator = defaultcomparator;
		File file = new File(inputfile);
		BufferedReader fbr = new BufferedReader(new InputStreamReader(new FileInputStream(file), cs));
		//split the whole output file and sort
		List<File> l = sortInBatch(fbr, file.length(), comparator, maxtmpfiles,
				Utils.getAvailableMemory(), cs, tempFileStore, blockSize);

		//merge sorted subfiles
		mergeSortedFiles(l, new File(outputfile), comparator, cs, false);
		} catch (Exception x) {
			x.printStackTrace();
		}
		
	}

	public static int mergeSortedFiles(List<File> files, File outputfile, final Comparator<String> cmp, Charset cs,
			boolean append) throws IOException {
		ArrayList<BinaryFileBuffer> bfbs = new ArrayList<>();
		for (File f : files) {

			InputStream in = new FileInputStream(f);
			BufferedReader br;

			br = new BufferedReader(new InputStreamReader(in, cs));

			BinaryFileBuffer bfb = new BinaryFileBuffer(br);
			bfbs.add(bfb);
		}
		BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile, append), cs));

		PriorityQueue<BinaryFileBuffer> heap = new PriorityQueue<>(11, new Comparator<BinaryFileBuffer>() {
			@Override
			public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {
				return cmp.compare(i.peek(), j.peek());
			}
		});
		for (BinaryFileBuffer bfb : bfbs) {
			if (!bfb.empty()) {
				heap.add(bfb);
			}
		}
		int rowcounter = 0;
		try {
			while (heap.size() > 0) {
				BinaryFileBuffer bfb = heap.poll();
				String r = bfb.pop();
				fbw.write(r);
				fbw.newLine();
				++rowcounter;
				if (bfb.empty()) {
					bfb.fbr.close();
				} else {
					heap.add(bfb);
				}
			}

		} finally {
			fbw.close();
			for (BinaryFileBuffer bfb : heap) {
				bfb.close();
			}
		}
		for (File f : files) {
			f.delete();
		}
		return rowcounter;
	}



	public static List<File> sortInBatch(final BufferedReader fbr, final long datalength, final Comparator<String> cmp,
			final int maxtmpfiles, long maxMemory, final Charset cs, final File tmpdirectory, long blockSize) throws IOException {
		List<File> files = new ArrayList<>();
		long bestBlockSize = 0;
		if(blockSize<=0) {
			bestBlockSize = Utils.getBlockSize(datalength, maxtmpfiles, maxMemory);
		} else {
			bestBlockSize = blockSize;
		}
		try {
			List<String> tmplist = new ArrayList<>();
			String line = "";
			try {
				while (line != null) {
					long currentblocksize = 0;
					while ((currentblocksize < bestBlockSize) && ((line = fbr.readLine()) != null)) {

						tmplist.add(line);
						currentblocksize += Utils.getStringSize(line);
					}
					files.add(sortAndSave(tmplist, cmp, cs, tmpdirectory));
					tmplist.clear();
				}
			} catch (EOFException oef) {
				if (tmplist.size() > 0) {
					files.add(sortAndSave(tmplist, cmp, cs, tmpdirectory));
					tmplist.clear();
				}
			}
		} finally {
			fbr.close();
		}
		return files;
	}
	
	public static File sortAndSave(List<String> tmplist, Comparator<String> cmp, Charset cs, File tmpdirectory)
			throws IOException {

		tmplist = tmplist.parallelStream().sorted(cmp).collect(Collectors.toCollection(ArrayList<String>::new));
		File newtmpfile = File.createTempFile("sortInBatch", "flatfile", tmpdirectory);
		newtmpfile.deleteOnExit();
		OutputStream out = new FileOutputStream(newtmpfile);

		try (BufferedWriter fbw = new BufferedWriter(new OutputStreamWriter(out, cs))) {
			for (String r : tmplist) {
				fbw.write(r);
				fbw.newLine();
			}

		}
		return newtmpfile;
	}

	public static Comparator<String> defaultcomparator = new Comparator<String>() {
		@Override
		public int compare(String line1, String line2) {
			return line1.compareTo(line2);
		}
	};

}

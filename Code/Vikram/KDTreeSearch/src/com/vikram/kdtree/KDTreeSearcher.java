package com.vikram.kdtree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.javaml.core.kdtree.KDTree;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

public class KDTreeSearcher {
	public static final Integer NumSearchResults = 6;
	public static final Integer QueryVectorsSamplingInterval = 1;
	
	// KDTREE CONFIGURATION
	/*
	static final Integer Dims = 128;
	static final Integer NumNeighbours = 1;
	static final Integer Margin = 50;
	 */
	//
	static Integer Dims = null;
	static final String DimsKey = "featureVectorDims";
	static final Integer NumNeighbours = 1;
	static final Integer Margin = 50;
	// END KDTREE CONFIGURATION

	// KDTREE DATA STRUCTURE
	private static KDTree kdTreeObj = null;
	private static AmazonS3 s3Client = new AmazonS3Client();
	private static final String MetadataExt = "xml";

	static {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("Config.properties"));
			KDTreeSearcher.Dims = Integer.valueOf(properties.getProperty(DimsKey));
		} catch (Exception e) {
			System.out.println("ERR: Could not read property so using default : " + DimsKey);
			KDTreeSearcher.Dims = 64;
		}
		kdTreeObj = new KDTree(KDTreeSearcher.Dims);
	}
	
	public static void main(String[] args) {
		// TEST CONFIGURATION
		/*
		 * String searchFrame = "lena"; String basePath =
		 * "C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/KDTreeSearch/DaveMetadata/SmallDataset/features";
		 * Boolean doNotMatchQueryFrame = true;
		 */
		//
		String searchFrame = "465.jpg";
		String basePath = "C:/Users/vikmenon/Documents/VikramWorkingDirectory/EclipseWorkspace1/KDTreeSearch/DaveMetadata/BigDataset/features/";
		Boolean doNotMatchQueryFrame = true;
		// END TEST CONFIGURATION

		String fileToSearch = searchFrame + "." + KDTreeSearcher.MetadataExt;
		File dir = new File(basePath);

		File[] directoryListing = dir.listFiles();

		if (directoryListing != null) {
			for (File child : directoryListing) {
				// We now process all the files except the query file.
				if (!(doNotMatchQueryFrame && child.getName().equals(
						fileToSearch))) {
					kdTreeInsertVectorsForFile(kdTreeObj, child);
				}
			}

			System.out.println("Insert phase complete.");

			// Process the query file.
			SearchResults results = new SearchResults();
			findBestMatches(basePath + fileToSearch, results, kdTreeObj);
			System.out.println("The best matched movie for the query "
					+ fileToSearch + " was: " + results.heap.peek());

		} else {
			System.out.println("ERR: Not a directory! " + basePath);
		}
	}

	private static class SearchResults {
		public Map<String, Integer> counterMap = new HashMap<String, Integer>();
		public PriorityQueue<String> heap = new PriorityQueue<String>(10, new MyComparator());
		
		public class MyComparator implements Comparator<String>
		{
			public int compare( String file1, String file2 )
			{
				return counterMap.get(file2) - counterMap.get(file1);
			}
		};
		
		public void addOccurrence(String folderName) {
			if (counterMap.containsKey(folderName)) {
				// Increment the occurrence count for this file
				counterMap.put(folderName,
						counterMap.get(folderName) + 1);
			} else {
				// Initialize the occurrence count for this file
				counterMap.put(folderName, 1);
			}
			heap.remove(folderName);
			heap.add(folderName);
		}
	};
	
	private static void findBestMatches(String fileNameToSearch, SearchResults results, KDTree kdTreeObj) {
		findBestMatches(new File(fileNameToSearch), results, kdTreeObj);
	}
	
	private static void findBestMatches(File fileToSearch, SearchResults results, KDTree kdTreeObj) {
		// Search the KDTree to find the best matches for the query file

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileToSearch));

			boolean dataHasStarted = false;
			List<String> allMatches = new ArrayList<String>();

			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					// Before data tag is reached
					if (line.contains("<data>")) {
						dataHasStarted = true;
						continue;
					}
					// Once data tag is reached
					if (dataHasStarted) {
						Matcher m = Pattern.compile(" \\d+").matcher(line);
						while (m.find()) {
							allMatches.add(m.group());
						}
					}
				}
				br.close();
			} catch (IOException e) {
				System.out.println("ERR: Failed to read a line in the file: "
						+ fileToSearch);
			}

			// Now iterate over the values and compare them to the KDTree
			Iterator<String> iter = allMatches.iterator();
			ArrayList<Double> doubleBuffer = new ArrayList<Double>();

			int count = 0, count2 = 0;
			while (iter.hasNext()) {
				count++;
				doubleBuffer.add(Double.valueOf(iter.next().trim()));

				if (count == KDTreeSearcher.Dims) {
					count2++;
					if (count2 % QueryVectorsSamplingInterval != 0) {
						// Skip this vector
						doubleBuffer.clear();
						count = 0;
						continue;
					}
					
					// We can build a key and insert it into the KDTree: new key
					// => filename (value)
					double[] newKey = new double[KDTreeSearcher.Dims];
					int indx = 0;
					for (Double val : doubleBuffer) {
						newKey[indx++] = (double) val;
					}

					// Search the KDTree
					// double[] deltas = new double[KDTreeSearcher.Dims];
					// Hack! TODO: Use the top N matches for a given frame, not just the top match.
					for (Object currentMatchedFilePathObj : kdTreeObj.nearest(
							newKey, KDTreeSearcher.NumNeighbours)) {
						String currentMatchedFile = (String) currentMatchedFilePathObj;
						
						/* Register an occurrence of the parent file (movie) as the nearest match for this vector. */
						// Add the folder:
						results.addOccurrence(new File(currentMatchedFile).getParentFile().getName());
						
						// Add the file:
						// results.addOccurrence(currentMatchedFile);
					}

					// Clear the buffer of double values
					doubleBuffer.clear();
					count = 0;
				}
			}
		} catch (Exception e1) {
			// Exception in BufferedReader!
			e1.printStackTrace();
		}
	}

	private static boolean kdTreeInsertVectorsForFile(KDTree kdTreeObj,
			File child) {
		// Add vectors from this file to the KDTree
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(child));

			boolean dataHasStarted = false;
			List<String> allMatches = new ArrayList<String>();

			try {
				String line = null;
				while ((line = br.readLine()) != null) {
					// Before data tag is reached
					if (line.contains("<data>")) {
						dataHasStarted = true;
						continue;
					}
					// Once data tag is reached
					if (dataHasStarted) {
						Matcher m = Pattern.compile(" \\d+").matcher(line);
						while (m.find()) {
							allMatches.add(m.group());
						}
					}
				}
				br.close();
			} catch (IOException e) {
				System.out.println("ERR: Failed to read a line in the file: "
						+ child.getName());
			}

			// Now iterate over the values and push them into the KDTree
			Iterator<String> iter = allMatches.iterator();
			ArrayList<Double> doubleBuffer = new ArrayList<Double>();

			int count = 0;
			while (iter.hasNext()) {
				count++;
				doubleBuffer.add(Double.valueOf(iter.next().trim()));

				if (count == KDTreeSearcher.Dims) {
					// We can build a key and insert it into the KDTree: new key
					// => filename (value)
					double[] newKey = new double[KDTreeSearcher.Dims];
					int indx = 0;
					for (Double val : doubleBuffer) {
						newKey[indx++] = (double) val;
					}

					// Insert into the KDTree
					kdTreeObj.insert(newKey, child.getAbsolutePath());

					// Clear the buffer of double values
					doubleBuffer.clear();
					count = 0;
				}
			}

			// All the vectors for this file have been inserted into the KDTree.
			return true;
		} catch (Exception e1) {
			// Exception in BufferedReader!
			e1.printStackTrace();
			return false;
		}
	}

	public int insertFrames(String framesPathKey, String bucketName) {
		System.out.println("INSERTING in bucket " + bucketName
				+ " for framesPathKey: " + framesPathKey);
		int numAdded = 0;

		for (String childFileKey : S3FileUtils.listKeysInDirectory(framesPathKey,
				bucketName, s3Client)) {
			// Download the frame metadata file
			File localFileCopy = S3FileUtils.downloadFile(childFileKey, bucketName,
					childFileKey, s3Client);

			// Now process the frame metadata file
			kdTreeInsertVectorsForFile(kdTreeObj, localFileCopy);
			numAdded++;
		}

		// Delete the temporary directory recursively
		S3FileUtils.deleteLocalFolder(framesPathKey);

		System.out.println("Insert phase complete.");

		return numAdded;
	}

	public List<String> searchFrames(String queryFramesPathKey, String bucketName) {
		System.out.println("SEARCHING in bucket " + bucketName
				+ " for queryFramesPathKey: " + queryFramesPathKey);
		List<String> bestMatchedFramesUrl = new ArrayList<String>();

/*		queryFrameKey = "FeatureVectors/0wphXCXaWgtzk2ehm27WqMdPZNRYZ2bF/4.jpg.xml";
		File localFileCopy = downloadFile(queryFrameKey, "syncre-datasets", queryFrameKey, s3Client);*/
		S3FileUtils.listBucketInfoAndExit(s3Client, true, false);

		SearchResults results = new SearchResults();
		for (String childFileKey : S3FileUtils.listKeysInDirectory(queryFramesPathKey,
				bucketName, s3Client)) {
			// Download the frame metadata file
			File localFileCopy = S3FileUtils.downloadFile(childFileKey, bucketName,
					childFileKey, s3Client);

			// Process the query file.
			findBestMatches(localFileCopy, results, kdTreeObj);
		}

		// Delete the temporary directory recursively
		S3FileUtils.deleteLocalFolder(queryFramesPathKey);

		// Pick top matches by combining results for all key frames under this query key
		String nextFolderName = null;
		for (int i = 0; i < NumSearchResults; i++) {
			if ((nextFolderName = results.heap.poll()) == null)
				break;
			bestMatchedFramesUrl.add(nextFolderName);
		}

		System.out.println("The " + NumSearchResults + " best matched movies for the query key "
				+ queryFramesPathKey + " were: " + bestMatchedFramesUrl.toString());

		return bestMatchedFramesUrl;
	}

	public static void test_simple_kdtree(String[] args) {
		double[] A = { 2, 5 };
		double[] B = { 1, 1 };
		double[] C = { 3, 9 };
		double[] T = { 1, 10 };

		// make a KD-tree and add some nodes
		KDTree kd = new KDTree(2);

		try {
			kd.insert(A, new String("Value 1"));
			kd.insert(B, new String("Value 2"));
			kd.insert(C, new String("Value 3"));
		} catch (Exception e) {
			System.err.println(e);
		}

		// look for node B
		try {
			String n = (String) kd.search(B);
			System.err.println("Node B's nearest: " + n);
		} catch (Exception e) {
			System.err.println(e);
		}

		try {
			// find T's nearest neighbor, which should be Value 3
			int numNearest = 1;
			String n[] = Arrays.copyOf(kd.nearest(T, numNearest), numNearest,
					String[].class);
			;
			System.err.println("Search vector's nearest " + numNearest
					+ " are: " + n[0] + " ; size of retlist: " + n.length);

			// remove C from the tree
			kd.delete(C);

			// now T's nearest neighbor should be Value 1
			n = Arrays.copyOf(kd.nearest(T, numNearest), numNearest,
					String[].class);

			System.err.println("Search vector's nearest " + numNearest
					+ " are: " + n[0] + " ; size of retlist: " + n.length);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void startS3Client(BasicAWSCredentials credentialsArg) {
		KDTreeSearcher.s3Client = new AmazonS3Client(credentialsArg);
	}
}

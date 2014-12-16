package com.vikram.kdtree;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public abstract class S3FileUtils {

	public static void deleteLocalFolder(String fileName) {
		deleteLocalFolder(new File(fileName));
	}
	
	public static void deleteLocalFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) { // some JVMs return null for empty dirs
			for (File f : files) {
				if (f.isDirectory()) {
					deleteLocalFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	public static List<String> listKeysInDirectory(String prefix,
			String bucketName, AmazonS3 currS3Client) {
		return listKeysInDirectory(prefix, bucketName, currS3Client, true);
	}

	public static List<String> listKeysInDirectory(String prefix,
			String bucketName, AmazonS3 currS3Client, Boolean appendDelimiter) {
		String delimiter = AWSDeploymentManager.S3PrefixDelimiter;
		if (appendDelimiter && !prefix.endsWith(delimiter)) {
			prefix += delimiter;
		}
		System.out.println("Listing keys from bucket " + bucketName
				+ " under prefix: " + prefix);

		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName).withPrefix(prefix);

		// Read all pages of results (as MaxKeys = 1000)
		ObjectListing objectListing = currS3Client
				.listObjects(listObjectsRequest);
		List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();
		while (objectListing.isTruncated()) {
			objectListing = currS3Client.listNextBatchOfObjects(objectListing);
			summaries.addAll(objectListing.getObjectSummaries());
		}

		// Extract the keys from the object summaries
		List<String> retList = new ArrayList<String>();
		for (S3ObjectSummary objSumm : summaries) {
			retList.add(objSumm.getKey());
		}
		return retList;
	}

	public static File downloadFile(String s3FileKey, String bucketName,
			String localFilePath, AmazonS3 currS3Client) {
		System.out.println("Downloading object from bucket " + bucketName
				+ " with key: " + s3FileKey);
		S3Object object = currS3Client.getObject(new GetObjectRequest(
				bucketName, s3FileKey));
		InputStream reader = new BufferedInputStream(object.getObjectContent());
		File localFile = new File(localFilePath);
		OutputStream writer = null;

		// Ensure that we can write to this file by creating parent directory
		localFile.getParentFile().mkdirs();

		try {
			writer = new BufferedOutputStream(new FileOutputStream(localFile));
		} catch (FileNotFoundException e) {
			System.out
					.println("ERR: Failed to create local copy of file from S3: "
							+ localFilePath);
			e.printStackTrace();
		}

		int read = -1;

		try {
			while ((read = reader.read()) != -1) {
				writer.write(read);
			}

			writer.flush();
			writer.close();
			reader.close();
		} catch (IOException e) {
			System.out
					.println("ERR: IOException on attempting to read from S3/write to local file: "
							+ localFilePath);
			e.printStackTrace();
		}

		return localFile;
	}

	public static void listBucketInfoAndExit(AmazonS3 currS3Client,
			Boolean listBucketContents, Boolean exitProgram) {
		if (listBucketContents) {
			// Rahul buckets
//			String bucket = "sincre-data";
			// Dave buckets
			String bucket = "syncre-datasets";
//			String bucket = "syncre-keyframes";
			for (String key : listKeysInDirectory("", bucket,
					currS3Client, false)) {
				System.out.println(bucket + " CONTAINS: " + key);
			}
		}
		if (exitProgram) {
			System.exit(0);
		}
	}
}


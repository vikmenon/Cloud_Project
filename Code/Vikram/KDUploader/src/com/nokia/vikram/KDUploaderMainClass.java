package com.nokia.vikram;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nokia.vikram.frames.SimilarFrameEliminator;
import com.nokia.vikram.frames.ThumbsGenerator;
import com.nokia.vikram.sift.SIFTGenerator;

public class KDUploaderMainClass {
	private static Properties deployerProperties = new Properties();
	private static BasicAWSCredentials credentials;
	private static AmazonS3 s3Client = null;
	
	private static final boolean SkipXuggleForWindows = true;
	
	private static String s3VideosBucket = null;
	private static String s3VideosKey = null;

	private static final String AwsAccessKey = "awsAccessKey";
	private static final String AwsSecretKey = "awsSecretKey";
	private static final String S3VideosBucketCfg = "s3VideosBucket";
	private static final String S3VideosKeyCfg = "s3VideosKey";
	
	static {
		try {
			deployerProperties.load(new FileInputStream("KDUploader.properties"));
			credentials = new BasicAWSCredentials(
					deployerProperties.getProperty(AwsAccessKey),
					deployerProperties.getProperty(AwsSecretKey)
				);
			s3Client = new AmazonS3Client(credentials);
			s3VideosBucket = deployerProperties.getProperty(S3VideosBucketCfg);
			s3VideosKey = deployerProperties.getProperty(S3VideosKeyCfg);
		} catch (Exception e) {
			System.out.println("Exception while creating S3 client: " + e);
		}
	}

	/* Generates keyframes and metadata and pushes to S3 with the required key
	 *   => CLI for upload/search
	 * TODO: Python server + web client for upload/search
	 */
	public static void main(String[] args) {
		String localFileName = null;
		Integer operation = null;
		
		if (args.length != 2) {
			System.out.println("\nERR: Arguments required in the format: filepath operation\nwhere operation is 0 for INSERT and 1 for SEARCH in the video database.\n");
			System.exit(0);
		} else {
			String[] localFilePathParts = args[0].split("/");
			localFileName = localFilePathParts[localFilePathParts.length - 1];
			operation = Integer.valueOf(args[1]);
		}
		
		generateKeyframesAndMetadataForVideo(localFileName);
		
		if (s3VideosBucket != null && s3VideosKey != null) {
			callBackEndOperation(
					uploadAndCleanupKeyframesAndMetadata(localFileName, s3VideosKey, s3VideosBucket, false),
					operation
				);
		}
		
		System.out.println("\n--- KD FRAMES UPLOADER PROCESS COMPLETE ---");
	}

	private static boolean callBackEndOperation(String uploadKeyframesAndMetadata, Integer operation) {
		// TODO
		return true;
	}

	private static String uploadAndCleanupKeyframesAndMetadata(String movieName, String s3VideosKeyArg, String s3VideosBucketArg, boolean deleteTempData) {
		s3VideosKeyArg += '/' + movieName + '/';
		
		// Push the keyframes to S3 bucket
		File keyframesDir = new File(SimilarFrameEliminator.ReducedFramesDir + '/' + movieName);
		for (File file : keyframesDir.listFiles()) {
			if (! file.getName().contains("thumbs.db")) {
				System.out.println("Starting upload to S3: " + file.getAbsolutePath());
				s3Client.putObject(new PutObjectRequest(s3VideosBucketArg, 
										s3VideosKeyArg + file.getName(), file));
			}
		}
		
		// Push the metadata to S3 bucket
		File metadataFilesDir = new File(SIFTGenerator.SiftMetadataDir + '/' + movieName);
		for (File file : metadataFilesDir.listFiles()) {
			System.out.println("Starting upload to S3: " + file.getAbsolutePath());
			s3Client.putObject(new PutObjectRequest(s3VideosBucketArg, 
									s3VideosKeyArg + file.getName(), file));
		}
		
		// Delete all the temporary data
		if (deleteTempData) {
			new File(movieName).delete();
			
			S3FileUtils.deleteLocalFolder(ThumbsGenerator.AllFramesDir);
			S3FileUtils.deleteLocalFolder(SimilarFrameEliminator.ReducedFramesDir);
			S3FileUtils.deleteLocalFolder(SIFTGenerator.SiftMetadataDir);
		}
		
		return movieName;
	}

	private static boolean generateKeyframesAndMetadataForVideo(String localFilePath) {
		// Algorithm to generate SIFT video metadata
		if (! SkipXuggleForWindows) {
			ThumbsGenerator.generateThumbnailsForFile(new File(localFilePath), ThumbsGenerator.AllFramesDir);
			SimilarFrameEliminator.eliminateFrames(ThumbsGenerator.AllFramesDir, SimilarFrameEliminator.ReducedFramesDir);
		}
		SIFTGenerator.generateMetadataForFrames(SimilarFrameEliminator.ReducedFramesDir, SIFTGenerator.SiftMetadataDir);
		
		return true;
	}
}

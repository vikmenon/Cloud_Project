package com.nokia.vikram;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.nokia.vikram.frames.SimilarFrameEliminator;
import com.nokia.vikram.frames.ThumbsGenerator;
import com.nokia.vikram.lire.LireIndexer;

public class GreatDeployer {
	private Properties deployerProperties = new Properties();
	private BasicAWSCredentials credentials;
	private AmazonS3 s3Client = new AmazonS3Client();
	private static volatile GreatDeployer greatDeployer = new GreatDeployer();

	private static final String AwsAccessKey = "awsAccessKey";
	private static final String AwsSecretKey = "awsSecretKey";
	
	private static final String S3VideosBucket = "s3VideosBucket";
	private static final String S3VideosKey = "s3VideosKey";
	
	private static final String ScpRemoteUser = "scpRemoteUser";
	private static final String ScpRemotePass = "scpRemotePass";
	private static final String ScpRemoteHost = "scpRemoteHost";
	private static final String ScpRemotePath = "scpRemotePath";
	
	private static final String LocalVideoCopiesDir = "LocalVideoCopies" + File.separator;
	
	private GreatDeployer() {
		try {
			deployerProperties.load(new FileInputStream("GreatDeployer.properties"));
			this.credentials = new BasicAWSCredentials(
					deployerProperties.getProperty(AwsAccessKey),
					deployerProperties.getProperty(AwsSecretKey)
				);
			this.s3Client = new AmazonS3Client(this.credentials);
		} catch (Exception e) {
			System.out.println("Exception while creating S3 client: " + e);
		}
	}

    public static GreatDeployer getInstance(){
        return greatDeployer;
    }
 
	public static void main(String[] args) {
		/* TODO Implement:
		 *  Given: Bucket/VideosFolder + bucket [or list of keys], scp URI for the index on an EBS instance
		 * X Pull all videos in the directory
		 * X Extract frames for all videos in the directory
		 * X Eliminate duplicate frames for all videos and write resultant set to another directory
		 * X Build the LIRE index from the reduced set of frames (image => frame link)
		 * - Write common METHOD: Insert frame(s) into existing LIRE index
		 */

		// Should we read the file from S3 or the local directory?
		String s3VideosKey = greatDeployer.deployerProperties.getProperty(S3VideosKey);
		String s3VideosBucket = greatDeployer.deployerProperties.getProperty(S3VideosBucket);

		if (s3VideosBucket != null && s3VideosKey != null) {
			greatDeployer.updateIndexWithVideos(s3VideosKey, s3VideosBucket);
		}
		else {
			greatDeployer.updateIndexWithVideos(getFiles(LocalVideoCopiesDir), LocalVideoCopiesDir, false);
		}
		
		System.out.println("\n--- GREAT DEPLOYER PROCESS COMPLETE ---");
	}

	private static List<File> getFiles(String videosDir) {
		File dir = new File(videosDir);
		File[] files = dir.listFiles();
		if (files == null) {
			System.out.println("ERR: Not a directory! " + videosDir);
			return null;
		}
		
		return new ArrayList<File>(Arrays.asList(files));
	}

	private boolean updateIndexWithVideos(String s3VideosKey, String bucketName) {
		// Read video names at key, download them, create a list and pass it on
		List<File> videoFiles = new ArrayList<File>();
		for (String videoKey : S3FileUtils.listKeysInDirectory(s3VideosKey, bucketName, s3Client)) {
			videoFiles.add(
				S3FileUtils.downloadFile(videoKey, bucketName,
				LocalVideoCopiesDir + videoKey, s3Client));
		}
		return updateIndexWithVideos(videoFiles, LocalVideoCopiesDir, false);
	}

	private boolean updateIndexWithVideos(List<File> videoFiles, String inputVideosDir, Boolean deleteTempData) {
		
		// Algorithm to generate LIRE video search index
		ThumbsGenerator.generateThumbnailsForFiles(videoFiles, inputVideosDir, ThumbsGenerator.AllFramesDir);
		SimilarFrameEliminator.eliminateFrames(ThumbsGenerator.AllFramesDir, SimilarFrameEliminator.ReducedFramesDir);
		LireIndexer.indexImages(SimilarFrameEliminator.ReducedFramesDir, LireIndexer.LireIndexDir);
		
		// Push the index to a remote machine
		scpIndexCopy(LireIndexer.LireIndexDir);
		
		// Delete all the temporary data
		if (deleteTempData) {
			for (File videoFile : videoFiles) {
				videoFile.delete();
			}
			S3FileUtils.deleteLocalFolder(ThumbsGenerator.AllFramesDir);
			S3FileUtils.deleteLocalFolder(SimilarFrameEliminator.ReducedFramesDir);
			S3FileUtils.deleteLocalFolder(LireIndexer.LireIndexDir);
		}
		
		return true;
	}
	
	public void scpIndexCopy(String localFilePath) {
		/* NOTE: If keyless login is disabled, then SSH keys must be set up on the local/remote machines
		 */
		String str;
		Process proc;
		try {
			// Read remote machine info from properties
			String user = deployerProperties.getProperty(ScpRemoteUser);
			String passwd = deployerProperties.getProperty(ScpRemotePass);
			String host = deployerProperties.getProperty(ScpRemoteHost);
			String remoteFilePath = deployerProperties.getProperty(ScpRemotePath);
			
			// Build the command
			String scpCommand = "scp -rv "
					+ localFilePath + " " 
					+ user + "@"
					+ host + ":"
					+ remoteFilePath;
			
			System.out.println("Executing: " + scpCommand);
			proc = Runtime.getRuntime().exec(scpCommand);
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(proc.getOutputStream()));
			BufferedReader br = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
			
			// Process the response from the remote server if any.
			while (br.ready() && (str = br.readLine()) != null) {
				System.out.println("STDOUT: " + str);
				if (str.contains("re you sure")) {
					bw.write("yes");
					bw.flush();
				} else if (str.contains("assword:")) {
					bw.write(passwd);
					bw.flush();
				}
			}
			proc.waitFor();
			System.out.println ("Process exit code was: " + proc.exitValue());
			proc.destroy();
		} catch (Exception e) {}
	}
}

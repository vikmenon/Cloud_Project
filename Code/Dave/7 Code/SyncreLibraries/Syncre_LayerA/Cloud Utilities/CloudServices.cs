using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

using Utilities;

namespace CloudServices
{

    public static class CloudServices
    {

        #region AWS S3 Services

        static string videoLocalFolder = "syncre-videos";
        static string datasetsLocalFolder = "syncre-datasets";
        static string framesLocalFolder = "syncre-keyframes";

        //downloads the specified video from the cloud. Returns the local path and filename of the video
        public static string DownloadVideo(string Videofilename, ref string errorMsg)
        {
            string localFilename = aws_s3.Read(videoLocalFolder, Videofilename, ref errorMsg);

            if (errorMsg != "")
            {
                LogManager.Write(LogManager.LogType.ERROR, errorMsg);
                errorMsg = "Error while downloading video from cloud services. <Video may already exist on server.>";
            }
            else
                errorMsg = "";

            return localFilename;
        }

        //upload frames set to cloud
        public static void UploadFrames(/*List<string> keyFramesFileNames,*/ string videoKeyname, ref string errMsg)
        {
            //if (keyFramesFileNames == null)
            //{
            //    errMsg = "No keyframes to upload.";
            //    return;
            //}

            DirectoryInfo dirInfo = new DirectoryInfo(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, framesLocalFolder, videoKeyname));
            foreach (FileInfo file in dirInfo.GetFiles())
            {
                UploadFrame(file.FullName, videoKeyname + "/" + file.Name, ref errMsg);
            }

            //for (int i = 0; i < keyFramesFileNames.Count; i++)
            //{
            //    Uri uri = new Uri(keyFramesFileNames[i]);              
            //    string filename = Path.GetFileName(uri.LocalPath);

            //    string destRelPath = videoKeyname + "/";

            //    UploadFrame(keyFramesFileNames[i], destRelPath + filename, ref errMsg);
            //}
        }

        //uploads frame to cloud storage
        //NOTE: "Relative path" in destRelPathAndFile means without the "bucket name" on cloud store. e.g: video2/keyframe4
        private static void UploadFrame(string SrcFileAndPath, string destRelPathAndFile, ref string errorMsg)
        {
            aws_s3.Write(SrcFileAndPath, framesLocalFolder, destRelPathAndFile, ref errorMsg);

            if (errorMsg != "")
            {
                errorMsg = "Error while uploading video to cloud services";
                LogManager.Write(LogManager.LogType.ERROR, errorMsg);
            }
            else
                errorMsg = "";
        }

        //upload features data set to cloud store
        public static void UploadFeaturesData(/*List<string> featuresDataFileNames,*/ string videoKeyname, ref string errMsg)
        {
            //if (featuresDataFileNames.Count == 0)
            //{
            //    errMsg = "No features data to upload.";
            //    return;
            //}

            DirectoryInfo dirInfo = new DirectoryInfo(Path.Combine(AppDomain.CurrentDomain.BaseDirectory, datasetsLocalFolder, videoKeyname));
            foreach (FileInfo file in dirInfo.GetFiles())
            {
                //string fileName = file.Name.Substring(0, file.Name.Count() - 4);
                UploadFeaturesData(file.FullName, videoKeyname + "/" + file.Name, ref errMsg);
            }

            //for (int i = 0; i < featuresDataFileNames.Count; i++)
            //{
            //    //string destRelPath = videoKeyname + "/";
            //    string destFileName = i + 1 + ".xml";

            //    UploadFeaturesData(featuresDataFileNames[i], destRelPath + destFileName, ref errMsg);
            //}
        }

        //uploads features data to cloud store
        private static void UploadFeaturesData(string SrcFileAndPath, string destRelPathAndFile, ref string errorMsg)
        {
            string exactLocation = "FeatureVectors/" + destRelPathAndFile;
            aws_s3.Write(SrcFileAndPath, datasetsLocalFolder, exactLocation, ref errorMsg);

            if (errorMsg != "")
            {
                errorMsg = "Error while uploading feature vectors to cloud services";
                LogManager.Write(LogManager.LogType.ERROR, errorMsg);
            }
            else
                errorMsg = "";
        }

        #endregion


        #region AWS SQS Services

        static String queue_w_b_url = "https://sqs.us-east-1.amazonaws.com/139012132121/syncre-link-w-b";       //items to be processed queue                                                                          //items to be processed queue  
        static String queue_b_c_url = "https://sqs.us-east-1.amazonaws.com/139012132121/syncre-link-b-c";       //processed items queue  


        #region Link W - B Queue

        //Put object in queue 
        public static void PutObject(string messageBody, ref string errMsg, bool msgAttrib_STORE = true)
        {
            aws_sqs.SendMessage(messageBody, /*msgAttrib_STORE,*/ queue_w_b_url, ref errMsg);
        }

        //Read objects from queue 
        public static List<string> GetObjects(ref List<string> data, ref string errMsg)
        {
            List<Amazon.SQS.Model.Message> messages = new List<Amazon.SQS.Model.Message>();
            List<string> transactionHandle = new List<string>();

            //get messages
            bool errOccured = aws_sqs.ReceiveMessage(queue_w_b_url, ref messages, ref errMsg);

            //check if error occured
            if (errOccured == true)
            {
                errMsg = "Could not read transaction from cloud queue services.";
                return null;
            }

            //else de-serialize amazon queue message
            foreach (Amazon.SQS.Model.Message message in messages)
            {
                string messageId = message.MessageId;
                string receiptHandle = message.ReceiptHandle;
                string md5OfBody = message.MD5OfBody;
                string body = message.Body;
                //KeyValuePair<string, string> attributes    = message.Attributes;

                //record the relevant info
                data.Add(body);
                transactionHandle.Add(receiptHandle);
            }
            return transactionHandle;
        }

        //Remove objects from queue 
        public static void RemoveObject(List<string> objectHandles, ref string statusMsg)
        {
            foreach (string handle in objectHandles)
            {
                aws_sqs.DeleteMessage(queue_w_b_url, handle, ref statusMsg);
            }
        }

        //Remove object
        public static void RemoveObject(string objectHandle, ref string statusMsg)
        {
            aws_sqs.DeleteMessage(queue_w_b_url, objectHandle, ref statusMsg);
        }

        #endregion


        #region Link B - C Queue

        //Put transaction in queue 
        public static void PutTransaction(string messageBody, ref string errMsg, bool msgAttrib_STORE = true)
        {
            aws_sqs.SendMessage(messageBody, /*msgAttrib_STORE,*/ queue_b_c_url, ref errMsg);
        }

        //Read transaction from queue 
        public static List<string> GetTransaction(ref List<string> data, ref string errMsg)
        {
            List<Amazon.SQS.Model.Message> messages = new List<Amazon.SQS.Model.Message>();
            List<string> transactionHandle = new List<string>();

            //get messages
            aws_sqs.ReceiveMessage(queue_b_c_url, ref messages, ref errMsg);

            //check if error occured
            if (errMsg != "")
            {
                errMsg = "Could not read transaction from cloud queue services.";
                return null;
            }

            //check if message was gotten
            if (messages.Count == 0)
            {
                errMsg = "EMPTY";       //queue was empty
                LogManager.Write(LogManager.LogType.ERROR, "Queue was empty during GetTransaction() attempt");
                return null;
            }
            else
            //de-serialize amazon queue message
            {
                foreach (Amazon.SQS.Model.Message message in messages)
                {
                    string messageId = message.MessageId;
                    string receiptHandle = message.ReceiptHandle;
                    string md5OfBody = message.MD5OfBody;
                    string body = message.Body;
                    //KeyValuePair<string, string> attributes    = message.Attributes;

                    //record the relevant info
                    data.Add(body);
                    transactionHandle.Add(receiptHandle);
                }
            }
            return transactionHandle;
        }

        //Remove transaction from queue 
        public static void RemoveTransaction(List<string> transactions, ref string errMsg)
        {
            foreach (string tranx in transactions)
            {
                aws_sqs.DeleteMessage(queue_b_c_url, tranx, ref errMsg);
            }
        }

        #endregion

        #endregion
    }
}
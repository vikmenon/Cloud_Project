using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.IO;
using Amazon;
using Amazon.S3;
using Amazon.S3.Model;
using Amazon.S3.Transfer;


namespace CloudServices
{
    public static class aws_s3
    {

        public static string Read(string bucketName, string keyName, ref string errorMessage)
        {
            IAmazonS3 client = Amazon.AWSClientFactory.CreateAmazonS3Client(RegionEndpoint.USEast1);

            try
            {
                GetObjectRequest request = new GetObjectRequest()
                {
                    BucketName = bucketName,
                    Key = keyName
                };

                using (GetObjectResponse response = client.GetObject(request))
                {
                    string dest = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, bucketName + "\\" + keyName);
                    //if (!File.Exists(dest))
                    //{
                    response.WriteResponseStreamToFile(dest);
                    errorMessage = "";
                    return dest;
                    //}
                    //else
                    //  errorMessage = "ERROR: The filename already exist on the local store.";
                }
            }
            catch (AmazonS3Exception amazonS3Exception)
            {
                if (amazonS3Exception.ErrorCode != null &&
                    (amazonS3Exception.ErrorCode.Equals("InvalidAccessKeyId") ||
                    amazonS3Exception.ErrorCode.Equals("InvalidSecurity")))
                {
                    errorMessage = "ERROR: Invalid cloud account credentials";
                }
                else
                {
                    errorMessage = "ERROR: Could not read object";
                }
            }
            return null;
        }

        public static void Write(string filePath, string bucketName, string keyName, ref string errorMessage)
        {
            IAmazonS3 client = Amazon.AWSClientFactory.CreateAmazonS3Client(RegionEndpoint.USEast1);
            try
            {
                PutObjectRequest titledRequest = new PutObjectRequest()
                {
                    BucketName = bucketName,
                    FilePath = filePath,
                    Key = keyName
                };
                titledRequest.Metadata.Add("Content", "FeatureVectors");

                client.PutObject(titledRequest);
                errorMessage = "";
            }
            catch (AmazonS3Exception amazonS3Exception)
            {
                if (amazonS3Exception.ErrorCode != null &&
                    (amazonS3Exception.ErrorCode.Equals("InvalidAccessKeyId") ||
                    amazonS3Exception.ErrorCode.Equals("InvalidSecurity")))
                {
                    errorMessage = "ERROR: Invalid cloud account credentials";
                }
                else
                {
                    errorMessage = "ERROR: Could not read object";
                }
            }
        }
    }
}
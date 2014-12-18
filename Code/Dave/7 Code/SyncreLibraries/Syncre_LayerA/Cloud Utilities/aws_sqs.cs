using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Amazon;
using Amazon.SQS;
using Amazon.SQS.Model;
using System.Net;
using System.Net.Sockets;

using Utilities;


namespace CloudServices
{
    public static class aws_sqs
    {
        static IAmazonSQS sqs = AWSClientFactory.CreateAmazonSQSClient(RegionEndpoint.USEast1);

        //send a message to SQS queue
        public static void SendMessage(string messageBody, /*bool msgAttrib_STORE,*/ String queueUrl, ref string errMsg)
        {
            try
            {
                SendMessageRequest sendMessageRequest = new SendMessageRequest();
                sendMessageRequest.QueueUrl = queueUrl;
                sendMessageRequest.MessageBody = messageBody;

                #region construt message attribute
                //MessageAttributeValue _store  = new MessageAttributeValue();         //store or search. Set to Store.
                //MessageAttributeValue _format = new MessageAttributeValue();         //format of data to be read by consumer
                //MessageAttributeValue _sender = new MessageAttributeValue();         //ip address of producer

                //_store.StringValue = msgAttrib_STORE.ToString();
                //_format.StringValue = "xml";
                //_sender.StringValue = Dns.GetHostEntry(Dns.GetHostName()).AddressList.FirstOrDefault(ip => ip.AddressFamily == AddressFamily.InterNetwork).ToString();

                //sendMessageRequest.MessageAttributes.Add("store",  _store);
                //sendMessageRequest.MessageAttributes.Add("format", _format);    
                //sendMessageRequest.MessageAttributes.Add("sender", _sender);
                #endregion

                //send message
                sqs.SendMessage(sendMessageRequest);
                errMsg = "";
            }
            catch (AmazonSQSException ex)
            {
                errMsg = ex.Message;
                LogManager.Write(LogManager.LogType.ERROR, ex.Message);
            }
        }

        //reads message on top of SQS queue
        public static bool ReceiveMessage(String queueUrl, ref List<Message> messages, ref string statusMsg)
        {
            try
            {
                ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest();
                receiveMessageRequest.QueueUrl = queueUrl;
                ReceiveMessageResponse receiveMessageResponse = sqs.ReceiveMessage(receiveMessageRequest);

                //if nothing in queue
                if (receiveMessageResponse.Messages.Count == 0)
                {
                    statusMsg = "";
                    return false;       //no error
                }

                //else, serialize message format
                foreach (Message message in receiveMessageResponse.Messages)
                {
                    messages.Add(new Message()
                        {
                            MessageId = message.MessageId,
                            ReceiptHandle = message.ReceiptHandle,
                            MD5OfBody = message.MD5OfBody,
                            Body = message.Body,
                            Attributes = message.Attributes
                        }
                    );
                }
                statusMsg = "";   //no error
            }
            catch (AmazonSQSException ex)
            {
                statusMsg = ex.Message;    //error
                //LogManager.Write(LogManager.LogType.ERROR, ex.Message);
                return true;
            }

            return false;    //no error
        }

        //delete message with specified handler from queue
        public static void DeleteMessage(String queueUrl, String messageRecieptHandle, ref string errMsg)
        {
            try
            {
                DeleteMessageRequest deleteRequest = new DeleteMessageRequest();
                deleteRequest.QueueUrl = queueUrl;
                deleteRequest.ReceiptHandle = messageRecieptHandle;
                sqs.DeleteMessage(deleteRequest);

                errMsg = "";
            }
            catch (AmazonSQSException ex)
            {
                //Console.WriteLine("Caught Exception: " + ex.Message);
                //Console.WriteLine("Response Status Code: " + ex.StatusCode);
                //Console.WriteLine("Error Code: " + ex.ErrorCode);
                //Console.WriteLine("Error Type: " + ex.ErrorType);
                //Console.WriteLine("Request ID: " + ex.RequestId);

                errMsg = ex.Message;
                LogManager.Write(LogManager.LogType.ERROR, ex.Message);
            }
        }
    }
}
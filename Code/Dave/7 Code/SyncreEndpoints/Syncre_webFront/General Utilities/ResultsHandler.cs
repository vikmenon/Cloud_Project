using System;
using System.Threading;
using System.Runtime.Remoting.Messaging;
using System.Collections.Generic;
using Utilities;
using System.IO;

namespace Syncre_website.General_Utilities
{

    //Asynchronously gets results from Layer C
    class ResultsGetter
    {
        //the get result method
        public static bool GetResults(string key, ref List<string> keys_matched)
        {                     
            try
            {
                //create http listening server
                HttpListenningServer webServer = new HttpListenningServer("http://sincre.elasticbeanstalk.com/searchresult/");

                //receive data from client: search Layer C
                 //webServer.Receive();              //blocks until data is received

                //de-serialize data
                //keys_matched.Add(receivedData.ReadByte().ToString());
                //keys_matched.Add(receivedData);
            }
            catch(Exception err)
            {
                return false;
            }

            return true;
        }
    }


    //asychronous delegate (matches the GerResult funtion)
    delegate bool GetResultsDelegate(string key, ref List<string> keys_matched);


    public class GetResult
    {
        // The waiter object used to keep the main application thread 
        // from terminating before the callback method completes.
        ManualResetEvent waiter;

        //method that receives a callback when the results are available
        void ResultsReady(IAsyncResult result)
        {
            List<string> keys_matched = new List<string>();

            // Extract the delegate from the  
            // System.Runtime.Remoting.Messaging.AsyncResult.
            GetResultsDelegate getResultDelegate = (GetResultsDelegate)((AsyncResult)result).AsyncDelegate;
            int g = (int)result.AsyncState;

            // Obtain the result
            bool return_val = getResultDelegate.EndInvoke(ref keys_matched, result);

            waiter.Set();
        }


        //Method1: asynchronous pattern using a callback method
        public void GetResultUsingCallback(string key, ref List<string> keys_matched)
        {
            //declare instance of delegate
            GetResultsDelegate getResultDelegte = new GetResultsDelegate(ResultsGetter.GetResults);

            // Waiter will keep the main application thread from  
            // ending before the callback completes because 
            // the main thread blocks until the waiter is signaled 
            // in the callback
            waiter = new ManualResetEvent(false);

            //define the AsyncCallback delegate
            AsyncCallback callback = new AsyncCallback(this.ResultsReady);

            // Asynchronously invoke the GetRsults method
            IAsyncResult result = getResultDelegte.BeginInvoke(key, ref keys_matched, callback, key);

            // Do some other useful work while  
            // waiting for the asynchronous operation to complete. 

            // When no more work can be done, wait.
            waiter.WaitOne();
        }

        //Method2: asynchronous pattern using a BeginInvoke, followed by waiting with a time-out
        public void GetResultsAndWait(string key, ref List<string> keys_matched, int waitTime = 10000)
        {
            //declare instance of delegate
            GetResultsDelegate getResultDelegate = new GetResultsDelegate(ResultsGetter.GetResults);

            //asynchonously invoke the GetResult method
            IAsyncResult result = getResultDelegate.BeginInvoke(key, ref keys_matched, null, null);

            while (!result.IsCompleted)
            {
                //do any work that can done before waiting
                result.AsyncWaitHandle.WaitOne(waitTime, false);
            }
            result.AsyncWaitHandle.Close();

            //the asynchronous operation has completed
            bool return_val = getResultDelegate.EndInvoke(ref keys_matched, result);
        }
    }
}
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Threading;

namespace Utilities
{
    public class HttpListenningServer
    {
        HttpListener httpListener;
        MemoryStream data = null;

        System.Timers.Timer rcvTimer;

        public HttpListenningServer(string hostingAddress)
        {
            //create HttpListener, set endpoint and start listening
            httpListener = new HttpListener();
            httpListener.Prefixes.Add(hostingAddress);
            httpListener.Start();
        }

        public MemoryStream Receive(double timeout, ref string statusMsg)
        {
            //set receive timer
            rcvTimer = new System.Timers.Timer(timeout);
            rcvTimer.AutoReset = false;                      //happen only once
            rcvTimer.Elapsed += rcvTimer_Elapsed;
            rcvTimer.Start(); 

            //keeps listening until data received or timer elapsed
            statusMsg = ReceiveIncoming(false);

            //stop listening
            if (httpListener.IsListening)
                httpListener.Stop();

            return data;
        }

        void rcvTimer_Elapsed(object sender, System.Timers.ElapsedEventArgs e)
        {
            if (httpListener.IsListening)
                httpListener.Close();
        }

        string ReceiveIncoming(bool threaded = false)
        {         
            while (data == null || data.Length == 0)
            {
                //if time elapsed, return
                if (!rcvTimer.Enabled)
                    return "";
                
                try
                {
                    //get context
                    var httpContext = httpListener.GetContext();

                    //get data from client request   
                    data = new MemoryStream();
                    httpContext.Request.InputStream.CopyTo(data);

                    //if data received.. 
                    if (data.Length != 0)
                    {
                        //stop receive
                        rcvTimer.Stop();

                        //send response back to client 
                        httpContext.Response.StatusCode = 200;
                        httpContext.Response.StatusDescription = "OK";
                        httpContext.Response.Close();    
                    }
                }
                catch (HttpListenerException exp)
                {
                    //http listener execption. catch exception when still listening after timer has closed the port
                }
                catch (Exception err)
                {
                    //other exceptions
                }
            }
            return "";           //data successfully received and response sent back.
        }


        #region Threaded receive

        void ReceiveIncoming()
        {
            //while nothing is received, keep listening
            while (data == null)
            {
                ReceiveIncoming_threaded();
            }
        }

        void ReceiveIncoming_threaded()
        {
            var result = httpListener.BeginGetContext(GetContextCallback, httpListener);
            result.AsyncWaitHandle.WaitOne();
        }

        private void GetContextCallback(IAsyncResult result)
        {
            //stop get context
            var httpContext = httpListener.EndGetContext(result);

            //get data from client request
            HttpListenerRequest request = httpContext.Request;
            request.InputStream.CopyTo(data);

            //send response back to client
            httpContext.Response.StatusCode = 200;
            httpContext.Response.StatusDescription = "OK";
            httpContext.Response.Close();
        }

        #endregion
    }
}

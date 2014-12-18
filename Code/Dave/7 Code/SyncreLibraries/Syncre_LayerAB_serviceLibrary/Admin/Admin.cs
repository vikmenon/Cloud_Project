using System;
using System.Collections.Generic;
using System.Configuration;
using System.IO;
using System.Linq;
using System.Text;
using System.Timers;

using Syncre_LayerA;
using Syncre_LayerB_library;

namespace Syncre_LayerAB_wcfServiceLibrary.Admin
{
    public static class Admin
    {

        static Timer timer_queuePoll;
        static string searchServerAdress;

        public static string DisablePolledProcessing()
        {
            if (timer_queuePoll == null)
                return "Polled processing is not active.";
            else
            {
                if (timer_queuePoll.Enabled == false)
                    return "Polled processing is already disabled.";
                else
                {
                    timer_queuePoll.Enabled = false;
                    return "Polled processing enabled.";
                }
            }
        }

        //TODO: may need to implement delayed-polling system for fairness
        public static string EnableQueuePolling(int pollInterval, string searchSvrAddress)
        {
            searchServerAdress = searchSvrAddress;

            //validate poll interval
            if (pollInterval == null || pollInterval < 1000)
                return "Poll processing interval must be at least 1000ms.";

            timer_queuePoll = new Timer(pollInterval);
            timer_queuePoll.Elapsed += timer_queuePoll_Elapsed;

            timer_queuePoll.Start();
            return "Polled processing enabled and set to " + pollInterval + "ms.";
        }


        //call-back method that polls queue to get filename and search/store parameter
        static void timer_queuePoll_Elapsed(object sender, ElapsedEventArgs e)
        {
            timer_queuePoll.Enabled = false;

            List<string> items = new List<string>();                        //although queue set to currently return one item
            List<string> item_handles = new List<string>();
            string statusMsg = "";

            //get video filename from sqs queue  
            item_handles = CloudServices.CloudServices.GetObjects(ref items, ref statusMsg);

            //check error
            if (statusMsg != "")
            {
                LogManager.Write(statusMsg, "ERROR");
                timer_queuePoll.Enabled = false;

                return;
            }

            /* decode item and get videofilename and search/store parameter, then proess it */
            string videoFilename;
            bool isSearch;

            for (int i = 0; i < items.Count; i++)
            {
                string item = items[0];

                //video filename
                videoFilename = item.Substring(0, item.IndexOf("\n"));

                //to search or to store?
                string srch = item.Substring(item.IndexOf("\n") + 1, 1);
                if (srch == "1")
                    isSearch = true;
                else
                    isSearch = false;

                //item handle
                string itemHandle = item_handles[i];

                //process video
                LayerB layerB = new LayerB(videoFilename, isSearch, searchServerAdress);  
                try
                {
                    //process
                    layerB.Process();

                    //remove from queue
                    CloudServices.CloudServices.RemoveObject(itemHandle, ref statusMsg);

                }
                catch (Exception err)
                {
                    statusMsg = err.Message;
                }
            }

            //done processing (if item was in recevived from queue). Enable timer for next poll
            timer_queuePoll.Enabled = true;
        }
    }
}

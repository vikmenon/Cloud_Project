using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;

using Syncre_LayerB_library;
using Syncre_LayerAB_wcfServiceLibrary.Admin;

namespace Syncre_LayerB_librabry
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "Service1" in both code and config file together.
    public class VideoProcessor : IVideoProcessor
    {        
        string searchServerAddress;

        public void SetSearchServerEndpoint(string endpointAddress)
        {
            searchServerAddress = endpointAddress;            //must be set before doing on-demand process/search. Get this setting from hosted wcf web.config file     
        }
        

        #region User-end Functions

        public string ProcessVideo(string videoFilename, bool search = true, bool onDemand = true, bool keepFrames = false)
        {
            string statusMsg = "";
            LayerB layerB = new LayerB(videoFilename, search, searchServerAddress);  //TODO: ensure that search server address is not null
            try
            {
                statusMsg = layerB.Process(onDemand, keepFrames);
            }
            catch(Exception err)
            {
                statusMsg = err.Message;
            }
            return statusMsg;
        }

        #endregion


        #region  Admin Functions

        //async
        public string EnablePolledProcessing(int pollingInterval, string searchServerAddress)
        {
            return Admin.EnableQueuePolling(pollingInterval, searchServerAddress);
        }

        //async
        public string DisablePolledProcessing()
        {
            return Admin.DisablePolledProcessing();
        }

        #endregion
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;
using System.Timers;
using System.Configuration;

//using CloudServices;
//using Utilities;
using Syncre_LayerB_librabry;  //(actually is Syncre_LayerAB_serviceLibrary)


namespace Syncre_LayerAB_WebService
{
    public class LayerAB_WebService : ILayerAB_WebService
    {
        #region User Funtions

        //exposed public method to process video on demand. Protocol is basic-HTTP. See system.serviceModel section of Config file for more details. 
        public string ProcessVideo(string videoFilename, bool search = true, bool onDemand = true)
        {
            Syncre_LayerB_librabry.VideoProcessor vp = new VideoProcessor();            //uses Syncre_LayerAB_Service_Library

            vp.SetSearchServerEndpoint(ConfigurationManager.AppSettings.Get("SearchServerAddress"));         
            return vp.ProcessVideo(videoFilename, search, onDemand, true);  //keep frames
        }

        #endregion


        #region Admin Functions

        public string EnablePolledProcessing(int pollingInterval, string searchServerEndpoint)
        {
            Syncre_LayerB_librabry.VideoProcessor vp = new VideoProcessor();
            return vp.EnablePolledProcessing(pollingInterval, searchServerEndpoint);
        }

        public string DisablePolledProcessing()
        {
            Syncre_LayerB_librabry.VideoProcessor vp = new VideoProcessor();
            return vp.DisablePolledProcessing();
        }

        #endregion
    }
}

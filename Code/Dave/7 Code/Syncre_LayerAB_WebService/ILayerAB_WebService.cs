using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;

namespace Syncre_LayerAB_WebService
{
    [ServiceContract]
    public interface ILayerAB_WebService
    {
        #region User funtions

        //public method to process video on demand
        [OperationContract]
        string ProcessVideo(string videoKeyname, bool search = true, bool onDemand = true);

        #endregion

        #region Admin Functions

        //Admin's method to enable polled processing
        [OperationContract]
        string EnablePolledProcessing(int pollingInterval, string searchServerEndpoint);

        //Admin's method to disable polled processing
        [OperationContract]
        string DisablePolledProcessing();

        #endregion 
    }
}

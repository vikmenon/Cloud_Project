using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;

namespace Syncre_LayerB_librabry
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the interface name "IService1" in both code and config file together.
    [ServiceContract]
    public interface IVideoProcessor
    {
     
        #region User-facing Functions

        [OperationContract]
        string ProcessVideo(string videoKeyname, bool search = true, bool onDemand = true, bool keepFrames = false);

        #endregion


        #region Admin Functions

        [OperationContract]
        void SetSearchServerEndpoint(string endpointAddress);

        //Admin function for enabling polling of SQS to obtain video filename
        [OperationContract]
        string EnablePolledProcessing(int pollingInterval, string searchServerAddress);

        //Admin funtion for disabling polling
        [OperationContract]
        string DisablePolledProcessing();

        #endregion
    }   
}

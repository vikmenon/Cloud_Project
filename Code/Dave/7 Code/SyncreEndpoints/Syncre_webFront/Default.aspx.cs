using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.IO;
using System.Drawing;
using System.Text;
using System.Threading;
using System.ComponentModel;
using System.Diagnostics;
using System.Configuration;

using Syncre_website.Utilities;
using Utilities;
using CloudServices;



namespace Syncre_website
{
    public partial class _Default : Page
    {
        String[] allowedExtensions = {".mov", ".avi", ".mp4", ".wmv", ".mpeg", ".mpg" };    //{ ".gif", ".png", ".jpeg", ".jpg" };

        string backend2_searchServerAddress;
        string httpListenerServerAddress;
        double searchResltRcvTimeout;
        string processingServer_statusMsg = "";
        //BackgroundWorker bgWorker_reciveResult;

        string videofilename = "";
        string videoFormat;
        string key;
        bool search;
        List<string> searchResult;     //the first item in the list is the query key used for validating returned result from search server


        protected void Page_Load(object sender, EventArgs e)
        {
            //enalbe logs
            LogManager.EnableLog = true;

            //get settings from configuration
            httpListenerServerAddress = ConfigurationManager.AppSettings.Get("httpListenerServerAddress");
            searchResltRcvTimeout = double.Parse(ConfigurationManager.AppSettings.Get("searchResltRcvTimeout"));
            backend2_searchServerAddress = ConfigurationManager.AppSettings.Get("backend2_searchServerAddress");

            #region test polling admin

            //tests... WCF functions
            //LayerAB_WebServiceClient service = null;
            //string status;

            //try
            //{
            //    //open connection to server
            //    service = new LayerAB_WebServiceClient("BasicHttpBinding_ILayerAB_WebService");
            //    service.Open();

            //    //call ADMIN! functions
            //    status = service.DisablePolledProcessing();
            //    status = service.EnablePolledProcessing(100, backend2_searchServerAddress);
            //    status = service.EnablePolledProcessing(1000, backend2_searchServerAddress);

            //    status = service.DisablePolledProcessing();
            //    status = service.EnablePolledProcessing(2000, backend2_searchServerAddress);

            //}
            //catch (Exception err)
            //{
            //    status = err.ToString();
            //}

            ////close connection
            //if (service != null)
            //    service.Close();

            //end tests.

            #endregion

            //test
            //GetSerachResult_async();
        }

        //connect directly to application and process video on demand
        //NOTE: other option is a connectioness system. application server just queries queue to begin processing
        bool ProcessVideo(string videoFilename, bool search)
        {
            LayerAB_WebServiceClient layer_ab = null;
            bool remote_error = false;
            try
            {
                //open connection to server
                layer_ab = new LayerAB_WebServiceClient("BasicHttpBinding_ILayerAB_WebService");
                layer_ab.Open();

                //call process video function (LayerAB webs service) to process on-demand
                processingServer_statusMsg = layer_ab.ProcessVideo(videoFilename, search, true);


                /* check return status */

                if (processingServer_statusMsg == "")
                    processingServer_statusMsg = "Video processed sucessfully.";
                else if (processingServer_statusMsg == "QUEUED-UP FOR SEARCH")          //check queued-up condition (case when search-server was not online during transaction)
                    processingServer_statusMsg = "QUEUED-UP FOR SEARCH";                //"The Search server could not be contacted. Transaction has been saved to queue for later processing; you will be notified of its completed.";            
                else
                {
                    processingServer_statusMsg = "An internal error occured while attempting to process video.";
                    remote_error = true;
                }

                //close connection
                layer_ab.Close();
            }
            catch (Exception err)
            {
                remote_error = true;
                LogManager.Write(LogManager.LogType.NOTIFICATION, processingServer_statusMsg);

                processingServer_statusMsg = "An error occured while attempting to connect to processing servers. Please try again later.";
            }

            //check if error in processing
            if (remote_error)
                return false;
            return true;
        }


        protected void btnSearch_Click(object sender, EventArgs e)
        {
            lblStatus.Text = "";

            //set search flag
            search = true;

            //save temp file
            String uploadPath = Server.MapPath("~/Uploads/");
            if (!UploadToWebServer(FileUploadSearch, uploadPath))
                return;                                                //return if not successful in getting file

            //upload file to cloud store for processing         
            UploadToCloudStore(uploadPath, videofilename);

            //begin processing on LayerAB; when done, send transaction to Layer C to return results here
            PrintStatusMsg("Processing...", Color.CadetBlue);
            ProcessVideo(videofilename, search);                 //method1: On-Demand processing

            //Receive search result aysnchronously and process result
            //waits for backend search process to return result. Listens for http request  background walker takes charge of page transfer to search results page
            PrintStatusMsg("Searching...", Color.CadetBlue);
            GetSerachResult_async();

            //flush temp file from web server
            Cleanup(videofilename);

            //write log (or email)
            Utilities.LogManager.CommitLogToFile(Server.MapPath("//Logs//log.txt"));            
        }

        protected void btnStore_Click(object sender, EventArgs e)
        {
            lblStatus.Text = "";

            //clear search flag (needs to store)
            search = false;

            //save temp file
            String uploadPath = Server.MapPath("~/Uploads/");
            if (!UploadToWebServer(FileUploadStore, uploadPath))
                return;                                                    //return if not successful in getting file

            //upload file to cloud store for processing
            UploadToCloudStore(uploadPath, videofilename);

            //send message to begin processing
            SendMessageToQueue(videofilename, search);                    //method2: Send message to queue. Method1 not advisable - no need.

            //notify user of submission of data for processing
            PrintStatusMsg("Your submission has been sent for processing. You will be notified when intelligence gathering is completed.\n", Color.CadetBlue);


            //flush temp file from web server
            Cleanup(videofilename);

            //write log
            Utilities.LogManager.CommitLogToFile(Server.MapPath("//Logs//log.txt"));
        }



        //save file to temp local store
        bool UploadToWebServer(FileUpload fileUpload, string uploadPath)
        {
            if (fileUpload.HasFile)
            {
                string statusMsg = "";
                String fileExtension = Path.GetExtension(fileUpload.FileName).ToLower();
                if (CheckFileFormat(fileExtension))
                {
                    try
                    {
                        videofilename = KeyGenerator.GetUniqueKey() + fileExtension;
                        key = KeyGenerator.GetUniqueKey();
                        fileUpload.PostedFile.SaveAs(uploadPath + videofilename);

                        LogManager.Write(LogManager.LogType.NOTIFICATION, "File sucessfully uploaded to web server.");
                    }
                    catch (Exception ex)
                    {
                        videofilename = "";
                        statusMsg = ex.ToString();

                        PrintStatusMsg("File could not be uploaded.", Color.DarkRed);
                        return false;
                    }
                }
                else
                {
                    PrintStatusMsg("Cannot accept files of this type.", Color.DarkRed);
                    return false;
                }
            }
            return true;
        }

        //upload file to cloud store
        bool UploadToCloudStore(string sourcePath, string filename)
        {
            string statusMsg = "";
            bool remote_error = false;
            CloudServices.CloudServices.UploadVideo(Path.Combine(sourcePath, filename), filename, ref statusMsg);

            if (statusMsg == "")
                statusMsg = "Sucessfully uploaded data to cloud store.";
            else
            {
                PrintStatusMsg("Error communicating with cloud store. The server lost connection.", Color.DarkRed);
                remote_error = true;
            }

            //log status
            LogManager.Write(LogManager.LogType.NOTIFICATION, statusMsg);

            //check error in uploading
            if (remote_error)
                return false;
            return true;
        }

        void SendMessageToQueue(string tranxKey, bool search)
        {
            string srch = "0";
            if (search == true)
                srch = "1";

            StringBuilder message = new StringBuilder();
            message.AppendLine(tranxKey);
            message.AppendLine(srch);

            string statusMsg = "";
            CloudServices.CloudServices.PutObject(message.ToString(), ref statusMsg);
        }

        //delete video from local store
        void Cleanup(string videofilename)
        {
            string file = Path.Combine(Server.MapPath("~/Uploads/"), videofilename);
            if (File.Exists(file))
                File.Delete(file);
        }



        HttpListenningServer httpListennerSever;
        MemoryStream resultsData;
        string rcvStatusMsg;
        //checks for result of search, asynchronous operation
        void GetSerachResult_async()
        {
            //listen for incoming result from search layer
            httpListennerSever = new HttpListenningServer(httpListenerServerAddress);
            resultsData = httpListennerSever.Receive(searchResltRcvTimeout, ref rcvStatusMsg);        //blocks until data is received  

            //see if result gotten or timeout occured
            if(resultsData == null)
                PrintStatusMsg("The search operation took longer than expected.", Color.Green);
            else
                ProcessSearchResult();
        }


        //process results recevied
        void ProcessSearchResult()
        {
            //process search results
            searchResult = new List<string>();

            //de-serialize the data
            searchResult = JsonUtility.Deserialize(resultsData);


            //validate received transaction
            //TEST: OVERIDE VALIDATION
            //key = "RaQksAdQku1m5GqaGXdmV61E7vRkkdKk";   //test key for returning mocked result from external mocked search server node. #Also remove .jpg test.
            if (searchResult[0] != key)
            {
                PrintStatusMsg("Error validating result. The key received did not match the key that was sent for processing.", Color.Red);
                return;
            }

            //prepare data for transfer to display page                  
            Session["results"] = searchResult;

            //transfer to display page
            Server.Transfer("Search Result.aspx?");
        }


        //checks video extension
        bool CheckFileFormat(string fileExtension)
        {
            Boolean fileOK = false;
            for (int i = 0; i < allowedExtensions.Length; i++)
            {
                if (fileExtension == allowedExtensions[i])
                {
                    fileOK = true;
                }
            }
            return fileOK;
        }

        void PrintStatusMsg(string msg, Color color)
        {
            lblStatus.Text = msg;
            lblStatus.ForeColor = color;
        }

        //delete method. Not used. Testing only/.
        void TestCallbacks()
        {
            Syncre_website.General_Utilities.GetResult resultGet = new General_Utilities.GetResult();
            List<string> results = new List<string>();

            //resultGet.GetResultUsingCallback("RaQksAdQku1m5GqaGXdmV61E7vRkkdKk", ref results);
            resultGet.GetResultsAndWait("RaQksAdQku1m5GqaGXdmV61E7vRkkdKk", ref results, 10000);
        }
    }
}
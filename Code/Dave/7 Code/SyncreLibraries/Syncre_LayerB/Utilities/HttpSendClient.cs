using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Net.Http.Headers;
using System.Net;
using System.IO;

namespace Syncre_LayerB.Utilities
{
    public class HttpSendClient
    {
        HttpClient client;
        string address;

        public HttpSendClient(string serverAddress)
        {
            client = new HttpClient();
            this.address = serverAddress;
        }

        public void SendPlainString(string data, ref string statusMsg)
        {
            var content = new StringContent(data);
            try
            {
                //send data to server
                HttpWebRequest req = (HttpWebRequest)WebRequest.Create(address);               

                req.Method = "POST";

                byte[] data_bytes = Encoding.UTF8.GetBytes(data);               
                req.ContentLength = data_bytes.Length;

                Stream data_stream = req.GetRequestStream(); 
                data_stream.Write(data_bytes, 0, data_bytes.Length);
                data_stream.Close();


                //get response from server
                WebResponse response = req.GetResponse();
                MemoryStream respData = new MemoryStream();

                response.GetResponseStream().CopyTo(respData);
                response.Close();

                string respString = new StreamReader(respData).ReadToEnd();
                respData.Close();


                //check response from server
                if (respString != "OK")
                    statusMsg = "Search server did not acknowledge query.";
                else
                    statusMsg = "";
            }
            catch(HttpRequestException ex)
            {
                statusMsg = ex.Message;
            }
            catch (Exception err)
            {
                statusMsg = err.Message;
            }
        }

        //static async Task RunAsync()
        //{
        //    using (var client = new HttpClient())
        //    {
        //        // TODO - Send HTTP requests
        //    }
        //}

        public void SendJsonData(string data)
        {
            var content = new StringContent(data.ToString(), Encoding.UTF8, "application/json");

            client.PostAsync(address, content);

            //HttpResponseMessage response = client.GetAsync(address);
            //response.EnsureSuccessStatusCode();
        }
    }
}

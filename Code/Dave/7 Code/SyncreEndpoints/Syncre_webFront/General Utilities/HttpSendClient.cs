using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Net.Http;

namespace Syncre_website.General_Utilities
{
    public class HttpSendClient
    {
        HttpClient client;  

        public HttpSendClient(Uri serverAddress)
        {
            client = new HttpClient();
            client.BaseAddress = serverAddress;
        }

        public void Send(string data)
        {
            var content = new StringContent("hello");
            client.PostAsync("/", content);
        }
    }
}
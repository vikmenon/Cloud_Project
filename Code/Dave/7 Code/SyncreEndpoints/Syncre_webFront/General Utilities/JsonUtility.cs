using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Runtime.Serialization;
using System.Runtime.Serialization.Json;
using System.IO;

using System.Web.Script.Serialization;

namespace Utilities
{
    public static class JsonUtility
    {

        public static void Serialize()
        {
            Transaction tranx = new Transaction();


            DataContractJsonSerializer jsonSerializer = new DataContractJsonSerializer(typeof(Transaction));
            MemoryStream stream = new MemoryStream();

            jsonSerializer.WriteObject(stream, tranx);

            //show JSON output
            //stream.Position = 0;
            //StreamReader sr = new StreamReader(stream1);
            //Console.Write("JSON form of Person object: ");
            //Console.WriteLine(sr.ReadToEnd());
        }

        public static List<string> Deserialize(MemoryStream data)
        {
            data.Position = 0;
            string jsonData = new StreamReader(data).ReadToEnd();

            JavaScriptSerializer serializer = new JavaScriptSerializer();
            return (List<string>)serializer.Deserialize(jsonData, typeof(List<string>));

            //DataContractJsonSerializer jsonSerializer = new DataContractJsonSerializer(typeof(List<Transaction>));
            //List<Transaction> tranx = (List<Transaction>) jsonSerializer.ReadObject(data);
        }


        [DataContract]
        public class Transaction
        {
            [DataMember]
            public string key;
        }
    }


    public static class TestJsonDeserialization
    {
        public static void Test()
        {                
           Stream fs = new FileStream("C:\\myfile.txt", FileMode.Open);
           string jsonData = new StreamReader(fs).ReadToEnd();

           JavaScriptSerializer serliazer = new JavaScriptSerializer();
           List<string> cc = (List<string>)serliazer.Deserialize(jsonData, typeof(List<string>));

           fs.Close();            
            
        }
    }
}
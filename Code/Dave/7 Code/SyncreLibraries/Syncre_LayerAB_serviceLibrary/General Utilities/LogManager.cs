using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;


namespace Utilities
{
    public static class LogManager
    {

        static string logFilename = AppDomain.CurrentDomain.BaseDirectory + ("\\log\\log.txt");

        public static void Write(LogType logType, string info)
        {
            using (StreamWriter writer = new StreamWriter(new FileStream(logFilename, FileMode.Append)))
            {
                writer.WriteLine("{0} {1}\t{2}\t{3}", DateTime.Now.ToString(), System.DateTime.Now.ToString(), logType, info);
            }
        }

        public enum LogType { NOTIFICATION, WARNING, ERROR};      
    }
}
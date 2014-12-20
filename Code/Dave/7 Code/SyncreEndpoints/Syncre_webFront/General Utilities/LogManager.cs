using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Text;
using System.IO;

namespace Syncre_website.Utilities
{
    public static class LogManager
    {
        public static bool EnableLog = false;
        static StringBuilder sb_log = new StringBuilder();

        public static void Write(LogType logType, string logInfo)
        {
            if (!EnableLog)
                return;

            sb_log.AppendFormat("{0}\t{1}\t{2}\n", System.DateTime.Now.ToString(), logType, logInfo);
        }

        public static void CommitLogToFile(string file)
        {
            if (!EnableLog)
                return;

            StreamWriter writer = new StreamWriter(new FileStream(file, FileMode.Append));
            writer.Write(sb_log.ToString());
            writer.Close();
        }

        public enum LogType { NOTIFICATION, WARNING, ERROR}
    }
}
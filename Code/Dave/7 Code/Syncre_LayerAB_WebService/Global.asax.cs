using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Security;
using System.Web.SessionState;

namespace Syncre_LayerAB_WebService
{
    public class Global : System.Web.HttpApplication
    {
        
        protected void Application_Start(object sender, EventArgs e)
        {
            //purposedly sets path of custom dlls. Should be alrady fine with the inclusion of those dlls in the app bin folder
            String _path = String.Concat(System.Environment.GetEnvironmentVariable("PATH"), ";", System.AppDomain.CurrentDomain.RelativeSearchPath);
            System.Environment.SetEnvironmentVariable("PATH", _path, EnvironmentVariableTarget.Process);
        }

        protected void Session_Start(object sender, EventArgs e)
        {

        }

        protected void Application_BeginRequest(object sender, EventArgs e)
        {

        }

        protected void Application_AuthenticateRequest(object sender, EventArgs e)
        {

        }

        protected void Application_Error(object sender, EventArgs e)
        {

        }

        protected void Session_End(object sender, EventArgs e)
        {

        }

        protected void Application_End(object sender, EventArgs e)
        {

        }
    }
}
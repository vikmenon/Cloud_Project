using System;
using System.Threading;
using System.Runtime.Remoting.Messaging;

namespace Test
{
    // Create a class that factors a number. 
    public class PrimeFactorFinder
    {
        public static bool Factorize(ref string key)
        {
            
                return true;
        }
    }

    // Create an asynchronous delegate that matches the Factorize method. 
    public delegate bool AsyncFactorCaller(ref string key);

    public class DemonstrateAsyncPattern
    {
        // The waiter object used to keep the main application thread 
        // from terminating before the callback method completes.
        ManualResetEvent waiter;

        // Define the method that receives a callback when the results are available. 
        public void FactorizedResults(IAsyncResult result)
        {
            string key = "dad";

            // Extract the delegate from the  
            // System.Runtime.Remoting.Messaging.AsyncResult.
            AsyncFactorCaller factorDelegate = (AsyncFactorCaller)((AsyncResult)result).AsyncDelegate;
            int number = (int)result.AsyncState;

            // Obtain the result. 
            bool answer = factorDelegate.EndInvoke(ref key, result);
            
            waiter.Set();
        }

        // The following method demonstrates the asynchronous pattern using a callback method. 
        public void FactorizeNumberUsingCallback()
        {
            AsyncFactorCaller factorDelegate = new AsyncFactorCaller(PrimeFactorFinder.Factorize);
            int number = 1000589023;
            string key = "";
            // Waiter will keep the main application thread from  
            // ending before the callback completes because 
            // the main thread blocks until the waiter is signaled 
            // in the callback.
            waiter = new ManualResetEvent(false);

            // Define the AsyncCallback delegate.
            AsyncCallback callBack = new AsyncCallback(this.FactorizedResults);

            // Asynchronously invoke the Factorize method.
            IAsyncResult result = factorDelegate.BeginInvoke(ref key, callBack,  number);

            // Do some other useful work while  
            // waiting for the asynchronous operation to complete. 

            // When no more work can be done, wait.
            waiter.WaitOne();
        }

        // The following method demonstrates the asynchronous pattern  
        // using a BeginInvoke, followed by waiting with a time-out. 
        public void FactorizeNumberAndWait()
        {
            AsyncFactorCaller factorDelegate = new AsyncFactorCaller(PrimeFactorFinder.Factorize);

            int number = 1000589023;
            string key = "";

            // Asynchronously invoke the Factorize method.
            IAsyncResult result = factorDelegate.BeginInvoke(ref key, null, null);

            while (!result.IsCompleted)
            {
                // Do any work you can do before waiting.
                result.AsyncWaitHandle.WaitOne(10000, false);
            }
            result.AsyncWaitHandle.Close();

            // The asynchronous operation has completed. 
            int factor1 = 0;
            int factor2 = 0;

            // Obtain the result. 
            bool answer = factorDelegate.EndInvoke(ref key, result);

            // Output the results.
            Console.WriteLine("Sequential : Factors of {0} : {1} {2} - {3}",
                              number, factor1, factor2, answer);
        }

        public static void Main()
        {
            DemonstrateAsyncPattern demonstrator = new DemonstrateAsyncPattern();
            demonstrator.FactorizeNumberUsingCallback();
            demonstrator.FactorizeNumberAndWait();
        }
    }
}
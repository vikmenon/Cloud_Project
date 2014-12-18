[System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
[System.ServiceModel.ServiceContractAttribute(ConfigurationName="ILayerAB_WebService")]
public interface ILayerAB_WebService
{
    
    [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/ILayerAB_WebService/ProcessVideo", ReplyAction="http://tempuri.org/ILayerAB_WebService/ProcessVideoResponse")]
    string ProcessVideo(string videoKeyname, bool search);
    
    [System.ServiceModel.OperationContractAttribute(Action="http://tempuri.org/ILayerAB_WebService/ProcessVideo", ReplyAction="http://tempuri.org/ILayerAB_WebService/ProcessVideoResponse")]
    System.Threading.Tasks.Task<string> ProcessVideoAsync(string videoKeyname, bool search);
}

[System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
public interface ILayerAB_WebServiceChannel : ILayerAB_WebService, System.ServiceModel.IClientChannel
{
}

[System.Diagnostics.DebuggerStepThroughAttribute()]
[System.CodeDom.Compiler.GeneratedCodeAttribute("System.ServiceModel", "4.0.0.0")]
public partial class LayerAB_WebServiceClient : System.ServiceModel.ClientBase<ILayerAB_WebService>, ILayerAB_WebService
{
    
    public LayerAB_WebServiceClient()
    {
    }
    
    public LayerAB_WebServiceClient(string endpointConfigurationName) : 
            base(endpointConfigurationName)
    {
    }
    
    public LayerAB_WebServiceClient(string endpointConfigurationName, string remoteAddress) : 
            base(endpointConfigurationName, remoteAddress)
    {
    }
    
    public LayerAB_WebServiceClient(string endpointConfigurationName, System.ServiceModel.EndpointAddress remoteAddress) : 
            base(endpointConfigurationName, remoteAddress)
    {
    }
    
    public LayerAB_WebServiceClient(System.ServiceModel.Channels.Binding binding, System.ServiceModel.EndpointAddress remoteAddress) : 
            base(binding, remoteAddress)
    {
    }
    
    public string ProcessVideo(string videoKeyname, bool search)
    {
        return base.Channel.ProcessVideo(videoKeyname, search);
    }
    
    public System.Threading.Tasks.Task<string> ProcessVideoAsync(string videoKeyname, bool search)
    {
        return base.Channel.ProcessVideoAsync(videoKeyname, search);
    }
}

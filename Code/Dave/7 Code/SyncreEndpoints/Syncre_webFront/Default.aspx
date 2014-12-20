<%@ Page Title="Home Page" Language="C#" MasterPageFile="~/Site.Master" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="Syncre_website._Default" Async="true" %>

<asp:Content runat="server" ID="FeaturedContent" ContentPlaceHolderID="FeaturedContent">
    <section class="featured">
        <div class="content-wrapper">
            <hgroup class="title">
                <h1>&nbsp;Content-Based Video Retrieval System</h1>
            </hgroup>
            <p>
                <em>your smarter way of searching...</em></p>
        </div>
    </section>
</asp:Content>
<asp:Content runat="server" ID="BodyContent" ContentPlaceHolderID="MainContent">
    <h3 class="auto-style1">What would you like to do?</h3>
    <ol class="round">
        <li class="one">
            <h5>Search</h5>
            Specify a sample of a video to search.&nbsp;&nbsp;&nbsp;
            <asp:FileUpload ID="FileUploadSearch" runat="server" />
&nbsp;&nbsp;&nbsp;
            <asp:Button ID="btnSearch" runat="server" Height="16px" OnClick="btnSearch_Click" Width="50px" />
        </li>
    </ol>
    <p style="font-weight: 700">
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; OR&nbsp;</p>
    <ol class="round">
        <li class="two">
            <h5>Store</h5>
            Specify a video to store and let the indexing system take care of the rest.&nbsp;&nbsp;&nbsp;
            <asp:FileUpload ID="FileUploadStore" runat="server" />
&nbsp;&nbsp;&nbsp;
            <asp:Button ID="btnStore" runat="server" Height="10px" OnClick="btnStore_Click" Width="50px" />
            </li>
    </ol>
    <p>
        <asp:Label ID="lblStatus" runat="server"></asp:Label>
    </p>
</asp:Content>
<asp:Content ID="Content1" runat="server" contentplaceholderid="HeadContent">
    <style type="text/css">
    .auto-style1 {
        font-weight: normal;
    }
</style>
</asp:Content>


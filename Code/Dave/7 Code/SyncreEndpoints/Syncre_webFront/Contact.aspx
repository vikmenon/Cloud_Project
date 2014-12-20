<%@ Page Title="Contact" Language="C#" MasterPageFile="~/Site.Master" AutoEventWireup="true" CodeBehind="Contact.aspx.cs" Inherits="Syncre_website.Contact" %>

<asp:Content runat="server" ID="BodyContent" ContentPlaceHolderID="MainContent">
    <hgroup class="title">
        <h1><%: Title %>&nbsp;Us</h1>
    </hgroup>

    <section class="contact">
        <header>
            <h3>Phone:</h3>
        </header>
        <p>
            <span class="label">Main:</span>
            813<span>.813.1111</span>
        </p>
        <p>
            <span class="label">After Hours:</span>
            814<span>.813.000</span>
        </p>
    </section>

    <section class="contact">
        <header>
            <h3>Email:</h3>
        </header>
        <p>
            <span class="label">Support:</span> <span><a href="mailto:Support@example.com">support@sincre.com</a></span>
        </p>
        <p>
            <span class="label">Marketing:</span>&nbsp; <span><a href="mailto:Marketing@example.com">marketing@sincre.com</a></span>
        </p>
        <p>
            <span class="label">General:</span> <span><a href="mailto:General@example.com">general@sincre.com</a></span>
        </p>
    </section>

    <section class="contact">
        <header>
            <h3>Address:</h3>
        </header>
        <p>
            #1 Cloud Drive<br />
            Gainesville, FL 32601</p>
    </section>
</asp:Content>
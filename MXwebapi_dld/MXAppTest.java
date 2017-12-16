/*                                                      MXAppTest.java
 **      MX WEB Client API module.
 **
 **      This module contains client interface software to allow access to
 **      IMO Message Exchange WEB server functionality. It is used by the
 **      Client Application software.
 **
 **      This module defines the MXRequest class.
 **
 **      MXRequest uses the PostOutputStream and
 **      EntrustRequest classes to communicate with the MX server. That is, the
 **      above two classes act as the actual HTTP interface. The
 **      PostOutputStream class is used for posting requests and writing
 **      responses for the client to use in their appication. The EntrustRequest
 **      class is used for SSL I/O and digital signaturing.
 **
 **      The public methods currently implemented are as follows:
 **
 **              * getMessage
 **              * sendReject
 **              * sendAccept
 **
 **      See javadoc file for more details.
 **
 ** History:
 **      000     Apr, 2001    pxp    Created
 **
 *
 *
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */

import  com.abb.mpi.MXRequest;
/*
 ** Global imports.
 */
import  java.io.*;
import  java.net.URL;
import  java.applet.*;
import  java.util.*;
import  java.text.MessageFormat;
//import  java.net.HttpURLConnection;
import sun.net.www.protocol.http.HttpURLConnection;

/**
 **
 ** The <code>MXAppTest</code> class is used to test <code><B>MXRequest API</B></code>
 ** functionality. It will used by Market Participants as a guidance tool.
 ** <p>
 ** <code><B>MXAppTest</B></code> uses the
 ** <code><B>MXRequest API</B></code> as a door to log in to the
 ** IMO MX Server, through the WEB Server.
 ** <p>
 ** Please take a look at the Javadoc for the <code>MXRequest API</code>.
 ** <p>
 ** The MXAppTest has both the <code>BASIC</code> and <code>ENTRUST</code> login methods.
 ** Strong authentication, data encryption and digital signaturing using the provided
 ** Entrust PKI infrastructure.
 ** Market Participant has to use the <code>ENTRUST</code> for testing the functinality
 ** of this test program.
 ** <p>
 ** Note that this class is only a supporting class. This does not provide
 ** access to Message Exchange on its own. The <code>MXRequest API</code>
 ** class provides the access.
 ** <p>
 **    <i>Login.</i> <code>MXRequest</code> users may authenticate using
 *     Entrust strong authentication. This is dicated by
 **    the constructor used. Invoke the <code>login</code> method
 **    after creating an <code>MXRequest</code> instance to perform the actual
 **    login. In MXAppTest, the login is done via asking all the parameters to the user
 **    on a command line. Later, user has to provide all the parameters to MXRequest for
 **    successful login.
 ** <p>
 **    <i>Download Messages.</i> This method is used to get new,active,alarm
 **    messages from server once there is a successful login. Use the
 **    <code>getMessage</code> method.
 ** <p>
 **    <i>Receipt.</i> Message Exchange Server requires that once the message
 **    is received at the clients' end, client should send back the receipt with a
 **    message ID, so that message can be marked as received in the Database.
 **    This functin is called automatically for the user. User doesn't have to worry
 **    about sending the receipts back.
 ** <p>
 **    <i>Response.</i> Message Exchange Server requires that the client send
 **    send back a response for a particular messageID. A <code><B>Vector</B></code>
 *     of messageID's which are to be rejected, is passed as a parameter to the sendReject method.
 **    Use the following method to send Response:
 **    <code>sendReject</code>.
 ** <p>
 ** <i>Notes.</i>
 ** <p>
 ** For application users, ensure that your CLASSPATH includes an archive
 ** file: <code>mxapi.jar</code>, <code>MPIApplication.jar</code> and
 *  <code>capsapi_classes.zip</code>.
 ** <p>
 ** Please take a look at the javadoc for the MXRequest for full information.
 */
public class MXAppTest {

    /**
     ** Create and exercise test <code>MXAppTest</code> object. The program
     ** logs-in in Entrust authentication mode. Based on
     ** the mode specified, required login information
     ** is collected (e.g. Entrust needs CA host, port, EPF and EPF
     ** password). If Entrust is specified, this program downloads all the message
     ** types and sends the receipt as well as the response.
     ** <p>
     ** In this application, message is parsed using <code>MessageFormat</code> class provided by Sun.
     ** Users are free to use any XML Parsers available in the Market.
     ** <p>
     ** The main function should be used like <code>usage: java MPIRequest [basic | entrust]</code>
     ** <p>
     ** If <code>Entrust</code> authentication is required, fetch CA host, port,
     ** EPF (Entrust Profile File) file name.
     ** @param <code>Web_host</code>: The Web Server IP Address to connect to.
     ** @param <code>Web_Port</code>: The Web Server port to connect to.
     ** @param <code>Dir_host</code>: The CA Directory Server, the user should connect to.
     ** @param <code>Dir_Port</code>: The CA Directory Server Port Number.
     ** @param <code>EPF</code>: The EPF File physical location.
     ** @param <code>EPF_Password</code>: The EPF Password.
     *
     */
    public static void main (String args[]) {

        MXRequest mr = null;
        /*
         ** Determine mode to run in - Entrust or Basic authentication.
         ** Collect processing parameters and execute user' MX processing requests.
         */
        BufferedReader ci = new BufferedReader(new InputStreamReader(System.in));
        /*
         ** Set some default processing variables.
         */
        boolean dbg = false;
        int port = 80;
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        Vector response = new Vector(100);
        /*
         ** Are we going to debug this session.
         */
        try {
            dbg = true;
            host = "";
            port = 0;
            password = "";

             dbg = fetchParam(ci, "Debug (Y/N)?    : ").equalsIgnoreCase("Y");
             host = fetchParam(ci, "Web host        : ");
             port = Integer.parseInt(fetchParam(ci, "Web port (443 or 80)        : "));
             protocol = fetchParam(ci, "Protocol (HTTPS or HTTP)        : ");
             username = fetchParam(ci, "Username             : ");
             password = fetchParam(ci, "Password    : ");

	     mr = new MXRequest(host, protocol, port, username,
             password, MXRequest.APPLICATION);
            /*
             ** Set debug and attempt to login Entrust user.
             */
            mr.setDebug(dbg);
            try {
                if (mr.login() == false) {
                    //System.out.println(mr.getStatus());
                    System.exit(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*
             ** Login OK, see if we can fetch market status information.
             */
            System.out.println("Logged-in user: " + mr.getUsername());
            String request = "not done";
            /*
             ** Process user requests until we're "done".
             */
	     Thread.sleep(3000);
            while (true) {
                try {
                    Vector receivedMessages = mr.getMessage();
                    if (receivedMessages != null) {
                        int count = receivedMessages.size();
                        // loop through all the elements in the vector
                        for (int i = 0; i < count; i++) {
                            String message = (String)receivedMessages.elementAt(i);
                            String messid = "";
                            String type = "";
                            //System.out.println("The Message Received is:\n"
                             //      + message);
                            try {
                                MessageFormat form = new MessageFormat("{0}<MESSAGEID>{1}</MESSAGEID>{2}<TYPE>{3}</TYPE>{4}");
                                Object[] retO = form.parse(message);
                                messid = retO[1].toString();                    //RESOURCEID
                                type = retO[3].toString();
                                if (type.equalsIgnoreCase("DISPATCH"))
                                    response.add(messid);
                            } catch (Exception e) {
			      System.out.println(message);
                                e.printStackTrace();
                            }
                        }
                        if (response.size() > 0) {
                            mr.sendReject(response);
                            response.removeAllElements();
                        }
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        /*
         ** Something went wrong, probably the EntrustRequest login failed.
         */

    }

    /*
     ** fetchParam - Fetch processing parameter from specified reader.
     */
    private static String fetchParam (BufferedReader ci, String prompt) throws IOException {
        System.out.print("-> " + prompt);
        return  ci.readLine();
    }
}




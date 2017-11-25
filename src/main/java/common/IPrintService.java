package common;

import common.AuthTicket;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by ricky on 01/11/2017.
 */
public interface IPrintService extends Remote {
    AuthTicket print(String filename, String printer, AuthTicket authTicket) throws Exception;   // prints file filename on the specified printer

    AuthTicket queue(AuthTicket authTicket) throws Exception;   // lists the print_service queue on the user's display in lines of the form <job number>   <file name>

    AuthTicket topQueue(int job, AuthTicket authTicket) throws Exception;   // moves job to the top of the queue

    AuthTicket start(AuthTicket authTicket) throws Exception;   // starts the print_service server

    AuthTicket stop(AuthTicket authTicket) throws Exception;   // stops the print_service server

    AuthTicket restart(AuthTicket authTicket) throws Exception;   // stops the print_service server, clears the print_service queue and starts the print_service server again

    AuthTicket status(AuthTicket authTicket) throws Exception;  // prints status of printer on the user's display

    AuthTicket readConfig(String parameter, AuthTicket authTicket) throws Exception;   // prints the value of the parameter on the user's display

    AuthTicket setConfig(String parameter, String value, AuthTicket authTicket) throws Exception;   // sets the parameter to value
}

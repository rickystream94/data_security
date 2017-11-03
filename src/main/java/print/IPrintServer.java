package print;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by ricky on 01/11/2017.
 */
public interface IPrintServer extends Remote {
    AuthTicket print(String filename, String printer, AuthTicket authTicket) throws RemoteException;   // prints file filename on the specified printer

    void queue(String username) throws RemoteException;   // lists the print queue on the user's display in lines of the form <job number>   <file name>

    void topQueue(int job, String username) throws RemoteException;   // moves job to the top of the queue

    void start(String username) throws RemoteException;   // starts the print server

    void stop(String username) throws RemoteException;   // stops the print server

    void restart(String username) throws RemoteException;   // stops the print server, clears the print queue and starts the print server again

    void status(String username) throws RemoteException;  // prints status of printer on the user's display

    void readConfig(String parameter, String username) throws RemoteException;   // prints the value of the parameter on the user's display

    void setConfig(String parameter, String value, String username) throws RemoteException;   // sets the parameter to value

    /**
     * Logs the specified user in the system and returns a welcome message
     *
     * @param authTicket client's ticket with authentication information
     * @return welcome message from server
     */
    AuthTicket login(AuthTicket authTicket) throws RemoteException;

    AuthTicket signUp(AuthTicket authTicket) throws RemoteException;

    void quitSession(AuthTicket authTicket) throws RemoteException;
}

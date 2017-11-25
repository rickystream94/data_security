package common;

import server.PermissionType;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAuthenticator extends Remote {
    /**
     * Logs the specified user in the system and returns a welcome message
     *
     * @param authTicket client's ticket with authentication information
     * @return welcome message from server
     */
    AuthTicket login(AuthTicket authTicket) throws RemoteException;

    void quitSession(AuthTicket authTicket) throws RemoteException;
}

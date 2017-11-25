package server;

import common.AuthTicket;

import java.security.Permission;

public class AclPolicyManager extends PolicyManager {

    /**
     * Checks if the user specified by the AuthTicket has the right to perform the action identified by the specified Permission
     * @param permission
     * @param authTicket
     * @throws Exception
     */
    @Override
    public void checkPermission(Permission permission, AuthTicket authTicket) throws Exception {
    }
}

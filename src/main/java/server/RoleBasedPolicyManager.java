package server;

import common.AuthTicket;

import java.security.Permission;

public class RoleBasedPolicyManager extends PolicyManager {

    RoleBasedPolicyManager(String policyFilePath) {
        super(policyFilePath);
    }

    @Override
    public void checkPermission(Permission permission, String username) throws Exception {
        //TODO: TO IMPLEMENT
    }

    @Override
    public void grantPermission(Permission permission, String username) throws Exception {

    }

    @Override
    public void denyPermission(Permission permission, String username) throws Exception {

    }
}

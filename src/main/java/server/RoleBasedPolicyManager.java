package server;

import common.Util;

import java.security.Permission;

public class RoleBasedPolicyManager extends PolicyManager {

    private static final String POLICY_FILE_PATH = "access_control/rbac_policy.json";
    private static final String TAG = "[*** RoleBasedPolicyManager ***]";

    RoleBasedPolicyManager() {
        super(POLICY_FILE_PATH);
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

    private void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}

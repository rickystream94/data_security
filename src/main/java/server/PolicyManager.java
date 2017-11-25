package server;

import common.AuthTicket;

import java.security.Permission;

public abstract class PolicyManager {
    private static final String POLICY_FILE_PATH = "policy/policy.json";

    public PolicyManager() {

    }

    public abstract void checkPermission(Permission permission, AuthTicket authTicket) throws Exception;
}

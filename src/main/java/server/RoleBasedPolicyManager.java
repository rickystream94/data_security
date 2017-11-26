package server;

import common.Util;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RoleBasedPolicyManager extends PolicyManager {

    private static final String POLICY_FILE_PATH = "access_control/rbac_policy.json";
    private static final String TAG = "[*** RoleBasedPolicyManager ***]";

    RoleBasedPolicyManager() {
        super(POLICY_FILE_PATH);
    }

    @Override
    public void checkPermission(Permission permission, String username) throws Exception {
        String error;
        JsonObject userRoles = super.policy.getJsonObject("user_roles");
        JsonObject rolesPolicy = super.policy.getJsonObject("roles");

        //Return immediately if user doesn't have a role
        if (!userRoles.keySet().contains(username)) {
            error = "Error: User " + username + " doesn't have a role!";
            logInfo(error);
            throw new Exception(error);
        }

        //Check if user was granted the specified permission
        if (permission instanceof PrinterPermission) {
            //Get type of requested permission
            PermissionType permissionType = ((PrinterPermission) permission).getPermissionType();

            //Get user's role
            String userRole = userRoles.getString(username);

            //Compute set of permissions the specified user has
            Set<String> permits = new HashSet<>();
            permits = getPermitsPerRole(rolesPolicy, userRole, permits);
            logInfo("Checking if user " + username + " has '" + permissionType.name() + "' permit among all his permits: " + permits.toString() + "");

            //Check if user has requested permission
            if (permits.contains(permissionType.name())) {
                logInfo("Permission " + permissionType.name() + " granted for user " + username + ": Authorized!");
                return;
            } else {
                error = "Error: User " + username + " doesn't have the requested '" + permissionType.name() + "' permission to perform this action: Unauthorized!";
                logInfo(error);
                throw new Exception(error);
            }
        }

        //Other permission types should be handled here...
        throw new Exception("Unhandled permission type!");
    }

    /**
     * Computes recursively all the permissions that a user with a specified role is granted, according to the role hierarchy
     * defined in the rolesPolicy
     *
     * @param rolesPolicy
     * @param role
     * @param permits
     * @return
     */
    private Set<String> getPermitsPerRole(JsonObject rolesPolicy, String role, Set<String> permits) {
        JsonObject userRolePolicy = rolesPolicy.getJsonObject(role);
        JsonArray permitsJsonArray = userRolePolicy.getJsonArray("permits");
        JsonArray inheritsJsonArray = userRolePolicy.getJsonArray("inherits");
        String[] permitsArray = new String[permitsJsonArray.size()];
        String[] inheritsArray = new String[inheritsJsonArray.size()];
        for (int i = 0; i < permitsJsonArray.size(); i++) {
            permitsArray[i] = permitsJsonArray.getString(i);
        }
        for (int i = 0; i < inheritsJsonArray.size(); i++) {
            inheritsArray[i] = inheritsJsonArray.getString(i);
        }
        //Add all permits for the current role
        permits.addAll(Arrays.asList(permitsArray));

        //Recursive step: add also all the permits for the parent roles
        if (inheritsArray.length != 0)
            Arrays.stream(inheritsArray).forEach(parentRole -> permits.addAll(getPermitsPerRole(rolesPolicy, parentRole, permits)));
        return permits;
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

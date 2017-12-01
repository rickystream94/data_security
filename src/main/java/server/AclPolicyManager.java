package server;

import common.Util;

import javax.json.*;
import java.security.Permission;
import java.util.Arrays;

public class AclPolicyManager extends PolicyManager {

    private static final String POLICY_FILE_PATH = "access_control/acl_policy.json";
    private static final String TAG = "[*** AclPolicyManager ***]";

    AclPolicyManager() {
        super(POLICY_FILE_PATH);
    }

    @Override
    public void checkPermission(Permission permission, String username) throws Exception {
        String error;
        JsonObject aclEntries = super.policy.getJsonObject("entries");

        //Return immediately if user doesn't show up in the ACL
        if (!aclEntries.keySet().contains(username)) {
            error = "Error: User " + username + " doesn't have any permission!";
            logInfo(error);
            throw new Exception(error);
        }

        //Check if user was granted the specified permission
        if (permission instanceof PrinterPermission) {
            //Get type of requested permission
            PermissionType permissionType = ((PrinterPermission) permission).getPermissionType();

            //Get the AclEntry corresponding to the specified user
            JsonObject aclEntry = aclEntries.getJsonObject(username);

            //Get the list of permits
            JsonArray permitsJsonArray = aclEntry.getJsonArray("permits");
            String[] permitsArray = new String[permitsJsonArray.size()];
            for (int i = 0; i < permitsJsonArray.size(); i++) {
                permitsArray[i] = permitsJsonArray.getString(i);
            }
            logInfo("Checking if user " + username + " has '" + permissionType.name() + "' permit among all his permits: " + Arrays.asList(permitsArray).toString() + "");

            //If user has the specified permission return silently
            if (Arrays.stream(permitsArray).anyMatch(permit -> permit.equals(permissionType.name()))) {
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

    private void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}

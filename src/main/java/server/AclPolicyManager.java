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

    /**
     * Checks if the user specified by the AuthTicket has the right to perform the action identified by the specified Permission.
     * If the user is authorized, then the method returns silently. Otherwise, an exception with an error message is raised.
     *
     * @param permission
     * @param username
     * @throws Exception
     */
    @Override
    public void checkPermission(Permission permission, String username) throws Exception {
        if (permission instanceof PrinterPermission) {
            PermissionType permissionType = ((PrinterPermission) permission).getPermissionType();

            //Get the AclEntry corresponding to the specified user
            JsonObject aclEntry = super.policy.getJsonObject(username);

            //Get the list of permits
            JsonArray permitsJsonArray = aclEntry.getJsonArray("allowed");
            String[] permitsArray = new String[permitsJsonArray.size()];
            for (int i = 0; i < permitsJsonArray.size(); i++) {
                permitsArray[i] = permitsJsonArray.getString(i);
            }

            //If user has the specified permission OR the special "ALL" permission type return silently
            if (Arrays.stream(permitsArray).anyMatch(x -> x.equals(PermissionType.ALL.name()) || x.equals(permissionType.name())))
                return;
            else
                throw new Exception("Error: User " + username + " doesn't have the requested '" + permissionType.name() + "' permission to perform this action!");
        }
        //Other permission types should be handled here...
        throw new Exception("Unhandled permission type!");
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

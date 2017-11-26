package server;

import common.Util;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.security.Permission;

public abstract class PolicyManager {

    private static final String TAG = "[*** PolicyManager ***]";
    JsonObject policy;

    PolicyManager(String policyFilePath) {
        try {
            loadPolicy(policyFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void loadPolicy(String policyFilePath) throws IOException {
        File policyFile = new File(policyFilePath);
        if (!policyFile.exists())
            throw new FileNotFoundException("Policy file is not found with path " + policyFilePath);
        try (FileInputStream inputStream = new FileInputStream(policyFile)) {
            byte[] data = inputStream.readAllBytes();
            String content = new String(data, "UTF-8");
            JsonReader jsonReader = Json.createReader(new StringReader(content));
            policy = jsonReader.readObject();
            logInfo("JSON Policy File correctly loaded!");
        }
    }

    public abstract void checkPermission(Permission permission, String username) throws Exception;

    public abstract void grantPermission(Permission permission, String username) throws Exception;

    public abstract void denyPermission(Permission permission, String username) throws Exception;

    private void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}

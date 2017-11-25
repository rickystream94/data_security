package server;

import java.security.Permission;

public class PrinterPermission extends Permission {
    PermissionType permissionType;
    /**
     * Constructs a permission with the specified name.
     */
    public PrinterPermission(PermissionType permissionType) {
        super("printerPermission");
        this.permissionType = permissionType;
    }

    public PermissionType getPermissionType() {
        return this.permissionType;
    }

    @Override
    public boolean implies(Permission permission) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getActions() {
        return null;
    }
}

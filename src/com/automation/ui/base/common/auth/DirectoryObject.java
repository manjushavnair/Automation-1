package com.automation.ui.base.common.auth;

/**
 * Directory Service Contributor
 */
public abstract class DirectoryObject {


    public DirectoryObject() {
        super();
    }

    /**
     * @return String velue
     */
    public abstract String getObjectId();

    /**
     *@param objectId
     */
    public abstract void setObjectId(String objectId);

    /**
     * @return String velue
     */
    public abstract String getObjectType();

    /**
     * @param objectType
     */
    public abstract void setObjectType(String objectType);

    /**
     * @return String velue
     */
    public abstract String getDisplayName();

    /**
     * @param displayName
     */
    public abstract void setDisplayName(String displayName);

}
package com.app.model;

import java.util.Date;

public class SharedDocument {
    private int id;
    private int documentId;
    private int sharedWithUserId;
    private String permission;
    private int sharedBy;
    private Date sharedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public int getSharedWithUserId() {
        return sharedWithUserId;
    }

    public void setSharedWithUserId(int sharedWithUserId) {
        this.sharedWithUserId = sharedWithUserId;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public int getSharedBy() {
        return sharedBy;
    }

    public void setSharedBy(int sharedBy) {
        this.sharedBy = sharedBy;
    }

    public Date getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Date sharedAt) {
        this.sharedAt = sharedAt;
    }
}

package gov.mota.scholarship.data.model;

import java.io.Serializable;

public class WalletDocument implements Serializable {
    private String docId;
    private String title;
    private String authority;
    private String docNumber;
    private String status;
    private String issueDate;
    private boolean isReused;
    private boolean fileAttached; // true only if a real file was uploaded by the user
    private String localFilePath; // Internal private storage path in the app
    private String mimeType;      // e.g. application/pdf, image/jpeg

    public WalletDocument() {}

    // Constructor without fileAttached — used by JSON seed (defaults to true = verified document)
    public WalletDocument(String docId, String title, String authority, String docNumber, String status, String issueDate, boolean isReused) {
        this.docId = docId;
        this.title = title;
        this.authority = authority;
        this.docNumber = docNumber;
        this.status = status;
        this.issueDate = issueDate;
        this.isReused = isReused;
        this.fileAttached = true;
        this.localFilePath = "";
        this.mimeType = "application/pdf";
    }

    // Full constructor with fileAttached flag
    public WalletDocument(String docId, String title, String authority, String docNumber, String status, String issueDate, boolean isReused, boolean fileAttached) {
        this.docId = docId;
        this.title = title;
        this.authority = authority;
        this.docNumber = docNumber;
        this.status = status;
        this.issueDate = issueDate;
        this.isReused = isReused;
        this.fileAttached = fileAttached;
        this.localFilePath = "";
        this.mimeType = "application/pdf";
    }

    // Extended constructor with local file path and mimeType
    public WalletDocument(String docId, String title, String authority, String docNumber, String status, String issueDate, boolean isReused, boolean fileAttached, String localFilePath, String mimeType) {
        this.docId = docId;
        this.title = title;
        this.authority = authority;
        this.docNumber = docNumber;
        this.status = status;
        this.issueDate = issueDate;
        this.isReused = isReused;
        this.fileAttached = fileAttached;
        this.localFilePath = localFilePath != null ? localFilePath : "";
        this.mimeType = mimeType != null ? mimeType : "application/pdf";
    }

    public String getDocId() { return docId; }
    public String getTitle() { return title; }
    public String getAuthority() { return authority; }
    public String getDocNumber() { return docNumber; }
    public String getStatus() { return status; }
    public String getIssueDate() { return issueDate; }
    public boolean isReused() { return isReused; }
    public boolean isFileAttached() { return fileAttached; }
    public String getLocalFilePath() { return localFilePath; }
    public void setLocalFilePath(String localFilePath) { this.localFilePath = localFilePath; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}

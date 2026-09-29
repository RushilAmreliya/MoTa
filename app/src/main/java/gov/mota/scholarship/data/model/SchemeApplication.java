package gov.mota.scholarship.data.model;

import java.io.Serializable;
import java.util.List;

public class SchemeApplication implements Serializable {
    private String applicationId;
    private String schemeCode;
    private String schemeTitle;
    private String academicYear;
    private String sourceSystem;
    private String stage;
    private String status;
    private List<String> verificationBadges;
    private String dbtStatus;
    private String amountInr;
    private String pfmsTransactionId;
    private String remarks;

    public SchemeApplication() {}

    public SchemeApplication(String applicationId, String schemeCode, String schemeTitle,
                             String academicYear, String sourceSystem, String stage,
                             String status, List<String> verificationBadges, String dbtStatus,
                             String amountInr, String pfmsTransactionId, String remarks) {
        this.applicationId = applicationId;
        this.schemeCode = schemeCode;
        this.schemeTitle = schemeTitle;
        this.academicYear = academicYear;
        this.sourceSystem = sourceSystem;
        this.stage = stage;
        this.status = status;
        this.verificationBadges = verificationBadges;
        this.dbtStatus = dbtStatus;
        this.amountInr = amountInr;
        this.pfmsTransactionId = pfmsTransactionId;
        this.remarks = remarks;
    }

    public String getApplicationId() { return applicationId; }
    public String getSchemeCode() { return schemeCode; }
    public String getSchemeTitle() { return schemeTitle; }
    public String getAcademicYear() { return academicYear; }
    public String getSourceSystem() { return sourceSystem; }
    public String getStage() { return stage; }
    public String getStatus() { return status; }
    public List<String> getVerificationBadges() { return verificationBadges; }
    public String getDbtStatus() { return dbtStatus; }
    public String getAmountInr() { return amountInr; }
    public String getPfmsTransactionId() { return pfmsTransactionId; }
    public String getRemarks() { return remarks; }
}

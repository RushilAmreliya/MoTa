package gov.mota.scholarship.data.model;

import java.io.Serializable;
import java.util.List;

public class Beneficiary implements Serializable {
    private String fullName;
    private String aadhaarNumber;
    private String dob;
    private String gender;
    private String category;
    private boolean isPvtg;
    private String pvtgCommunityName;
    private String householdId;
    private String otrId;
    private String apaarId;
    private String mobileNumber;
    private String state;
    private String district;
    private String institutionName;
    private String aisheUdiseCode;
    private String bankName;
    private String accountMasked;
    private boolean npciMapped;
    private String npciFailureReason;
    private String npciRemediation;
    private List<SchemeApplication> applications;
    private List<WalletDocument> documents;

    public Beneficiary() {}

    public Beneficiary(String fullName, String aadhaarNumber, String dob, String gender,
                       String category, boolean isPvtg, String pvtgCommunityName,
                       String householdId, String otrId, String apaarId, String mobileNumber,
                       String state, String district, String institutionName,
                       String aisheUdiseCode, String bankName, String accountMasked,
                       boolean npciMapped, String npciFailureReason, String npciRemediation,
                       List<SchemeApplication> applications, List<WalletDocument> documents) {
        this.fullName = fullName;
        this.aadhaarNumber = aadhaarNumber;
        this.dob = dob;
        this.gender = gender;
        this.category = category;
        this.isPvtg = isPvtg;
        this.pvtgCommunityName = pvtgCommunityName;
        this.householdId = householdId;
        this.otrId = otrId;
        this.apaarId = apaarId;
        this.mobileNumber = mobileNumber;
        this.state = state;
        this.district = district;
        this.institutionName = institutionName;
        this.aisheUdiseCode = aisheUdiseCode;
        this.bankName = bankName;
        this.accountMasked = accountMasked;
        this.npciMapped = npciMapped;
        this.npciFailureReason = npciFailureReason;
        this.npciRemediation = npciRemediation;
        this.applications = applications;
        this.documents = documents;
    }

    public String getFullName() { return fullName; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getCategory() { return category; }
    public boolean isPvtg() { return isPvtg; }
    public String getPvtgCommunityName() { return pvtgCommunityName; }
    public String getHouseholdId() { return householdId; }
    public String getOtrId() { return otrId; }
    public String getApaarId() { return apaarId; }
    public String getMobileNumber() { return mobileNumber; }
    public String getState() { return state; }
    public String getDistrict() { return district; }
    public String getInstitutionName() { return institutionName; }
    public String getAisheUdiseCode() { return aisheUdiseCode; }
    public String getBankName() { return bankName; }
    public String getAccountMasked() { return accountMasked; }
    public boolean isNpciMapped() { return npciMapped; }
    public String getNpciFailureReason() { return npciFailureReason; }
    public String getNpciRemediation() { return npciRemediation; }
    public List<SchemeApplication> getApplications() { return applications; }
    public List<WalletDocument> getDocuments() { return documents; }
}

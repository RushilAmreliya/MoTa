package gov.mota.scholarship.data.model;

import java.io.Serializable;

public class SchemeCatalogItem implements Serializable {
    private String schemeCode;
    private String schemeTitle;
    private String targetAudience;
    private String financialBenefit;
    private String sourcePortal;
    private String eligibilitySummary;
    private String requiredRegistry;

    public SchemeCatalogItem(String schemeCode, String schemeTitle, String targetAudience,
                             String financialBenefit, String sourcePortal, String eligibilitySummary,
                             String requiredRegistry) {
        this.schemeCode = schemeCode;
        this.schemeTitle = schemeTitle;
        this.targetAudience = targetAudience;
        this.financialBenefit = financialBenefit;
        this.sourcePortal = sourcePortal;
        this.eligibilitySummary = eligibilitySummary;
        this.requiredRegistry = requiredRegistry;
    }

    public String getSchemeCode() { return schemeCode; }
    public String getSchemeTitle() { return schemeTitle; }
    public String getTargetAudience() { return targetAudience; }
    public String getFinancialBenefit() { return financialBenefit; }
    public String getSourcePortal() { return sourcePortal; }
    public String getEligibilitySummary() { return eligibilitySummary; }
    public String getRequiredRegistry() { return requiredRegistry; }
}

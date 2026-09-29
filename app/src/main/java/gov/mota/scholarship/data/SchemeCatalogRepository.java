package gov.mota.scholarship.data;

import gov.mota.scholarship.data.model.SchemeCatalogItem;
import java.util.ArrayList;
import java.util.List;

public class SchemeCatalogRepository {
    private static final List<SchemeCatalogItem> CATALOG = new ArrayList<>();

    static {
        CATALOG.add(new SchemeCatalogItem(
                "PRE_MATRIC",
                "Pre-Matric Scholarship for ST Students",
                "Students in Classes IX and X (Day Scholars & Hostellers)",
                "₹ 12,000 / year + Ad-hoc Grant",
                "NSP / MoTA Unified Portal",
                "Parental annual income <= ₹ 2.50 Lakh. Enrolled in recognized govt/aided school.",
                "UDISE+ School Registry, State e-District ST Certificate"
        ));

        CATALOG.add(new SchemeCatalogItem(
                "POST_MATRIC",
                "Post-Matric Scholarship for ST Students",
                "Class XI through Post-Graduation & Professional Degrees",
                "₹ 48,000 to ₹ 75,000 / year (Tuition + Maintenance)",
                "NSP / MoTA Unified Portal",
                "Parental annual income <= ₹ 2.50 Lakh. Valid ST caste certificate.",
                "APAAR/ABC, AISHE Institute Code, DigiLocker Income & Caste"
        ));

        CATALOG.add(new SchemeCatalogItem(
                "TOP_CLASS",
                "Top Class Education Scheme for ST Students",
                "Undergraduate / Postgraduate studies in Notified Top Institutes (IITs, IIMs, NITs, AIIMS)",
                "Full Tuition Fee + Living Expenses (Up to ₹ 2,20,000 / year)",
                "NSP / MoTA Direct",
                "Admitted to MoTA-notified premier institutions. Family income <= ₹ 6.00 Lakh.",
                "AISHE Premier Institute Roster, DigiLocker Caste & Income"
        ));

        CATALOG.add(new SchemeCatalogItem(
                "NFST",
                "National Fellowship for Higher Education of ST Students",
                "M.Phil and Ph.D Scholars in Indian Universities",
                "₹ 31,000 to ₹ 35,000 / month + HRA + Contingency Grant",
                "SFMP - Canara Bank",
                "Cleared UGC-NET or CSIR-NET. Enrolled in regular full-time Ph.D/M.Phil.",
                "NTA UGC-NET Roll Verification, AISHE University Code"
        ));

        CATALOG.add(new SchemeCatalogItem(
                "NOS",
                "National Overseas Scholarship (NOS) for ST Candidates",
                "Master’s & Ph.D degrees in Top 500 QS-Ranked Foreign Universities",
                "Full Tuition Fee + £ 18,500 / $ 15,400 per annum Living Allowance + Visa & Travel",
                "Standalone NOS Portal",
                "Age < 35 years. Total family income <= ₹ 8.00 Lakh. Min 60% marks in qualifying degree.",
                "Unconditional Foreign University Offer Letter, Passport/Visa Registry"
        ));
    }

    public static List<SchemeCatalogItem> getAllSchemes() {
        return CATALOG;
    }

    public static SchemeCatalogItem getSchemeByCode(String code) {
        for (SchemeCatalogItem item : CATALOG) {
            if (item.getSchemeCode().equalsIgnoreCase(code)) {
                return item;
            }
        }
        return null;
    }
}

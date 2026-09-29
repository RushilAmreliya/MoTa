# MoTA Unified Scholarship Android App

An Android application built strictly adhering to the requirements of:
- [MoTA_Scholarship_Platform_PRD.md](MoTA_Scholarship_Platform_PRD.md)
- [MoTA_Scholarship_Tech_Stack_Doc.md](MoTA_Scholarship_Tech_Stack_Doc.md)

---

## 🚀 Key Feature Updates

### 1. Robust Scheme Selection (RadioGroup)
- Replaced the previous dropdown spinner with a direct **Material 3 Scheme Radio Selection Card**.
- Tap directly to select between all **5 MoTA Schemes**:
  1. **Pre-Matric Scholarship for ST Students (Class IX-X)**
  2. **Post-Matric Scholarship for ST Students (Class XI to PG)**
  3. **Top Class Education Scheme (Premier Institutes: IIT/NIT/IIM/AIIMS)**
  4. **National Fellowship for Higher Education (NFST M.Phil/Ph.D)**
  5. **National Overseas Scholarship (NOS for Foreign QS Top 500 Universities)**
- Selecting any scheme immediately updates the **financial benefits, eligibility criteria, and required verification registries**.

### 2. Dynamic Scheme-Specific Application Forms
The application form dynamically reconfigures to ask only for the specific information and documents required for the chosen scheme:

| Scheme | Dynamic Particulars Asked | Dynamic Documents Asked |
| :--- | :--- | :--- |
| **Pre-Matric** | School UDISE+ Code, Enrolled Class (IX/X), Hosteller or Day Scholar | ST Certificate, Income Cert (&le; ₹ 2.5L), Previous Standard Marksheet / Headmaster Bonafide |
| **Post-Matric** | College AISHE Code, Course & Year of Study, APAAR / ABC Student ID | ST Certificate, Income Cert (&le; ₹ 2.5L), Class X Board Marksheet / APAAR Record |
| **Top Class** | Notified Top Institute Name, Entrance Exam & All India Rank (AIR), Annual Tuition Fee Claim | ST Certificate, Income Cert (&le; ₹ 6.0L), Premier Institute Allotment Letter & Fee Structure |
| **NFST (Fellowship)** | UGC-NET / CSIR-NET Roll Number, Ph.D Research Topic, Ph.D Registration Date | ST Certificate, University Ph.D Enrolment & Supervisor Bonafide, UGC-NET/JRF Scorecard |
| **NOS (Overseas)** | Foreign University (QS Top 500), Foreign Degree & Subject, Passport Number | ST Certificate, Total Household Income Cert (&le; ₹ 8.0L), Unconditional Offer Letter & Passport |

---

## 🔑 Demo Login Credentials

| Full Name | 12-Digit Aadhaar | Status / Role | Testing Purpose |
| :--- | :--- | :--- | :--- |
| **Kavita Marandi** | `789012345678` | **⭐ Fresh Applicant** | **0 Apps, 0 Docs. Test scheme selection & dynamic scheme forms!** |
| **Birsa Munda** | `234567890123` | Existing ST | Household `HH-JH-RANCHI-001` (Post-Matric) |
| **Salomi Munda** | `234567890124` | Existing ST | Household `HH-JH-RANCHI-001` (Pre-Matric) |
| **Sunita Marandi** | `345678901234` | Existing ST (PVTG) | NFST Fellowship |
| **Kiran Kumar Jamatia** | `456789012345` | Existing ST | Top Class Education Scheme |
| **Anjali Gond** | `567890123456` | Existing ST | NOS (Oxford) |
| **Rameshwar Uraon** | `678901234567` | Existing ST | Pre-Matric (*DBT Failed / NPCI Remediation Demo*) |

*(The login page features 1-tap quick buttons to load any of these verified credentials).*

---

---

## 📦 Direct APK Download & Installation

The application is pre-compiled into a standalone APK ready for installation on any Android device running Android 7.0 (Nougat, API 24) or higher.

### 📥 Option 1: 1-Click Direct Download Link
Click this link to download the APK directly without opening the GitHub file viewer:
👉 **[Click Here to Download MoTA-Scholarship-Portal.apk](https://github.com/RushilAmreliya/MoTa/raw/main/release/MoTA-Scholarship-Portal.apk)** (File size: ~6.05 MB)

Or in the GitHub file viewer, click the **Download raw file (⬇)** button or **View raw** link.

- Transfer it to your Android device or download it directly via your mobile browser.
- Tap the `.apk` file and select **Install** (allow *Install unknown apps* for your browser/file manager if prompted).
- The app runs immediately without any extra setup or backend dependencies!

### ⚙️ Option 2: Automated GitHub Releases / Actions Artifact
- Every push to GitHub runs the [Build Android APK workflow](.github/workflows/build-apk.yml).
- You can download the latest generated APK directly under the **Actions** tab -> **Artifacts** -> **`MoTA-Scholarship-Portal-APK`**.
- Or via GitHub Releases: [Releases Page](https://github.com/RushilAmreliya/MoTa/releases)

---

## 📱 How to Run in Android Studio on your Mobile Phone

1. Open **Android Studio** (`D:\Android\bin\studio64.exe`).
2. Open directory **`D:\Final`**.
3. Plug in your Android mobile device via USB with **USB Debugging** enabled.
4. Click the green **Run (▶)** button or press `Shift + F10`.
5. Select **Kavita Marandi** (`⭐ [FRESH APPLICANT]`), tap **+ Apply Scholarship**, and tap between the 5 schemes to see the dynamic fields adapt in real time.


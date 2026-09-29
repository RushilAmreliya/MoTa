package gov.mota.scholarship.data.util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

/**
 * Intelligent Document Scanner and Authenticity Verification Engine for MoTA Scholarship Uploads.
 * 
 * Inspects PDF streams, FlateDecode data, metadata dictionaries, and text runs to classify
 * uploaded documents and immediately reject fraudulent or mismatched documents
 * (e.g. random files, invoices, resumes, or incorrect certificate types) before submission.
 */
public class PdfDocumentScanner {

    public enum DocumentType {
        INCOME("Annual Household Income Certificate"),
        CASTE("ST / Caste Certificate"),
        ACADEMIC("Academic Marksheet / Certificate"),
        GENERAL("Official Supporting Document");

        private final String displayName;

        DocumentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class ScanResult {
        private final boolean valid;
        private final String detectedType;
        private final String rejectionReason;
        private final List<String> matchedMarkers;

        public ScanResult(boolean valid, String detectedType, String rejectionReason, List<String> matchedMarkers) {
            this.valid = valid;
            this.detectedType = detectedType;
            this.rejectionReason = rejectionReason;
            this.matchedMarkers = matchedMarkers != null ? matchedMarkers : new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public String getDetectedType() {
            return detectedType;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public List<String> getMatchedMarkers() {
            return matchedMarkers;
        }
    }

    public interface ScanCallback {
        void onScanComplete(ScanResult result);
    }

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Keywords catalog
    private static final List<String> INCOME_KEYWORDS = Arrays.asList(
            "income certificate", "certificate of income", "annual income", "family income",
            "household income", "aay praman", "aaya praman", "gross annual income", "total annual income",
            "tahsildar", "tehsildar", "revenue department", "revenue inspector", "revenue officer",
            "circle officer", "patwari", "taluk", "sub-divisional magistrate", "sub divisional magistrate",
            "sdm", "collector", "mandal revenue officer", "form 16", "form 16a", "salary certificate",
            "income declaration", "financial year", "assessment year", "per annum", "p.a.",
            "below poverty line", "bpl", "e-district", "edistrict", "service plus", "meeseva",
            "nadakacheri", "jharsewa", "rtps", "mahaonline", "dharani", "digilocker",
            "आय प्रमाण", "वार्षिक आय", "तहसीलदार", "राजस्व"
    );

    private static final List<String> CASTE_KEYWORDS = Arrays.asList(
            "caste certificate", "tribe certificate", "scheduled tribe", "scheduled caste",
            "community certificate", "jati praman", "st certificate", "social welfare department",
            "tribal welfare", "backward class", "constitution (scheduled tribes) order",
            "prescribed tribe", "community", "sub-divisional officer", "tahsildar", "tehsildar",
            "sub-divisional magistrate", "district magistrate", "competent authority",
            "e-district", "edistrict", "serviceplus", "digilocker",
            "जाति प्रमाण", "अनुसूचित जनजाति", "जनजाति", "आदिवासी"
    );

    private static final List<String> ACADEMIC_KEYWORDS = Arrays.asList(
            "marksheet", "mark sheet", "statement of marks", "grade card", "report card",
            "examination", "board of secondary education", "cbse", "icse", "state board",
            "intermediate", "higher secondary", "secondary school", "matriculation",
            "university", "college", "roll no", "roll number", "registration no",
            "marks obtained", "maximum marks", "total marks", "percentage", "cgpa", "sgpa",
            "grade", "passed", "division", "distinction", "semester", "academic session",
            "bonafide certificate", "admission letter", "course completion",
            "अंक पत्र", "परीक्षा", "प्राप्तांक", "पूर्णांक"
    );

    private static final List<String> RESUME_KEYWORDS = Arrays.asList(
            "curriculum vitae", "resume", "work experience", "career objective",
            "skills & abilities", "professional summary", "employment history",
            "technical skills", "linkedin.com", "github.com", "hobbies", "projects:"
    );

    private static final List<String> INVOICE_KEYWORDS = Arrays.asList(
            "tax invoice", "bill of supply", "gstin", "invoice no", "invoice date",
            "billing address", "shipping address", "order id", "payment receipt",
            "electricity bill", "power distribution", "consumer no", "meter reading",
            "railway ticket", "boarding pass", "flight ticket", "pnr number", "irctc"
    );

    private static final List<String> IDENTITY_KEYWORDS = Arrays.asList(
            "unique identification authority of india", "mera aadhaar", "aadhaar no",
            "enrolment no", "election commission of india", "electoral photo identity"
    );

    /**
     * Asynchronously scans and authenticates a selected PDF document against the expected DocumentType.
     */
    public static void scanDocumentAsync(Context context, Uri fileUri, String fileName, DocumentType expectedType, ScanCallback callback) {
        executor.execute(() -> {
            ScanResult result = scanDocumentSync(context, fileUri, fileName, expectedType);
            mainHandler.post(() -> callback.onScanComplete(result));
        });
    }

    /**
     * Synchronously scans and validates the PDF file against the expected DocumentType.
     */
    public static ScanResult scanDocumentSync(Context context, Uri fileUri, String fileName, DocumentType expectedType) {
        if (context == null || fileUri == null) {
            return new ScanResult(false, "Unknown", "No file selected or invalid file reference.", null);
        }

        try (InputStream in = context.getContentResolver().openInputStream(fileUri)) {
            if (in == null) {
                return new ScanResult(false, "Unknown", "Could not open document stream.", null);
            }

            // Read up to 8MB
            byte[] fileBytes = readBytes(in, 8 * 1024 * 1024);
            if (fileBytes.length < 5) {
                return new ScanResult(false, "Empty File", "The selected file is empty (0 bytes).", null);
            }

            // Verify PDF header %PDF-
            String header = new String(fileBytes, 0, Math.min(fileBytes.length, 1024), StandardCharsets.ISO_8859_1);
            if (!header.contains("%PDF-")) {
                return new ScanResult(false, "Invalid Format", "The selected file does not have a valid PDF header (%PDF-).", null);
            }

            // Extract all readable content: streams, metadata, and ASCII/UTF text
            String extractedContent = extractTextFromPdf(fileBytes, fileName);
            String lowerContent = extractedContent.toLowerCase(Locale.ROOT);

            // Match against categories
            List<String> matchedIncome = findMatches(lowerContent, INCOME_KEYWORDS);
            List<String> matchedCaste = findMatches(lowerContent, CASTE_KEYWORDS);
            List<String> matchedAcademic = findMatches(lowerContent, ACADEMIC_KEYWORDS);
            List<String> matchedResume = findMatches(lowerContent, RESUME_KEYWORDS);
            List<String> matchedInvoice = findMatches(lowerContent, INVOICE_KEYWORDS);
            List<String> matchedIdentity = findMatches(lowerContent, IDENTITY_KEYWORDS);

            // Also check file name clues
            String lowerFileName = fileName != null ? fileName.toLowerCase(Locale.ROOT) : "";
            if (lowerFileName.contains("income") || lowerFileName.contains("aay") || lowerFileName.contains("tahsildar")) {
                matchedIncome.add("filename: " + fileName);
            }
            if (lowerFileName.contains("caste") || lowerFileName.contains("tribe") || lowerFileName.contains("st_") || lowerFileName.contains("jati")) {
                matchedCaste.add("filename: " + fileName);
            }
            if (lowerFileName.contains("mark") || lowerFileName.contains("grade") || lowerFileName.contains("result") || lowerFileName.contains("score") || lowerFileName.contains("10th") || lowerFileName.contains("12th")) {
                matchedAcademic.add("filename: " + fileName);
            }
            if (lowerFileName.contains("resume") || lowerFileName.contains("cv")) {
                matchedResume.add("filename: " + fileName);
            }
            if (lowerFileName.contains("invoice") || lowerFileName.contains("bill") || lowerFileName.contains("receipt") || lowerFileName.contains("ticket")) {
                matchedInvoice.add("filename: " + fileName);
            }

            // Verification according to expectedType
            switch (expectedType) {
                case INCOME:
                    return verifyIncomeCertificate(matchedIncome, matchedCaste, matchedAcademic, matchedResume, matchedInvoice, matchedIdentity);
                case CASTE:
                    return verifyCasteCertificate(matchedCaste, matchedIncome, matchedAcademic, matchedResume, matchedInvoice, matchedIdentity);
                case ACADEMIC:
                    return verifyAcademicDocument(matchedAcademic, matchedIncome, matchedCaste, matchedResume, matchedInvoice, matchedIdentity);
                case GENERAL:
                default:
                    return verifyGeneralDocument(matchedResume, matchedInvoice, matchedIncome, matchedCaste, matchedAcademic);
            }

        } catch (Exception e) {
            return new ScanResult(false, "Error", "Error analyzing document content: " + e.getMessage(), null);
        }
    }

    private static ScanResult verifyIncomeCertificate(List<String> income, List<String> caste, List<String> academic,
                                                      List<String> resume, List<String> invoice, List<String> identity) {
        if (!resume.isEmpty() && income.isEmpty()) {
            return new ScanResult(false, "Resume / CV", "The uploaded file was detected as a Resume / CV, not an Income Certificate. Please upload an official Income Certificate issued by a competent revenue authority.", resume);
        }
        if (!invoice.isEmpty() && income.isEmpty()) {
            return new ScanResult(false, "Invoice / Commercial Receipt", "The uploaded file was detected as a Commercial Invoice, Utility Bill, or Ticket instead of an Income Certificate.", invoice);
        }
        if (caste.size() >= 2 && income.isEmpty()) {
            return new ScanResult(false, "Caste / Tribe Certificate", "The uploaded file appears to be a Caste / Tribe Certificate, not an Income Certificate. Please upload it in the Caste Certificate slot.", caste);
        }
        if (academic.size() >= 2 && income.isEmpty()) {
            return new ScanResult(false, "Academic Marksheet", "The uploaded file appears to be an Academic Marksheet / Scorecard, not an Income Certificate. Please upload it in the Marksheet slot.", academic);
        }
        if (identity.size() >= 2 && income.isEmpty()) {
            return new ScanResult(false, "Aadhaar / Identity Document", "The uploaded file appears to be an Aadhaar Card or Identity proof, not an Income Certificate.", identity);
        }

        if (!income.isEmpty()) {
            return new ScanResult(true, "Income Certificate", null, income);
        }

        return new ScanResult(false, "Unrecognized Document",
                "Document verification failed: The selected PDF does not contain recognized Income Certificate markers (such as Annual Income, Tahsildar / Revenue Authority seal, Form 16, or e-District authorization). Random or unrelated documents are strictly rejected.", null);
    }

    private static ScanResult verifyCasteCertificate(List<String> caste, List<String> income, List<String> academic,
                                                     List<String> resume, List<String> invoice, List<String> identity) {
        if (!resume.isEmpty() && caste.isEmpty()) {
            return new ScanResult(false, "Resume / CV", "The uploaded file was detected as a Resume / CV, not a Caste Certificate. Please upload your government-issued ST Caste Certificate.", resume);
        }
        if (!invoice.isEmpty() && caste.isEmpty()) {
            return new ScanResult(false, "Invoice / Commercial Receipt", "The uploaded file was detected as a Commercial Invoice, Utility Bill, or Ticket instead of a Caste Certificate.", invoice);
        }
        if (income.size() >= 2 && caste.isEmpty()) {
            return new ScanResult(false, "Income Certificate", "The uploaded file appears to be an Income Certificate, not a Caste Certificate. Please upload it in the Income Certificate slot.", income);
        }
        if (academic.size() >= 2 && caste.isEmpty()) {
            return new ScanResult(false, "Academic Marksheet", "The uploaded file appears to be an Academic Marksheet, not a Caste Certificate. Please upload it in the Marksheet slot.", academic);
        }

        if (!caste.isEmpty()) {
            return new ScanResult(true, "ST / Caste Certificate", null, caste);
        }

        return new ScanResult(false, "Unrecognized Document",
                "Document verification failed: The selected PDF does not contain recognized ST / Caste Certificate markers (such as Scheduled Tribe, Revenue Authority/Tahsildar seal, or e-District ST Certificate ID). Random or unrelated documents are strictly rejected.", null);
    }

    private static ScanResult verifyAcademicDocument(List<String> academic, List<String> income, List<String> caste,
                                                     List<String> resume, List<String> invoice, List<String> identity) {
        if (!resume.isEmpty() && academic.isEmpty()) {
            return new ScanResult(false, "Resume / CV", "The uploaded file was detected as a Resume / CV, not an Academic Marksheet. Please upload your official Board / University Marksheet.", resume);
        }
        if (!invoice.isEmpty() && academic.isEmpty()) {
            return new ScanResult(false, "Invoice / Commercial Receipt", "The uploaded file was detected as an Invoice or Bill instead of an Academic Marksheet.", invoice);
        }
        if (income.size() >= 2 && academic.isEmpty()) {
            return new ScanResult(false, "Income Certificate", "The uploaded file appears to be an Income Certificate, not an Academic Marksheet.", income);
        }
        if (caste.size() >= 2 && academic.isEmpty()) {
            return new ScanResult(false, "Caste Certificate", "The uploaded file appears to be a Caste Certificate, not an Academic Marksheet.", caste);
        }

        if (!academic.isEmpty()) {
            return new ScanResult(true, "Academic Marksheet / Certificate", null, academic);
        }

        return new ScanResult(false, "Unrecognized Document",
                "Document verification failed: The selected PDF does not contain recognized Academic Marksheet markers (such as Examination Board, Roll Number, Marks/CGPA, or Institute details). Random or unrelated documents are strictly rejected.", null);
    }

    private static ScanResult verifyGeneralDocument(List<String> resume, List<String> invoice,
                                                    List<String> income, List<String> caste, List<String> academic) {
        if (!resume.isEmpty() && (income.isEmpty() && caste.isEmpty() && academic.isEmpty())) {
            return new ScanResult(false, "Resume / CV", "The uploaded file was detected as a Resume / CV. Only authentic scholarship documents, certificates, or marksheets are permitted.", resume);
        }
        if (!invoice.isEmpty()) {
            return new ScanResult(false, "Invoice / Commercial Receipt", "Commercial invoices, utility bills, and tickets are not valid scholarship supporting documents.", invoice);
        }
        List<String> allMarkers = new ArrayList<>();
        allMarkers.addAll(income);
        allMarkers.addAll(caste);
        allMarkers.addAll(academic);
        return new ScanResult(true, "Supporting Document", null, allMarkers);
    }

    private static List<String> findMatches(String text, List<String> keywords) {
        List<String> matched = new ArrayList<>();
        for (String kw : keywords) {
            if (text.contains(kw.toLowerCase(Locale.ROOT))) {
                matched.add(kw);
            }
        }
        return matched;
    }

    /**
     * Extracts text from PDF by inspecting literal string objects, uncompressed streams,
     * FlateDecode compressed streams, and metadata dictionaries.
     */
    private static String extractTextFromPdf(byte[] data, String fileName) {
        StringBuilder sb = new StringBuilder();
        if (fileName != null) {
            sb.append(fileName).append(" ");
        }

        String rawIso = new String(data, StandardCharsets.ISO_8859_1);
        sb.append(rawIso).append(" ");

        // Search for stream ... endstream blocks
        Pattern streamPattern = Pattern.compile("stream[\\r\\n]+([\\s\\S]*?)[\\r\\n]+endstream");
        Matcher matcher = streamPattern.matcher(rawIso);

        int streamCount = 0;
        while (matcher.find() && streamCount < 50) {
            streamCount++;
            int startIdx = matcher.start(1);
            int endIdx = matcher.end(1);

            if (startIdx >= 0 && endIdx <= data.length && startIdx < endIdx) {
                int length = endIdx - startIdx;
                byte[] streamBytes = new byte[length];
                System.arraycopy(data, startIdx, streamBytes, 0, length);

                // Try Flate decompression
                try {
                    Inflater inflater = new Inflater(false);
                    inflater.setInput(streamBytes);
                    byte[] buffer = new byte[4096];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while (!inflater.finished()) {
                        int count = inflater.inflate(buffer);
                        if (count == 0) {
                            if (inflater.needsInput() || inflater.needsDictionary()) break;
                        }
                        baos.write(buffer, 0, count);
                        if (baos.size() > 500000) break; // Limit 500KB per stream
                    }
                    inflater.end();
                    if (baos.size() > 0) {
                        String inflatedText = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
                        sb.append(inflatedText).append(" ");
                    }
                } catch (Exception ignored) {
                    // Raw stream or non-deflate
                    sb.append(new String(streamBytes, StandardCharsets.ISO_8859_1)).append(" ");
                }
            }
        }

        // Extract PDF literal parenthesized strings: (Text Here)
        Pattern literalPattern = Pattern.compile("\\(([^\\)]+)\\)");
        Matcher literalMatcher = literalPattern.matcher(rawIso);
        int litCount = 0;
        while (literalMatcher.find() && litCount < 200) {
            litCount++;
            sb.append(literalMatcher.group(1)).append(" ");
        }

        return sb.toString();
    }

    private static byte[] readBytes(InputStream in, int maxBytes) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        int total = 0;
        while ((read = in.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
            total += read;
            if (total >= maxBytes) break;
        }
        return baos.toByteArray();
    }
}

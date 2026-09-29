package gov.mota.scholarship.data.util;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Manages secure internal file storage for beneficiary uploaded documents.
 * All files are isolated per beneficiary OTR ID in the app's private files directory:
 * context.getFilesDir()/documents/{otrId}/{prefix_timestamp.ext}
 */
public class DocumentStorageManager {

    private static final String BASE_DOCS_DIR = "documents";

    /**
     * Copies a file from a picked URI into the app's private internal storage.
     *
     * @param context Application or Activity context
     * @param otrId The unique Beneficiary OTR ID for tenant isolation
     * @param sourceUri The picked file content URI
     * @param prefix Document type prefix (e.g., "caste", "income", "marksheet")
     * @return Absolute path to the saved private file, or null on failure
     */
    public static String saveFileLocally(Context context, String otrId, Uri sourceUri, String prefix) {
        if (context == null || otrId == null || sourceUri == null) {
            return null;
        }

        try {
            // Determine file extension from MIME type
            String mimeType = context.getContentResolver().getType(sourceUri);
            String extension = "pdf"; // default fallback
            if (mimeType != null) {
                String extFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extFromMime != null && !extFromMime.isEmpty()) {
                    extension = extFromMime;
                }
            }

            // Create private directory: filesDir/documents/{otrId}/
            File baseDir = new File(context.getFilesDir(), BASE_DOCS_DIR);
            File userDir = new File(baseDir, sanitizeOtr(otrId));
            if (!userDir.exists()) {
                userDir.mkdirs();
            }

            // Target file name: {prefix}_{timestamp}.{ext}
            String fileName = prefix + "_" + System.currentTimeMillis() + "." + extension;
            File targetFile = new File(userDir, fileName);

            // Copy bytes from ContentResolver InputStream to FileOutputStream
            try (InputStream in = context.getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(targetFile)) {
                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Determines the MIME type of a URI or file.
     */
    public static String getMimeType(Context context, Uri uri) {
        if (context == null || uri == null) return "application/pdf";
        try {
            String type = context.getContentResolver().getType(uri);
            if (type != null && !type.isEmpty()) return type;
        } catch (Exception ignored) {}
        return "application/pdf";
    }

    /**
     * Checks if a document has a valid, existing local file.
     */
    public static boolean fileExists(String localFilePath) {
        if (localFilePath == null || localFilePath.trim().isEmpty()) {
            return false;
        }
        File file = new File(localFilePath);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    private static String sanitizeOtr(String otrId) {
        return otrId.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}

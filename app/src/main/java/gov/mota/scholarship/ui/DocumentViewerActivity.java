package gov.mota.scholarship.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.model.WalletDocument;

import java.io.File;

/**
 * In-app Document Viewer Activity.
 * Displays uploaded files (PDFs, Images) directly inside the app.
 * Utilizes Android's native PdfRenderer for PDF files and BitmapFactory for images,
 * with secure FileProvider fallback to system viewers.
 */
public class DocumentViewerActivity extends BaseActivity {

    public static final String EXTRA_DOCUMENT = "extra_document";

    private MaterialToolbar toolbarDocViewer;
    private TextView tvDocViewerTitle;
    private TextView tvDocViewerMeta;
    private TextView tvDocViewerStoragePath;
    private ScrollView svImageContainer;
    private ImageView ivDocPreview;
    private ScrollView svPdfContainer;
    private ImageView ivPdfPagePreview;
    private TextView tvPdfPageCount;
    private LinearLayout llFallbackView;
    private TextView tvFallbackNotice;
    private MaterialButton btnOpenExternal;

    private WalletDocument document;
    private ParcelFileDescriptor pdfParcelFileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        document = (WalletDocument) getIntent().getSerializableExtra(EXTRA_DOCUMENT);
        if (document == null) {
            Toast.makeText(this, "Document data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        displayDocument();
    }

    private void initViews() {
        toolbarDocViewer = findViewById(R.id.toolbarDocViewer);
        tvDocViewerTitle = findViewById(R.id.tvDocViewerTitle);
        tvDocViewerMeta = findViewById(R.id.tvDocViewerMeta);
        tvDocViewerStoragePath = findViewById(R.id.tvDocViewerStoragePath);
        svImageContainer = findViewById(R.id.svImageContainer);
        ivDocPreview = findViewById(R.id.ivDocPreview);
        svPdfContainer = findViewById(R.id.svPdfContainer);
        ivPdfPagePreview = findViewById(R.id.ivPdfPagePreview);
        tvPdfPageCount = findViewById(R.id.tvPdfPageCount);
        llFallbackView = findViewById(R.id.llFallbackView);
        tvFallbackNotice = findViewById(R.id.tvFallbackNotice);
        btnOpenExternal = findViewById(R.id.btnOpenExternal);

        toolbarDocViewer.setTitle(document.getTitle());
        toolbarDocViewer.setNavigationOnClickListener(v -> finish());

        tvDocViewerTitle.setText(document.getTitle());
        tvDocViewerMeta.setText(document.getDocNumber() + " • " + document.getAuthority() + " (" + document.getIssueDate() + ")");

        btnOpenExternal.setOnClickListener(v -> openFileExternally());
    }

    private void displayDocument() {
        String localPath = document.getLocalFilePath();
        if (localPath == null || localPath.trim().isEmpty()) {
            showFallback("Digital Certificate Record", "No local file binary is attached. Document is verified via DigiLocker / Central Registry.");
            tvDocViewerStoragePath.setText("Storage: Verified DigiLocker Record");
            btnOpenExternal.setVisibility(View.GONE);
            return;
        }

        File file = new File(localPath);
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            showFallback("File Not Found", "The document file was not found in the local vault path: " + localPath);
            tvDocViewerStoragePath.setText("Path: " + localPath);
            btnOpenExternal.setVisibility(View.GONE);
            return;
        }

        tvDocViewerStoragePath.setText("Local Vault: " + file.getName() + " (" + (file.length() / 1024) + " KB)");

        String mimeType = document.getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = localPath.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/jpeg";
        }

        if (mimeType.startsWith("image/")) {
            renderImage(file);
        } else if (mimeType.contains("pdf")) {
            renderPdf(file);
        } else {
            showFallback("File Attached: " + file.getName(), "Format: " + mimeType);
        }
    }

    private void renderImage(File file) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                ivDocPreview.setImageBitmap(bitmap);
                svImageContainer.setVisibility(View.VISIBLE);
                svPdfContainer.setVisibility(View.GONE);
                llFallbackView.setVisibility(View.GONE);
            } else {
                showFallback("Image preview unavailable", "Tap below to open with system viewer.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showFallback("Unable to render image", e.getMessage());
        }
    }

    private void renderPdf(File file) {
        try {
            pdfParcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            if (pdfParcelFileDescriptor != null) {
                pdfRenderer = new PdfRenderer(pdfParcelFileDescriptor);
                int pageCount = pdfRenderer.getPageCount();
                if (pageCount > 0) {
                    currentPage = pdfRenderer.openPage(0);
                    // Render page into bitmap with screen-appropriate density
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = (int) (((float) currentPage.getHeight() / currentPage.getWidth()) * width);
                    if (height <= 0) height = 1200;

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    ivPdfPagePreview.setImageBitmap(bitmap);

                    tvPdfPageCount.setText("Viewing Page 1 of " + pageCount + " (Rendered in App)");
                    svPdfContainer.setVisibility(View.VISIBLE);
                    svImageContainer.setVisibility(View.GONE);
                    llFallbackView.setVisibility(View.GONE);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If in-app PdfRenderer fails (e.g. password protected or complex vector PDF)
        showFallback("PDF Document Stored", "Tap below to open or view this PDF with your device's PDF reader.");
    }

    private void showFallback(String title, String subtitle) {
        svImageContainer.setVisibility(View.GONE);
        svPdfContainer.setVisibility(View.GONE);
        llFallbackView.setVisibility(View.VISIBLE);
        tvFallbackNotice.setText(title);
    }

    private void openFileExternally() {
        String localPath = document.getLocalFilePath();
        if (localPath == null || localPath.isEmpty()) return;

        File file = new File(localPath);
        if (!file.exists()) {
            Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            String mime = document.getMimeType();
            if (mime == null || mime.isEmpty()) {
                mime = localPath.toLowerCase().endsWith(".pdf") ? "application/pdf" : "image/*";
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(contentUri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open with"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot open file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (currentPage != null) {
                currentPage.close();
            }
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
            if (pdfParcelFileDescriptor != null) {
                pdfParcelFileDescriptor.close();
            }
        } catch (Exception ignored) {}
    }
}

package gov.mota.scholarship.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.model.WalletDocument;

import java.util.List;

public class WalletDocumentAdapter extends RecyclerView.Adapter<WalletDocumentAdapter.ViewHolder> {

    private final Context context;
    private final List<WalletDocument> documents;

    public WalletDocumentAdapter(Context context, List<WalletDocument> documents) {
        this.context = context;
        this.documents = documents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallet_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WalletDocument doc = documents.get(position);

        holder.tvDocTitle.setText(doc.getTitle());
        holder.tvDocStatusBadge.setText(doc.getStatus());

        // Status chip colors per Design.md Section 6
        String status = doc.getStatus() != null ? doc.getStatus().toUpperCase() : "";
        if (status.contains("VERIFIED") || status.contains("DISBURSED")) {
            holder.tvDocStatusBadge.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            holder.tvDocStatusBadge.setBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
        } else if (status.contains("DEFICIEN") || status.contains("REJECT")) {
            holder.tvDocStatusBadge.setTextColor(android.graphics.Color.parseColor("#C62828"));
            holder.tvDocStatusBadge.setBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
        } else {
            // Under Review / Pending
            holder.tvDocStatusBadge.setTextColor(android.graphics.Color.parseColor("#EF6C00"));
            holder.tvDocStatusBadge.setBackgroundColor(android.graphics.Color.parseColor("#FFF3E0"));
        }

        holder.tvDocAuthority.setText(context.getString(R.string.authority_prefix, doc.getAuthority(), doc.getIssueDate()));
        holder.tvDocNumber.setText(context.getString(R.string.cert_id_prefix, doc.getDocNumber()));

        if (doc.isReused()) {
            holder.tvReusedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvReusedBadge.setVisibility(View.GONE);
        }

        // Show whether the actual file is stored securely inside the app's private files
        if (doc.getLocalFilePath() != null && !doc.getLocalFilePath().isEmpty()) {
            holder.tvInAppStorageBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvInAppStorageBadge.setVisibility(View.GONE);
        }

        View.OnClickListener openViewer = v -> {
            android.content.Intent intent = new android.content.Intent(context, DocumentViewerActivity.class);
            intent.putExtra(DocumentViewerActivity.EXTRA_DOCUMENT, doc);
            context.startActivity(intent);
        };

        holder.btnViewDoc.setOnClickListener(openViewer);
        holder.itemView.setOnClickListener(openViewer);
    }

    @Override
    public int getItemCount() {
        return documents != null ? documents.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDocTitle;
        TextView tvDocStatusBadge;
        TextView tvDocAuthority;
        TextView tvDocNumber;
        TextView tvReusedBadge;
        TextView tvInAppStorageBadge;
        com.google.android.material.button.MaterialButton btnViewDoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDocTitle = itemView.findViewById(R.id.tvDocTitle);
            tvDocStatusBadge = itemView.findViewById(R.id.tvDocStatusBadge);
            tvDocAuthority = itemView.findViewById(R.id.tvDocAuthority);
            tvDocNumber = itemView.findViewById(R.id.tvDocNumber);
            tvReusedBadge = itemView.findViewById(R.id.tvReusedBadge);
            tvInAppStorageBadge = itemView.findViewById(R.id.tvInAppStorageBadge);
            btnViewDoc = itemView.findViewById(R.id.btnViewDoc);
        }
    }
}

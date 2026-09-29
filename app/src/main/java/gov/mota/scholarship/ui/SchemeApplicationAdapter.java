package gov.mota.scholarship.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import gov.mota.scholarship.R;
import gov.mota.scholarship.data.model.SchemeApplication;

import java.util.Arrays;
import java.util.List;

public class SchemeApplicationAdapter extends RecyclerView.Adapter<SchemeApplicationAdapter.ViewHolder> {

    private final Context context;
    private final List<SchemeApplication> applications;

    // PRD Section 5.1.2: 6 standard lifecycle stages
    private static final List<String> LIFECYCLE_STAGE_KEYS = Arrays.asList(
            "stage_submission",
            "stage_inst_verification",
            "stage_dist_approval",
            "stage_mota_sanction",
            "stage_pfms_dbt",
            "stage_bank_credit"
    );

    private static final List<String> LIFECYCLE_STAGES_ENGLISH = Arrays.asList(
            "Submission",
            "Institute Verification",
            "District/State Approval",
            "MoTA Sanction",
            "PFMS/SFMP DBT Generation",
            "Bank Account Credit"
    );

    public SchemeApplicationAdapter(Context context, List<SchemeApplication> applications) {
        this.context = context;
        this.applications = applications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scheme_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SchemeApplication app = applications.get(position);

        holder.tvSchemeTitle.setText(app.getSchemeTitle());
        holder.tvStatusBadge.setText(app.getStatus());

        if ("APPROVED".equalsIgnoreCase(app.getStatus()) || "DISBURSED".equalsIgnoreCase(app.getStatus()) || "VERIFIED".equalsIgnoreCase(app.getStatus())) {
            holder.tvStatusBadge.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else if ("DEFICIENT".equalsIgnoreCase(app.getStatus()) || "DEFICIENCY".equalsIgnoreCase(app.getStatus()) || "REJECTED".equalsIgnoreCase(app.getStatus())) {
            holder.tvStatusBadge.setTextColor(Color.parseColor("#C62828"));
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFEBEE"));
        } else {
            // Under Review / Pending / In Progress
            holder.tvStatusBadge.setTextColor(Color.parseColor("#EF6C00"));
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFF3E0"));
        }

        String meta = context.getString(R.string.app_meta_format, app.getApplicationId(), app.getAcademicYear(), app.getSourceSystem());
        holder.tvAppMeta.setText(meta);
        holder.tvStage.setText(getLocalizedStageName(app.getStage()));
        holder.tvAmount.setText(app.getAmountInr());
        holder.tvPfmsTxn.setText(context.getString(R.string.ref_id_label, app.getPfmsTransactionId(), app.getDbtStatus()));
        holder.tvRemarks.setText(app.getRemarks());

        // Setup 6-stage lifecycle progress
        buildTimeline(holder.llTimelineSteps, app.getStage(), app.getStatus());

        // Badges
        holder.cgBadges.removeAllViews();
        if (app.getVerificationBadges() != null) {
            for (String badge : app.getVerificationBadges()) {
                Chip chip = new Chip(context);
                chip.setText(badge);
                chip.setTextSize(10f);
                chip.setChipBackgroundColorResource(R.color.mota_surface_variant);
                chip.setTextColor(context.getResources().getColor(R.color.mota_on_surface_variant));
                chip.setCheckable(false);
                holder.cgBadges.addView(chip);
            }
        }
    }

    private String getLocalizedStageName(String rawStage) {
        if (rawStage == null) return "";
        for (int i = 0; i < LIFECYCLE_STAGES_ENGLISH.size(); i++) {
            if (LIFECYCLE_STAGES_ENGLISH.get(i).equalsIgnoreCase(rawStage) ||
                    rawStage.contains(LIFECYCLE_STAGES_ENGLISH.get(i)) ||
                    LIFECYCLE_STAGES_ENGLISH.get(i).contains(rawStage)) {
                return getStageStringByIndex(i);
            }
        }
        return rawStage;
    }

    private String getStageStringByIndex(int index) {
        switch (index) {
            case 0: return context.getString(R.string.stage_submission);
            case 1: return context.getString(R.string.stage_inst_verification);
            case 2: return context.getString(R.string.stage_dist_approval);
            case 3: return context.getString(R.string.stage_mota_sanction);
            case 4: return context.getString(R.string.stage_pfms_dbt);
            case 5: return context.getString(R.string.stage_bank_credit);
            default: return LIFECYCLE_STAGES_ENGLISH.get(index);
        }
    }

    private void buildTimeline(LinearLayout container, String currentStage, String status) {
        container.removeAllViews();
        int currentStageIndex = 0;
        for (int i = 0; i < LIFECYCLE_STAGES_ENGLISH.size(); i++) {
            if (LIFECYCLE_STAGES_ENGLISH.get(i).equalsIgnoreCase(currentStage) || 
                currentStage.contains(LIFECYCLE_STAGES_ENGLISH.get(i)) || 
                LIFECYCLE_STAGES_ENGLISH.get(i).contains(currentStage)) {
                currentStageIndex = i;
                break;
            }
        }

        for (int i = 0; i < LIFECYCLE_STAGES_ENGLISH.size(); i++) {
            TextView stepView = new TextView(context);
            String localizedStage = getStageStringByIndex(i);
            String title = (i + 1) + ". " + localizedStage;
            stepView.setText(title);
            stepView.setTextSize(10f);
            stepView.setPadding(12, 6, 12, 6);

            if (i < currentStageIndex) {
                // Completed past stage
                stepView.setTextColor(Color.parseColor("#15803D"));
                stepView.setBackgroundColor(Color.parseColor("#DCFCE7"));
                stepView.setText("✓ " + title);
            } else if (i == currentStageIndex) {
                // Current active stage
                if ("DEFICIENT".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
                    stepView.setTextColor(Color.parseColor("#B91C1C"));
                    stepView.setBackgroundColor(Color.parseColor("#FEE2E2"));
                    stepView.setText("⚠ " + title);
                } else {
                    stepView.setTextColor(Color.parseColor("#1E3A8A"));
                    stepView.setBackgroundColor(Color.parseColor("#DBEAFE"));
                    stepView.setTypeface(null, Typeface.BOLD);
                    stepView.setText("▶ " + title);
                }
            } else {
                // Upcoming stage
                stepView.setTextColor(Color.parseColor("#64748B"));
                stepView.setBackgroundColor(Color.parseColor("#F1F5F9"));
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 8, 0);
            stepView.setLayoutParams(lp);

            container.addView(stepView);
        }
    }

    @Override
    public int getItemCount() {
        return applications != null ? applications.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSchemeTitle;
        TextView tvStatusBadge;
        TextView tvAppMeta;
        TextView tvStage;
        TextView tvAmount;
        TextView tvPfmsTxn;
        TextView tvRemarks;
        ChipGroup cgBadges;
        LinearLayout llTimelineSteps;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchemeTitle = itemView.findViewById(R.id.tvSchemeTitle);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvAppMeta = itemView.findViewById(R.id.tvAppMeta);
            tvStage = itemView.findViewById(R.id.tvStage);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvPfmsTxn = itemView.findViewById(R.id.tvPfmsTxn);
            tvRemarks = itemView.findViewById(R.id.tvRemarks);
            cgBadges = itemView.findViewById(R.id.cgBadges);
            llTimelineSteps = itemView.findViewById(R.id.llTimelineSteps);
        }
    }
}

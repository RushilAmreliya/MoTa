package gov.mota.scholarship.ui;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import gov.mota.scholarship.util.LocaleHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }
}

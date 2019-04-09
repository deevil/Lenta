package com.lenta.bp10writeoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lenta.shared.DialogBuilder;

public class AuxiliaryMenuActivity extends AppCompatActivity {

    boolean MainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auxiliary_menu);

        Intent intent = getIntent();
        MainActivity = intent.getBooleanExtra("MainActivity",false);
    }

    public void imgBtn_onClick(View v)
    {
        // определяем нажатую кнопку и выполняем соответствующую операцию
        switch (v.getId()) {
            case R.id.HomeBtnImg:
                this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
                break;
            case R.id.ExitBtnImg:
                DialogBuilder dialogCloseApp = new DialogBuilder(this);
                dialogCloseApp.setTypeDialog(dialogCloseApp.TD_CLOSE_APP).setTitleText(R.string.name_buisness_process).show();
                break;
            case R.id.SettingsBtnImg:
                Intent intSettings = new Intent(this, SettingsActivity.class);
                intSettings.putExtra("MainActivity",MainActivity);
                startActivity(intSettings);
                break;
            case R.id.SupportBtnImg:
                Intent intSupport = new Intent(this, SupportActivity.class);
                startActivity(intSupport);
                break;
            default:
                break;
        }
    }
}

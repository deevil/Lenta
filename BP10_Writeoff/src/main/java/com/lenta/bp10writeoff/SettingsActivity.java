package com.lenta.bp10writeoff;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lenta.bp10writeoff.databinding.ActivitySettingsBindingImpl;
import com.lenta.shared.DialogBuilder;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBindingImpl binding;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        Intent intent = getIntent();
        boolean MainActivity = intent.getBooleanExtra("MainActivity",false);
        if (MainActivity) {
            binding.PrinterBtnImg.setEnabled(true);
            binding.PrinterBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_select_printer", "mipmap", getPackageName())));
            binding.WorkBtnImg.setEnabled(false);
            binding.WorkBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_select_working_mode_disable", "mipmap", getPackageName())));
            binding.TechLoginBtnImg.setEnabled(false);
            binding.TechLoginBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_tech_login_disable", "mipmap", getPackageName())));
        } else {
            binding.PrinterBtnImg.setEnabled(false);
            binding.PrinterBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_select_printer_disable", "mipmap", getPackageName())));
            binding.WorkBtnImg.setEnabled(true);
            binding.WorkBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_select_working_mode", "mipmap", getPackageName())));
            binding.TechLoginBtnImg.setEnabled(true);
            binding.TechLoginBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_tech_login", "mipmap", getPackageName())));
        }
    }

    public void imgBtn_onClick(View v)
    {
        // определяем нажатую кнопку и выполняем соответствующую операцию
        switch (v.getId()) {
            case R.id.BackBtnImg:
                this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
                break;
            case R.id.ExitBtnImg:
                DialogBuilder dialogCloseApp = new DialogBuilder(this);
                dialogCloseApp.setTypeDialog(dialogCloseApp.TD_CLOSE_APP).setTitleText(R.string.name_buisness_process).show();
                break;
            case R.id.PrinterBtnImg:
                Intent intPrinterChange = new Intent(this, PrinterChangeActivity.class);
                startActivity(intPrinterChange);
                break;
            case R.id.WorkBtnImg:
                Intent intSelectWorkingMode = new Intent(this, SelectWorkingModeActivity.class);
                startActivity(intSelectWorkingMode);
                break;
            case R.id.TechLoginBtnImg:
                Intent intTechLogin = new Intent(this, TechLoginActivity.class);
                startActivity(intTechLogin);
                break;
            default:
                break;
        }
    }
}

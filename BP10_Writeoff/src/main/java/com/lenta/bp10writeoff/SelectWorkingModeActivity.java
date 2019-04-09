package com.lenta.bp10writeoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lenta.shared.DialogBuilder;

public class SelectWorkingModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_working_mode);
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
            case R.id.TestEnvirBtnImg:
                Intent intTestEnvir = new Intent(this, TestEnvironmentActivity.class);
                startActivity(intTestEnvir);
                break;
            case R.id.WorkEnvirBtnImg:
                break;
            default:
                break;
        }
    }
}

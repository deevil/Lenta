package com.lenta.bp10writeoff;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lenta.bp10writeoff.databinding.ActivityDataLoadingBindingImpl;
import com.lenta.bp10writeoff.db.DBHelper;
import com.lenta.shared.DialogBuilder;

public class DataLoadingActivity extends AppCompatActivity {

    ActivityDataLoadingBindingImpl binding;
    long startTime = 0;
    DBHelper dbHelper;
    boolean dbSynchronization = false;

    //timer
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            binding.SpeedTxtView.setText(getResources().getString(R.string.acvtDataLoad_SpeedTxtView)+" 240 КБ/С");
            binding.LoadTxtView.setText(getResources().getString(R.string.acvtDataLoad_LoadTxtView)+" 12,4 МБ");
            binding.TimeTxtView.setText(getResources().getString(R.string.acvtDataLoad_TimeTxtView)+String.format(" %d:%02d", minutes, seconds));
            if (seconds > 0) { //останавливаем таймер
            //if (dbSynchronization) {
                timerHandler.removeCallbacks(timerRunnable);
                transitionToNext();
            } else {
                timerHandler.postDelayed(this, 1000);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_data_loading);

        //запускаем тайиер
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        //код для удаления базы данных
        //this.deleteDatabase("DB_LENTA_BP10");

        //загружаем справочники в отдельном потоке
        //создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper( this);
        Thread threadDB = new Thread(new Runnable() {
            public void run() {
                //dbSynchronization = dbHelper.onSynchronization();
                dbHelper.close();
            }
        });
        threadDB.start();
    }

    public void ExitBtnImg_onClick(View v)
    {
        DialogBuilder dialogCloseApp = new DialogBuilder(this);
        dialogCloseApp.setTypeDialog(dialogCloseApp.TD_CLOSE_APP).setTitleText(R.string.name_buisness_process).show();
    }

    public void transitionToNext()
    {
        //переходим на активити "Определение исполнителя"
        Intent intDefinitionPerformer = new Intent(this, DefinitionPerformerActivity.class);
        startActivity(intDefinitionPerformer);
        //close activity
        this.finish();
    }
}

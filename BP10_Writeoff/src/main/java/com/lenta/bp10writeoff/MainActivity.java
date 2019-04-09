package com.lenta.bp10writeoff;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lenta.shared.DialogBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void imgBtn_onClick(View v)
    {
        // определяем нажатую кнопку и выполняем соответствующую операцию
        switch (v.getId()) {
            case R.id.ExitBtnImg:
                DialogBuilder dialogCloseApp = new DialogBuilder(this);
                dialogCloseApp.setTypeDialog(dialogCloseApp.TD_CLOSE_APP).setTitleText(R.string.name_buisness_process).show();
                break;
            case R.id.AuxiliaryMenuBtnImg:
                Intent intAuxiliaryMenu = new Intent(this, AuxiliaryMenuActivity.class);
                intAuxiliaryMenu.putExtra("MainActivity",true);
                startActivity(intAuxiliaryMenu);
                break;
            case R.id.CreateTaskBtnImg:
                Intent intTaskCard = new Intent(this, TaskCardActivity.class);
                startActivity(intTaskCard);
                break;
            default:
                break;
        }
    }
}

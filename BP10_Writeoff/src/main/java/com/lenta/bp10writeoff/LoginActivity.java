package com.lenta.bp10writeoff;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lenta.bp10writeoff.databinding.ActivityLoginBindingImpl;
import com.lenta.bp10writeoff.db.AndroidDatabaseManager;
import com.lenta.shared.Click_ThreeBtnBotPanel;
import com.lenta.shared.DialogBuilder;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBindingImpl binding;
    boolean lastOpenBotPanel =true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    InputMethodManager imm;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        binding.editTxtLogin.setText("");
        binding.editTxtLogin.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText
        binding.editTxtPass.setText("");
        binding.editTxtPass.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText

        //делаем кнопку показа нижней панели неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false);

        //для вызова и скрытия клавиатуры
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    //метод обрабатывающий нажатие на кнопки "Показать клавиатуру"(KeyOpenBtnImg), "Показать нижнюю панель"(ShowBotPanelBtnImg) и "Свернуть"(UpDownBtnImg)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ThreeBtnBotPanel_onClick(View btnClick)
    {
        lastOpenBotPanel = Click_ThreeBtnBotPanel.imgBtn_onClick( this,
                                                                  this,
                                                                   imm,
                                                                   btnClick,
                                                                   binding.KeyOpenBtnImg,
                                                                   binding.UpDownBtnImg,
                                                                   binding.ShowBotPanelBtnImg,
                                                                   binding.BottomPanel,
                                                                   lastOpenBotPanel,
                                                                  true
                                                                 );
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
                 intAuxiliaryMenu.putExtra("MainActivity",false);
                 startActivity(intAuxiliaryMenu);
                break;
            case R.id.BtnEnterBtnImg:
                //создаем и запускаем Activity - SelectTKActivity
                binding.editTxtLogin.setText("");
                binding.editTxtLogin.requestFocus();
                binding.editTxtPass.setText("");
                Intent intSelectTK = new Intent(this, SelectTKActivity.class);
                startActivity(intSelectTK);
                break;
            default:
                break;
        }
    }

    public void SQLiteMngBtn_onClick(View v)
    {
        Intent dbmanager = new Intent(this, AndroidDatabaseManager.class);
        startActivity(dbmanager);
    }
}

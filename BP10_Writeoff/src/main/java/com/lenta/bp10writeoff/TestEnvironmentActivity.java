package com.lenta.bp10writeoff;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lenta.bp10writeoff.databinding.ActivityTestEnvironmentBindingImpl;
import com.lenta.shared.Click_ThreeBtnBotPanel;

public class TestEnvironmentActivity extends AppCompatActivity {

    ActivityTestEnvironmentBindingImpl binding;
    boolean lastOpenBotPanel =true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    InputMethodManager imm;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_test_environment);

        binding.editTxtPin1.setText("");
        binding.editTxtPin1.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText
        binding.editTxtPin2.setText("");
        binding.editTxtPin2.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText
        binding.editTxtPin3.setText("");
        binding.editTxtPin3.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText
        binding.editTxtPin4.setText("");
        binding.editTxtPin4.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText

        //делаем кнопку показа нижней панели неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false);

        //для вызова и скрытия клавиатуры
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    //метод обрабатывающий нажатие на кнопки "Показать клавиатуру"(KeyOpenBtnImg), "Показать нижнюю панель"(ShowBotPanelBtnImg) и "Свернуть"(UpDownBtnImg)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ThreeBtnBotPanel_onClick(View btnClick)
    {
        lastOpenBotPanel = Click_ThreeBtnBotPanel.imgBtn_onClick(this,
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
            case R.id.GoBtnImg:
                break;
            case R.id.BackBtnImg:
                binding.editTxtPin1.setText("");
                binding.editTxtPin2.setText("");
                binding.editTxtPin3.setText("");
                binding.editTxtPin4.setText("");
                this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
                break;
            default:
                break;
        }
    }
}

package com.lenta.bp10writeoff;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lenta.bp10writeoff.databinding.ActivitySupportBindingImpl;
import com.lenta.shared.Click_ThreeBtnBotPanel;

public class SupportActivity extends AppCompatActivity {

    ActivitySupportBindingImpl binding;
    boolean lastOpenBotPanel = true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_support);

        binding.KeyOpenBtnImg.setEnabled(false); //кнопку открытия клавиатуры делаем неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false); //делаем кнопку показа нижней панели неактивной
    }

    //метод обрабатывающий нажатие на кнопки "Показать клавиатуру"(KeyOpenBtnImg), "Показать нижнюю панель"(ShowBotPanelBtnImg) и "Свернуть"(UpDownBtnImg)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void ThreeBtnBotPanel_onClick(View btnClick)
    {
        lastOpenBotPanel = Click_ThreeBtnBotPanel.imgBtn_onClick(this,
                this,
                null,
                btnClick,
                binding.KeyOpenBtnImg,
                binding.UpDownBtnImg,
                binding.ShowBotPanelBtnImg,
                binding.BottomPanel,
                lastOpenBotPanel,
                false
        );
    }

    public void BackBtnImg_onClick(View v)
    {
        this.finish(); //close this activity
        onBackPressed();// возврат на предыдущий activity
    }
}

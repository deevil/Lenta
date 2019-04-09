package com.lenta.bp10writeoff;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.lenta.bp10writeoff.databinding.ActivitySelectTkBindingImpl;
import com.lenta.shared.Click_ThreeBtnBotPanel;
import com.lenta.shared.DialogBuilder;

public class SelectTKActivity extends AppCompatActivity {

    ActivitySelectTkBindingImpl binding;
    boolean lastOpenBotPanel = true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_tk);

        binding.KeyOpenBtnImg.setEnabled(false); //кнопку открытия клавиатуры делаем неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false); //делаем кнопку показа нижней панели неактивной

        /**=====SPINNER==============================*/
        String[] data = {"первый", "второй", "третий", "четвертый", "пятый", "шестой", "седьмой", "восьмой", "девятый", "десятый"};
        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.style_spinner_item, data);
        adapter.setDropDownViewResource(R.layout.style_spinner_dropdown_item);

        binding.spinner.setAdapter(adapter);
        // выделяем элемент
        binding.spinner.setSelection(0);
        // устанавливаем обработчик нажатия
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // показываем позиция нажатого элемента
                binding.AddressTxtView.setText(getResources().getString(R.string.acvtSelectTK_AddressTxtView) + " " + binding.spinner.getSelectedItem().toString());
                //Toast.makeText(getBaseContext(), "Position = " + position, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        /**==================================================*/
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

    public void imgBtn_onClick(View v)
    {
        // определяем нажатую кнопку и выполняем соответствующую операцию
        switch (v.getId()) {
            case R.id.ExitBtnImg:
                DialogBuilder dialogCloseApp = new DialogBuilder(this);
                dialogCloseApp.setTypeDialog(dialogCloseApp.TD_CLOSE_APP).setTitleText(R.string.name_buisness_process).show();
                break;
            case R.id.NextBtnImg:
                Intent intDataLoading = new Intent(this, DataLoadingActivity.class);
                startActivity(intDataLoading);
                break;
            default:
                break;
        }
    }
}

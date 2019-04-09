package com.lenta.bp10writeoff;

import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.lenta.bp10writeoff.databinding.ActivityPrinterChangeBindingImpl;
import com.lenta.shared.Click_ThreeBtnBotPanel;

public class PrinterChangeActivity extends AppCompatActivity {

    ActivityPrinterChangeBindingImpl binding;
    boolean lastOpenBotPanel = true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_printer_change);

        binding.KeyOpenBtnImg.setEnabled(false); //кнопку открытия клавиатуры делаем неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false); //делаем кнопку показа нижней панели неактивной

        /**=====SPINNER==============================*/
        String[] data = {"1-GRP601", "2-GRP602", "3-GRP603"};
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
                binding.DescriptionTxtView.setText(getResources().getString(R.string.acvtPrintChange_DescriptionTxtView) + " " + binding.spinner.getSelectedItem().toString());
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
            case R.id.ApplyBtnImg:
                break;
            case R.id.BackBtnImg:
                this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
                break;
            default:
                break;
        }
    }
}

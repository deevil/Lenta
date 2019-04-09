package com.lenta.shared;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lenta.shared.databinding.ActivityDialogBuilderBindingImpl;

public class DialogBuilderActivity extends AppCompatActivity {

    ActivityDialogBuilderBindingImpl binding;
    boolean lastOpenBotPanel = true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    DialogBuilder paramDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dialog_builder);

        //делаем все кнопки неактивными, при создании диалога, если кнопке будет присвоена картинка, тогда активируем ее
        binding.Button1.setEnabled(false);
        binding.Button2.setEnabled(false);
        binding.Button3.setEnabled(false);
        binding.Button4.setEnabled(false);
        binding.Button5.setEnabled(false);

        binding.KeyOpenBtnImg.setEnabled(false); //кнопку открытия клавиатуры делаем неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false); //делаем кнопку показа нижней панели неактивной

        /**вытаскиваем наш DialogBuilder-объект из Intent */
        paramDialog = getIntent().getParcelableExtra(DialogBuilder.class.getCanonicalName());
        binding.TitleTxtView.setText(paramDialog.getTitleText());
        binding.HeadTxtView.setText(paramDialog.getHeadText());
        binding.QuestionTxtView.setText(paramDialog.getTextDialog());
        if (paramDialog.getImageDialog() != 0) {
            binding.imageView.setImageDrawable(getDrawable(paramDialog.getImageDialog()));
        }
        if (paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_1) != 0) {
            binding.Button1.setImageDrawable(getDrawable(paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_1)));
            binding.Button1.setEnabled(true);
        }
        if (paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_2) != 0) {
            binding.Button2.setImageDrawable(getDrawable(paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_2)));
            binding.Button2.setEnabled(true);
        }
        if (paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_3) != 0) {
            binding.Button3.setImageDrawable(getDrawable(paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_3)));
            binding.Button3.setEnabled(true);
        }
        if (paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_4) != 0) {
            binding.Button4.setImageDrawable(getDrawable(paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_4)));
            binding.Button4.setEnabled(true);
        }
        if (paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_5) != 0) {
            binding.Button5.setImageDrawable(getDrawable(paramDialog.getImageButtonBotPanel(paramDialog.BUTTON_5)));
            binding.Button5.setEnabled(true);
        }
        //если диалог динамический, а не типовой, то прописываем события нажатия для всех кнопок с возвращением результата об идентификаторе нажатой кнопке
        if (paramDialog.getTypeDialog() == 0) {
            View.OnClickListener onClickButton = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    // определяем нажатую кнопку и передаем соответствующий код в intent
                    int btnClickID = v.getId();
                    if (btnClickID == R.id.Button1) {
                        intent.putExtra("button", 1);

                    } else if (btnClickID == R.id.Button2) {
                        intent.putExtra("button", 2);

                    } else if (btnClickID == R.id.Button3) {
                        intent.putExtra("button", 3);

                    } else if (btnClickID == R.id.Button4) {
                        intent.putExtra("button", 4);

                    } else if (btnClickID == R.id.Button5) {
                        intent.putExtra("button", 5);

                    }
                    setResult(RESULT_OK, intent);
                    finish(); //close this activity
                    onBackPressed();// возврат на предыдущий activity
                }
            };
            binding.Button1.setOnClickListener(onClickButton);
            binding.Button2.setOnClickListener(onClickButton);
            binding.Button3.setOnClickListener(onClickButton);
            binding.Button4.setOnClickListener(onClickButton);
            binding.Button5.setOnClickListener(onClickButton);
        }
        //если тип диалога TD_CLOSE_APP, тогда прописываем события на нажатие кнопок(НЕТ и ДА) в диалоге
        if (paramDialog.getTypeDialog() == paramDialog.TD_CLOSE_APP) {
            View.OnClickListener oclBtnNo = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); //close this activity
                    onBackPressed();// возврат на предыдущий activity
                }
            };

            View.OnClickListener oclBtnYes = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAffinity(); //закрываем приложениее, убивает полностью процесс
                }
            };
            binding.Button1.setOnClickListener(oclBtnNo);
            binding.Button5.setOnClickListener(oclBtnYes);
        }
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
}

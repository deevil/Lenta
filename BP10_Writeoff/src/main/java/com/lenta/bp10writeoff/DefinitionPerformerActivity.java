package com.lenta.bp10writeoff;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.lenta.bp10writeoff.databinding.ActivityDefinitionPerformerBindingImpl;
import com.lenta.bp10writeoff.db.DBHelper;
import com.lenta.shared.Click_ThreeBtnBotPanel;
import com.lenta.shared.DialogBuilder;

public class DefinitionPerformerActivity extends AppCompatActivity {

    ActivityDefinitionPerformerBindingImpl binding;
    boolean lastOpenBotPanel =true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    InputMethodManager imm;
    int i = 1;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Handler handler;
    String columnFIO;
    String columnPOST;

    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_definition_performer);

        binding.SearchEditTxt.setText("");
        binding.SearchEditTxt.setShowSoftInputOnFocus(false); //отключаем появление клавиатуры при фокусе на этом EditText
        //делаем кнопку показа нижней панели неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false);
        //делаем кнопку "Назад" неактивной
        binding.BackBtnImg.setEnabled(false);
        //для вызова и скрытия клавиатуры
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //для работы с потоком, чтобы изменять текст в TextView
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    // обновляем TextView
                    binding.FIOTxtView.setText(getResources().getString(R.string.acvtDefPerf_FIOTxtView) + " " + columnFIO);
                    binding.PostTxtView.setText(getResources().getString(R.string.acvtDefPerf_PostTxtView) + " " + columnPOST);
                } else {
                    //иначе выводим сообщение, что данные не найдены
                    binding.FIOTxtView.setText(getResources().getString(R.string.acvtDefPerf_FIOTxtView));
                    binding.PostTxtView.setText(getResources().getString(R.string.acvtDefPerf_PostTxtView));
                    toastNoData();
                }
            }
        };
    }

    public void toastNoData() {
        Toast.makeText(this, "Данные отсутствуют", Toast.LENGTH_SHORT).show();
    }

    //обработчик события закрытия диалога
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (data.getIntExtra("button", 0)) {
                case 1:
                    Toast.makeText(this, "Была нажата кнопка 'НАЗАД'", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(this, "Была нажата кнопка 'УДАЛИТЬ'", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(this, "Была нажата кнопка 'ПЕРЕЙТИ'", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
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
            case R.id.NextBtnImg:
                if (binding.checkBox.isChecked()) {
                    DialogBuilder dialogIncompleteData = new DialogBuilder(this);
                    dialogIncompleteData.setTitleText(R.string.name_buisness_process)
                            .setHeadText(R.string.head_dialogIncompleteData)
                            .setTextDialog(R.string.text_dialogIncompleteData)
                            .setImageDialog(R.mipmap.dialog_question)
                            .setImageButtonBotPanel(dialogIncompleteData.BUTTON_1, R.mipmap.button_back)
                            .setImageButtonBotPanel(dialogIncompleteData.BUTTON_3, R.mipmap.button_delete)
                            .setImageButtonBotPanel(dialogIncompleteData.BUTTON_5, R.mipmap.button_pass)
                            .show(true);
                } else {
                    Intent intMain = new Intent(this, MainActivity.class);
                    startActivity(intMain);
                }
                break;
            default:
                break;
        }
    }

    public void searchBtnImg_onClick(View v)
    {
        //запускаем поиск в отдельном потоке
        //создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
        //создаем объект для работы с БД
        db = dbHelper.getWritableDatabase();
        Thread threadDB = new Thread(new Runnable() {
            public void run() {
                // делаем запрос, получаем Cursor
                Cursor cursorDB = db.rawQuery("SELECT fio, post FROM definition_performer WHERE num = ?",
                                               new String[] {binding.SearchEditTxt.getText().toString()});
                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (cursorDB.moveToFirst()) {
                    // присваиваем значения из БД переменным
                    columnFIO = cursorDB.getString(cursorDB.getColumnIndex("fio"));
                    columnPOST = cursorDB.getString(cursorDB.getColumnIndex("post"));
                    // создаем сообщение, с информацией, что поиск завершен успешно
                    handler.sendEmptyMessage(1);
                } else {
                    // создаем сообщение, с информацией, что поиск завершен без результата
                    handler.sendEmptyMessage(0);
                }
                cursorDB.close();
                dbHelper.close();
            }
        });
        threadDB.start();
    }

}

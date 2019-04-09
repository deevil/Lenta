package com.lenta.bp10writeoff;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.lenta.bp10writeoff.databinding.ActivityTaskCardBindingImpl;
import com.lenta.bp10writeoff.db.AndroidDatabaseManager;
import com.lenta.bp10writeoff.db.DBHelper;
import com.lenta.bp10writeoff.objects.TTaskCard;
import com.lenta.shared.Click_ThreeBtnBotPanel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskCardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityTaskCardBindingImpl binding;
    boolean lastOpenBotPanel =true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    InputMethodManager imm;
    DBHelper dbHelper;
    Handler handler;
    List<String> arrAllTypeTask;
    List<String> arrStock;
    String typeMoveForTypeTask;
    List<String> gisForTypeTask;
    List<String> typeGoodsForTypeTask;
    TTaskCard taskCard;

    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_task_card);

        //отключаем появление клавиатуры при фокусе на этом EditText
        binding.NameTaskEditTxt.setShowSoftInputOnFocus(false);

        //делаем кнопку показа нижней панели неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false);

        //для вызова и скрытия клавиатуры
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        /**=====SpinnerTypeTask==============================*/
        // адаптер
        arrAllTypeTask = new ArrayList<>();
        final ArrayAdapter<String> adapterTypeTask = new ArrayAdapter<>(this, R.layout.style_spinner_item, arrAllTypeTask);
        adapterTypeTask.setDropDownViewResource(R.layout.style_spinner_dropdown_item);

        binding.SpinnerTypeTask.setAdapter(adapterTypeTask);
        // выделяем элемент 0
        binding.SpinnerTypeTask.setSelection(0);
        // устанавливаем обработчик выбора элемента
        binding.SpinnerTypeTask.setOnItemSelectedListener(this);
        /**==================================================*/

        //название задания
        Date dt = new Date();
        String nameTask = "Списание от "+ DateFormat.format("dd.MM", dt.getTime())+" "+ DateFormat.format("hh:mm", dt.getTime());
        binding.NameTaskEditTxt.setText(nameTask);

        /**=====SpinnerStock==============================*/
        // адаптер
        arrStock = new ArrayList<>();
        final ArrayAdapter<String> adapterStock = new ArrayAdapter<>(this, R.layout.style_spinner_item, arrStock);
        adapterStock.setDropDownViewResource(R.layout.style_spinner_dropdown_item);

        binding.SpinnerStock.setAdapter(adapterStock);
        // выделяем элемент 0
        binding.SpinnerStock.setSelection(0);
        // устанавливаем обработчик выбора элемента
        binding.SpinnerStock.setOnItemSelectedListener(this);
        /**==================================================*/

        //для работы с потоком, чтобы изменять значения View-элементов
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 1:
                        // обновляем adapter
                        adapterTypeTask.clear();
                        adapterTypeTask.addAll(arrAllTypeTask);
                        adapterTypeTask.notifyDataSetChanged();
                        break;
                    case 2:
                        adapterStock.clear();
                        adapterStock.addAll(arrStock);
                        adapterStock.notifyDataSetChanged();
                        binding.TypeMoveValTxtView.setText(typeMoveForTypeTask);
                        String tmpGIS = null;
                        for(int i = 0; i < gisForTypeTask.size(); i++) {
                            if ( i==0 ) {
                                tmpGIS = gisForTypeTask.get(i);
                            }
                            else {
                                tmpGIS = tmpGIS + ", " + gisForTypeTask.get(i);
                            }
                        }
                        binding.GISValTxtView.setText(tmpGIS);
                        String tmpTypeGoods = null;
                        for(int i = 0; i < typeGoodsForTypeTask.size(); i++) {
                            if ( i==0 ) {
                                tmpTypeGoods = typeGoodsForTypeTask.get(i);
                            }
                            else {
                                tmpTypeGoods = tmpTypeGoods + ", " + typeGoodsForTypeTask.get(i);
                            }
                        }
                        binding.TypeGoodsValTxtView.setText(tmpTypeGoods);
                        break;
                    default:
                        break;
                }
            }
        };

        //запускаем поиск в отдельном потоке
        Thread threaTypeTask = new Thread(new Runnable() {
            public void run() {
                arrAllTypeTask.clear();
                arrAllTypeTask = dbHelper.getAllTypeTask(); //запускаем поиск
                // создаем сообщение для handler, с информацией, что поиск завершен успешно
                handler.sendEmptyMessage(1);
            }
        });
        threaTypeTask.start();

    }

    public void startThreaDB()
    {
        //запускаем поиск в отдельном потоке
        Thread threaStock = new Thread(new Runnable() {
            public void run() {
                arrStock.clear();
                arrStock = dbHelper.getStockForTypeTask(binding.SpinnerTypeTask.getSelectedItem().toString()); //запускаем поиск по складам
                typeMoveForTypeTask = dbHelper.getTypeMoveForTypeTask(binding.SpinnerTypeTask.getSelectedItem().toString()); //запускаем поиск по виду движения
                gisForTypeTask = dbHelper.getGISForTypeTask(binding.SpinnerTypeTask.getSelectedItem().toString()); //запускаем поиск по ГИС-контролю
                typeGoodsForTypeTask = dbHelper.getTypeGoodsForTypeTask(binding.SpinnerTypeTask.getSelectedItem().toString()); //запускаем поиск по типку товара
                // создаем сообщение для handler, с информацией, что поиск завершен успешно
                handler.sendEmptyMessage(2);
            }
        });
        threaStock.start();
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
            case R.id.NextBtnImg:
                dbHelper.close();
                taskCard = new TTaskCard(binding.SpinnerTypeTask.getSelectedItem().toString(),
                                         binding.NameTaskEditTxt.getText().toString(),
                                         binding.SpinnerStock.getSelectedItem().toString(),
                                         typeMoveForTypeTask,
                                         gisForTypeTask,
                                         typeGoodsForTypeTask
                                        );
                Intent intGoodsList = new Intent(this, GoodsListActivity.class);
                intGoodsList.putExtra(TTaskCard.class.getCanonicalName(), taskCard);
                startActivity(intGoodsList);
                break;
            case R.id.BackBtnImg:
                this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId()==binding.SpinnerTypeTask.getId()) {
            startThreaDB();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

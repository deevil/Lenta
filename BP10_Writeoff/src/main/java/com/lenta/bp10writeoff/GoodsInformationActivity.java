package com.lenta.bp10writeoff;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import com.lenta.bp10writeoff.databinding.ActivityGoodsInformationBindingImpl;
import com.lenta.bp10writeoff.db.AndroidDatabaseManager;
import com.lenta.bp10writeoff.db.DBHelper;
import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;
import com.lenta.bp10writeoff.objects.TTaskCard;
import com.lenta.shared.Click_ThreeBtnBotPanel;

import java.util.ArrayList;
import java.util.List;

public class GoodsInformationActivity extends AppCompatActivity {

    ActivityGoodsInformationBindingImpl binding;
    boolean lastOpenBotPanel =true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    boolean keyboardEnable = true;
    InputMethodManager imm;
    TGoods goods;
    TGoodsReason goodsReason;
    List<TGoodsReason> newGoodsReasonList = new ArrayList<>();
    TTaskCard taskCard;
    List<String> arrReason;
    DBHelper dbHelper;
    boolean boolNewGoods;
    int countGoods;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_goods_information);

        //делаем кнопку показа нижней панели неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false);

        //отключаем появление клавиатуры при фокусе на этом EditText
        binding.WriteoffValEditTxt.setShowSoftInputOnFocus(false);

        //для вызова и скрытия клавиатуры
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        /**=====SpinnerReason==============================*/
        // адаптер
        arrReason = new ArrayList<>();
        final ArrayAdapter<String> adapSpinReason = new ArrayAdapter<>(this, R.layout.style_spinner_item, arrReason);
        adapSpinReason.setDropDownViewResource(R.layout.style_spinner_dropdown_item);

        binding.SpinnerReason.setAdapter(adapSpinReason);
        // выделяем элемент 0
        binding.SpinnerReason.setSelection(0);
        /**==================================================*/

        /**вытаскиваем из Intent значение новый это товар или старый*/
        boolNewGoods = getIntent().getBooleanExtra("boolNewGoods",true);

        /**вытаскиваем TTaskCard-объект из Intent */
        taskCard = getIntent().getParcelableExtra(TTaskCard.class.getCanonicalName());

        /**вытаскиваем TGoods-объект из Intent */
        goods = getIntent().getParcelableExtra(TGoods.class.getCanonicalName());
        // определяем тип матрицы
        switch (goods.getTypeMatrix()) {
            case "A":
                binding.TypeMatrixImg.setImageDrawable(getDrawable(getResources().getIdentifier("icon_active_matrix", "mipmap", getPackageName())));
                break;
            case "P":
                binding.TypeMatrixImg.setImageDrawable(getDrawable(getResources().getIdentifier("icon_passive_matrix", "mipmap", getPackageName())));
                break;
            case "D":
                binding.TypeMatrixImg.setImageDrawable(getDrawable(getResources().getIdentifier("icon_deleted_matrix", "mipmap", getPackageName())));
                break;
            default:
                binding.TypeMatrixImg.setImageDrawable(getDrawable(getResources().getIdentifier("icon_unknown_matrix", "mipmap", getPackageName())));
                break;
        }
        binding.NumberSectionTxt.setText(goods.getNumberSection());
        binding.WriteoffValEditTxt.setText("0 "+goods.getNameUnit());
        binding.TotalValTxtView.setText("0 "+goods.getNameUnit());
        /**=====================================*/

        //запускаем поиск категорий (Reason) в отдельном потоке
        Thread threaReasonDB = new Thread(new Runnable() {
            public void run() {
                arrReason.clear();
                arrReason = dbHelper.getReason(taskCard.getTypeTask(), taskCard.getStock()); //запускаем поиск
                // обновляем adapter спинера (adapSpinReason)
                adapSpinReason.clear();
                adapSpinReason.addAll(arrReason);
                adapSpinReason.notifyDataSetChanged();
            }
        });
        threaReasonDB.start();

        //устанавливаем событие на нажатие ВВОД на клавиатуре
        binding.WriteoffValEditTxt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        countGoods = Integer.valueOf(binding.WriteoffValEditTxt.getText().toString());
                        binding.WriteoffValEditTxt.setText(binding.WriteoffValEditTxt.getText()+" "+goods.getNameUnit());
                        //вызываем событие нажатия на кнопку ShowBotPanelBtnImg, чтобы после нажатия на ВВОД на клавиатуре спрятать клавиатуру и показать нижнию панель с кнопками
                        binding.ShowBotPanelBtnImg.callOnClick();
                        return true;
                    }
                return false;
            }
        });
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
                keyboardEnable
        );

        //если была нажата кнопка показа клавиатуры, тогда в кол-ве (WriteoffValEditTxt) убираем name unit
        if (btnClick.getId() == binding.KeyOpenBtnImg.getId()) {
            binding.WriteoffValEditTxt.setText(null);
        }
    }

    public void imgBtn_onClick(View v)
    {
        // определяем нажатую кнопку и выполняем соответствующую операцию
        switch (v.getId()) {
            case R.id.BackBtnImg:
                this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
                break;
            case R.id.DetailsBtnImg:
                Intent intWrDetails = new Intent(this, WriteoffDetails.class);
                startActivity(intWrDetails);
                break;
            case R.id.AddBtnImg:
                break;
            case R.id.ApplyBtnImg:
                Intent intOutput = new Intent();
                if (boolNewGoods) {
                    intOutput.putExtra("boolNewGoods", true);
                    newGoodsReasonList.clear();
                    goodsReason = new TGoodsReason(goods.getMaterialNumGoods(),binding.SpinnerReason.getSelectedItem().toString(), countGoods);
                    newGoodsReasonList.add(goodsReason);
                    goodsReason = new TGoodsReason("materialNumGoods","Срок годности", 0);
                    newGoodsReasonList.add(goodsReason);

                    intOutput.putParcelableArrayListExtra("goodsReasonList", (ArrayList<TGoodsReason>) newGoodsReasonList);
                    setResult(RESULT_OK, intOutput);
                }
                else {
                    ///////////
                }

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
}

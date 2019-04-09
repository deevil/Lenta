package com.lenta.bp10writeoff;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.lenta.bp10writeoff.adapter.GoodsListPageAdap;
import com.lenta.bp10writeoff.databinding.ActivityGoodsListBindingImpl;
import com.lenta.bp10writeoff.db.AndroidDatabaseManager;
import com.lenta.bp10writeoff.db.DBHelper;
import com.lenta.bp10writeoff.fragment.GoodsCountedTabFragm;
import com.lenta.bp10writeoff.fragment.GoodsFilterTabFragm;
import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;
import com.lenta.bp10writeoff.objects.TTaskCard;
import com.lenta.bp10writeoff.viewmodel.CountGoodsDelViewModel;
import com.lenta.bp10writeoff.viewmodel.GoodsReasonMapViewModel;
import com.lenta.bp10writeoff.viewmodel.GoodsViewModel;
import com.lenta.shared.Click_ThreeBtnBotPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoodsListActivity extends AppCompatActivity implements GoodsCountedTabFragm.IEventFragmentGoodsCount, GoodsFilterTabFragm.IEventFragmentGoodsFilter {

    ActivityGoodsListBindingImpl binding;
    boolean lastOpenBotPanel =true; //если true, значит последней была открыта нижняя панель, иначе (false) - клавиатура
    boolean keyboardEnable = true;
    int sequenceNumGoods = 0;
    InputMethodManager imm;
    DBHelper dbHelper;
    Handler handler;
    private GoodsListPageAdap adapterPage;
    Context context;
    Activity activity;
    GoodsViewModel goodsViewModel;
    CountGoodsDelViewModel countGoodsDelViewModel;
    TGoods goods;
    TTaskCard taskCard;
    List<TGoods> goodsList = new ArrayList<>();
    List<TGoodsReason> goodsReasonList = new ArrayList<>();

    Map<TGoods, List<TGoodsReason>> goodsReasonMap = new HashMap<>();
    GoodsReasonMapViewModel goodsReasonMapViewModel;

    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_goods_list);

        context = this;
        activity = this;

        //делаем кнопку показа нижней панели неактивной
        binding.ShowBotPanelBtnImg.setEnabled(false);

        //для вызова и скрытия клавиатуры
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        /**вытаскиваем TTaskCard-объект из Intent чтобы потом его передать в активити GoodsInformationActivity */
        taskCard = getIntent().getParcelableExtra(TTaskCard.class.getCanonicalName());

        //создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

        //для работы с потоком, чтобы по окончанию поиска товара вызвать метод addGoodsInArr
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 1) {
                    addNewGoods();
                }
            }
        };

        //Создаем ViewModel для Goods (GoodsViewModel and GoodsReasonMapViewModel), не подписываемся на получение данных после их обновления, т.к. это событие отлавивается в фрагментах, а здесь этого не надо
        goodsViewModel = ViewModelProviders.of(this).get(GoodsViewModel.class);
        goodsReasonMapViewModel = ViewModelProviders.of(this).get(GoodsReasonMapViewModel.class);

        //Создаем ViewModel для кол-ва выделенных товаров для удаления (CountGoodsDelViewModel) и подписываемся на получение данных после их обновления
        countGoodsDelViewModel = ViewModelProviders.of(this).get(CountGoodsDelViewModel.class);
        countGoodsDelViewModel.getCountGoodsDel().observe(this, new Observer<Integer>(){
        @Override
        public void onChanged(Integer newCountGoodsDel) {
            if ( newCountGoodsDel > 0 ) {
                //делаем кнопку УДАЛИТЬ актиной
                binding.DeleteBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_delete", "mipmap", getPackageName())));
                binding.DeleteBtnImg.setEnabled(true);
            }
            else {
                //делаем кнопку УДАЛИТЬ неактиной
                binding.DeleteBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("button_delete_disable", "mipmap", getPackageName())));
                binding.DeleteBtnImg.setEnabled(false);
            }
        }
        });

        //реализуем переключение вкладок (Tab)
        adapterPage = new GoodsListPageAdap (getSupportFragmentManager());
        binding.VPager.setAdapter(adapterPage);
        binding.VPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.VPager.setCurrentItem(tab.getPosition());
                switch (tab.getPosition()){
                    case 0:
                        keyboardEnable =true;
                        binding.KeyOpenBtnImg.setEnabled(true); //кнопку открытия клавиатуры делаем активной
                        binding.KeyOpenBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("keyboard_enabled", "mipmap", getPackageName())));
                        //устанавливаем фокус в компоненнте AddGoodsEditTxt в фрагменте GoodsCountedTabFragm
                        Fragment fragmentGoodsCounted = adapterPage.getFragment(0);
                        (fragmentGoodsCounted.getView().findViewById(R.id.AddGoodsEditTxt)).requestFocus();
                        break;
                    case 1:
                        //скрываем клавиатуру, если она была открыта перед переходом на ТАВ "Фильтр" и открываем нижнюю панель с кнопками
                        if (!binding.KeyOpenBtnImg.isEnabled()) {
                            lastOpenBotPanel = Click_ThreeBtnBotPanel.imgBtn_onClick( context,
                                    activity,
                                    imm,
                                    binding.ShowBotPanelBtnImg,
                                    binding.KeyOpenBtnImg,
                                    binding.UpDownBtnImg,
                                    binding.ShowBotPanelBtnImg,
                                    binding.BottomPanel,
                                    true,
                                    true
                            );
                        }
                        keyboardEnable =false;
                        binding.KeyOpenBtnImg.setEnabled(false); //кнопку открытия клавиатуры делаем неактивной
                        binding.KeyOpenBtnImg.setImageDrawable(getDrawable(getResources().getIdentifier("keyboard_disabled", "mipmap", getPackageName())));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

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
    }

    public void imgBtn_onClick(View v)
    {
        // определяем нажатую кнопку и выполняем соответствующую операцию
        switch (v.getId()) {
            case R.id.BackBtnImg:
                //this.finish(); //close this activity
                onBackPressed();// возврат на предыдущий activity
                break;
            case R.id.DeleteBtnImg:
                break;
            case R.id.PrintBtnImg:
                break;
            case R.id.SaveBtnImg:
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

    public void searchGoods(final String materialNumGoods)
    {
        //вызываем событие нажатия на кнопку ShowBotPanelBtnImg, чтобы после нажатия на ВВОД на клавиатуре спрятать клавиатуру и показать нижнию панель с кнопками
        binding.ShowBotPanelBtnImg.callOnClick();

        goods = null;
        //запускаем поиск в отдельном потоке
        Thread threadSearchGoods = new Thread(new Runnable() {
            public void run() {
                goods = dbHelper.getInfoGoods(materialNumGoods, taskCard.getTypeGoods()); //запускаем поиск товара
                // создаем сообщение для handler, с информацией, что поиск завершен успешно
                handler.sendEmptyMessage(1);
            }
        });
        threadSearchGoods.start();
    }

    public void addNewGoods()
    {
        if (goods != null) {
            sequenceNumGoods++;
            goods.setSortbyID(sequenceNumGoods);

            Intent intGoodsInfo = new Intent(this, GoodsInformationActivity.class);
            intGoodsInfo.putExtra("boolNewGoods", true);
            intGoodsInfo.putExtra(TGoods.class.getCanonicalName(), goods);
            intGoodsInfo.putExtra(TTaskCard.class.getCanonicalName(), taskCard);
            //intGoodsInfo.putParcelableArrayListExtra("goodsList", (ArrayList<TGoods>) goodsList);
            startActivityForResult(intGoodsInfo, 1);
        }
        else  {
            Toast.makeText(this, "товар НЕ найден", Toast.LENGTH_SHORT).show();
        }
    }

    //обработчик события (данных) при возвращении из активити GoodsInformationActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data.getBooleanExtra("boolNewGoods",true)) {
                goodsList.add(goods);
                goodsViewModel.setGoodsList(goodsList);

                goodsReasonList = data.getParcelableArrayListExtra("goodsReasonList");
                goodsReasonMap.put(goodsList.get(goodsList.size()-1), goodsReasonList);
                goodsReasonMapViewModel.setGoodsReasonMap(goodsReasonMap);
            }
            else {
                /////////
            }

        }
    }

    //метод, который вызывается из фрагмента GoodsCountedTabFragm
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void fgcOnSelectDelGoods(List<TGoods> newGoodsList) {
        goodsList.clear();
        goodsList.addAll(newGoodsList);
        goodsViewModel.setGoodsList(goodsList);
    }

    //метод, который вызывается из фрагмента GoodsFilterTabFragm
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void fgfOnSelectDelGoods(List<TGoods> newGoodsList) {
        goodsList.clear();
        goodsList.addAll(newGoodsList);
        goodsViewModel.setGoodsList(goodsList);
    }

    //метод, который вызывается из фрагмента GoodsFilterTabFragm
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public TTaskCard getTaskCard() {
        return taskCard;
    }
}

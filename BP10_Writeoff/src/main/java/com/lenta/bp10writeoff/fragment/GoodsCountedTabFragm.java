package com.lenta.bp10writeoff.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lenta.bp10writeoff.R;
import com.lenta.bp10writeoff.adapter.GoodsCountRecViewAdap;
import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;
import com.lenta.bp10writeoff.viewmodel.CountGoodsDelViewModel;
import com.lenta.bp10writeoff.viewmodel.GoodsReasonMapViewModel;
import com.lenta.bp10writeoff.viewmodel.GoodsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsCountedTabFragm extends Fragment {

    EditText addGoodsEditTxt;
    RecyclerView goodsRecyclerView;
    RecyclerView.Adapter goodsRecViewAdapter;
    RecyclerView.LayoutManager layoutManager;
    GoodsViewModel goodsViewModel;
    CountGoodsDelViewModel countGoodsDelViewModel;
    int countGoodsDel = 0;

    List<TGoods> goodsList = new ArrayList<>();

    Map<TGoods, List<TGoodsReason>> goodsReasonMap = new HashMap<>();
    GoodsReasonMapViewModel goodsReasonMapViewModel;

    /**реализуем интерфейс для взаимодействия с активити*/
    public interface IEventFragmentGoodsCount {
        void fgcOnSelectDelGoods(List<TGoods> newGoodsList);
        void searchGoods(String materialNumGoods);
    }

    IEventFragmentGoodsCount iEventFGC;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iEventFGC = (IEventFragmentGoodsCount) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " должен реализовать IEventFragmentGoodsCount");
        }
    }
    /**====================================*/

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vf = inflater.inflate(R.layout.goods_counted_tab_fragm, container, false);

        //отключаем появление клавиатуры при фокусе на этом EditText
        addGoodsEditTxt = vf.findViewById(R.id.AddGoodsEditTxt);
        addGoodsEditTxt.setShowSoftInputOnFocus(false);
        //устанавливаем событие на нажатие ВВОД на клавиатуре
        addGoodsEditTxt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        //вызываем метод по добавлению товара searchGoods в активити, здесь если реализовать добавление товара, то он не добавляется, нельзя goodsViewModel и на прослушивание поставить и на обновление данных
                        iEventFGC.searchGoods(addGoodsEditTxt.getText().toString());
                        addGoodsEditTxt.setText(null);
                        return true;
                    }
                return false;
            }
        });

        //адаптер для RecyclerView
        goodsRecyclerView = vf.findViewById(R.id.GoodsRecView);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        goodsRecyclerView.setLayoutManager(layoutManager);
        // specify an adapter
        goodsRecViewAdapter = new GoodsCountRecViewAdap(goodsList, goodsReasonMap, this);
        goodsRecyclerView.setAdapter(goodsRecViewAdapter);

        //Создаем ViewModel для Goods (GoodsViewModel) и подписываемся на получение данных после их обновления
        goodsViewModel = ViewModelProviders.of(getActivity()).get(GoodsViewModel.class);
        goodsViewModel.getGoodsList().observe(this, new Observer<List<TGoods>>(){
            @Override
            public void onChanged(List<TGoods> newGoodsList) {
                goodsList.clear();
                if (newGoodsList != null) { goodsList.addAll(newGoodsList);}
                sortGoodsList();
                goodsRecViewAdapter.notifyDataSetChanged();
            }
        });

        //Создаем ViewModel для GoodsReason (GoodsReasonMapViewModel) и подписываемся на получение данных после их обновления
        goodsReasonMapViewModel = ViewModelProviders.of(getActivity()).get(GoodsReasonMapViewModel.class);
        goodsReasonMapViewModel.getGoodsReasonMap().observe(this, new Observer<Map<TGoods, List<TGoodsReason>>>(){
            @Override
            public void onChanged(Map<TGoods, List<TGoodsReason>> newGoodsReasonMap) {
                goodsReasonMap.clear();
                if (newGoodsReasonMap != null) { goodsReasonMap.putAll(newGoodsReasonMap); }
                goodsRecViewAdapter.notifyDataSetChanged();
            }
        });

        //Создаем ViewModel для кол-ва выделенных товаров для удаления (CountGoodsDelViewModel) и подписываемся на получение данных после их обновления
        countGoodsDelViewModel = ViewModelProviders.of(getActivity()).get(CountGoodsDelViewModel.class);
        countGoodsDelViewModel.getCountGoodsDel().observe(this, new Observer<Integer>(){
            @Override
            public void onChanged(Integer newCountGoodsDel) {
                countGoodsDel = newCountGoodsDel;
            }
        });

        return vf;
    }

    /* сортировка массива по убыванию goodsList */
    public void sortGoodsList(){
        Collections.sort(goodsList, new Comparator<TGoods>(){
            public int compare(TGoods obj1, TGoods obj2) {
                return Integer.compare(obj2.getSortbyID(),obj1.getSortbyID());
            }
        });
    }

    //данный метод вызывается из адаптера GoodsCountRecViewAdap, который в свою очередь вызывает метод fgcOnSelectDelGoods в активити
    public void onSelectDelGoods(List<TGoods> newGoodsList){
        //вызываем метод в активити
        iEventFGC.fgcOnSelectDelGoods(newGoodsList);
    }

    //данный метод вызывается из адаптера GoodsCountRecViewAdap, и возвращает кол-во уже выделенных товаров для удаления
    public int getCountGoodsDel(){
        return countGoodsDel;
    }

    //данный метод вызывается из адаптера GoodsCountRecViewAdap, который обновляет во ViewModel (CountGoodsDelViewModel) кол-во выделенных товаров для удаления, чтобы потом в активити кнопку удаления делать либо активной либо нет
    public void setCountGoodsDel(int newCountGoodsDel){
        countGoodsDelViewModel.setCountGoodsDel(newCountGoodsDel);
    }
}

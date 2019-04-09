package com.lenta.bp10writeoff.fragment;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.lenta.bp10writeoff.R;
import com.lenta.bp10writeoff.adapter.GoodsFilterRecViewAdapr;
import com.lenta.bp10writeoff.db.DBHelper;
import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;
import com.lenta.bp10writeoff.objects.TTaskCard;
import com.lenta.bp10writeoff.viewmodel.CountGoodsDelViewModel;
import com.lenta.bp10writeoff.viewmodel.GoodsReasonMapViewModel;
import com.lenta.bp10writeoff.viewmodel.GoodsViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GoodsFilterTabFragm extends Fragment {

    Spinner SpinnerReason;
    RecyclerView goodsRecyclerView;
    RecyclerView.Adapter goodsRecViewAdapter;
    RecyclerView.LayoutManager layoutManager;
    GoodsViewModel goodsViewModel;
    CountGoodsDelViewModel countGoodsDelViewModel;
    int countGoodsDel = 0;
    TTaskCard taskCard;
    List<String> arrReason;
    DBHelper dbHelper;

    List<TGoods> goodsList = new ArrayList<>();

    Map<TGoods, List<TGoodsReason>> goodsReasonMap = new HashMap<>();
    GoodsReasonMapViewModel goodsReasonMapViewModel;

    /**реализуем интерфейс для взаимодействия с активити*/
    public interface IEventFragmentGoodsFilter {
        void fgfOnSelectDelGoods(List<TGoods> newGoodsList);
        TTaskCard getTaskCard();
    }

    IEventFragmentGoodsFilter iEventFGF;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            iEventFGF = (IEventFragmentGoodsFilter) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " должен реализовать IEventFragmentGoodsFilter");
        }
    }
    /**====================================*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View vf = inflater.inflate(R.layout.goods_filter_tab_fragm, container, false);

        //создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(getContext());

        /**=====SpinnerReason==============================*/
        // адаптер
        SpinnerReason = vf.findViewById(R.id.Spinner);
        arrReason = new ArrayList<>();
        final ArrayAdapter<String> adapSpinReason = new ArrayAdapter<>(getActivity(), R.layout.style_spinner_item, arrReason);
        adapSpinReason.setDropDownViewResource(R.layout.style_spinner_dropdown_item);

        SpinnerReason.setAdapter(adapSpinReason);
        // выделяем элемент 0
        SpinnerReason.setSelection(0);
        /**==================================================*/

        //адаптер для RecyclerView
        goodsRecyclerView = vf.findViewById(R.id.GoodsRecView);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        goodsRecyclerView.setLayoutManager(layoutManager);
        // specify an adapter
        goodsRecViewAdapter = new GoodsFilterRecViewAdapr(goodsList, goodsReasonMap,this);
        goodsRecyclerView.setAdapter(goodsRecViewAdapter);

        //Создаем ViewModel для Goods (GoodsViewModel) и подписываемся на получение данных после их обновления
        goodsViewModel = ViewModelProviders.of(getActivity()).get(GoodsViewModel.class);
        goodsViewModel.getGoodsList().observe(this, new Observer<List<TGoods>>(){
            @Override
            public void onChanged(@Nullable List<TGoods> newGoodsList) {
                goodsList.clear();
                if (newGoodsList != null) {goodsList.addAll(newGoodsList);}
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

        //запускаем поиск категорий (Reason) для спинера в отдельном потоке
        taskCard = iEventFGF.getTaskCard();
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

    //данный метод вызывается из адаптера GoodsFilterRecViewAdapr, который в свою очередь вызывает метод fgfOnSelectDelGoods в активити
    public void onSelectDelGoods(List<TGoods> newGoodsList){
        //вызываем метод в активити
        iEventFGF.fgfOnSelectDelGoods(newGoodsList);
    }

    //данный метод вызывается из адаптера GoodsFilterRecViewAdapr, и возвращает кол-во уже выделенных товаров для удаления
    public int getCountGoodsDel(){
        return countGoodsDel;
    }

    //данный метод вызывается из адаптера GoodsFilterRecViewAdapr, который обновляет во ViewModel (CountGoodsDelViewModel) кол-во выделенных товаров для удаления, чтобы потом в активити кнопку удаления делать либо активной либо нет
    public void setCountGoodsDel(int newCountGoodsDel){
        countGoodsDelViewModel.setCountGoodsDel(newCountGoodsDel);
    }

}

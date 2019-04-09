package com.lenta.bp10writeoff.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.lenta.bp10writeoff.objects.TGoods;

import java.util.List;


public class GoodsViewModel extends ViewModel {

    private final MutableLiveData<List<TGoods>> goodsList = new MutableLiveData<>();

    public void setGoodsList(List<TGoods> newGoodsList) {
        this.goodsList.setValue(newGoodsList);
    }

    public LiveData<List<TGoods>> getGoodsList() {
        return this.goodsList;
    }
}
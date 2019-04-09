package com.lenta.bp10writeoff.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class CountGoodsDelViewModel extends ViewModel {

    private final MutableLiveData<Integer> countGoodsDel = new MutableLiveData<>();

    public void setCountGoodsDel(Integer countGoodsDel) {
        this.countGoodsDel.setValue(countGoodsDel);
    }

    public LiveData<Integer> getCountGoodsDel() {
        return this.countGoodsDel;
    }
}
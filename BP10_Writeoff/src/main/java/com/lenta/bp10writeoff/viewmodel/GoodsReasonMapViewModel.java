package com.lenta.bp10writeoff.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;

import java.util.List;
import java.util.Map;

public class GoodsReasonMapViewModel extends ViewModel {

    private final MutableLiveData<Map<TGoods, List<TGoodsReason>>> goodsReaasonMap = new MutableLiveData<>();

    public void setGoodsReasonMap(Map<TGoods, List<TGoodsReason>> goodsReaasonMap) {
        this.goodsReaasonMap.setValue(goodsReaasonMap);
    }

    public LiveData<Map<TGoods, List<TGoodsReason>>> getGoodsReasonMap() {
        return goodsReaasonMap;
    }
}

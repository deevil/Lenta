package com.lenta.bp10writeoff.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.lenta.bp10writeoff.fragment.GoodsCountedTabFragm;
import com.lenta.bp10writeoff.fragment.GoodsFilterTabFragm;

public class GoodsListPageAdap extends FragmentStatePagerAdapter {

    GoodsCountedTabFragm tab1;
    GoodsFilterTabFragm tab2;

    public GoodsListPageAdap(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                tab1 = new GoodsCountedTabFragm();
                return tab1;
            case 1:
                tab2 = new GoodsFilterTabFragm();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    //этот метод вызывается в активити GoodsListActivity, чтобы создать экземпляр фрагмента и получить доступ к его компонентам
    public Fragment getFragment(int position) {

        switch (position) {
            case 0:
                return tab1;
            case 1:
                return tab2;
            default:
                return null;
        }
    }

}

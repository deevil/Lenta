package com.lenta.bp10writeoff.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lenta.bp10writeoff.R;
import com.lenta.bp10writeoff.fragment.GoodsCountedTabFragm;
import com.lenta.bp10writeoff.fragment.GoodsFilterTabFragm;
import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsCountRecViewAdap extends RecyclerView.Adapter<GoodsCountRecViewAdap.GoodsViewHolder> {

    private final List<TGoods> goodsList;
    private final Map<TGoods, List<TGoodsReason>> goodsReasonMap;
    private GoodsCountedTabFragm fragmentGoodsCounted;
    private int countGoodsDel;

    public class GoodsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView SequenceNumGoods;
        TextView MaterialNumNameGoods;
        TextView CountGoods;
        boolean checkedGoods;

        GoodsViewHolder(View itemView) {
            super(itemView);
            SequenceNumGoods = itemView.findViewById(R.id.SequenceNumTxtView);
            SequenceNumGoods.setOnClickListener(this);
            MaterialNumNameGoods = itemView.findViewById(R.id.MaterialNumNameGoodsTxtView);
            MaterialNumNameGoods.setOnClickListener(this);
            CountGoods = itemView.findViewById(R.id.CountGoodsTxtView);
        }

        @Override
        public void onClick(View v) {
            countGoodsDel = 0;
            // определяем нажатый TextView и выполняем соответствующую операцию
            if (v.getId()==SequenceNumGoods.getId()) {
                TGoods replaceGoods = goodsList.get(getAdapterPosition());
                if (replaceGoods.getChecked()) {
                    replaceGoods.setChecked(false);
                    goodsList.set(getAdapterPosition(),replaceGoods);
                    notifyDataSetChanged();
                    fragmentGoodsCounted.onSelectDelGoods(goodsList);
                    countGoodsDel = fragmentGoodsCounted.getCountGoodsDel();
                    if ( countGoodsDel > 0 ) {
                        countGoodsDel--;
                        fragmentGoodsCounted.setCountGoodsDel(countGoodsDel);
                    }
                } else {
                    replaceGoods.setChecked(true);
                    goodsList.set(getAdapterPosition(),replaceGoods);
                    notifyDataSetChanged();
                    fragmentGoodsCounted.onSelectDelGoods(goodsList);
                    countGoodsDel = fragmentGoodsCounted.getCountGoodsDel();
                    countGoodsDel++;
                    fragmentGoodsCounted.setCountGoodsDel(countGoodsDel);
                }
            } else if (v.getId()==MaterialNumNameGoods.getId()) {
                MaterialNumNameGoods.setText(String.valueOf(getItemCount()));
            }
        }
    }

    public GoodsCountRecViewAdap(List<TGoods> goodsList, Map<TGoods, List<TGoodsReason>> goodsReasonMap, GoodsCountedTabFragm fragmentGoodsCounted) {
        this.goodsList = goodsList;
        this.goodsReasonMap = goodsReasonMap;
        this.fragmentGoodsCounted = fragmentGoodsCounted;
    }

    @NonNull
    @Override
    public GoodsViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.goods_item3, parent, false);

        GoodsViewHolder gvh = new GoodsViewHolder(itemView);

        return gvh;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull GoodsViewHolder holder, int position) {
        TGoods goods = goodsList.get(position);
        holder.SequenceNumGoods.setText(String.valueOf(goodsList.size()-position));
        String strMatNumNameGoods = goods.getMaterialNumGoods() + " " + goods.getNameGoods();
        holder.MaterialNumNameGoods.setText(strMatNumNameGoods);

        List<TGoodsReason> goodsMapValue = goodsReasonMap.get(goods);
        if (goodsReasonMap.containsKey(goods)) {
            int countGoods = 0;
            for(int i = 0; i < goodsMapValue.size(); i++) {
                countGoods = countGoods + goodsMapValue.get(i).getCountGoods();
                holder.CountGoods.setText(String.valueOf(countGoods) + " " + goods.getNameUnit());
            }
        }
        else {
            holder.CountGoods.setText("0 " + goods.getNameUnit());
        }

        if (goods.getChecked()) {
            holder.SequenceNumGoods.setBackgroundResource(R.drawable.frame_circle_fields_check);
        }
        else {
            holder.SequenceNumGoods.setBackgroundResource(R.drawable.frame_circle_fields);
        }
    }

    @Override
    public int getItemCount() {
        return goodsList.size();
    }

}

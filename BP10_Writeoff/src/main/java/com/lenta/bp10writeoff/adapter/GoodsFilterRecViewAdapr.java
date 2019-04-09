package com.lenta.bp10writeoff.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lenta.bp10writeoff.R;
import com.lenta.bp10writeoff.fragment.GoodsFilterTabFragm;
import com.lenta.bp10writeoff.objects.TGoods;
import com.lenta.bp10writeoff.objects.TGoodsReason;

import java.util.List;
import java.util.Map;

public class GoodsFilterRecViewAdapr extends RecyclerView.Adapter<GoodsFilterRecViewAdapr.GoodsViewHolder> {

    private final List<TGoods> goodsList;
    private final Map<TGoods, List<TGoodsReason>> goodsReasonMap;
    private GoodsFilterTabFragm fragmentGoodsFilter;
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
                    fragmentGoodsFilter.onSelectDelGoods(goodsList);
                    countGoodsDel = fragmentGoodsFilter.getCountGoodsDel();
                    if ( countGoodsDel > 0 ) {
                        countGoodsDel--;
                        fragmentGoodsFilter.setCountGoodsDel(countGoodsDel);
                    }
                } else {
                    replaceGoods.setChecked(true);
                    goodsList.set(getAdapterPosition(),replaceGoods);
                    notifyDataSetChanged();
                    fragmentGoodsFilter.onSelectDelGoods(goodsList);
                    countGoodsDel = fragmentGoodsFilter.getCountGoodsDel();
                    countGoodsDel++;
                    fragmentGoodsFilter.setCountGoodsDel(countGoodsDel);
                }
            } else if (v.getId()==MaterialNumNameGoods.getId()) {
                MaterialNumNameGoods.setText(String.valueOf(getItemCount()));
            }
        }
    }

    public GoodsFilterRecViewAdapr(List<TGoods> goodsList, Map<TGoods, List<TGoodsReason>> goodsReasonMap, GoodsFilterTabFragm fragmentGoodsFilter) {
        this.goodsList = goodsList;
        this.goodsReasonMap = goodsReasonMap;
        this.fragmentGoodsFilter = fragmentGoodsFilter;
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

    @Override
    public void onBindViewHolder(@NonNull GoodsViewHolder holder, int position) {
        TGoods goods = goodsList.get(position);
        if (goodsReasonMap.containsKey(goods)) {
            holder.SequenceNumGoods.setText(String.valueOf(goodsList.size()-position));
            List<TGoodsReason> goodsMapValue = goodsReasonMap.get(goods);
            for(int i = 0; i < goodsMapValue.size(); i++) {
                String strMatNumNameGoods = goods.getMaterialNumGoods() + " " + goods.getNameGoods() + "\n" + goodsMapValue.get(i).getReasonGoods();
                holder.MaterialNumNameGoods.setText(strMatNumNameGoods);

                holder.CountGoods.setText(String.valueOf(goodsMapValue.get(i).getCountGoods()));

                if (goodsMapValue.get(i).getChecked()) {
                    holder.SequenceNumGoods.setBackgroundResource(R.drawable.frame_circle_fields_check);
                }
                else {
                    holder.SequenceNumGoods.setBackgroundResource(R.drawable.frame_circle_fields);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return goodsList.size();
    }

}

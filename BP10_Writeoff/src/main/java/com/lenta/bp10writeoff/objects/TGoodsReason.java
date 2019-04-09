package com.lenta.bp10writeoff.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class TGoodsReason implements Parcelable {

    private String materialNumGoods;
    private String reasonGoods;
    private int countGoods;
    private boolean[] booleanArr = new boolean[1];

    public TGoodsReason(String materialNumGoods, String reasonGoods, int countGoods){
        this.materialNumGoods = materialNumGoods;
        this.reasonGoods = reasonGoods;
        this.countGoods = countGoods;
        booleanArr[0] = false; //checkedGoods
    }

    /**==========Parcelable=================*/
    /** битовая маска, указывающая набор специальных типов объектов, маршалируемых этим экземпляром объекта Parcelable.
     Значение либо 0, либо CONTENTS_FILE_DESCRIPTOR.*/
    public int describeContents() {
        return 0;
    }

    /** получаем на вход Parcel и упаковываем в него наш объект.*/
    public final void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(materialNumGoods);
        parcel.writeString(reasonGoods);
        parcel.writeInt(countGoods);
        parcel.writeBooleanArray(booleanArr);
    }

    /** распаковываем объект из Parcel*/
    public final static Parcelable.Creator<TGoodsReason> CREATOR
            = new Parcelable.Creator<TGoodsReason>() {
        public TGoodsReason createFromParcel(Parcel in) {
            return new TGoodsReason(in);
        }

        public TGoodsReason[] newArray(int size) {
            return new TGoodsReason[size];
        }
    };

    /** конструктор, считывающий данные из Parcel*/
    private TGoodsReason(Parcel parcel) {
        materialNumGoods = parcel.readString();
        reasonGoods = parcel.readString();
        countGoods = parcel.readInt();
        parcel.readBooleanArray(booleanArr);
    }
    /**==========================================================*/

    public String getMaterialNumGoods () {
        return materialNumGoods;
    }

    public String getReasonGoods () {
        return reasonGoods;
    }

    public int getCountGoods () {
        return countGoods;
    }

    public TGoodsReason setChecked (boolean checked) {
        if (checked) {
            booleanArr[0] = true;
        }
        else {
            booleanArr[0] = false;
        }
        return this;
    }

    public boolean getChecked () {
        return booleanArr[0];
    }
}

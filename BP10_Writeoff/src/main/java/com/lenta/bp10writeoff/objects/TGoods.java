package com.lenta.bp10writeoff.objects;

import android.os.Parcel;
import android.os.Parcelable;

public class TGoods implements Parcelable {

    private int sortbyID;
    private String materialNumGoods;
    private String nameGoods;
    private String nameUnit;
    private String typeGoods;
    private String typeMatrix;
    private String numberSection;
    private boolean[] booleanArr = new boolean[3];

    public TGoods(String materialNumGoods, String nameGoods, String typeGoods, String nameUnit, String typeMatrix, String numberSection, boolean excise, boolean alcohol){
        this.materialNumGoods = materialNumGoods;
        this.nameGoods = nameGoods;
        this.typeGoods = typeGoods;
        this.nameUnit = nameUnit;
        this.typeMatrix = typeMatrix;
        this.numberSection = numberSection;
        this.sortbyID = 0;
        booleanArr[0] = false; //checkedGoods
        booleanArr[1] = excise;
        booleanArr[2] = alcohol;
    }

    /**==========Parcelable=================*/
    /** битовая маска, указывающая набор специальных типов объектов, маршалируемых этим экземпляром объекта Parcelable.
     Значение либо 0, либо CONTENTS_FILE_DESCRIPTOR.*/
    public int describeContents() {
        return 0;
    }

    /** получаем на вход Parcel и упаковываем в него наш объект.*/
    public final void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(sortbyID);
        parcel.writeString(materialNumGoods);
        parcel.writeString(nameGoods);
        parcel.writeString(typeGoods);
        parcel.writeString(nameUnit);
        parcel.writeString(typeMatrix);
        parcel.writeString(numberSection);
        parcel.writeBooleanArray(booleanArr);
    }

    /** распаковываем объект из Parcel*/
    public final static Parcelable.Creator<TGoods> CREATOR
            = new Parcelable.Creator<TGoods>() {
        public TGoods createFromParcel(Parcel in) {
            return new TGoods(in);
        }

        public TGoods[] newArray(int size) {
            return new TGoods[size];
        }
    };

    /** конструктор, считывающий данные из Parcel*/
    private TGoods(Parcel parcel) {
        sortbyID = parcel.readInt();
        materialNumGoods = parcel.readString();
        nameGoods = parcel.readString();
        typeGoods = parcel.readString();
        nameUnit = parcel.readString();
        typeMatrix = parcel.readString();
        numberSection = parcel.readString();
        parcel.readBooleanArray(booleanArr);
    }
    /**==========================================================*/

    public TGoods setSortbyID (int sortbyID) {
        this.sortbyID = sortbyID;
        return this;
    }

    public int getSortbyID () {
        return sortbyID;
    }

    public String getMaterialNumGoods () {
        return materialNumGoods;
    }

    public String getNameGoods () {
        return nameGoods;
    }

    public String getNameUnit () {
        return nameUnit;
    }

    public String getTypeGoods () {
        return typeGoods;
    }

    public String getTypeMatrix () {
        return typeMatrix;
    }

    public String getNumberSection () {
        return numberSection;
    }

    public boolean getExcise () {
        return booleanArr[1];
    }

    public boolean getAlcohol () {
        return booleanArr[2];
    }

    public TGoods setChecked (boolean checked) {
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

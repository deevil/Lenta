package com.lenta.bp10writeoff.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class TTaskCard implements Parcelable {

    private String typeTask;
    private String nameTask;
    private String stock;
    private String typeMove;
    private List<String> gisControl;
    private List<String> typeGoods;

    public TTaskCard (String typeTask, String nameTask, String stock, String typeMove, List<String> gisControl, List<String> typeGoods){
        this.typeTask = typeTask;
        this.nameTask = nameTask;
        this.stock = stock;
        this.typeMove = typeMove;
        this.gisControl = gisControl;
        this.typeGoods = typeGoods;
    }

    /**==========Parcelable=================*/
    /** битовая маска, указывающая набор специальных типов объектов, маршалируемых этим экземпляром объекта Parcelable.
     Значение либо 0, либо CONTENTS_FILE_DESCRIPTOR.*/
    public int describeContents() {
        return 0;
    }

    /** получаем на вход Parcel и упаковываем в него наш объект.*/
    public final void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(typeTask);
        parcel.writeString(nameTask);
        parcel.writeString(stock);
        parcel.writeString(typeMove);
        parcel.writeStringList(gisControl);
        parcel.writeStringList(typeGoods);
    }

    /** распаковываем объект из Parcel*/
    public final static Parcelable.Creator<TTaskCard> CREATOR
            = new Parcelable.Creator<TTaskCard>() {
        public TTaskCard createFromParcel(Parcel in) {
            return new TTaskCard(in);
        }

        public TTaskCard[] newArray(int size) {
            return new TTaskCard[size];
        }
    };

    /** конструктор, считывающий данные из Parcel*/
    private TTaskCard(Parcel parcel) {
        typeTask = parcel.readString();
        nameTask = parcel.readString();
        stock = parcel.readString();
        typeMove = parcel.readString();
        gisControl = parcel.createStringArrayList();
        typeGoods = parcel.createStringArrayList();
    }
    /**==========================================================*/

    public String getTypeTask () {
        return typeTask;
    }

    public String getNameTask () {
        return nameTask;
    }

    public String getStock () {
        return stock;
    }

    public String getTypeMove () {
        return typeMove;
    }

    public List<String> getGISControl () {
        return gisControl;
    }

    public List<String> getTypeGoods () {
        return typeGoods;
    }
}

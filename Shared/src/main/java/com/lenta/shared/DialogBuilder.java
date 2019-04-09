package com.lenta.shared;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class DialogBuilder implements Parcelable {

    private Activity _activity;
    private String titleText;
    private String headText;
    private String textDialog;
    private int imageDialog = 0;
    private int button1 = 0;
    private int button2 = 0;
    private int button3 = 0;
    private int button4 = 0;
    private int button5 = 0;

    /** константы типов диалога*/
    private int typeDialog = 0;
    public final int TD_CLOSE_APP = 1;

    /** константы для кнопок нижней панели*/
    public final int BUTTON_1 = 1;
    public final int BUTTON_2 = 2;
    public final int BUTTON_3 = 3;
    public final int BUTTON_4 = 4;
    public final int BUTTON_5 = 5;

    /** конструктор */
    public DialogBuilder (Activity activity) {
        _activity = activity;
    }

    /** битовая маска, указывающая набор специальных типов объектов, маршалируемых этим экземпляром объекта Parcelable.
     Значение либо 0, либо CONTENTS_FILE_DESCRIPTOR.*/
    public int describeContents() {
        return 0;
    }

    /** получаем на вход Parcel и упаковываем в него наш объект.*/
    public final void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(titleText);
        parcel.writeString(headText);
        parcel.writeInt(imageDialog);
        parcel.writeString(textDialog);
        parcel.writeInt(button1);
        parcel.writeInt(button2);
        parcel.writeInt(button3);
        parcel.writeInt(button4);
        parcel.writeInt(button5);
        parcel.writeInt(typeDialog);
    }

    /** распаковываем объект из Parcel*/
    public final static Parcelable.Creator<DialogBuilder> CREATOR
            = new Parcelable.Creator<DialogBuilder>() {
        public DialogBuilder createFromParcel(Parcel in) {
            return new DialogBuilder(in);
        }

        public DialogBuilder[] newArray(int size) {
            return new DialogBuilder[size];
        }
    };

    /** конструктор, считывающий данные из Parcel*/
    private DialogBuilder(Parcel parcel) {
        titleText = parcel.readString();
        headText = parcel.readString();
        imageDialog = parcel.readInt();
        textDialog = parcel.readString();
        button1 = parcel.readInt();
        button2 = parcel.readInt();
        button3 = parcel.readInt();
        button4 = parcel.readInt();
        button5 = parcel.readInt();
        typeDialog = parcel.readInt();
    }

    /** методы для выбора типа диалога*/
    public final DialogBuilder setTypeDialog (int td) {
        typeDialog = td;
        return this;
    }

    public final int getTypeDialog () {
        return typeDialog;
    }

    /** методы для работы с Title*/
    public final DialogBuilder setTitleText (String string) {
        titleText = string;
        return this;
    }

    public final DialogBuilder setTitleText (int resID) {
        titleText = _activity.getResources().getString(resID);
        return this;
    }

    public final String getTitleText () {
        return titleText;
    }

    /** методы для работы с Head*/
    public final DialogBuilder setHeadText (String string) {
        headText = string;
        return this;
    }

    public final DialogBuilder setHeadText (int resID) {
        headText = _activity.getResources().getString(resID);
        return this;
    }

    public final String getHeadText () {
        return headText;
    }

    /** методы для работы с центральным изображением диалога*/
    public final DialogBuilder setImageDialog (int resID) {
        imageDialog = resID;
        return this;
    }

    public final int getImageDialog () {
        return imageDialog;
    }

    /** методы для работы с центральным текстом дилаога*/
    public final DialogBuilder setTextDialog (String string) {
        textDialog = string;
        return this;
    }

    public final DialogBuilder setTextDialog (int resID) {
        textDialog = _activity.getResources().getString(resID);
        return this;
    }

    public final String getTextDialog () {
        return textDialog;
    }

    /** методы для работы с 5 кнопками нижней панели*/
    public final DialogBuilder setImageButtonBotPanel (int button, int resID) {
        switch (button) {
            case BUTTON_1:
                button1 = resID;
                break;
            case BUTTON_2:
                button2 = resID;
                break;
            case BUTTON_3:
                button3 = resID;
                break;
            case BUTTON_4:
                button4 = resID;
                break;
            case BUTTON_5:
                button5 = resID;
                break;
            default:
                break;
        }
        return this;
    }

    /** методы для работы с 5 кнопками нижней панели*/
    public final int getImageButtonBotPanel (int button) {
        int getButton;
        switch (button) {
            case BUTTON_1:
                getButton = button1;
                break;
            case BUTTON_2:
                getButton = button2;
                break;
            case BUTTON_3:
                getButton = button3;
                break;
            case BUTTON_4:
                getButton = button4;
                break;
            case BUTTON_5:
                getButton = button5;
                break;
            default:
                getButton = 0;
            break;
        }
        return getButton;
    }

    /** метод для создания стандартного диалога TD_CLOSE_APP*/
    private void buildDlgCloseApp () {
        headText = _activity.getResources().getString(R.string.head_dialog_close_app);
        textDialog = _activity.getResources().getString(R.string.text_dialog_close_app);
        imageDialog = R.mipmap.dialog_question;
        button1 = R.mipmap.button_no;
        button2 = 0;
        button3 = 0;
        button4 = 0;
        button5 = R.mipmap.button_yes;
    }

    /** методы для отображения дилаога*/
    public final void show (boolean forResult, int requestCode) {
        switch (typeDialog) {
            case TD_CLOSE_APP:
                buildDlgCloseApp();
                break;
            default:
                break;
        }
        Intent intent = new Intent(_activity, DialogBuilderActivity.class);
        intent.putExtra(DialogBuilder.class.getCanonicalName(), this);
        if (!forResult) {
            _activity.startActivity(intent);
        } else {
            _activity.startActivityForResult(intent, requestCode);
        }
    }

    //перезагрузка метода Show
    public final void show () {
        // Значение параметра forResult по умолчанию FALSE, Значение параметра requestCode по умолчанию 1
        show(false, 1);
    }
    //перезагрузка метода Show
    public final void show (boolean forResult) {
        // Значение параметра requestCode по умолчанию 1
        show(forResult, 1);
    }
}

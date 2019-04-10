package com.lenta.shared;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Click_ThreeBtnBotPanel {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public final static boolean imgBtn_onClick(Context context, Activity activity, InputMethodManager imm, View btnClick, ImageButton KeyOpenBtnImg, ImageButton UpDownBtnImg, ImageButton ShowBotPanelBtnImg, ConstraintLayout BottomPanel, boolean lastOpenBotPanel, boolean keyboardEnable)
    {
        boolean boolOpenBotPanel = lastOpenBotPanel;

        int btnClickID = btnClick.getId();

        if (btnClickID == KeyOpenBtnImg.getId()) {
            //устанавливаем, что послдней была открыта клавиатура
            boolOpenBotPanel = false;
            //скрываем нижнюю панель
            BottomPanel.setVisibility(View.GONE);
            //меняем стрелки вверх на вниз у кнопки UpDownBtnImg
            UpDownBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("arrow_down", "mipmap", context.getPackageName())));
            //открываем клавиатуру, если она используется в данном активити
            if (keyboardEnable) {
                imm.showSoftInput(activity.getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
                //делаем кнопку отображения нижней панели активной и меняем ей картинку
                ShowBotPanelBtnImg.setEnabled(true);
                ShowBotPanelBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("buttons_enabled", "mipmap", context.getPackageName())));
            }
            //делаем кнопку неактивной и меняем ей картинку
            KeyOpenBtnImg.setEnabled(false);
            KeyOpenBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("keyboard_disabled", "mipmap", context.getPackageName())));

        } else if (btnClickID == UpDownBtnImg.getId()) { //СКРЫВАЕМ/ПОКАЗЫВАЕМ НИЖНЮЮ панель/клавиатуру
            if ((KeyOpenBtnImg.isEnabled() || !keyboardEnable) && ShowBotPanelBtnImg.isEnabled()) { //значит и клавиатура и панель свернуты
                if (lastOpenBotPanel) { //значит последней была открыта панель, ее и отображаем
                    imgBtn_onClick(context,
                            activity,
                            imm,
                            ShowBotPanelBtnImg,
                            KeyOpenBtnImg,
                            UpDownBtnImg,
                            ShowBotPanelBtnImg,
                            BottomPanel,
                            boolOpenBotPanel,
                            keyboardEnable
                    );
                } else { //иначе последней была открыта клавиатура, ее и отображаем
                    imgBtn_onClick(context,
                            activity,
                            imm,
                            KeyOpenBtnImg,
                            KeyOpenBtnImg,
                            UpDownBtnImg,
                            ShowBotPanelBtnImg,
                            BottomPanel,
                            boolOpenBotPanel,
                            keyboardEnable
                    );
                }
            } else { //иначе открыта либо клавиатура, либо панель, сворачиваем все
                //скрываем нижнюю панель
                BottomPanel.setVisibility(View.GONE);
                //скрываем клавиатуру, если она используется в данном активити
                if (keyboardEnable) {
                    imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                    //делаем кнопку отображения клавиатуры активной и меняем ей картинку
                    KeyOpenBtnImg.setEnabled(true);
                    KeyOpenBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("keyboard_enabled", "mipmap", context.getPackageName())));
                }
                //меняем стрелки вниз на вверх у кнопки UpDownBtnImg
                UpDownBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("arrow_up", "mipmap", context.getPackageName())));
                //делаем кнопку отображения нижней панели активной и меняем ей картинку
                ShowBotPanelBtnImg.setEnabled(true);
                ShowBotPanelBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("buttons_enabled", "mipmap", context.getPackageName())));
            }

        } else if (btnClickID == ShowBotPanelBtnImg.getId()) { //ПОКАЗЫВАЕМ НИЖНЮЮ ПАНЕЛЬ
            //устанавливаем, что послдней была открыта нижняя панель
            boolOpenBotPanel = true;
            //скрываем клавиатуру
            if (keyboardEnable) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                //делаем кнопку отображения клавиатуры активной и меняем ей картинку
                KeyOpenBtnImg.setEnabled(true);
                KeyOpenBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("keyboard_enabled", "mipmap", context.getPackageName())));
            }
            //показываем нижнюю панель
            BottomPanel.setVisibility(View.VISIBLE);
            //меняем стрелки вверх на вниз у кнопки UpDownBtnImg
            UpDownBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("arrow_down", "mipmap", context.getPackageName())));
            //делаем кнопку неактивной и меняем ей картинку
            ShowBotPanelBtnImg.setEnabled(false);
            ShowBotPanelBtnImg.setImageDrawable(context.getDrawable(context.getResources().getIdentifier("buttons_disabled", "mipmap", context.getPackageName())));

        }

        return boolOpenBotPanel;

    }

}

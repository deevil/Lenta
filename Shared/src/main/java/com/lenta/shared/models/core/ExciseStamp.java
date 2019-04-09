package com.lenta.shared.models.core;

import lombok.Getter;

public class ExciseStamp implements IExciseStamp {

    @Getter private String materialNumber;
    @Getter private String code;

    public ExciseStamp(String materialNumber, String code)
    {
        this.materialNumber = materialNumber;
        this.code = code;
    }

    @Override
    public int egaisVersion() {
        return getEgaisVersion(code);
    }

    public static int getEgaisVersion(String code)
    {
        switch (code.length())
        {
            case EgaisStampVersion.V2:
                return EgaisStampVersion.V2;
            case EgaisStampVersion.V3:
                return EgaisStampVersion.V3;
            default:
                return EgaisStampVersion.UNKNOWN;
        }
    }

}

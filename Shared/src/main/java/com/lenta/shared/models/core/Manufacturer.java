package com.lenta.shared.models.core;

import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class Manufacturer {

    public static final Manufacturer DEFAULT = new Manufacturer(null, "Нет производителя");

    @JsonProperty("ZPROD")
    @Getter public String code;

    @JsonProperty("PROD_NAME")
    @Getter public String name;

    public Manufacturer(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || getClass()!= obj.getClass())
        {
            return false;
        }

        Manufacturer manufacturer =(Manufacturer)obj;
        if (manufacturer == null)
        {
            return false;
        }
        return equals(manufacturer);
    }

    public boolean equals(Manufacturer manufacturer)
    {
        if (manufacturer == null)
        {
            return false;
        }
        return code.equals(manufacturer.code);
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }
}

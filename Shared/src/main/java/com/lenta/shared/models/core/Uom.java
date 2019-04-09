package com.lenta.shared.models.core;

import lombok.Getter;

public class Uom {

    public static final Uom DEFAULT = new Uom("ST", "шт");

    @Getter private String code;
    @Getter private String name;

    public Uom(String code, String name)
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
    public boolean equals(Object obj)
    {
        if (obj == null || getClass()!= obj.getClass())
        {
            return false;
        }

        Uom uom =(Uom)obj;
        if (uom == null)
        {
            return false;
        }
        return equals(uom);
    }

    public boolean equals(Uom uom)
    {
        if (uom == null)
        {
            return false;
        }
        return code.equals(uom.code);
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

}

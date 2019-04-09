package com.lenta.shared.models.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Supplier {

    public static final Supplier DEFAULT = new Supplier(null, "Нет поставщика");
    public static final Supplier DEFAULT_SELECT = new Supplier(null, "Выберите поставщика (кредитора)");
    public static final Supplier DEFAULT_ALL = new Supplier(null, "Все поставщики");

    @JsonProperty("LIFNR")
    public String code;

    @JsonProperty("LIFNR_NAME")
    public String name;

    public Supplier(String code, String name)
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

        Supplier supplier =(Supplier)obj;
        if (supplier == null)
        {
            return false;
        }
        return equals(supplier);
    }

    public boolean equals(Supplier supplier)
    {
        if (supplier == null)
        {
            return false;
        }
        return code.equals(supplier.code);
    }

    @Override
    public int hashCode()
    {
        return code.hashCode();
    }

}

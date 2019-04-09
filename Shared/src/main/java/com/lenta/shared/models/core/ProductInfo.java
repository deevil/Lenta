package com.lenta.shared.models.core;

import lombok.Getter;

public class ProductInfo implements IProduct {

    @Getter private String materialNumber;
    @Getter private String description;
    @Getter private Uom uom;
    @Getter private ProductType type;
    @Getter private boolean isSet;
    @Getter private int sectionNumber;
    @Getter private MatrixType matrixType;
    @Getter private String materialType;

    public ProductInfo(String materialNumber, String description, Uom uom, ProductType type,
                       boolean isSet, int sectionNumber, MatrixType matrixType, String materialType)
    {
        this.materialNumber = materialNumber;
        this.description = description;
        this.uom = uom;
        this.type = type;
        this.isSet = isSet;
        this.sectionNumber = sectionNumber;
        this.matrixType = matrixType;
        this.materialType = materialType;
    }

    public ProductInfo(IProduct product)
    {
        this.materialNumber =product.getMaterialNumber();
        this.description = product.getDescription();
        this.uom = product.getUom();
        this.type = product.getType();
        this.isSet = product.isSet();
        this.sectionNumber = product.getSectionNumber();
        this.matrixType = product.getMatrixType();
        this.materialType = product.getMaterialType();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || getClass()!= obj.getClass())
        {
            return false;
        }

        ProductInfo pi =(ProductInfo)obj;
        if (pi == null)
        {
            return false;
        }
        return equals(pi);
    }

    public boolean equals(ProductInfo pi)
    {
        if (pi == null)
        {
            return false;
        }
        return pi.materialNumber == materialNumber
                && pi.uom.equals(uom)
                && pi.type == type
                && pi.isSet == isSet;
    }

    @Override
    public int hashCode()
    {
        return materialNumber.hashCode() * 197 +
                uom.hashCode() * 197 +
                type.hashCode() * 197/** +
                isSet.hashCode() * 197*/;
    }

    @Override
    public String getMaterialLastSix() {
        return materialNumber.length() > 6
                ? materialNumber.substring(materialNumber.length() - 6)
                : materialNumber;
    }
}

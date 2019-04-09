package com.lenta.shared.models.core;

public interface IProduct {
    String getMaterialNumber();
    String getMaterialLastSix();
    String getDescription();
    Uom getUom();
    ProductType getType();
    boolean isSet();
    int getSectionNumber();
    MatrixType getMatrixType();
    String getMaterialType();
}

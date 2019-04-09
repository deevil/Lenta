package com.lenta.shared.models.core;

import java.util.Date;

public interface IBatch {
    String getBatchNumber();
    Manufacturer getManufacturer();
    Date getBottlingDate();
    int getCount();
}

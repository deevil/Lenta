package com.lenta.shared.models.core;

import java.util.Date;

import lombok.Getter;

public class Batch implements IBatch {

    @Getter private String batchNumber;
    @Getter private Manufacturer manufacturer;
    @Getter private Date bottlingDate;
    @Getter private int count;

    public Batch(String batchNumber, Manufacturer manufacturer, Date bottlingDate, int countInBatch)
    {
        this.batchNumber = batchNumber;
        this.manufacturer = manufacturer;
        this.bottlingDate = bottlingDate;
        this.count = countInBatch;
    }

    public static Batch setBottlingDate(Batch batch, Date bottlingDate)
    {
        return new Batch(batch.batchNumber, batch.manufacturer, bottlingDate, batch.count);
    }

}

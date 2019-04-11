package com.lenta.lentabp10.models.repositories;

import com.lenta.shared.models.core.IProduct;
import com.lenta.shared.models.core.ProductInfo;

import java.util.List;

public interface ITaskProductRepository {
    List<ProductInfo> getProducts();
    ProductInfo findProduct(IProduct product);
    boolean addProduct(ProductInfo product);
    boolean deleteProduct(ProductInfo product);
    void clear();
    ProductInfo get(int index);
    int lenght();
}

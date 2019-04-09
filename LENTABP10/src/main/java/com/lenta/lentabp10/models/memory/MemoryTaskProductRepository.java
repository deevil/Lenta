package com.lenta.lentabp10.models.memory;

import com.lenta.lentabp10.models.repositories.ITaskProductRepository;
import com.lenta.shared.models.core.IProduct;
import com.lenta.shared.models.core.ProductInfo;

import java.util.ArrayList;
import java.util.List;

public class MemoryTaskProductRepository implements ITaskProductRepository {

    protected final List<ProductInfo> productInfo = new ArrayList<>();

    public MemoryTaskProductRepository() {
    }

    @Override
    public ProductInfo findProduct(IProduct product) {
        for(int i=0; i<productInfo.size(); i++) {
            if ( product.getMaterialNumber() == productInfo.get(i).getMaterialNumber() ) {
                return productInfo.get(i);
            }
        }
        return null;
    }

    @Override
    public boolean addProduct(ProductInfo product) {
        if (product == null)
        {
            throw new NullPointerException("product");
        }

        int index = -1;
        for(int i=0; i<productInfo.size(); i++) {
            if ( product.getMaterialNumber() == productInfo.get(i).getMaterialNumber() ) {
                index = i;
            }
        }

        if (index == -1)
        {
            productInfo.add(product);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteProduct(ProductInfo product) {
        int index = -1;
        for(int i=0; i<productInfo.size(); i++) {
            if ( product.getMaterialNumber() == productInfo.get(i).getMaterialNumber() ) {
                index = i;
            }
        }

        if (index == -1)
        {
            return false;
        }

        productInfo.remove(index);
        return true;
    }

    @Override
    public void clear() {
        productInfo.clear();
    }

    @Override
    public ProductInfo get(int index) {
        return productInfo.get(index);
    }

    @Override
    public int lenght() {
        return productInfo.size();
    }
}

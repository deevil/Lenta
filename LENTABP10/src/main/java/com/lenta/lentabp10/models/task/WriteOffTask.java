package com.lenta.lentabp10.models.task;

import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.shared.models.core.IProduct;
import com.lenta.shared.models.core.ProductInfo;
import com.lenta.shared.models.core.ProductType;

import java.util.List;

public class WriteOffTask {

    private TaskDescription taskDescription;
    private ITaskRepository taskRepository;

    public WriteOffTask(TaskDescription taskDescription, ITaskRepository taskRepository){
        this.taskDescription = taskDescription;
        this.taskRepository = taskRepository;
    }


    public WriteOffTask deleteProducts(List<ProductInfo> products) {
        //todo (Артем И., 09.04.2019) удалить перечень продуктов (products), причины списания и марки
        for(int i=0; i<products.size(); i++) {
            taskRepository.getExciseStamps().deleteExciseStampsForProduct(products.get(i));
            taskRepository.getWriteOffReasons().deleteWriteOffReasonsForProduct(products.get(i));
            taskRepository.getProducts().deleteProduct(products.get(i));
        }
        return this;
    }

    public ProcessGeneralProductService processGeneralProduct(IProduct product) {
        //todo (Артем И., 09.04.2019) search product taskRepository если есть то (проверяем, что обычный продукт - не алкоголь) create ProcessGeneralProductService
        //todo (Артем И., 10.04.2019) поиск продукта в репозитории не делать, проверять только тип товара на General и возвращать ProcessGeneralProductService
        if (product.getType() == ProductType.General) {
            return new ProcessGeneralProductService(taskDescription, taskRepository, (ProductInfo) product);
        }

        return null;
    }

    public ProcessNonExciseAlcoProductService processNonExciseAlcoProduct(IProduct product) {
        //todo (Артем И., 11.04.2019) тоже самое, что и в ProcessGeneralProductService
        if (product.getType() == ProductType.NonExciseAlcohol) {
            return new ProcessNonExciseAlcoProductService(taskDescription, taskRepository, (ProductInfo) product);
        }

        return null;
    }

    public ProcessExciseAlcoProductService processExciseAlcoProduct(IProduct product) {
        //todo (Артем И., 11.04.2019) тоже самое, что и в ProcessGeneralProductService
        if (product.getType() == ProductType.ExciseAlcohol) {
            return new ProcessExciseAlcoProductService(taskDescription, taskRepository, (ProductInfo) product);
        }

        return null;
    }

    public List<ProductInfo> getProcessedProducts(){
        return taskRepository.getProducts().getProducts();
    }

    public int getProductCount(){
        //todo (Артем И., 11.04.2019) данный метод пока оставить так
        return taskRepository.getProducts().lenght();
    }

    public double getTotalCountOfProduct(ProductInfo product) {
        //todo считать ИТОГО причин списания, а для акцизного товара ИТОГО + кол-во марок
        double totalCount;
        switch (product.getType()) {
            case General:
                totalCount = processGeneralProduct(product).getTotalCount();
                break;
            case NonExciseAlcohol:
                totalCount = processNonExciseAlcoProduct(product).getTotalCount();
                break;
            case ExciseAlcohol:
                totalCount = processExciseAlcoProduct(product).getTotalCount();
                break;
            default:
                totalCount = 0;
                break;
        }

        return totalCount;
    }

    public TaskSaveModel getTaskSaveModel() {
        return new TaskSaveModel(taskDescription, taskRepository);
    }

    void clearTask() {
        //todo (Артем И., 11.04.2019) очистить все репозитории, taskDescription не очищать
        taskRepository.getProducts().clear();
        taskRepository.getWriteOffReasons().clear();
        taskRepository.getExciseStamps().clear();
    }
}

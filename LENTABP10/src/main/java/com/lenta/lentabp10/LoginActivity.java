package com.lenta.lentabp10;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.lenta.lentabp10.models.memory.MemoryTaskExciseStampRepository;
import com.lenta.lentabp10.models.memory.MemoryTaskProductRepository;
import com.lenta.lentabp10.models.memory.MemoryTaskRepository;
import com.lenta.lentabp10.models.memory.MemoryTaskWriteOffReasonRepository;
import com.lenta.lentabp10.models.repositories.ITaskExciseStampRepository;
import com.lenta.lentabp10.models.repositories.ITaskProductRepository;
import com.lenta.lentabp10.models.repositories.ITaskRepository;
import com.lenta.lentabp10.models.repositories.ITaskWriteOffReasonRepository;
import com.lenta.lentabp10.models.task.TaskDescription;
import com.lenta.lentabp10.models.task.TaskType;
import com.lenta.lentabp10.models.task.WriteOffReason;
import com.lenta.lentabp10.models.task.WriteOffTask;
import com.lenta.shared.models.core.MatrixType;
import com.lenta.shared.models.core.ProductInfo;
import com.lenta.shared.models.core.ProductType;
import com.lenta.shared.models.core.Uom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    String tmpSTR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        List<String> typeMove = new ArrayList<>();
        typeMove.add("949ВД");
        TaskDescription taskDescription = new TaskDescription(new TaskType("СГП", "nСГП"),
                                                    "Списание от 04.06 10:23",
                                                        "0002",
                                                              typeMove,
                                                              new ArrayList<>(Arrays.asList("N")),
                                                              new ArrayList<>(Arrays.asList("2FER", "3ROH"))
        );

        ITaskProductRepository taskProductRepository = new MemoryTaskProductRepository();
        ITaskExciseStampRepository taskExciseStampRepository = new MemoryTaskExciseStampRepository();
        ITaskWriteOffReasonRepository taskWriteOfReasonRepository = new MemoryTaskWriteOffReasonRepository();
        ITaskRepository taskRepository = new MemoryTaskRepository(taskProductRepository, taskExciseStampRepository, taskWriteOfReasonRepository);

        WriteOffTask task = new WriteOffTask(taskDescription, taskRepository);

        ProductInfo product1 = new ProductInfo("materialNumber1", "description", new Uom("ST", "шт"), ProductType.General,
                false, 1, MatrixType.Active, "materialType");
        ProductInfo product2 = new ProductInfo("materialNumber2", "description", new Uom("ST", "шт"), ProductType.General,
                false, 2, MatrixType.Active, "materialType");
        ProductInfo product3 = new ProductInfo("materialNumber2", "description", new Uom("ST", "шт"), ProductType.General,
                false, 2, MatrixType.Active, "materialType");
        WriteOffReason reason1 = new WriteOffReason("01", "Срок годности");
        WriteOffReason reason2 = new WriteOffReason("02", "Срок негодности");

        /**task = task
                .processGeneralProduct(product1)
                .add(reason1, 1)
                .apply();*/


        TextView TextView = findViewById(R.id.TextView);
        TextView.setText(taskDescription.getTaskType().getCode());

        /**=============================MemoryTaskProductRepository===================================*/
/**        tmpSTR = "MemoryTaskProductRepository:";


        ITaskProductRepository taskProductRepository = new MemoryTaskProductRepository();
        //addProduct
        if (taskProductRepository.addProduct(product1)) {
            tmpSTR = tmpSTR + "\naddProduct1=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\naddProduct1=FALSE";
        }
        if (taskProductRepository.addProduct(product2)) {
            tmpSTR = tmpSTR + "\naddProduct2=TRUE";
        }
        else {
            tmpSTR =  tmpSTR + "\naddProduct2=FALSE";
        }
        if (taskProductRepository.addProduct(product3)) {
            tmpSTR = tmpSTR + "\naddProduct3=TRUE";
        }
        else {
            tmpSTR =  tmpSTR + "\naddProduct3=FALSE";
        }
        tmpSTR = tmpSTR + "\nlengthProduct=" + String.valueOf(taskProductRepository.lenght());
        TextView.setText(tmpSTR);

        //productFind
        ProductInfo productFind = taskProductRepository.findProduct(product2);
        tmpSTR = tmpSTR + "\n" + "productFind: " + productFind.getMaterialNumber();
        TextView.setText(tmpSTR);

        //productGet
        ProductInfo productGet = taskProductRepository.get(0);
        tmpSTR = tmpSTR + "\n" + "productGet1: " + productGet.getMaterialNumber();
        TextView.setText(tmpSTR);
        productGet = taskProductRepository.get(1);
        tmpSTR = tmpSTR + "\n" + "productGet2: " + productGet.getMaterialNumber();
        TextView.setText(tmpSTR);

        //deleteProduct
        if (taskProductRepository.deleteProduct(product2)) {
            tmpSTR = tmpSTR + "\ndeleteProduct=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\ndeleteProduct=FALSE";
        }
        tmpSTR = tmpSTR + "\n" + "lengthProduct=" + String.valueOf(taskProductRepository.lenght());
        TextView.setText(tmpSTR);

        //clear
        taskProductRepository.clear();
        tmpSTR = tmpSTR + "\n" + "clear -> lengthProduct=" + String.valueOf(taskProductRepository.lenght());
        TextView.setText(tmpSTR);
        /**===========================================================================================*/

        /**=============================MemoryTaskExciseStampRepository===================================*/
/**        tmpSTR = tmpSTR + "\n\nMemoryTaskExciseStampRepository:";
        TaskExciseStamp exciseStamp1 = new TaskExciseStamp("materialNumber1", "1234567890", "setMaterialNumber", "Лом/бой", false);
        TaskExciseStamp exciseStamp2 = new TaskExciseStamp("materialNumber1", "123", "setMaterialNumber", "Срок годности", false);
        TaskExciseStamp exciseStamp3 = new TaskExciseStamp("materialNumber1", "1234567890", "setMaterialNumber", "Срок годности2", false);
        TaskExciseStamp exciseStamp4 = new TaskExciseStamp("materialNumber1", "12345", "setMaterialNumber", "test", false);
        TaskExciseStamp exciseStamp5 = new TaskExciseStamp("materialNumber1", "1234567890", "setMaterialNumber", "Лом/бой", false);

        ITaskExciseStampRepository taskExciseStampRepository = new MemoryTaskExciseStampRepository();

        //addExciseStamp
        if (taskExciseStampRepository.addExciseStamp(exciseStamp1)) {
            tmpSTR = tmpSTR + "\naddExciseStamp1=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\naddExciseStamp1=FALSE";
        }
        if (taskExciseStampRepository.addExciseStamp(exciseStamp2)) {
            tmpSTR = tmpSTR + "\naddExciseStamp2=TRUE";
        }
        else {
            tmpSTR =  tmpSTR + "\naddExciseStamp2=FALSE";
        }
        if (taskExciseStampRepository.addExciseStamp(exciseStamp3)) {
            tmpSTR = tmpSTR + "\naddExciseStamp3=TRUE";
        }
        else {
            tmpSTR =  tmpSTR + "\naddExciseStamp3=FALSE";
        }
        if (taskExciseStampRepository.addExciseStamp(exciseStamp4)) {
            tmpSTR = tmpSTR + "\naddExciseStamp4=TRUE";
        }
        else {
            tmpSTR =  tmpSTR + "\naddExciseStamp4=FALSE";
        }
        tmpSTR = tmpSTR + "\nlengthExciseStamp=" + String.valueOf(taskExciseStampRepository.lenght());

        //findExciseStampsOfProduct
        List<TaskExciseStamp> foundExciseStamps = taskExciseStampRepository.findExciseStampsOfProduct(product1);
        tmpSTR = tmpSTR + "\nfindExciseStampsOfProduct: ";
        for (int i=0; i < foundExciseStamps.size(); i++) {
            tmpSTR = tmpSTR + foundExciseStamps.get(i).getWriteOffReason()+"; ";
        }

        //deleteExciseStamp
        if (taskExciseStampRepository.deleteExciseStamp(exciseStamp2)) {
            tmpSTR = tmpSTR + "\ndeleteExciseStamp=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\ndeleteExciseStamp=FALSE";
        }
        tmpSTR = tmpSTR + "\n" + "lengthExciseStamp=" + String.valueOf(taskExciseStampRepository.lenght());
        foundExciseStamps = taskExciseStampRepository.findExciseStampsOfProduct(product1);
        tmpSTR = tmpSTR + "\nfindExciseStampsOfProduct: ";
        for (int i=0; i < foundExciseStamps.size(); i++) {
            tmpSTR = tmpSTR + foundExciseStamps.get(i).getWriteOffReason()+"; ";
        }

        //deleteExciseStampsForProduct
        if (taskExciseStampRepository.deleteExciseStampsForProduct(product1)) {
            tmpSTR = tmpSTR + "\ndeleteExciseStampsForProduct=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\ndeleteExciseStampsForProduct=FALSE";
        }
        tmpSTR = tmpSTR + "\n" + "lengthExciseStamp=" + String.valueOf(taskExciseStampRepository.lenght());

        //addExciseStamps
        List<TaskExciseStamp> exciseStamps = new ArrayList<>();
        exciseStamps.add(exciseStamp1);
        exciseStamps.add(exciseStamp2);
        exciseStamps.add(exciseStamp3);
        exciseStamps.add(exciseStamp4);
        exciseStamps.add(exciseStamp5);
        if (taskExciseStampRepository.addExciseStamps(exciseStamps)) {
            tmpSTR = tmpSTR + "\naddExciseStamps=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\naddExciseStamps=FALSE";
        }
        tmpSTR = tmpSTR + "\n" + "lengthExciseStamp=" + String.valueOf(taskExciseStampRepository.lenght());

        //deleteExciseStamps
        List<TaskExciseStamp> delExciseStamps = new ArrayList<>();
        delExciseStamps.add(exciseStamp1);
        delExciseStamps.add(exciseStamp2);
        delExciseStamps.add(exciseStamp3);
        delExciseStamps.add(exciseStamp4);
        delExciseStamps.add(exciseStamp5);
        if (taskExciseStampRepository.deleteExciseStamps(delExciseStamps)) {
            tmpSTR = tmpSTR + "\ndeleteExciseStamps=TRUE";
        }
        else {
            tmpSTR = tmpSTR + "\ndeleteExciseStamps=FALSE";
        }
        tmpSTR = tmpSTR + "\n" + "lengthExciseStamp=" + String.valueOf(taskExciseStampRepository.lenght());

        //clear
        taskExciseStampRepository.clear();
        tmpSTR = tmpSTR + "\n" + "clear -> lengthExciseStamp=" + String.valueOf(taskExciseStampRepository.lenght());

        TextView.setText(tmpSTR);
        /**===========================================================================================*/
    }
}

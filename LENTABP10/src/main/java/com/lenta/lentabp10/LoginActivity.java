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

        TextView TextView = findViewById(R.id.TextView);

        List<String> typeMove = new ArrayList<>();
        typeMove.add("949ВД");
        TaskDescription taskDescription = new TaskDescription(new TaskType("СГП", "nСГП"),
                                                    "Списание от 04.06 10:23",
                                                        "0002",
                                                              typeMove,
                                                              new ArrayList<>(Arrays.asList("N")),
                                                              new ArrayList<>(Arrays.asList("2FER","3ROH")),
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      null
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
        WriteOffReason reason1 = new WriteOffReason("01", "Срок годности");
        WriteOffReason reason2 = new WriteOffReason("02", "Срок негодности");

        task  = task.processGeneralProduct(product1)
                .add(reason1,1)
                .apply();

        tmpSTR = String.valueOf("Кол-во продуктов(1) = " + task.getProcessedProducts().size());
        tmpSTR = tmpSTR + "\nИТОГО списано по проудкту1 = " + String.valueOf(task.getTotalCountOfProduct(product1));

        task = task.processGeneralProduct(product2)
                .add(reason1, 1)
                .add(reason2, 2)
                .apply();


        tmpSTR = tmpSTR + "\n\nКол-во продуктов(2) = " + String.valueOf(task.getProcessedProducts().size());
        tmpSTR = tmpSTR + "\nИТОГО списано по проудкту2 = " + String.valueOf(task.getTotalCountOfProduct(product2));

        TextView.setText(tmpSTR);

    }
}

package com.lenta.inventory.models.task

class TaskStorePlaceInfo(val placeCode: String,	//Код места хранения
                         val countProducts: String, //Кол-во товаров
                         val placeStat: String, //Индикатор из одной позиции
                         val lockUser: String,	//Имя пользователя
                         val lockIP: String	//IP адрес ТСД
                        ) {
}

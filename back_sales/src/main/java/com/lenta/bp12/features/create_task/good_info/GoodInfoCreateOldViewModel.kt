package com.lenta.bp12.features.create_task.good_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.GoodKind
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.model.MarkStatus
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.ExciseInfoNetRequest
import com.lenta.bp12.request.ExciseInfoParams
import com.lenta.bp12.request.GoodInfoNetRequest
import com.lenta.bp12.request.GoodInfoParams
import com.lenta.bp12.request.pojo.ProducerInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoCreateOldViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var goodInfoNetRequest: GoodInfoNetRequest

    @Inject
    lateinit var exciseInfoNetRequest: ExciseInfoNetRequest


    val task by lazy {
        manager.currentTask
    }

    val good by lazy {
        manager.currentGood
    }

    val title by lazy {
        good.map { good ->
            good?.getNameWithMaterial()
        }
    }

    val lastScannedNumber = MutableLiveData("")

    val marks = MutableLiveData(mutableListOf<Mark>())

    val isCompactMode by lazy {
        good.map { good ->
            good?.type == GoodKind.COMMON
        }
    }

    val quantityType by lazy {
        good.map { good ->
            good?.type?.let { type ->
                when {
                    type == GoodKind.EXCISE && lastScannedNumber.value?.length == Constants.EXCISE_68 -> "Партионно"
                    type == GoodKind.EXCISE -> "Марочно"
                    type == GoodKind.ALCOHOL -> "Партионно"
                    else -> "Количество"
                }
            }
        }
    }

    val markScanEnabled by lazy {
        good.map { good ->
            good?.type == GoodKind.EXCISE
        }
    }

    // todo Добавить текущую позицию?
    val quantity by lazy {
        good.map { good ->
            if (good?.isBox() == true) good.innerQuantity.dropZeros() else "1"
        }
    }

    val quantityEnabled by lazy {
        good.map { good ->
            good?.isBox() == false
        }
    }

    private val totalQuantity by lazy {
        quantity.map { quantity ->
            quantity?.toDoubleOrNull().sumWith(good.value?.getTotalQuantity())
        }
    }

    val totalWithUnits by lazy {
        totalQuantity.map { quantity ->
            "${quantity.dropZeros()} ${good.value?.units?.name}"
        }
    }

    private val basket by lazy {
        good.combineLatest(providerPosition).map {
            it?.first?.let { good ->
                task.value?.let { task ->
                    task.baskets.find {basket ->
                        basket.section == good.section && basket.matype == good.matype && basket.control == good.control && basket.provider == getProvider()
                    }
                }
            }
        }
    }

    val basketNumber by lazy {
        basket.map { basket ->
            val number = task.value?.baskets?.indexOf(basket) ?: -1
            if (number >= 0) "${number + 1}" else ""
        }
    }

    val basketQuantity by lazy {
        quantity.combineLatest(basket).map {
            it?.first?.let { quantity ->
                "${task.value?.getQuantityByBasket(basket.value).sumWith(quantity.toDoubleOrNull() ?: 0.0).dropZeros()} ${good.value?.units?.name}"
            }
        }
    }

    val totalTitle = MutableLiveData("Итого")

    val basketTitle = MutableLiveData("По корзине")

    private val providers by lazy {
        good.map { good ->
            good?.providers?.let { providers ->
                val list = providers.toMutableList()
                if (list.size > 1) {
                    list.add(0, ProviderInfo())
                }

                list.toList()
            }
        }
    }

    val providerList by lazy {
        providers.map { list ->
            list?.map { it.name }
        }
    }

    val providerEnabled by lazy {
        providerList.map { providers ->
            providers?.size ?: 0 > 1
        }
    }

    val providerPosition = MutableLiveData(0)

    val onSelectProvider = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            providerPosition.value = position
        }
    }

    private val producers by lazy {
        good.map { good ->
            good?.producers?.let { producers ->
                val list = producers.toMutableList()
                if (list.size > 1) {
                    list.add(0, ProducerInfo())
                }

                list.toList()
            }
        }
    }

    val producerList by lazy {
        producers.map { list ->
            list?.map { it.name }
        }
    }

    val producerEnabled by lazy {
        producers.map { producers ->
            producers?.size ?: 0 > 1
        }
    }

    val producerPosition = MutableLiveData(0)

    val onSelectProducer = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            producerPosition.value = position
        }
    }

    val date = MutableLiveData("")

    /*private val enteredDate = date.map {
        val entered = it ?: ""
        var parseDate: Date? = null

        if (entered.isNotEmpty()) {
            try {
                parseDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(entered)
            } catch (e: Exception) {
                Logg.d { "Date parse exception!" }
            }
        }

        parseDate
    }*/

    private val isCorrectDate = date.map { date ->
        date?.length ?: 0 == 10
    }

    val dateEnabled = MutableLiveData(true)

    val applyEnabled by lazy {
        quantity.combineLatest(isCorrectDate).map {
            val quantity = it?.first?.toDoubleOrNull() ?: 0.0
            val isCorrectDate = it?.second ?: false

            good.value?.let { good ->
                when (good.type) {
                    GoodKind.COMMON -> quantity > 0
                    GoodKind.ALCOHOL -> quantity > 0 && isCorrectDate
                    GoodKind.EXCISE -> quantity > 0 && isCorrectDate
                }
            }
        }
    }

    val detailsVisibility = MutableLiveData(true)

    /*val rollbackVisibility by lazy {
        good.map { good ->
            good?.kind == GoodKind.EXCISE
        }
    }*/

    val rollbackVisibility = MutableLiveData(true)

    val rollbackEnabled = MutableLiveData(true)

    var isExistUnsavedData = false

    var lastScannedMark: Mark? = null

    // -----------------------------

    init {
        viewModelScope.launch {
            checkSearchNumber(manager.searchNumber)
        }
    }

    // -----------------------------

    private fun checkSearchNumber(number: String) {
        lastScannedNumber.value = number

        number.length.let { length ->
            if (length >= Constants.SAP_6) {
                when (length) {
                    Constants.SAP_6 -> getGoodByMaterial(number)
                    Constants.SAP_18 -> getGoodByMaterial(number)
                    Constants.SAP_OR_BAR_12 -> {
                        navigator.showTwelveCharactersEntered(
                                sapCallback = { getGoodByMaterial(number) },
                                barCallback = { getGoodByEan(number) }
                        )
                    }
                    Constants.EXCISE_68 -> {
                        //loadMarkInfo(number)
                    }
                    Constants.EXCISE_150 -> {
                        Logg.d { "--> EXCISE_150 scanned!" }
                        loadMarkInfo(number)
                    }
                    else -> getGoodByEan(number)
                }
            }
        }
    }

    private fun getGoodByEan(ean: String) {
        manager.findGoodByEan(ean)?.let { good ->
            manager.updateCurrentGood(good)
        } ?: loadGoodInfo(ean = ean)
    }

    private fun getGoodByMaterial(material: String) {
        manager.findGoodByMaterial(material)?.let { good ->
            manager.updateCurrentGood(good)
        } ?: loadGoodInfo(material = material)
    }

    private fun loadGoodInfo(ean: String? = null, material: String? = null) {
        require((ean != null) || (material != null)) {
            "At least one param must be not null - ean: $ean, material: $material"
        }

        viewModelScope.launch {
            navigator.showProgressLoadingData()

            goodInfoNetRequest(GoodInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    ean = ean ?: "",
                    material = material ?: "",
                    taskType = task.value!!.properties!!.type
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { goodInfo ->
                viewModelScope.launch {
                    if (manager.isGoodCanBeAdded(goodInfo)) {
                        manager.putInCurrentGood(goodInfo)
                        isExistUnsavedData = true
                    } else {
                        navigator.showNotMatchTaskSettingsAddingNotPossible {
                            if (manager.openGoodFromList) {
                                manager.openGoodFromList = false
                                manager.searchNumber = ""
                                navigator.goBack()
                                navigator.goBack()
                            } else {
                                navigator.goBack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadMarkInfo(number: String) {
        Logg.d { "--> loadMarkInfo started!" }

        viewModelScope.launch {
            navigator.showProgressLoadingData()

            exciseInfoNetRequest(ExciseInfoParams(
                    tkNumber = sessionInfo.market ?: "Not found!",
                    material = good.value!!.material,
                    markNumber = number,
                    mode = 1,
                    quantity = 0.0
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { exciseInfo ->
                viewModelScope.launch {
                    Logg.d { "--> exciseInfo = $exciseInfo" }

                    when (exciseInfo.status) {
                        MarkStatus.OK.code -> {
                            addScannedMark()
                        }
                        MarkStatus.BAD.code -> {
                            addScannedMark(true)
                        }
                        else -> navigator.openAlertScreen(exciseInfo.statusDescription)
                    }



                    /*if (exciseInfo.marks.isEmpty()) {
                        Logg.d { "--> Марка найдена, но список пустой" }
                    } else {
                        isExistUnsavedData = true
                        exciseInfo.marks[0].let { markInfo ->
                            lastScannedMark = Mark(
                                    material = markInfo.material,
                                    markNumber = markInfo.markNumber,
                                    boxNumber = markInfo.boxNumber,
                                    isBadMark = markInfo.isBadMark.isSapTrue(),
                                    providerCode = markInfo.providerCode
                            )
                        }

                        lastScannedNumber.value?.let { number ->
                            if (number.length == Constants.EXCISE_150) {
                                saveExcise150()
                            }
                        }
                    }*/
                }
            }
        }
    }

    private fun addScannedMark(isBadMark: Boolean = false) {
        good.value?.let { good ->
            /*marks.value?.add(Mark(
                    material = good.material,
                    markNumber = lastScannedNumber.value!!,

            ))*/
        }


    }

    private fun saveExcise150() {
        good.value?.let { good ->

        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.goBack()
        navigator.openAlertScreen(failure)
    }

    fun onScanResult(number: String) {
        Logg.d { "--> onScanResult number length: ${number.length}" }

        if (applyEnabled.value!! && number.length >= Constants.SAP_6) {
            saveGoodInTask()
        }

        if (number.length >= Constants.SAP_6) {
            manager.openGoodFromList = false
            manager.searchNumber = number
            checkSearchNumber(number)
        }
    }

    private fun saveGoodInTask() {
        good.value?.let { good ->
            val quantity = quantity.value?.toDoubleOrNull() ?: 0.0
            good.addPosition(quantity, getProvider(), date.value)

            manager.updateCurrentGood(good)

            if (basket.value == null) {
                manager.addBasket(Basket(
                        section = good.section,
                        matype = good.matype,
                        control = good.control,
                        provider = getProvider()
                ))
            } else {
                manager.updateCurrentBasket(basket.value)
            }
        }

        manager.addCurrentGoodInTask()
    }

    private fun getProvider(): ProviderInfo? {
        val position = providerPosition.value!!
        return  providers.value?.let { providers ->
            when (providers.size) {
                0 -> null
                1 -> providers[0]
                else -> if (position != 0) providers[position] else null
            }
        }
    }

    fun onBackPressed() {
        if (isExistUnsavedData) {
            navigator.showUnsavedDataWillBeLost {
                manager.openGoodFromList = false
                manager.searchNumber = ""
                navigator.goBack()
            }
        } else {
            navigator.goBack()
        }
    }

    fun onClickApply() {
        saveGoodInTask()
        navigator.goBack()
        navigator.openBasketGoodListScreen()
    }

    fun onClickDetails() {
        navigator.openGoodDetailsCreateScreen()
    }

    fun addProvider() {
        navigator.openAddProviderScreen()
    }

    fun onClickRollback() {

    }

    fun updateData() {
        manager.updateCurrentGood(good.value)
    }

}
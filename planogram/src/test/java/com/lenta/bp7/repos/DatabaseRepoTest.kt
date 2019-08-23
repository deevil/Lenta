package com.lenta.bp7.repos

import com.lenta.bp7.data.Enabled
import com.lenta.bp7.data.StoreRetailType
import com.lenta.bp7.data.model.EnteredCode
import com.lenta.bp7.data.model.GoodInfo
import com.lenta.shared.fmp.resources.dao_ext.*
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz23V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DatabaseRepoTest {

    companion object {
        const val EXT_PATH = "com.lenta.shared.fmp.resources.dao_ext"
    }

    private lateinit var databaseRepo: IDatabaseRepo

    private lateinit var units: ZmpUtz07V001
    private lateinit var settings: ZmpUtz14V001
    private lateinit var stores: ZmpUtz23V001
    private lateinit var productInfo: ZfmpUtz48V001
    private lateinit var barCodeInfo: ZmpUtz25V001

    private val ean = "ean"
    private val material = "material"
    private val matcode = "matcode"
    private val unitCode = "unitCode"
    private val marketNumber = "0001"

    @BeforeAll
    fun setUpp() {
        units = mockk()
        settings = mockk()
        stores = mockk()
        productInfo = mockk()
        barCodeInfo = mockk()

        databaseRepo = DatabaseRepo(
                hyperHive = mockk(),
                units = units,
                settings = settings,
                stores = stores,
                productInfo = productInfo,
                barCodeInfo = barCodeInfo)
    }

    private fun getGoodInfo(enteredCode: EnteredCode): GoodInfo {
        return GoodInfo(
                ean = "12345678",
                material = "000000000000123456",
                matcode = "123456789012",
                enteredCode = enteredCode)
    }

    private fun getEtEans(goodInfo: GoodInfo): ZmpUtz25V001.ItemLocal_ET_EANS {
        val etEans = ZmpUtz25V001.ItemLocal_ET_EANS()
        etEans.ean = goodInfo.ean
        etEans.material = goodInfo.material
        etEans.uom = goodInfo.uom.code
        etEans.umrez = 0.0
        etEans.umren = 0.0
        return etEans
    }

    private fun getEtMatnrList(goodInfo: GoodInfo): ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST {
        val etMatnrList = ZfmpUtz48V001.ItemLocal_ET_MATNR_LIST()
        etMatnrList.material = goodInfo.material
        etMatnrList.name = goodInfo.name
        etMatnrList.matcode = goodInfo.matcode
        etMatnrList.buom = goodInfo.uom.code
        return etMatnrList
    }

    @Test
    fun `Get GoodInfo with unknown ean`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz25V001Kt").apply {
            every { barCodeInfo.getEanInfo(ean) } returns (null)
            assertEquals(null, databaseRepo.getGoodInfoByEan(ean))
        }
    }

    @Test
    fun `Get GoodInfo by ean`() = runBlocking {
        val goodInfo = getGoodInfo(EnteredCode.EAN)

        mockkStatic("$EXT_PATH.ZmpUtz25V001Kt").apply {
            every { barCodeInfo.getEanInfo(goodInfo.ean) } returns (getEtEans(goodInfo))
            mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
                every { productInfo.getProductInfoByMaterial(goodInfo.material) } returns (getEtMatnrList(goodInfo))
                mockkStatic("$EXT_PATH.ZmpUtz07V001Kt").apply {
                    every { units.getUnitName(goodInfo.uom.code) } returns (goodInfo.uom.name)
                    assertEquals(goodInfo, databaseRepo.getGoodInfoByEan(goodInfo.ean))
                }
            }
        }
    }

    @Test
    fun `Get GoodInfo with unknown material`() = runBlocking {
        mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
            every { productInfo.getProductInfoByMaterial(material) } returns (null)
            assertEquals(null, databaseRepo.getGoodInfoByMaterial(material))
        }
    }

    @Test
    fun `Get GoodInfo by material`() = runBlocking {
        val goodInfo = getGoodInfo(EnteredCode.MATERIAL)

        mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
            every { productInfo.getProductInfoByMaterial(goodInfo.material) } returns (getEtMatnrList(goodInfo))
            mockkStatic("$EXT_PATH.ZmpUtz25V001Kt").apply {
                every { barCodeInfo.getEanInfoFromMaterial(goodInfo.material) } returns (getEtEans(goodInfo))
                mockkStatic("$EXT_PATH.ZmpUtz07V001Kt").apply {
                    every { units.getUnitName(goodInfo.uom.code) } returns (goodInfo.uom.name)


                    assertEquals(goodInfo, databaseRepo.getGoodInfoByMaterial(goodInfo.material))
                }
            }
        }
    }

    @Test
    fun `Get GoodInfo with unknown matcode`() = runBlocking {
        mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
            every { productInfo.getProductInfoByMatcode(matcode) } returns (null)
            assertEquals(null, databaseRepo.getGoodInfoByMatcode(matcode))
        }
    }

    @Test
    fun `Get GoodInfo by matcode`() = runBlocking {
        val goodInfo = getGoodInfo(EnteredCode.MATCODE)

        mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
            every { productInfo.getProductInfoByMatcode(goodInfo.matcode) } returns (getEtMatnrList(goodInfo))
            mockkStatic("$EXT_PATH.ZmpUtz25V001Kt").apply {
                every { barCodeInfo.getEanInfoFromMaterial(goodInfo.material) } returns (getEtEans(goodInfo))
                mockkStatic("$EXT_PATH.ZmpUtz07V001Kt").apply {
                    every { units.getUnitName(goodInfo.uom.code) } returns (goodInfo.uom.name)
                    assertEquals(goodInfo, databaseRepo.getGoodInfoByMatcode(goodInfo.matcode))
                }
            }
        }
    }

    @Test
    fun `Get EanInfo by ean`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz25V001Kt").apply {
            every { barCodeInfo.getEanInfo(ean) } returns (null)
            databaseRepo.getEanInfoByEan(ean)
            verify { barCodeInfo.getEanInfo(ean) }
        }
    }

    @Test
    fun `Get EanInfo by material`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz25V001Kt").apply {
            every { barCodeInfo.getEanInfoFromMaterial(material) } returns (null)
            databaseRepo.getEanInfoByMaterial(material)
            verify { barCodeInfo.getEanInfoFromMaterial(material) }
        }
    }

    @Test
    fun `Get ProductInfo by material`() = runBlocking {
        mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
            every { productInfo.getProductInfoByMaterial(material) } returns (null)
            databaseRepo.getProductInfoByMaterial(material)
            verify { productInfo.getProductInfoByMaterial(material) }
        }
    }

    @Test
    fun `Get ProductInfo by matcode`() = runBlocking {
        mockkStatic("$EXT_PATH.ZfmpUtz48V001Kt").apply {
            every { productInfo.getProductInfoByMatcode(matcode) } returns (null)
            databaseRepo.getProductInfoByMatcode(matcode)
            verify { productInfo.getProductInfoByMatcode(matcode) }
        }
    }

    @Test
    fun `Get good unit name`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz07V001Kt").apply {
            every { units.getUnitName(unitCode) } returns (null)
            databaseRepo.getGoodUnitName(unitCode)
            verify { units.getUnitName(unitCode) }
        }
    }

    @Test
    fun `Get store retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns (null)
            databaseRepo.getRetailType(marketNumber)
            verify { stores.getRetailType(marketNumber) }
        }
    }

    @Test
    fun `Get facings param with hyper retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns (StoreRetailType.HYPER.type)
            mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
                every { settings.getFacingsHyperParam() } returns (null)
                databaseRepo.getFacingsParam(marketNumber)
                verify { settings.getFacingsHyperParam() }
            }
        }
    }

    @Test
    fun `Get facings param with super retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns (StoreRetailType.SUPER.type)
            mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
                every { settings.getFacingsSuperParam() } returns (null)
                databaseRepo.getFacingsParam(marketNumber)
                verify { settings.getFacingsSuperParam() }
            }
        }
    }

    @Test
    fun `Get facings param with unknown retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns ("U")
            assertEquals(Enabled.NO.type, databaseRepo.getFacingsParam(marketNumber))
        }
    }

    @Test
    fun `Get places param with hyper retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns (StoreRetailType.HYPER.type)
            mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
                every { settings.getPlacesHyperParam() } returns (null)
                databaseRepo.getPlacesParam(marketNumber)
                verify { settings.getPlacesHyperParam() }
            }
        }
    }

    @Test
    fun `Get places param with super retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns (StoreRetailType.SUPER.type)
            mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
                every { settings.getPlacesSuperParam() } returns (null)
                databaseRepo.getPlacesParam(marketNumber)
                verify { settings.getPlacesSuperParam() }
            }
        }
    }

    @Test
    fun `Get places param with unknown retail type`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz23V001Kt").apply {
            every { stores.getRetailType(marketNumber) } returns ("U")
            assertEquals(Enabled.NO.type, databaseRepo.getPlacesParam(marketNumber))
        }
    }

    @Test
    fun `Get self control pin code`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
            every { settings.getSelfControlPinCode() } returns (null)
            databaseRepo.getSelfControlPinCode()
            verify { settings.getSelfControlPinCode() }
        }
    }

    @Test
    fun `Get external audit pin code`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
            every { settings.getExternalAuditPinCode() } returns (null)
            databaseRepo.getExternalAuditPinCode()
            verify { settings.getExternalAuditPinCode() }
        }
    }

    @Test
    fun `Get allowed app version`() = runBlocking {
        mockkStatic("$EXT_PATH.ZmpUtz14V001Kt").apply {
            every { settings.getAllowedPleAppVersion() } returns (null)
            databaseRepo.getAllowedAppVersion()
            verify { settings.getAllowedPleAppVersion() }
        }
    }

}
package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.FileProvider
import com.example.data.AppDatabase
import com.example.data.entity.CatalogItemEntity
import com.example.data.entity.ClientEntity
import com.example.data.entity.ComboPackageEntity
import com.example.data.entity.DocumentEntity
import com.example.data.entity.DocumentItemEntity
import com.example.data.entity.DocumentWithItems
import com.example.data.repository.BusinessSettings
import com.example.data.repository.CatalogRepository
import com.example.data.repository.ClientRepository
import com.example.data.repository.ComboPackageRepository
import com.example.data.repository.DocumentRepository
import com.example.data.repository.SettingsRepository
import com.example.pdf.PdfGenerator
import com.example.util.LanguageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DraftItem(
    val id: Long = System.currentTimeMillis(),
    val itemName: String,
    val unit: String,
    val quantity: Double = 1.0,
    val rate: Double = 0.0,
    val amount: Double = quantity * rate
)

data class ActiveDocumentState(
    val editingDocId: Long? = null,
    val documentType: String = "INVOICE", // "INVOICE" or "QUOTATION"
    val documentNumber: String = "",
    val date: String = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
    val dueDate: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val items: List<DraftItem> = emptyList(),
    val discountType: String = "FIXED", // "FIXED" or "PERCENTAGE"
    val discountValue: Double = 0.0,
    val taxEnabled: Boolean = false,
    val taxPercentage: Double = 17.0, // Default GST percentage in PK
    val notes: String = "",
    val paymentStatus: String = "Unpaid", // "Unpaid", "Partial", "Paid"
    val amountPaid: Double = 0.0,
    val sitePhotos: List<String> = emptyList(), // URI strings
    val generatedPdfFile: File? = null,
    val generatedPdfUri: Uri? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.amount }

    val discount: Double
        get() = if (discountType == "PERCENTAGE") {
            (subtotal * (discountValue / 100.0)).coerceAtLeast(0.0)
        } else {
            discountValue.coerceAtLeast(0.0)
        }

    val taxAmount: Double
        get() = if (taxEnabled) (subtotal - discount).coerceAtLeast(0.0) * (taxPercentage / 100.0) else 0.0

    val grandTotal: Double
        get() = (subtotal - discount + taxAmount).coerceAtLeast(0.0)
}

data class MonthlySummary(
    val invoiceCount: Int = 0,
    val totalInvoiced: Double = 0.0,
    val totalReceived: Double = 0.0,
    val totalPending: Double = 0.0,
    val monthName: String = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(Date())
) {
    val totalInvoices: Int get() = invoiceCount
    val totalAmountInvoiced: Double get() = totalInvoiced
    val totalAmountReceived: Double get() = totalReceived
    val totalPendingBalance: Double get() = totalPending
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val documentRepository = DocumentRepository(db.documentDao())
    private val catalogRepository = CatalogRepository(db.catalogDao())
    private val clientRepository = ClientRepository(db.clientDao())
    private val comboPackageRepository = ComboPackageRepository(db.comboPackageDao())
    private val settingsRepository = SettingsRepository(application)

    val settings: StateFlow<BusinessSettings> = settingsRepository.settings

    val clients: StateFlow<List<ClientEntity>> = clientRepository.allClients
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val comboPackages: StateFlow<List<ComboPackageEntity>> = comboPackageRepository.allPackages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow("ALL") // "ALL", "INVOICE", "QUOTATION"
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private val _isAppUnlocked = MutableStateFlow(true)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    val catalogItems: StateFlow<List<CatalogItemEntity>> = catalogRepository.allCatalogItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allDocuments: StateFlow<List<DocumentWithItems>> = combine(
        documentRepository.allDocuments,
        _searchQuery,
        _filterType
    ) { docs, query, filter ->
        docs.filter { docWithItems ->
            val doc = docWithItems.document
            val matchesQuery = query.isBlank() ||
                    doc.clientName.contains(query, ignoreCase = true) ||
                    doc.documentNumber.contains(query, ignoreCase = true) ||
                    doc.date.contains(query, ignoreCase = true) ||
                    doc.clientPhone.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "INVOICE" -> doc.documentType.equals("INVOICE", ignoreCase = true)
                "QUOTATION" -> doc.documentType.equals("QUOTATION", ignoreCase = true)
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val monthlySummary: StateFlow<MonthlySummary> = allDocuments.map { docs ->
        val currentMonthYear = SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(Date())
        val currentMonthDocs = docs.filter { docWithItems ->
            val doc = docWithItems.document
            doc.documentType.equals("INVOICE", ignoreCase = true) && doc.date.contains(currentMonthYear.substring(0, 3), ignoreCase = true)
        }

        var count = 0
        var invoiced = 0.0
        var received = 0.0
        var pending = 0.0

        currentMonthDocs.forEach { docWithItems ->
            val doc = docWithItems.document
            count++
            invoiced += doc.grandTotal
            received += doc.amountPaid
            val remaining = (doc.grandTotal - doc.amountPaid).coerceAtLeast(0.0)
            pending += remaining
        }

        MonthlySummary(
            invoiceCount = count,
            totalInvoiced = invoiced,
            totalReceived = received,
            totalPending = pending
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlySummary()
    )

    private val _activeDocument = MutableStateFlow(ActiveDocumentState())
    val activeDocument: StateFlow<ActiveDocumentState> = _activeDocument.asStateFlow()

    init {
        viewModelScope.launch {
            catalogRepository.checkAndSeedCatalog()
            LanguageHelper.setLanguage(settings.value.language)
            _isAppUnlocked.value = settings.value.appPin.isNullOrEmpty()
        }
    }

    fun verifyPin(pin: String): Boolean {
        val currentPin = settings.value.appPin
        if (currentPin.isNullOrEmpty() || currentPin == pin) {
            _isAppUnlocked.value = true
            return true
        }
        return false
    }

    fun setAppPin(currentPinInput: String?, newPin: String?): Boolean {
        val savedPin = settings.value.appPin
        if (!savedPin.isNullOrEmpty() && savedPin != currentPinInput) {
            return false // Incorrect current PIN
        }
        val updated = settings.value.copy(appPin = if (newPin.isNullOrBlank()) null else newPin)
        updateSettings(updated)
        _isAppUnlocked.value = true
        return true
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(filter: String) {
        _filterType.value = filter
    }

    fun updateSettings(newSettings: BusinessSettings) {
        settingsRepository.updateSettings(newSettings)
        LanguageHelper.setLanguage(newSettings.language)
    }

    fun dismissTutorial() {
        settingsRepository.dismissTutorial()
    }

    fun startNewDocument(type: String) {
        val nextDocNum = settingsRepository.incrementDocNumber(type)
        val defaultNotes = settings.value.defaultNotes
        
        _activeDocument.value = ActiveDocumentState(
            editingDocId = null,
            documentType = type,
            documentNumber = nextDocNum,
            date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
            dueDate = "",
            clientName = "",
            clientPhone = "",
            clientAddress = "",
            items = emptyList(),
            discountType = "FIXED",
            discountValue = 0.0,
            taxEnabled = false,
            notes = defaultNotes,
            paymentStatus = "Unpaid",
            amountPaid = 0.0,
            sitePhotos = emptyList(),
            generatedPdfFile = null,
            generatedPdfUri = null
        )
    }

    fun loadDocumentForEdit(documentId: Long) {
        viewModelScope.launch {
            val docWithItems = documentRepository.getDocumentWithItemsById(documentId)
            if (docWithItems != null) {
                val doc = docWithItems.document
                val draftItems = docWithItems.items.map { item ->
                    DraftItem(
                        id = item.id,
                        itemName = item.itemName,
                        unit = item.unit,
                        quantity = item.quantity,
                        rate = item.rate,
                        amount = item.amount
                    )
                }

                val photosList = if (doc.sitePhotosJson.isNotBlank()) {
                    try {
                        val array = JSONArray(doc.sitePhotosJson)
                        (0 until array.length()).map { array.getString(it) }
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else emptyList()

                _activeDocument.value = ActiveDocumentState(
                    editingDocId = doc.id,
                    documentType = doc.documentType,
                    documentNumber = doc.documentNumber,
                    date = doc.date,
                    dueDate = doc.dueDate,
                    clientName = doc.clientName,
                    clientPhone = doc.clientPhone,
                    clientAddress = doc.clientAddress,
                    items = draftItems,
                    discountType = doc.discountType,
                    discountValue = doc.discountValue,
                    taxEnabled = doc.taxEnabled,
                    taxPercentage = if (doc.taxPercentage > 0) doc.taxPercentage else 17.0,
                    notes = doc.notes,
                    paymentStatus = doc.paymentStatus,
                    amountPaid = doc.amountPaid,
                    sitePhotos = photosList
                )
            }
        }
    }

    fun convertQuotationToInvoice(quotationDocWithItems: DocumentWithItems) {
        val nextDocNum = settingsRepository.incrementDocNumber("INVOICE")
        val doc = quotationDocWithItems.document
        val draftItems = quotationDocWithItems.items.map { item ->
            DraftItem(
                id = System.currentTimeMillis() + (0..1000).random(),
                itemName = item.itemName,
                unit = item.unit,
                quantity = item.quantity,
                rate = item.rate,
                amount = item.amount
            )
        }

        val photosList = if (doc.sitePhotosJson.isNotBlank()) {
            try {
                val array = JSONArray(doc.sitePhotosJson)
                (0 until array.length()).map { array.getString(it) }
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()

        _activeDocument.value = ActiveDocumentState(
            editingDocId = null,
            documentType = "INVOICE",
            documentNumber = nextDocNum,
            date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
            dueDate = "",
            clientName = doc.clientName,
            clientPhone = doc.clientPhone,
            clientAddress = doc.clientAddress,
            items = draftItems,
            discountType = doc.discountType,
            discountValue = doc.discountValue,
            taxEnabled = doc.taxEnabled,
            taxPercentage = if (doc.taxPercentage > 0) doc.taxPercentage else 17.0,
            notes = doc.notes,
            paymentStatus = "Unpaid",
            amountPaid = 0.0,
            sitePhotos = photosList
        )
    }

    fun duplicateDocument(docWithItems: DocumentWithItems) {
        val doc = docWithItems.document
        val newDocNum = settingsRepository.incrementDocNumber(doc.documentType)
        val draftItems = docWithItems.items.map { item ->
            DraftItem(
                id = System.currentTimeMillis() + (0..1000).random(),
                itemName = item.itemName,
                unit = item.unit,
                quantity = item.quantity,
                rate = item.rate,
                amount = item.amount
            )
        }

        _activeDocument.value = ActiveDocumentState(
            editingDocId = null,
            documentType = doc.documentType,
            documentNumber = newDocNum,
            date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
            dueDate = "",
            clientName = doc.clientName,
            clientPhone = doc.clientPhone,
            clientAddress = doc.clientAddress,
            items = draftItems,
            discountType = doc.discountType,
            discountValue = doc.discountValue,
            taxEnabled = doc.taxEnabled,
            taxPercentage = if (doc.taxPercentage > 0) doc.taxPercentage else 17.0,
            notes = doc.notes,
            paymentStatus = "Unpaid",
            amountPaid = 0.0,
            sitePhotos = emptyList()
        )
    }

    fun updateClientDetails(name: String, phone: String, address: String) {
        _activeDocument.value = _activeDocument.value.copy(
            clientName = name,
            clientPhone = phone,
            clientAddress = address
        )
    }

    fun updateDocumentMeta(docNumber: String, date: String, dueDate: String = _activeDocument.value.dueDate) {
        _activeDocument.value = _activeDocument.value.copy(
            documentNumber = docNumber,
            date = date,
            dueDate = dueDate
        )
    }

    fun updateTotalsAndNotes(
        discountType: String,
        discountValue: Double,
        taxEnabled: Boolean,
        taxPercentage: Double,
        notes: String
    ) {
        _activeDocument.value = _activeDocument.value.copy(
            discountType = discountType,
            discountValue = discountValue,
            taxEnabled = taxEnabled,
            taxPercentage = taxPercentage,
            notes = notes
        )
    }

    fun updateTotalsAndNotes(
        discountValue: Double,
        taxEnabled: Boolean,
        taxPercentage: Double,
        notes: String
    ) {
        _activeDocument.value = _activeDocument.value.copy(
            discountValue = discountValue,
            taxEnabled = taxEnabled,
            taxPercentage = taxPercentage,
            notes = notes
        )
    }

    fun addSitePhoto(uriString: String) {
        val current = _activeDocument.value.sitePhotos.toMutableList()
        if (current.size < 2) {
            current.add(uriString)
            _activeDocument.value = _activeDocument.value.copy(sitePhotos = current)
        }
    }

    fun removeSitePhoto(index: Int) {
        val current = _activeDocument.value.sitePhotos.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _activeDocument.value = _activeDocument.value.copy(sitePhotos = current)
        }
    }

    fun addItemToDraft(itemName: String, unit: String, rate: Double, quantity: Double = 1.0) {
        val newItem = DraftItem(
            id = System.currentTimeMillis(),
            itemName = itemName,
            unit = unit,
            quantity = quantity,
            rate = rate,
            amount = quantity * rate
        )
        val currentItems = _activeDocument.value.items.toMutableList()
        currentItems.add(newItem)
        _activeDocument.value = _activeDocument.value.copy(items = currentItems)

        viewModelScope.launch {
            val existing = catalogItems.value.find { it.name.equals(itemName, ignoreCase = true) }
            if (existing != null) {
                catalogRepository.updateCatalogItem(existing.copy(defaultUnit = unit, defaultRate = rate))
            } else {
                catalogRepository.insertCatalogItem(
                    CatalogItemEntity(name = itemName, defaultUnit = unit, defaultRate = rate, isCustom = true)
                )
            }
        }
    }

    fun updateDraftItem(index: Int, quantity: Double, rate: Double) {
        val currentItems = _activeDocument.value.items.toMutableList()
        if (index in currentItems.indices) {
            val old = currentItems[index]
            val updated = old.copy(
                quantity = quantity,
                rate = rate,
                amount = quantity * rate
            )
            currentItems[index] = updated
            _activeDocument.value = _activeDocument.value.copy(items = currentItems)

            viewModelScope.launch {
                val existing = catalogItems.value.find { it.name.equals(old.itemName, ignoreCase = true) }
                if (existing != null) {
                    catalogRepository.updateCatalogItem(existing.copy(defaultRate = rate))
                }
            }
        }
    }

    fun removeDraftItem(index: Int) {
        val currentItems = _activeDocument.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _activeDocument.value = _activeDocument.value.copy(items = currentItems)
        }
    }

    fun saveCurrentItemsAsPackage(packageName: String) {
        val currentItems = _activeDocument.value.items
        if (currentItems.isEmpty() || packageName.isBlank()) return
        val jsonArray = JSONArray()
        currentItems.forEach { item ->
            val obj = JSONObject().apply {
                put("itemName", item.itemName)
                put("unit", item.unit)
                put("quantity", item.quantity)
                put("rate", item.rate)
            }
            jsonArray.put(obj)
        }

        viewModelScope.launch {
            comboPackageRepository.savePackage(packageName.trim(), jsonArray.toString())
        }
    }

    fun applyPackageToDraft(pkg: ComboPackageEntity) {
        try {
            val jsonArray = JSONArray(pkg.itemsJson)
            val currentItems = _activeDocument.value.items.toMutableList()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.getString("itemName")
                val unit = obj.getString("unit")
                val qty = obj.getDouble("quantity")
                val rate = obj.getDouble("rate")

                currentItems.add(
                    DraftItem(
                        id = System.currentTimeMillis() + i,
                        itemName = name,
                        unit = unit,
                        quantity = qty,
                        rate = rate,
                        amount = qty * rate
                    )
                )
            }
            _activeDocument.value = _activeDocument.value.copy(items = currentItems)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteComboPackage(id: Long) {
        viewModelScope.launch {
            comboPackageRepository.deletePackageById(id)
        }
    }

    fun renameComboPackage(id: Long, newName: String) {
        viewModelScope.launch {
            comboPackageRepository.renamePackage(id, newName)
        }
    }

    fun updatePaymentStatus(documentId: Long, status: String, amountPaid: Double) {
        viewModelScope.launch {
            val docWithItems = documentRepository.getDocumentWithItemsById(documentId)
            if (docWithItems != null) {
                val updatedDoc = docWithItems.document.copy(
                    paymentStatus = status,
                    amountPaid = amountPaid
                )
                documentRepository.updateDocument(updatedDoc)
                if (_activeDocument.value.editingDocId == documentId) {
                    _activeDocument.value = _activeDocument.value.copy(
                        paymentStatus = status,
                        amountPaid = amountPaid
                    )
                }
            }
        }
    }

    suspend fun saveAndGeneratePdf(context: Context): Pair<DocumentEntity, Uri?> {
        val currState = _activeDocument.value

        // Auto-save client to Client Address Book
        if (currState.clientName.isNotBlank()) {
            clientRepository.saveOrUpdateClient(currState.clientName, currState.clientPhone, currState.clientAddress)
        }

        val photosJson = if (currState.sitePhotos.isNotEmpty()) {
            JSONArray(currState.sitePhotos).toString()
        } else ""

        val docEntity = DocumentEntity(
            id = currState.editingDocId ?: 0L,
            documentType = currState.documentType,
            documentNumber = currState.documentNumber,
            date = currState.date,
            dueDate = currState.dueDate,
            clientName = currState.clientName,
            clientPhone = currState.clientPhone,
            clientAddress = currState.clientAddress,
            subtotal = currState.subtotal,
            discount = currState.discount,
            taxEnabled = currState.taxEnabled,
            taxPercentage = currState.taxPercentage,
            taxAmount = currState.taxAmount,
            grandTotal = currState.grandTotal,
            notes = currState.notes,
            paymentStatus = currState.paymentStatus,
            amountPaid = currState.amountPaid,
            discountType = currState.discountType,
            discountValue = currState.discountValue,
            sitePhotosJson = photosJson
        )

        val itemEntities = currState.items.map { item ->
            DocumentItemEntity(
                documentId = 0L,
                itemName = item.itemName,
                unit = item.unit,
                quantity = item.quantity,
                rate = item.rate,
                amount = item.amount
            )
        }

        val savedDocId = documentRepository.saveDocumentWithItems(docEntity, itemEntities)
        val finalDocEntity = docEntity.copy(id = savedDocId)

        val pdfResult = withContext(Dispatchers.IO) {
            PdfGenerator.generatePdf(context, finalDocEntity, itemEntities, settings.value)
        }

        if (pdfResult != null) {
            _activeDocument.value = _activeDocument.value.copy(
                generatedPdfFile = pdfResult.file,
                generatedPdfUri = pdfResult.uri
            )
            documentRepository.updateDocument(finalDocEntity.copy(pdfPath = pdfResult.file.absolutePath))
        }

        return Pair(finalDocEntity, pdfResult?.uri)
    }

    suspend fun exportAccountantCsv(context: Context): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val docsWithItems = allDocuments.value
                val csvBuilder = StringBuilder()

                // Header
                csvBuilder.append("Document Number,Type,Date,Due Date,Client Name,Client Phone,Items Summary,Subtotal,Discount,Tax,Grand Total,Payment Status,Amount Paid,Amount Due\n")

                docsWithItems.forEach { docWithItem ->
                    val doc = docWithItem.document
                    val itemsSummary = docWithItem.items.joinToString(" ; ") { "${it.itemName} (${it.quantity} ${it.unit} @ Rs. ${it.rate})" }
                        .replace("\"", "'")
                    val amountDue = (doc.grandTotal - doc.amountPaid).coerceAtLeast(0.0)

                    csvBuilder.append("\"${doc.documentNumber}\",")
                        .append("\"${doc.documentType}\",")
                        .append("\"${doc.date}\",")
                        .append("\"${doc.dueDate}\",")
                        .append("\"${doc.clientName.replace("\"", "'")}\",")
                        .append("\"${doc.clientPhone}\",")
                        .append("\"$itemsSummary\",")
                        .append("${doc.subtotal},")
                        .append("${doc.discount},")
                        .append("${doc.taxAmount},")
                        .append("${doc.grandTotal},")
                        .append("\"${doc.paymentStatus}\",")
                        .append("${doc.amountPaid},")
                        .append("$amountDue\n")
                }

                val csvDir = File(context.getExternalFilesDir(null), "Exports")
                if (!csvDir.exists()) csvDir.mkdirs()

                val csvFile = File(csvDir, "chadhar_accountant_export_${System.currentTimeMillis()}.csv")
                csvFile.writeText(csvBuilder.toString())

                val authority = "${context.packageName}.fileprovider"
                FileProvider.getUriForFile(context, authority, csvFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun loadSampleDocumentForPreview(context: Context) {
        val sampleItems = listOf(
            DraftItem(id = 1, itemName = "ACP Sheet — 4mm Metallic Silver", unit = "sq.ft", quantity = 150.0, rate = 450.0, amount = 67500.0),
            DraftItem(id = 2, itemName = "Aluminum Box Section Frame", unit = "running ft", quantity = 120.0, rate = 250.0, amount = 30000.0),
            DraftItem(id = 3, itemName = "Fixing Labor Charges", unit = "sq.ft", quantity = 150.0, rate = 80.0, amount = 12000.0),
            DraftItem(id = 4, itemName = "Weather Proof Silicone Sealant", unit = "piece", quantity = 10.0, rate = 650.0, amount = 6500.0)
        )

        _activeDocument.value = ActiveDocumentState(
            editingDocId = null,
            documentType = "QUOTATION",
            documentNumber = "QUO-SAMPLE",
            date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
            clientName = "Al-Rehman Construction (Mr. Kamran)",
            clientPhone = "0321-7654321",
            clientAddress = "Gulberg III, Main Boulevard, Lahore",
            items = sampleItems,
            discountType = "FIXED",
            discountValue = 3500.0,
            taxEnabled = false,
            notes = "50% advance required upon confirmation. Rate valid for 15 days.",
            generatedPdfFile = null,
            generatedPdfUri = null
        )

        // Generate sample PDF for preview
        viewModelScope.launch {
            val sampleDoc = DocumentEntity(
                id = 0L,
                documentType = "QUOTATION",
                documentNumber = "QUO-SAMPLE",
                date = _activeDocument.value.date,
                clientName = _activeDocument.value.clientName,
                clientPhone = _activeDocument.value.clientPhone,
                clientAddress = _activeDocument.value.clientAddress,
                subtotal = _activeDocument.value.subtotal,
                discount = _activeDocument.value.discount,
                taxEnabled = false,
                taxPercentage = 0.0,
                taxAmount = 0.0,
                grandTotal = _activeDocument.value.grandTotal,
                notes = _activeDocument.value.notes
            )
            val sampleItemEntities = sampleItems.map {
                DocumentItemEntity(
                    documentId = 0L,
                    itemName = it.itemName,
                    unit = it.unit,
                    quantity = it.quantity,
                    rate = it.rate,
                    amount = it.amount
                )
            }
            val pdfResult = withContext(Dispatchers.IO) {
                PdfGenerator.generatePdf(context, sampleDoc, sampleItemEntities, settings.value)
            }
            if (pdfResult != null) {
                _activeDocument.value = _activeDocument.value.copy(
                    generatedPdfFile = pdfResult.file,
                    generatedPdfUri = pdfResult.uri
                )
            }
        }
    }

    fun duplicateDocument(docId: Long) {
        viewModelScope.launch {
            val docWithItems = documentRepository.getDocumentWithItemsById(docId)
            if (docWithItems != null) {
                val nextDocNum = settingsRepository.incrementDocNumber(docWithItems.document.documentType)
                val newDoc = docWithItems.document.copy(
                    id = 0L,
                    documentNumber = nextDocNum,
                    date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
                    pdfPath = null
                )
                val newItems = docWithItems.items.map { it.copy(id = 0L, documentId = 0L) }
                documentRepository.saveDocumentWithItems(newDoc, newItems)
            }
        }
    }

    fun convertQuotationToInvoice(docId: Long) {
        viewModelScope.launch {
            val docWithItems = documentRepository.getDocumentWithItemsById(docId)
            if (docWithItems != null) {
                val nextInvNum = settingsRepository.incrementDocNumber("INVOICE")
                val newDoc = docWithItems.document.copy(
                    id = 0L,
                    documentType = "INVOICE",
                    documentNumber = nextInvNum,
                    date = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
                    paymentStatus = "Unpaid",
                    amountPaid = 0.0,
                    pdfPath = null
                )
                val newItems = docWithItems.items.map { it.copy(id = 0L, documentId = 0L) }
                documentRepository.saveDocumentWithItems(newDoc, newItems)
            }
        }
    }

    suspend fun generatePaymentReceipt(context: Context, docId: Long): Uri? {
        return withContext(Dispatchers.IO) {
            val docWithItems = documentRepository.getDocumentWithItemsById(docId) ?: return@withContext null
            val result = PdfGenerator.generatePaymentReceiptPdf(context, docWithItems.document, settings.value)
            result?.uri
        }
    }

    suspend fun exportCsvForAccountant(context: Context): Uri? {
        return exportAccountantCsv(context)
    }

    fun markBackupExported() {
        val updated = settings.value.copy(hasExportedBackup = true)
        updateSettings(updated)
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            documentRepository.deleteDocument(document)
            if (!document.pdfPath.isNull_or_empty()) {
                try {
                    val file = File(document.pdfPath)
                    if (file.exists()) file.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deleteCatalogItem(item: CatalogItemEntity) {
        viewModelScope.launch {
            catalogRepository.deleteCatalogItem(item)
        }
    }

    fun updateCatalogItem(item: CatalogItemEntity) {
        viewModelScope.launch {
            catalogRepository.updateCatalogItem(item)
        }
    }

    suspend fun exportBackupJson(context: Context): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val docsWithItems = allDocuments.value
                val rootObj = JSONObject()
                rootObj.put("appName", "Chadhar Aluminium")
                rootObj.put("exportDate", System.currentTimeMillis())

                val docsArray = JSONArray()
                docsWithItems.forEach { docWithItem ->
                    val doc = docWithItem.document
                    val docObj = JSONObject().apply {
                        put("documentType", doc.documentType)
                        put("documentNumber", doc.documentNumber)
                        put("date", doc.date)
                        put("clientName", doc.clientName)
                        put("clientPhone", doc.clientPhone)
                        put("clientAddress", doc.clientAddress)
                        put("subtotal", doc.subtotal)
                        put("discount", doc.discount)
                        put("taxEnabled", doc.taxEnabled)
                        put("taxPercentage", doc.taxPercentage)
                        put("taxAmount", doc.taxAmount)
                        put("grandTotal", doc.grandTotal)
                        put("notes", doc.notes)

                        val itemsArray = JSONArray()
                        docWithItem.items.forEach { item ->
                            val itemObj = JSONObject().apply {
                                put("itemName", item.itemName)
                                put("unit", item.unit)
                                put("quantity", item.quantity)
                                put("rate", item.rate)
                                put("amount", item.amount)
                            }
                            itemsArray.put(itemObj)
                        }
                        put("items", itemsArray)
                    }
                    docsArray.put(docObj)
                }
                rootObj.put("documents", docsArray)

                val backupDir = File(context.getExternalFilesDir(null), "Backups")
                if (!backupDir.exists()) backupDir.mkdirs()

                val backupFile = File(backupDir, "chadhar_aluminium_backup_${System.currentTimeMillis()}.json")
                backupFile.writeText(rootObj.toString(2))

                val authority = "${context.packageName}.fileprovider"
                FileProvider.getUriForFile(context, authority, backupFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun importBackupJson(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: return@withContext false

                val rootObj = JSONObject(jsonString)
                val docsArray = rootObj.getJSONArray("documents")

                for (i in 0 until docsArray.length()) {
                    val docObj = docsArray.getJSONObject(i)
                    val docEntity = DocumentEntity(
                        documentType = docObj.optString("documentType", "INVOICE"),
                        documentNumber = docObj.optString("documentNumber", "INV-000"),
                        date = docObj.optString("date", ""),
                        clientName = docObj.optString("clientName", "Client"),
                        clientPhone = docObj.optString("clientPhone", ""),
                        clientAddress = docObj.optString("clientAddress", ""),
                        subtotal = docObj.optDouble("subtotal", 0.0),
                        discount = docObj.optDouble("discount", 0.0),
                        taxEnabled = docObj.optBoolean("taxEnabled", false),
                        taxPercentage = docObj.optDouble("taxPercentage", 0.0),
                        taxAmount = docObj.optDouble("taxAmount", 0.0),
                        grandTotal = docObj.optDouble("grandTotal", 0.0),
                        notes = docObj.optString("notes", "")
                    )

                    val itemsArray = docObj.getJSONArray("items")
                    val itemsList = mutableListOf<DocumentItemEntity>()
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        itemsList.add(
                            DocumentItemEntity(
                                documentId = 0L,
                                itemName = itemObj.optString("itemName", ""),
                                unit = itemObj.optString("unit", "piece"),
                                quantity = itemObj.optDouble("quantity", 1.0),
                                rate = itemObj.optDouble("rate", 0.0),
                                amount = itemObj.optDouble("amount", 0.0)
                            )
                        )
                    }
                    documentRepository.saveDocumentWithItems(docEntity, itemsList)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    fun addCustomCatalogItem(
        name: String,
        unit: String,
        rate: Double,
        quantity: Double = 1.0,
        addToDraft: Boolean = true
    ) {
        viewModelScope.launch {
            val catalogItem = CatalogItemEntity(
                name = name,
                defaultUnit = unit,
                defaultRate = rate,
                isCustom = true
            )
            catalogRepository.insertCatalogItem(catalogItem)
            if (addToDraft) {
                addItemToDraft(name, unit, rate, quantity)
            }
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()
}

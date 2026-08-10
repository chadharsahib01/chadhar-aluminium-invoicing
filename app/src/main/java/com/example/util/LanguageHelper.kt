package com.example.util

object LanguageHelper {

    enum class AppLanguage {
        ENGLISH,
        URDU
    }

    private var currentLanguage = AppLanguage.ENGLISH

    fun setLanguage(langCode: String) {
        currentLanguage = if (langCode == "ur") AppLanguage.URDU else AppLanguage.ENGLISH
    }

    fun isUrdu(): Boolean = currentLanguage == AppLanguage.URDU

    // Simple dictionary for key UI terms
    fun getString(key: String): String {
        return if (currentLanguage == AppLanguage.URDU) {
            when (key) {
                "new_quotation" -> "نیا کوٹیشن (New Quotation)"
                "new_invoice" -> "نیا انوائس (New Invoice)"
                "quotation" -> "کوٹیشن (Quotation)"
                "invoice" -> "انوائس (Invoice)"
                "documents_history" -> "ریکارڈ اور ہسٹری (History)"
                "client_name" -> "گاہک کا نام (Client Name)"
                "client_phone" -> "فون نمبر (Phone Number)"
                "client_address" -> "پتہ (Address)"
                "add_item" -> "سامان شامل کریں (+ Add Item)"
                "add_custom_item" -> "نیا سامان درج کریں (+ Custom Item)"
                "item_name" -> "سامان کا نام (Item Name)"
                "unit" -> "یونٹ (Unit)"
                "quantity" -> "مقدار (Qty)"
                "rate" -> "ریٹ / قیمت (Rate)"
                "amount" -> "کل رقم (Amount)"
                "subtotal" -> "سب ٹوٹل (Subtotal)"
                "discount" -> "رعایت (Discount)"
                "tax" -> "ٹیکس / جی ایس ٹی (Tax/GST)"
                "grand_total" -> "میزان / کل رقم (Grand Total)"
                "notes" -> "شرائط و نوٹ (Notes/Terms)"
                "save_pdf" -> "فون میں سیو کریں (Save PDF)"
                "share_whatsapp" -> "واٹس ایپ پر بھیجیں (Share WhatsApp)"
                "preview" -> "پیش نظارہ (Preview)"
                "settings" -> "سیٹنگز (Settings)"
                "business_name" -> "کاروبار کا نام (Business Name)"
                "owner_name" -> "مالک کا نام (Owner Name)"
                "search_placeholder" -> "گاہک یا نمبر سے تلاش کریں..."
                else -> key
            }
        } else {
            when (key) {
                "new_quotation" -> "New Quotation"
                "new_invoice" -> "New Invoice"
                "quotation" -> "Quotation"
                "invoice" -> "Invoice"
                "documents_history" -> "Document History"
                "client_name" -> "Client Name"
                "client_phone" -> "Phone Number"
                "client_address" -> "Client Address"
                "add_item" -> "+ Add Item"
                "add_custom_item" -> "+ Add Custom Item"
                "item_name" -> "Item Name"
                "unit" -> "Unit"
                "quantity" -> "Quantity"
                "rate" -> "Rate per unit"
                "amount" -> "Amount"
                "subtotal" -> "Subtotal"
                "discount" -> "Discount"
                "tax" -> "Tax / GST"
                "grand_total" -> "Grand Total"
                "notes" -> "Notes / Payment Terms"
                "save_pdf" -> "Save PDF to Phone"
                "share_whatsapp" -> "Share on WhatsApp"
                "preview" -> "Preview & Share PDF"
                "settings" -> "Business Settings"
                "business_name" -> "Business Name"
                "owner_name" -> "Owner Name"
                "search_placeholder" -> "Search by client name, number, or date..."
                else -> key
            }
        }
    }
}

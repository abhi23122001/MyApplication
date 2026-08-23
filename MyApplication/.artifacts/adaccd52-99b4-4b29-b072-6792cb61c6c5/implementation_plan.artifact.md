# Implementation Plan - Official Document Generation System (SHAH ERP)

Implement a professional document generation system for SHAH SURVEYORS AND CONSULTANCY, allowing Admin to generate official Quotations and Invoices that match the company's corporate identity.

## User Review Required

> [!IMPORTANT]
> - The PDF generation will switch from hardcoded values to dynamic data retrieved from a local Room database and Admin settings.
> - A new "Company Settings" section will be added to the Admin Hub to manage Logos, Seals, Signatures, and Bank Details.
> - The existing `BillingScreen` and `BillingDocumentGenerator` will be significantly refactored to support complex tables, tax calculations, and professional formatting.

## Proposed Changes

### Infrastructure & Data Layer
- **Room Database**: Add `androidx.room` dependencies for local persistence of settings and document history.
- **Entities**:
  - `CompanyProfile`: Store name, address, email, phone, logo path, seal path, signature path.
  - `BankDetails`: Store bank name, account number, IFSC, GSTIN, branch.
  - `BillingDocument`: Store quote/invoice metadata (No, Date, Client, Totals, Status, etc.).
  - `BillingItem`: Line items for documents.
  - `TermCondition`: Reusable terms and conditions templates.
  - `DocNumberingConfig`: Configure prefixes and starting numbers for different document types.

### Admin Settings (New UI)
- **[NEW] [CompanySettingsScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/admin/CompanySettingsScreen.kt)**: UI to edit company profile and upload images (Logo, Seal, Signature).
- **[NEW] [BankDetailsScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/admin/BankDetailsScreen.kt)**: UI to manage bank accounts.
- **[NEW] [TermsAndConditionsScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/admin/TermsAndConditionsScreen.kt)**: UI to manage reusable terms templates.

### Billing & Document Management
- **[MODIFY] [BillingScreen.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/ui/billing/BillingScreen.kt)**:
  - Add "Add/Edit/Delete Item" functionality.
  - Add GST Type selector (CGST+SGST, IGST, No GST).
  - Add Terms selection.
  - Support "Duplicate" and "Convert Quotation to Invoice".
- **[MODIFY] [BillingDocumentGenerator.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/BillingDocumentGenerator.kt)**:
  - Implement a professional A4 layout matching the SHAH corporate style.
  - Dynamically draw Logos, Seals, and Signatures from URIs.
  - Implement "Amount in Words" conversion logic.
  - Improve table rendering (proper borders, alignment, multi-page support if needed).

### Utilities
- **[NEW] [NumberToWords.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/NumberToWords.kt)**: Helper to convert currency values to English words.
- **[NEW] [FileStorageHelper.kt](file:///C:/Users/HP/OneDrive/Desktop/MyApplication/app/src/main/java/com/shahsurveyors/myapplication/utils/FileStorageHelper.kt)**: Handle saving uploaded images (Logo, Seal) to internal storage for persistent access.

## Verification Plan

### Automated Tests
- Unit tests for `TaxCalculator` logic.
- Unit tests for `NumberToWords` converter.
- Room DAO tests for document CRUD operations.

### Manual Verification
- Deploy to device and navigate to **Admin -> Settings**.
- Upload a custom logo and seal.
- Create a Quotation with multiple items and verify totals/tax.
- Generate PDF and verify the visual layout matches the SHAH corporate style (sharp text, aligned logo).
- Share the PDF via WhatsApp/Gmail.
- Test "Convert to Invoice" and verify all details are carried over accurately.

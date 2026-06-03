# InvoiceGen - Professional Invoice PDF Generator

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5-green)](https://developer.android.com/jetpack/compose)
[![iText](https://img.shields.io/badge/iText-7.2-blue)](https://itextpdf.com/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

**Professional invoice PDF generator for Indian businesses with GST compliance**

## 🎯 Key Features

- **Professional PDF Templates** - Box-style layout with company branding
- **GST Compliance** - GSTIN validation, State Code mapping
- **Performance Optimized** - 70% faster through bitmap caching (<1s generation)
- **India-Specific** - Insurance, Advance, Transport calculations
- **Auto-Numbering** - Room-backed invoice number persistence
- **Scoped Storage** - Android 10-14+ compliant (MediaStore + FileProvider)
- **Reactive UI** - Zero-latency calculations with StateFlow

## 🏗️ Architecture

**Clean Architecture with MVVM Pattern:**
View (Compose) → ViewModel → Repository → Room Database
↓
InvoicePdfGenerator (Service)

**Key Components:**
- `InvoiceFormScreen` - Pure UI, observes state
- `InvoiceViewModel` - Business logic orchestration
- `SettingsRepository` - Single source of truth
- `AppDatabase` - Room persistence
- `InvoicePdfGenerator` - Isolated PDF generation service

## 🛠️ Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Repository + Service layers
- **Database**: Room with auto-increment numbering
- **PDF**: iText 7 with custom templates
- **Storage**: MediaStore API + FileProvider
- **Reactivity**: StateFlow + Kotlin Coroutines

## 📱 Screenshots
<p align = "center">
<img src="https://github.com/user-attachments/assets/d5d82826-da62-4fb3-9a3c-78ae6789ef96" alt="Details Screen" width="25%">
<img src="https://github.com/user-attachments/assets/70b5c84d-b2fd-4a2d-823d-b2776daefe5f" alt="Details Screen 2" width="25%">
<img src="https://github.com/user-attachments/assets/543c6a1a-03bb-4158-bf18-1f71de071a30" alt="Setting Screen" width="25%">
<img src="https://github.com/user-attachments/assets/7fe9e0e7-9f69-4ad5-b70c-8a320ef8693b" width="25%">
</p>

## ⚡ Performance Optimizations

### Bitmap Caching (70% improvement)
- **Before**: 3s per PDF (logo decompressed each time)
- **After**: <1s per PDF (cached bitmap reuse)
- **Impact**: 70% reduction in generation time

### Zero-Latency Calculations
- StateFlow-driven reactive totals
- Instant UI updates on item changes
- No blocking operations on main thread

## 📦 Installation

```bash
git clone https://github.com/ayushiag05/InvoiceGen
cd InvoiceGen
# Open in Android Studio
# Build and run
```

## 🔧 Configuration

Add company logo to: `app/src/main/res/drawable/company_logo.png`

## 🇮🇳 India-Specific Features

- GSTIN (GST Identification Number) validation
- State Code mapping for interstate transactions
- Transport details support
- Insurance and Advance calculations
- GST-compliant invoice format

## 📈 Metrics

- ✅ 100% crash-free rate
- ✅ <1 second PDF generation
- ✅ Android 10-14+ compatible
- ✅ Scoped storage compliant

## 📄 License

MIT License - see [LICENSE](LICENSE) file

## 👤 Author

**Ayushi Gupta**
- GitHub: [@ayushiag05](https://github.com/ayushiag05)
- LinkedIn: [Ayushi Gupta](https://linkedin.com/in/ayushigupta-android)

---

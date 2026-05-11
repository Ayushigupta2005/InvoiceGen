package com.example.invoicegen.pdf

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.invoicegen.data.CompanySettings
import com.example.invoicegen.model.*
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class InvoicePdfGenerator(private val context: Context) {

    private val CHANNEL_ID = "invoice_notifications"
    private val NOTIFICATION_ID = 1

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Invoice Notifications"
            val descriptionText = "Notifications for generated invoices"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun generateInvoices(
        header: InvoiceHeader,
        items: List<InvoiceItem>,
        transport: TransportDetails,
        summary: InvoiceSummary,
        settings: CompanySettings,
        totalAmount: Double,
        grandTotal: Double
    ): List<Uri> {
        val generatedUris = mutableListOf<Uri>()

        // Load logo once before the loop to improve performance
        var logoImageData: com.itextpdf.io.image.ImageData? = null
        try {
            if (settings.logoPath.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeFile(settings.logoPath)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    logoImageData = ImageDataFactory.create(stream.toByteArray())
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("InvoicePdfGenerator", "Error preparing logo: ${e.message}")
        }

        try {
            // 1. Generate Combined PDF (Original + Duplicate)
            val combinedFileName = "Invoice_${header.invoiceNo}_Original_Duplicate.pdf"
            createPdfFile(
                combinedFileName,
                listOf("Original", "Duplicate"),
                header, items, transport, summary, settings, totalAmount, grandTotal, logoImageData
            )?.let { generatedUris.add(it) }

            // 2. Generate Triplicate PDF
            val triplicateFileName = "Invoice_${header.invoiceNo}_Triplicate.pdf"
            createPdfFile(
                triplicateFileName,
                listOf("Triplicate"),
                header, items, transport, summary, settings, totalAmount, grandTotal, logoImageData
            )?.let { generatedUris.add(it) }

        } catch (e: Exception) {
            android.util.Log.e("InvoicePdfGenerator", "Error generating PDFs: ${e.message}", e)
            throw e
        }
        return generatedUris
    }

    private fun createPdfFile(
        fileName: String,
        copyTypes: List<String>,
        header: InvoiceHeader,
        items: List<InvoiceItem>,
        transport: TransportDetails,
        summary: InvoiceSummary,
        settings: CompanySettings,
        totalAmount: Double,
        grandTotal: Double,
        logoImageData: com.itextpdf.io.image.ImageData?
    ): Uri? {
        val outputStream: OutputStream?
        var uri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/InvoiceGen")
            }
            uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            outputStream = uri?.let { contentResolver.openOutputStream(it) }
        } else {
            val outputDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "InvoiceGen")
            if (!outputDir.exists()) outputDir.mkdirs()
            val file = File(outputDir, fileName)
            outputStream = FileOutputStream(file)
            uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }

        if (outputStream != null && uri != null) {
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)

            copyTypes.forEachIndexed { index, copyType ->
                addInvoicePage(document, copyType, header, items, transport, summary, settings, totalAmount, grandTotal, logoImageData)
                if (index < copyTypes.size - 1) {
                    document.add(AreaBreak())
                }
            }

            document.close()
            android.util.Log.d("InvoicePdfGenerator", "PDF saved: $fileName")
            return uri
        }
        return null
    }

    private fun addInvoicePage(
        document: Document,
        copyType: String,
        header: InvoiceHeader,
        items: List<InvoiceItem>,
        transport: TransportDetails,
        summary: InvoiceSummary,
        settings: CompanySettings,
        totalAmount: Double,
        grandTotal: Double,
        logoImageData: com.itextpdf.io.image.ImageData?
    ) {
        // Logo (Fixed Position Top-Left)
        logoImageData?.let {
            val logo = Image(it)
                .scaleToFit(60f, 60f)
                .setFixedPosition(40f, 760f)
            document.add(logo)
        }

        // Company Details (Centered Paragraphs)
        document.add(Paragraph(settings.companyName).setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(18f).setMarginBottom(0f))
        document.add(Paragraph(settings.subtitle).setTextAlignment(TextAlignment.CENTER).setFontSize(12f).setMarginBottom(0f))
        document.add(Paragraph(settings.address).setTextAlignment(TextAlignment.CENTER).setFontSize(10f).setMarginBottom(0f))
        document.add(Paragraph("GSTIN: ${settings.gstin}").setTextAlignment(TextAlignment.CENTER).setFontSize(10f))

        // Company Mobile (Fixed Position Top-Right)
        if (settings.mobileNumbers.isNotEmpty()) {
            document.add(Paragraph("Mobile: ${settings.mobileNumbers}")
                .setFixedPosition(430f, 785f, 150f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10f))
        }

        val copyLabel = Paragraph(copyType.uppercase()).setTextAlignment(TextAlignment.RIGHT).setItalic().setFontSize(10f)
        document.add(copyLabel)

        document.add(LineSeparator(com.itextpdf.kernel.pdf.canvas.draw.SolidLine()))

        // Invoice & Party Details
        val infoTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
        val leftCol = Cell().setBorder(Border.NO_BORDER)
        leftCol.add(Paragraph("Invoice No: ${header.invoiceNo}"))
        leftCol.add(Paragraph("Date: ${header.date}"))
        leftCol.add(Paragraph("Name of Party: ${header.nameOfParty}"))
        leftCol.add(Paragraph("Address: ${header.address}"))
        leftCol.add(Paragraph("GSTIN: ${header.gstin}"))
        infoTable.addCell(leftCol)

        val rightCol = Cell().setBorder(Border.NO_BORDER)
        rightCol.add(Paragraph("Broker: ${header.broker}"))
        rightCol.add(Paragraph("Truck No: ${header.truckNo}"))
        rightCol.add(Paragraph("Mobile No: ${header.mobileNo}"))
        rightCol.add(Paragraph("Transport: ${header.transport}"))
        rightCol.add(Paragraph("State Code: ${header.stateCode}"))
        infoTable.addCell(rightCol)
        document.add(infoTable.setMarginTop(10f))

        // Items Table
        val itemTable = Table(UnitValue.createPointArray(floatArrayOf(3f, 1f, 1f, 1f, 1f, 1.5f))).useAllAvailableWidth()
        itemTable.addHeaderCell(Cell().add(Paragraph("Particulars").setBold()))
        itemTable.addHeaderCell(Cell().add(Paragraph("Qty (Bags)").setBold()))
        itemTable.addHeaderCell(Cell().add(Paragraph("Weight/Bag").setBold()))
        itemTable.addHeaderCell(Cell().add(Paragraph("Net Wt (QTL)").setBold()))
        itemTable.addHeaderCell(Cell().add(Paragraph("Rate/QTL").setBold()))
        itemTable.addHeaderCell(Cell().add(Paragraph("Amount").setBold()))

        items.forEach { item ->
            itemTable.addCell(Cell().add(Paragraph(item.particulars)))
            itemTable.addCell(Cell().add(Paragraph(item.qtyBags)))
            itemTable.addCell(Cell().add(Paragraph(item.weightPerBag)))
            itemTable.addCell(Cell().add(Paragraph("%.2f".format(item.netWeightQtl))))
            itemTable.addCell(Cell().add(Paragraph(item.ratePerQtl)))
            itemTable.addCell(Cell().add(Paragraph("%.2f".format(item.amount))))
        }
        
        val currentSize = items.size
        if (currentSize < 6) {
            for (i in 1..(6 - currentSize)) {
                for (j in 1..6) itemTable.addCell(Cell().setHeight(20f).add(Paragraph(" ")))
            }
        }
        
        itemTable.addCell(Cell(1, 5).add(Paragraph("TOTAL").setBold().setTextAlignment(TextAlignment.RIGHT)))
        itemTable.addCell(Cell().add(Paragraph("%.2f".format(totalAmount)).setBold()))
        itemTable.addCell(Cell(1, 5).add(Paragraph("Insurance (+)").setTextAlignment(TextAlignment.RIGHT)))
        itemTable.addCell(Cell().add(Paragraph(summary.insurance)))
        itemTable.addCell(Cell(1, 5).add(Paragraph("Advance (+)").setTextAlignment(TextAlignment.RIGHT)))
        itemTable.addCell(Cell().add(Paragraph(summary.advance)))
        itemTable.addCell(Cell(1, 5).add(Paragraph("GRAND TOTAL").setBold().setTextAlignment(TextAlignment.RIGHT).setFontSize(12f)))
        itemTable.addCell(Cell().add(Paragraph("%.2f".format(grandTotal)).setBold().setFontSize(12f)))

        val bankCell = Cell(1, 6).setPadding(5f)
        bankCell.add(Paragraph("EXEMPTED UNDER GST").setBold().setFontSize(10f))
        bankCell.add(Paragraph("HSN Code: 0713 (Pulses), 2302 (Cattle Feed)").setFontSize(9f))
        bankCell.add(Paragraph("Bank Details:").setBold().setUnderline().setMarginTop(5f))
        bankCell.add(Paragraph("ICICI: ${settings.iciciAccountNo} | IFSC: ${settings.iciciIfsc}").setFontSize(9f))
        bankCell.add(Paragraph("PNB: ${settings.pnbAccountNo} | IFSC: ${settings.pnbIfsc}").setFontSize(9f))
        itemTable.addCell(bankCell)

        val transportCell = Cell(1, 6).setPadding(5f)
        transportCell.add(Paragraph("Transport & Vehicle Details:").setBold().setUnderline())
        transportCell.add(Paragraph("Driver: ${transport.driverName} | License: ${transport.licenseNo} | Mobile: ${transport.mobileNo}\n" +
                "Owner: ${transport.truckOwner} | Engine: ${transport.engineNo} | Chassis: ${transport.chassisNo}").setFontSize(9f))
        itemTable.addCell(transportCell)

        val footerInfoCell = Cell(1, 6).setPadding(5f)
        val footerInnerTable = Table(UnitValue.createPointArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
        footerInnerTable.addCell(Cell().setBorder(Border.NO_BORDER).add(Paragraph("Terms & Conditions:\n${settings.termsAndConditions}").setFontSize(8f)))
        footerInnerTable.addCell(Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT).add(Paragraph("For: ${settings.companyName}\n\n\nAuthorised Sign").setMarginTop(20f)))
        footerInfoCell.add(footerInnerTable)
        itemTable.addCell(footerInfoCell)

        document.add(itemTable.setMarginTop(10f))
    }

    fun showNotification(pdfUri: Uri, invoiceNo: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Invoice Generated")
            .setContentText("Invoice #$invoiceNo is ready. Tap to open.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}

package com.aydanailroom.app.data

import android.content.Context
import androidx.room.withTransaction
import com.aydanailroom.app.R
import java.time.Instant
import java.util.Locale

class AydaRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    val dao: AppDao = database.appDao()

    val clients = dao.observeClients()
    val services = dao.observeServices()
    val appointments = dao.observeAppointments()
    val appointmentServices = dao.observeAppointmentServices()
    val gallery = dao.observeGallery()
    val transactions = dao.observeTransactions()
    val expenseCategories = dao.observeExpenseCategories()

    suspend fun ensureSeedData() {
        if (dao.countServices() == 0) {
            val names = listOf(
                context.getString(R.string.services_seed_extension),
                context.getString(R.string.services_seed_gel),
                context.getString(R.string.services_seed_french),
                context.getString(R.string.services_seed_refill),
                context.getString(R.string.services_seed_removal),
                context.getString(R.string.services_seed_nail_art),
            )
            val durations = listOf(120, 75, 90, 90, 30, 45)
            val prices = listOf(800_000L, 450_000L, 550_000L, 500_000L, 150_000L, 250_000L)
            names.forEachIndexed { index, name ->
                dao.insertService(
                    ServiceEntity(
                        name = name,
                        defaultPrice = prices[index],
                        defaultDurationMin = durations[index],
                        colorTag = listOf("#F4A6C0", "#9FD8C4", "#C7B2F0", "#F6E27A", "#F28B82", "#D8CBB2")[index],
                        sortOrder = index,
                    ),
                )
            }
        }
        dao.insertExpenseCategories(
            listOf("gel", "polish", "files", "tools", "rent", "transport", "ads", "other")
                .mapIndexed { index, key -> ExpenseCategoryEntity(key = key, sortOrder = index) },
        )
    }

    suspend fun addClient(
        fullName: String,
        phone: String,
        notes: String,
        nailShape: String,
        allergies: String,
    ): Long = dao.insertClient(
        ClientEntity(
            fullName = fullName.trim(),
            phone = phone.trim(),
            notes = notes.trim(),
            nailShape = nailShape,
            allergiesNote = allergies.trim(),
        ),
    )

    suspend fun updateClient(client: ClientEntity) = dao.updateClient(client)

    suspend fun addAppointment(
        appointment: AppointmentEntity,
        selectedServices: List<ServiceEntity>,
    ): Long = database.withTransaction {
        val appointmentId = dao.insertAppointment(appointment)
        if (selectedServices.isNotEmpty()) {
            dao.insertAppointmentServices(
                selectedServices.distinctBy { it.id }.map { service ->
                    AppointmentServiceEntity(
                        appointmentId = appointmentId,
                        serviceId = service.id,
                        priceAtTime = service.defaultPrice,
                    )
                },
            )
        }
        appointmentId
    }

    suspend fun completeAppointment(appointmentId: Long, requestedPaidAmount: Long) {
        database.withTransaction {
            val appointment = dao.getAppointment(appointmentId) ?: return@withTransaction
            val payable = (appointment.totalPrice - appointment.discount).coerceAtLeast(0L)
            val paid = requestedPaidAmount.coerceIn(0L, payable)
            dao.updateAppointment(appointment.copy(status = "done", paidAmount = paid))
            val existing = dao.transactionForAppointment(appointmentId)
            if (paid == 0L) {
                dao.deleteAppointmentTransaction(appointmentId)
            } else {
                val income = TransactionEntity(
                    id = existing?.id ?: 0,
                    type = "income",
                    amount = paid,
                    date = System.currentTimeMillis(),
                    category = "income_service",
                    note = appointment.notes,
                    appointmentId = appointmentId,
                )
                if (existing == null) dao.insertTransaction(income) else dao.updateTransaction(income)
            }
        }
    }

    suspend fun setAppointmentStatus(appointmentId: Long, status: String) {
        database.withTransaction {
            val appointment = dao.getAppointment(appointmentId) ?: return@withTransaction
            if (status == "cancelled" || status == "no_show") {
                dao.updateAppointment(appointment.copy(status = status, paidAmount = 0L))
                dao.deleteAppointmentTransaction(appointmentId)
            } else {
                dao.updateAppointment(appointment.copy(status = status))
            }
        }
    }

    suspend fun deleteAppointment(appointmentId: Long) {
        database.withTransaction {
            dao.deleteAppointmentTransaction(appointmentId)
            dao.deleteAppointment(appointmentId)
        }
    }

    suspend fun addExpense(amount: Long, category: String, note: String) {
        if (amount <= 0L) return
        dao.insertTransaction(
            TransactionEntity(
                type = "expense",
                amount = amount,
                category = category.lowercase(Locale.ROOT),
                note = note.trim(),
            ),
        )
    }

    suspend fun addService(name: String, price: Long, durationMinutes: Int) {
        if (name.isBlank()) return
        dao.insertService(
            ServiceEntity(
                name = name.trim(),
                defaultPrice = price.coerceAtLeast(0),
                defaultDurationMin = durationMinutes.coerceIn(15, 720),
                sortOrder = dao.countServices(),
            ),
        )
    }

    suspend fun updateService(service: ServiceEntity) = dao.updateService(service)

    suspend fun archiveClient(clientId: Long, archived: Boolean = true) {
        dao.setClientArchived(clientId, archived)
    }

    suspend fun addDesignRecord(record: DesignRecordEntity) {
        dao.insertDesignRecord(record)
    }

    suspend fun addGalleryItem(item: GalleryItemEntity) {
        dao.insertGalleryItem(item)
    }

    suspend fun toggleFavorite(item: GalleryItemEntity) {
        dao.updateGalleryItem(item.copy(isFavorite = !item.isFavorite))
    }

    suspend fun deleteGalleryItem(item: GalleryItemEntity) {
        dao.deleteGalleryItem(item.id)
        runCatching {
            val photo = java.io.File(item.photoPath).let { if (it.isAbsolute) it else java.io.File(context.filesDir, item.photoPath) }
            photo.delete()
        }
    }

    suspend fun getAppointment(appointmentId: Long): AppointmentEntity? = dao.getAppointment(appointmentId)

    suspend fun getClient(clientId: Long): ClientEntity? = dao.getClient(clientId)
}

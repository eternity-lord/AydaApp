package com.aydanailroom.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "clients", indices = [Index(value = ["fullName"]), Index(value = ["phone"])])
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String = "",
    val birthday: Long? = null,
    val notes: String = "",
    val nailShape: String = "unknown",
    val allergiesNote: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "services", indices = [Index(value = ["sortOrder"]), Index(value = ["isActive"])])
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val defaultPrice: Long,
    val defaultDurationMin: Int,
    val colorTag: String = "#F4A6C0",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
)

@Entity(tableName = "appointments", indices = [Index(value = ["clientId"]), Index(value = ["startAt"]), Index(value = ["status"])])
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val startAt: Long,
    val durationMin: Int,
    val status: String = "booked",
    val notes: String = "",
    val reminderMinutesBefore: Int? = 60,
    val totalPrice: Long = 0,
    val discount: Long = 0,
    val paidAmount: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "appointment_services",
    primaryKeys = ["appointmentId", "serviceId"],
    foreignKeys = [
        ForeignKey(
            entity = AppointmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["appointmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["serviceId"])],
)
data class AppointmentServiceEntity(
    val appointmentId: Long,
    val serviceId: Long,
    val priceAtTime: Long,
)

@Entity(tableName = "design_records", indices = [Index(value = ["clientId"]), Index(value = ["appointmentId"]), Index(value = ["date"])])
data class DesignRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val appointmentId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val colorsText: String = "",
    val brandProductText: String = "",
    val designDescription: String = "",
    val tags: String = "",
    val photoPaths: String = "",
    val clientFeedback: String = "",
)

@Entity(tableName = "gallery_items", indices = [Index(value = ["createdAt"]), Index(value = ["serviceId"])])
data class GalleryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoPath: String,
    val title: String = "",
    val tags: String = "",
    val serviceId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["date"]), Index(value = ["type"]), Index(value = ["appointmentId"], unique = true)],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    val date: Long = System.currentTimeMillis(),
    val category: String,
    val note: String = "",
    val appointmentId: Long? = null,
)

@Entity(tableName = "expense_categories", indices = [Index(value = ["sortOrder"])])
data class ExpenseCategoryEntity(
    @PrimaryKey val key: String,
    val sortOrder: Int,
    val isActive: Boolean = true,
)

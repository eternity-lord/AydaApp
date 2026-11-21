package com.aydanailroom.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM clients ORDER BY isArchived ASC, fullName COLLATE NOCASE ASC")
    fun observeClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE isArchived = 0 ORDER BY fullName COLLATE NOCASE ASC")
    fun observeActiveClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClient(id: Long): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("UPDATE clients SET isArchived = :archived WHERE id = :id")
    suspend fun setClientArchived(id: Long, archived: Boolean)

    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY sortOrder, name COLLATE NOCASE")
    fun observeActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services ORDER BY sortOrder, name COLLATE NOCASE")
    fun observeServices(): Flow<List<ServiceEntity>>

    @Query("SELECT COUNT(*) FROM services")
    suspend fun countServices(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: Long)

    @Query("SELECT * FROM appointments ORDER BY startAt DESC")
    fun observeAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointment(id: Long): AppointmentEntity?

    @Query("SELECT * FROM appointments WHERE startAt >= :from AND startAt < :until AND status != 'cancelled' ORDER BY startAt ASC")
    suspend fun appointmentsBetween(from: Long, until: Long): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE startAt >= :from AND startAt < :until AND status = 'booked' ORDER BY startAt ASC")
    suspend fun bookedAppointmentsBetween(from: Long, until: Long): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE startAt >= :from AND status = 'booked' ORDER BY startAt ASC")
    suspend fun upcomingBookedAppointments(from: Long): List<AppointmentEntity>

    @Query("SELECT COUNT(*) FROM appointments WHERE startAt >= :from AND startAt < :until AND status != 'cancelled'")
    suspend fun countAppointmentsBetween(from: Long, until: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointment(id: Long)

    @Query("SELECT * FROM appointment_services ORDER BY appointmentId")
    fun observeAppointmentServices(): Flow<List<AppointmentServiceEntity>>

    @Query("SELECT * FROM appointment_services WHERE appointmentId = :appointmentId")
    suspend fun appointmentServices(appointmentId: Long): List<AppointmentServiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointmentServices(services: List<AppointmentServiceEntity>)

    @Query("DELETE FROM appointment_services WHERE appointmentId = :appointmentId")
    suspend fun deleteAppointmentServices(appointmentId: Long)

    @Query("SELECT * FROM design_records WHERE clientId = :clientId ORDER BY date DESC")
    fun observeDesignRecords(clientId: Long): Flow<List<DesignRecordEntity>>

    @Query("SELECT * FROM design_records ORDER BY date DESC")
    fun observeAllDesignRecords(): Flow<List<DesignRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDesignRecord(record: DesignRecordEntity): Long

    @Query("SELECT * FROM gallery_items ORDER BY createdAt DESC")
    fun observeGallery(): Flow<List<GalleryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItem(item: GalleryItemEntity): Long

    @Update
    suspend fun updateGalleryItem(item: GalleryItemEntity)

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteGalleryItem(id: Long)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE appointmentId = :appointmentId LIMIT 1")
    suspend fun transactionForAppointment(appointmentId: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE appointmentId = :appointmentId")
    suspend fun deleteAppointmentTransaction(appointmentId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExpenseCategories(categories: List<ExpenseCategoryEntity>)

    @Query("SELECT * FROM expense_categories WHERE isActive = 1 ORDER BY sortOrder")
    fun observeExpenseCategories(): Flow<List<ExpenseCategoryEntity>>
}

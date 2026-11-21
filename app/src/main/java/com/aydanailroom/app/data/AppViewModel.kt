package com.aydanailroom.app.data

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aydanailroom.app.R
import com.aydanailroom.app.notifications.ReminderScheduler
import com.aydanailroom.app.util.AppointmentRules
import com.aydanailroom.app.util.JalaliDate
import com.aydanailroom.app.util.jalaliDayRange
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("ayda_settings", Application.MODE_PRIVATE)
    private val database = AppDatabase.get(application)
    private val repository = AydaRepository(application, database)

    val clients = repository.clients.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val services = repository.services.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val appointments = repository.appointments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val appointmentServices = repository.appointmentServices.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val gallery = repository.gallery.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val expenseCategories = repository.expenseCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val designRecords = database.appDao().observeAllDesignRecords().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var latinDigits = androidx.compose.runtime.mutableStateOf(preferences.getBoolean(KEY_LATIN_DIGITS, false))
        private set
    var onboardingCompleted = androidx.compose.runtime.mutableStateOf(preferences.getBoolean(KEY_ONBOARDING_COMPLETE, false))
        private set

    private val _events = MutableSharedFlow<Int>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repository.ensureSeedData() }.onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun setLatinDigits(enabled: Boolean) {
        latinDigits.value = enabled
        preferences.edit().putBoolean(KEY_LATIN_DIGITS, enabled).apply()
    }

    fun finishOnboarding() {
        onboardingCompleted.value = true
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    fun addClient(name: String, phone: String, notes: String, nailShape: String, allergies: String) {
        if (name.isBlank()) {
            _events.tryEmit(R.string.clients_name_required)
            return
        }
        viewModelScope.launch {
            runCatching { repository.addClient(name, phone, notes, nailShape, allergies) }
                .onSuccess { _events.emit(R.string.action_save) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun updateClient(client: ClientEntity) {
        viewModelScope.launch {
            runCatching { repository.updateClient(client) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun archiveClient(clientId: Long, archived: Boolean = true) {
        viewModelScope.launch {
            runCatching { repository.archiveClient(clientId, archived) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun addAppointment(appointment: AppointmentEntity, selectedServices: List<ServiceEntity>) {
        viewModelScope.launch {
            runCatching {
                val id = repository.addAppointment(appointment, selectedServices)
                val saved = repository.getAppointment(id)
                val client = repository.getClient(appointment.clientId)
                if (saved != null && client != null) {
                    ReminderScheduler.schedule(getApplication(), saved, client.fullName, client.phone)
                }
            }.onSuccess {
                _events.emit(R.string.action_save)
            }.onFailure {
                _events.emit(R.string.generic_error)
            }
        }
    }

    fun completeAppointment(appointmentId: Long, paidAmount: Long) {
        viewModelScope.launch {
            runCatching { repository.completeAppointment(appointmentId, paidAmount) }
                .onSuccess { ReminderScheduler.cancel(getApplication(), appointmentId) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun cancelAppointment(appointmentId: Long) {
        viewModelScope.launch {
            runCatching { repository.setAppointmentStatus(appointmentId, "cancelled") }
                .onSuccess { ReminderScheduler.cancel(getApplication(), appointmentId) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun markNoShow(appointmentId: Long) {
        viewModelScope.launch {
            runCatching { repository.setAppointmentStatus(appointmentId, "no_show") }
                .onSuccess { ReminderScheduler.cancel(getApplication(), appointmentId) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteAppointment(appointmentId) }
                .onSuccess { ReminderScheduler.cancel(getApplication(), appointmentId) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun findConflict(startAt: Long, durationMinutes: Int): AppointmentEntity? =
        AppointmentRules.findConflict(startAt, durationMinutes, appointments.value)

    fun findNextFreeSlot(date: JalaliDate, durationMinutes: Int, openingHour: Int = 9, closingHour: Int = 19): Long? {
        val range = jalaliDayRange(date)
        val zone = java.time.ZoneId.systemDefault()
        val dayStart = date.toGregorianDate().atTime(openingHour, 0).atZone(zone).toInstant().toEpochMilli()
        val dayEnd = date.toGregorianDate().atTime(closingHour, 0).atZone(zone).toInstant().toEpochMilli()
        return AppointmentRules.findNextFreeSlot(
            dayStart = maxOf(dayStart, range.first),
            dayEnd = minOf(dayEnd, range.last + 1),
            durationMinutes = durationMinutes,
            appointments = appointments.value,
        )
    }

    fun addService(name: String, price: Long, durationMinutes: Int) {
        viewModelScope.launch {
            runCatching { repository.addService(name, price, durationMinutes) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun updateService(service: ServiceEntity) {
        viewModelScope.launch {
            runCatching { repository.updateService(service) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun deleteService(serviceId: Long) {
        viewModelScope.launch {
            runCatching { database.appDao().deleteService(serviceId) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun addExpense(amount: Long, category: String, note: String) {
        viewModelScope.launch {
            runCatching { repository.addExpense(amount, category, note) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun addDesignRecord(record: DesignRecordEntity) {
        viewModelScope.launch {
            runCatching { repository.addDesignRecord(record) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun addGalleryPhoto(uri: Uri, title: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val context = getApplication<Application>()
                val directory = File(context.filesDir, "gallery").apply { mkdirs() }
                val temporary = File(context.cacheDir, "picked-${System.currentTimeMillis()}.image")
                context.contentResolver.openInputStream(uri)?.use { source ->
                    FileOutputStream(temporary).use { outputStream -> source.copyTo(outputStream) }
                } ?: error("Could not read selected photo")

                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(temporary.path, bounds)
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) error("Invalid image")
                val sampleSize = generateSequence(1) { it * 2 }
                    .takeWhile { maxOf(bounds.outWidth, bounds.outHeight) / it > 2_400 }
                    .lastOrNull() ?: 1
                val bitmap = BitmapFactory.decodeFile(
                    temporary.path,
                    BitmapFactory.Options().apply { inSampleSize = sampleSize; inPreferredConfig = Bitmap.Config.ARGB_8888 },
                ) ?: error("Could not decode selected photo")
                val scale = minOf(1f, 1_600f / maxOf(bitmap.width, bitmap.height))
                val resized = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt().coerceAtLeast(1),
                        (bitmap.height * scale).toInt().coerceAtLeast(1),
                        true,
                    )
                        .also { if (it !== bitmap) bitmap.recycle() }
                } else bitmap
                val output = File(directory, "${System.currentTimeMillis()}-${java.util.UUID.randomUUID()}.jpg")
                FileOutputStream(output).use { stream ->
                    resized.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                }
                resized.recycle()
                temporary.delete()
                repository.addGalleryItem(
                    GalleryItemEntity(
                        photoPath = "gallery/${output.name}",
                        title = title,
                    ),
                )
            }.onFailure {
                _events.emit(R.string.generic_error)
            }
        }
    }

    fun toggleFavorite(item: GalleryItemEntity) {
        viewModelScope.launch {
            runCatching { repository.toggleFavorite(item) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun deleteGalleryItem(item: GalleryItemEntity) {
        viewModelScope.launch {
            runCatching { repository.deleteGalleryItem(item) }
                .onFailure { _events.emit(R.string.generic_error) }
        }
    }

    fun exportBackup(uri: Uri, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            onFinished(BackupManager.export(getApplication(), uri))
        }
    }

    fun restoreBackup(uri: Uri, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch {
            onFinished(BackupManager.restore(getApplication(), uri))
        }
    }

    fun reportError() {
        _events.tryEmit(R.string.generic_error)
    }

    companion object {
        private const val KEY_LATIN_DIGITS = "latin_digits"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}

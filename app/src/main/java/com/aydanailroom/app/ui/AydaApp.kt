package com.aydanailroom.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aydanailroom.app.MainActivity
import com.aydanailroom.app.R
import com.aydanailroom.app.data.AppViewModel
import com.aydanailroom.app.data.AppointmentEntity
import com.aydanailroom.app.data.ClientEntity
import com.aydanailroom.app.data.DesignRecordEntity
import com.aydanailroom.app.data.ServiceEntity
import com.aydanailroom.app.ui.components.ChalkButton
import com.aydanailroom.app.ui.components.ChalkFrame
import com.aydanailroom.app.ui.screens.AppointmentsScreen
import com.aydanailroom.app.ui.screens.ClientsScreen
import com.aydanailroom.app.ui.screens.FinanceScreen
import com.aydanailroom.app.ui.screens.GalleryScreen
import com.aydanailroom.app.ui.screens.HomeScreen
import com.aydanailroom.app.ui.screens.ServicesScreen
import com.aydanailroom.app.ui.screens.SettingsScreen
import com.aydanailroom.app.ui.theme.ChalkColors
import java.io.File

private enum class Destination { TODAY, APPOINTMENTS, CLIENTS, GALLERY, FINANCE, SERVICES, SETTINGS }

@Composable
fun AydaApp(viewModel: AppViewModel) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val appointmentServices by viewModel.appointmentServices.collectAsStateWithLifecycle()
    val gallery by viewModel.gallery.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val designRecords by viewModel.designRecords.collectAsStateWithLifecycle()
    val latinDigits by viewModel.latinDigits
    val onboardingCompleted by viewModel.onboardingCompleted

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { messageId ->
            snackbarHostState.showSnackbar(context.getString(messageId))
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { photos ->
        photos.forEach { photo -> viewModel.addGalleryPhoto(photo) }
    }
    val backupCreator = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) {
            viewModel.exportBackup(uri) { success ->
                Toast.makeText(
                    context,
                    context.getString(if (success) R.string.settings_backup_success else R.string.settings_backup_error),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }
    var pendingRestore by remember { mutableStateOf<android.net.Uri?>(null) }
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        pendingRestore = uri
    }

    var destination by rememberSaveable { mutableStateOf(Destination.TODAY) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showClientDialog by remember { mutableStateOf(false) }
    var showAppointmentDialog by remember { mutableStateOf(false) }
    var prefillAppointmentNotes by rememberSaveable { mutableStateOf("") }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var paymentAppointment by remember { mutableStateOf<AppointmentEntity?>(null) }
    var detailClient by remember { mutableStateOf<ClientEntity?>(null) }
    var designClient by remember { mutableStateOf<ClientEntity?>(null) }

    if (!onboardingCompleted) {
        ChalkFrame(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            OnboardingScreen(
                onFinish = {
                    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    viewModel.finishOnboarding()
                },
            )
        }
        return
    }

    ChalkFrame(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentColor = ChalkColors.Chalk,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AydaNavigationBar(destination) { destination = it }
            },
            floatingActionButton = {
                if (destination in setOf(Destination.TODAY, Destination.APPOINTMENTS, Destination.CLIENTS, Destination.GALLERY, Destination.FINANCE)) {
                    FloatingActionButton(
                        onClick = {
                            when (destination) {
                                Destination.TODAY, Destination.APPOINTMENTS -> showAppointmentDialog = true
                                Destination.CLIENTS -> showClientDialog = true
                                Destination.GALLERY -> photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                Destination.FINANCE -> showExpenseDialog = true
                                else -> Unit
                            }
                        },
                        shape = CircleShape,
                        containerColor = ChalkColors.Pink,
                        contentColor = ChalkColors.BoardDeep,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.a11y_add), modifier = Modifier.size(34.dp))
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 7.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Text(
                            text = stringResource(R.string.brand_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = ChalkColors.Chalk,
                        )
                        Text(
                            text = stringResource(R.string.brand_handle),
                            style = MaterialTheme.typography.bodySmall,
                            color = ChalkColors.Pink,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.a11y_more), tint = ChalkColors.Chalk)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_services)) },
                                onClick = { destination = Destination.SERVICES; menuExpanded = false },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = { destination = Destination.SETTINGS; menuExpanded = false },
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = destination,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen-transition",
                    modifier = Modifier.weight(1f),
                ) { current ->
                    when (current) {
                        Destination.TODAY -> HomeScreen(
                            clients = clients,
                            services = services,
                            appointments = appointments,
                            appointmentServices = appointmentServices,
                            transactions = transactions,
                            latinDigits = latinDigits,
                            onAddAppointment = { showAppointmentDialog = true },
                            onMarkDone = { paymentAppointment = it },
                            onCancel = viewModel::cancelAppointment,
                            onClientClick = { id -> detailClient = clients.firstOrNull { it.id == id } },
                        )
                        Destination.APPOINTMENTS -> AppointmentsScreen(
                            clients = clients,
                            services = services,
                            appointments = appointments,
                            appointmentServices = appointmentServices,
                            latinDigits = latinDigits,
                            onAdd = { showAppointmentDialog = true },
                            onMarkDone = { paymentAppointment = it },
                            onCancel = viewModel::cancelAppointment,
                        )
                        Destination.CLIENTS -> ClientsScreen(
                            clients = clients,
                            appointments = appointments,
                            latinDigits = latinDigits,
                            onAdd = { showClientDialog = true },
                            onOpen = { detailClient = it },
                            onArchive = { viewModel.archiveClient(it.id, !it.isArchived) },
                        )
                        Destination.GALLERY -> GalleryScreen(
                            items = gallery,
                            filesDirectory = context.filesDir,
                            latinDigits = latinDigits,
                            onAdd = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            onToggleFavorite = viewModel::toggleFavorite,
                            onDelete = viewModel::deleteGalleryItem,
                        )
                        Destination.FINANCE -> FinanceScreen(
                            transactions = transactions,
                            appointments = appointments,
                            clients = clients,
                            latinDigits = latinDigits,
                            onAddExpense = { showExpenseDialog = true },
                        )
                        Destination.SERVICES -> ServicesScreen(
                            services = services,
                            latinDigits = latinDigits,
                            onAdd = { showServiceDialog = true },
                            onUpdate = viewModel::updateService,
                            onDelete = viewModel::deleteService,
                        )
                        Destination.SETTINGS -> SettingsScreen(
                            latinDigits = latinDigits,
                            onLatinDigitsChange = viewModel::setLatinDigits,
                            onExport = { backupCreator.launch("AydaNailroom-backup.zip") },
                            onImport = { backupPicker.launch(arrayOf("application/zip", "application/octet-stream")) },
                            onRequestNotifications = {
                                if (Build.VERSION.SDK_INT >= 33) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                        )
                    }
                }
            }
        }
    }

    if (showClientDialog) {
        ClientEditorDialog(
            latinDigits = latinDigits,
            onDismiss = { showClientDialog = false },
            onSave = { name, phone, notes, shape, allergies ->
                viewModel.addClient(name, phone, notes, shape, allergies)
                showClientDialog = false
            },
        )
    }
    if (showAppointmentDialog) {
        AppointmentEditorDialog(
            clients = clients.filterNot(ClientEntity::isArchived),
            services = services.filter(ServiceEntity::isActive),
            appointments = appointments,
            latinDigits = latinDigits,
            initialNotes = prefillAppointmentNotes,
            onDismiss = { showAppointmentDialog = false; prefillAppointmentNotes = "" },
            onFindNextSlot = viewModel::findNextFreeSlot,
            onSave = { appointment, selectedServices ->
                viewModel.addAppointment(appointment, selectedServices)
                showAppointmentDialog = false
                prefillAppointmentNotes = ""
            },
            onAddClient = {
                showAppointmentDialog = false
                showClientDialog = true
            },
        )
    }
    if (showExpenseDialog) {
        ExpenseEditorDialog(
            categories = categories,
            latinDigits = latinDigits,
            onDismiss = { showExpenseDialog = false },
            onSave = { amount, category, note ->
                viewModel.addExpense(amount, category, note)
                showExpenseDialog = false
            },
        )
    }
    if (showServiceDialog) {
        ServiceEditorDialog(
            latinDigits = latinDigits,
            onDismiss = { showServiceDialog = false },
            onSave = { name, price, duration ->
                viewModel.addService(name, price, duration)
                showServiceDialog = false
            },
        )
    }
    paymentAppointment?.let { appointment ->
        PaymentEditorDialog(
            appointment = appointment,
            latinDigits = latinDigits,
            onDismiss = { paymentAppointment = null },
            onSave = { amount ->
                viewModel.completeAppointment(appointment.id, amount)
                paymentAppointment = null
            },
        )
    }
    detailClient?.let { client ->
        ClientDetailsDialog(
            client = client,
            appointments = appointments.filter { it.clientId == client.id },
            designRecords = designRecords.filter { it.clientId == client.id },
            latinDigits = latinDigits,
            onDismiss = { detailClient = null },
            onAddDesign = { designClient = client; detailClient = null },
            onRepeatLast = { notes -> prefillAppointmentNotes = notes; detailClient = null; showAppointmentDialog = true },
            onArchive = {
                viewModel.archiveClient(client.id, !client.isArchived)
                detailClient = null
            },
        )
    }

    designClient?.let { client ->
        DesignEditorDialog(
            client = client,
            onDismiss = { designClient = null },
            onSave = { record ->
                viewModel.addDesignRecord(record)
                designClient = null
            },
        )
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(stringResource(R.string.settings_import_backup)) },
            text = { Text(stringResource(R.string.settings_restore_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestore = null
                    viewModel.restoreBackup(uri) { success ->
                        if (success) {
                            Toast.makeText(context, context.getString(R.string.settings_restore_success), Toast.LENGTH_LONG).show()
                            restartApp(context)
                        } else {
                            Toast.makeText(context, context.getString(R.string.settings_backup_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text(stringResource(R.string.action_cancel)) }
            },
            containerColor = ChalkColors.BoardDeep,
            titleContentColor = ChalkColors.Chalk,
            textContentColor = ChalkColors.ChalkDim,
        )
    }
}

@Composable
private fun AydaNavigationBar(selected: Destination, onSelect: (Destination) -> Unit) {
    NavigationBar(
        containerColor = ChalkColors.BoardDeep.copy(alpha = 0.98f),
        contentColor = ChalkColors.Chalk,
        tonalElevation = 0.dp,
    ) {
        val items = listOf(
            Triple(Destination.TODAY, R.string.nav_today, Icons.Rounded.CalendarToday),
            Triple(Destination.APPOINTMENTS, R.string.nav_appointments, Icons.Rounded.CalendarMonth),
            Triple(Destination.CLIENTS, R.string.nav_clients, Icons.Rounded.PeopleAlt),
            Triple(Destination.GALLERY, R.string.nav_gallery, Icons.Rounded.PhotoLibrary),
            Triple(Destination.FINANCE, R.string.nav_finance, Icons.Rounded.AccountBalanceWallet),
        )
        items.forEach { (destination, label, icon) ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(stringResource(label), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ChalkColors.Pink,
                    selectedTextColor = ChalkColors.Pink,
                    unselectedIconColor = ChalkColors.ChalkDim,
                    unselectedTextColor = ChalkColors.ChalkDim,
                    indicatorColor = ChalkColors.BoardLight,
                ),
            )
        }
    }
}

@Composable
private fun OnboardingScreen(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(0) }
    val titles = listOf(R.string.onboarding_title_1, R.string.onboarding_title_2, R.string.onboarding_title_3)
    val bodies = listOf(R.string.onboarding_body_1, R.string.onboarding_body_2, R.string.onboarding_body_3)
    val doodles = listOf("▦", "♡", "✧")
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.brand_name), style = MaterialTheme.typography.displayMedium, color = ChalkColors.Chalk)
        Text(stringResource(R.string.brand_handle), style = MaterialTheme.typography.bodyMedium, color = ChalkColors.Pink)
        Text(doodles[page], modifier = Modifier.padding(top = 30.dp), color = ChalkColors.Yellow, style = MaterialTheme.typography.displayLarge)
        Text(stringResource(titles[page]), modifier = Modifier.padding(top = 20.dp), style = MaterialTheme.typography.headlineMedium, color = ChalkColors.Chalk)
        Text(
            stringResource(bodies[page]),
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = ChalkColors.ChalkDim,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (page == 2) {
            Text(
                stringResource(R.string.onboarding_privacy),
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = ChalkColors.Mint,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.padding(top = 26.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(3) { index ->
                androidx.compose.foundation.Canvas(Modifier.size(if (index == page) 18.dp else 8.dp)) {
                    drawCircle(if (index == page) ChalkColors.Pink else ChalkColors.ChalkDim)
                }
            }
        }
        ChalkButton(
            text = stringResource(if (page == 2) R.string.action_continue else R.string.action_continue),
            onClick = { if (page < 2) page++ else onFinish() },
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
        )
        TextButton(onClick = onFinish, modifier = Modifier.padding(top = 4.dp)) {
            Text(stringResource(R.string.action_skip), color = ChalkColors.ChalkDim)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    if (intent != null) {
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }
}

package com.aydanailroom.app.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {
    suspend fun export(context: Context, target: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val database = AppDatabase.get(context)
            runCatching {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(TRUNCATE)").use { cursor ->
                    while (cursor.moveToNext()) Unit
                }
            }
            val databaseFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            require(databaseFile.exists())
            val resolver = context.contentResolver
            val output = resolver.openOutputStream(target, "wt") ?: error("Cannot open destination")
            ZipOutputStream(output.buffered()).use { zip ->
                zip.addFile("database/${AppDatabase.DATABASE_NAME}", databaseFile)
                File(context.filesDir, "gallery").takeIf(File::exists)?.walkTopDown()
                    ?.filter(File::isFile)
                    ?.forEach { file -> zip.addFile("gallery/${file.relativeTo(File(context.filesDir, "gallery")).invariantSeparatorsPath}", file) }

                val settings = Properties().apply {
                    setProperty(
                        "latin_digits",
                        context.getSharedPreferences("ayda_settings", Context.MODE_PRIVATE)
                            .getBoolean("latin_digits", false).toString(),
                    )
                }
                zip.putNextEntry(ZipEntry("settings.properties"))
                settings.store(zip, "Ayda Nailroom preferences")
                zip.closeEntry()
            }
            true
        }.getOrElse { false }
    }

    suspend fun restore(context: Context, source: Uri): Boolean = withContext(Dispatchers.IO) {
        val temporary = File(context.cacheDir, "ayda-restore-${System.currentTimeMillis()}")
        runCatching {
            temporary.mkdirs()
            val input = context.contentResolver.openInputStream(source) ?: error("Cannot open backup")
            var extractedBytes = 0L
            ZipInputStream(input.buffered()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val destination = File(temporary, entry.name).canonicalFile
                    require(destination.path.startsWith(temporary.canonicalPath + File.separator)) { "Unsafe backup path" }
                    if (entry.isDirectory) {
                        destination.mkdirs()
                    } else {
                        destination.parentFile?.mkdirs()
                        FileOutputStream(destination).use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            while (true) {
                                val read = zip.read(buffer)
                                if (read < 0) break
                                extractedBytes += read
                                require(extractedBytes <= MAX_RESTORE_BYTES) { "Backup is too large" }
                                output.write(buffer, 0, read)
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }

            val restoredDatabase = File(temporary, "database/${AppDatabase.DATABASE_NAME}")
            require(restoredDatabase.isFile && restoredDatabase.length() > 0L) { "Backup database is missing" }
            val appDatabase = File(temporary, "gallery")
            AppDatabase.closeInstance()
            val databaseTarget = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            databaseTarget.parentFile?.mkdirs()
            File(databaseTarget.path + "-wal").delete()
            File(databaseTarget.path + "-shm").delete()
            restoredDatabase.copyTo(databaseTarget, overwrite = true)
            val galleryTarget = File(context.filesDir, "gallery")
            galleryTarget.deleteRecursively()
            if (appDatabase.exists()) appDatabase.copyRecursively(galleryTarget, overwrite = true)
            File(temporary, "settings.properties").takeIf(File::isFile)?.inputStream()?.use { settingsStream ->
                val settings = Properties().apply { load(settingsStream) }
                context.getSharedPreferences("ayda_settings", Context.MODE_PRIVATE).edit()
                    .putBoolean("latin_digits", settings.getProperty("latin_digits")?.toBoolean() ?: false)
                    .apply()
            }
            // Opening it here validates the restored file before reporting success.
            AppDatabase.get(context).openHelper.writableDatabase
            true
        }.getOrElse { false }.also {
            temporary.deleteRecursively()
        }
    }

    private fun ZipOutputStream.addFile(path: String, file: File) {
        putNextEntry(ZipEntry(path))
        FileInputStream(file).use { input -> input.copyTo(this) }
        closeEntry()
    }

    private const val MAX_RESTORE_BYTES = 200L * 1024L * 1024L
}

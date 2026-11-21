package com.aydanailroom.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aydanailroom.app.R
import com.aydanailroom.app.data.GalleryItemEntity
import com.aydanailroom.app.ui.components.ChalkButton
import com.aydanailroom.app.ui.components.ChalkCard
import com.aydanailroom.app.ui.components.ChalkEmptyState
import com.aydanailroom.app.ui.theme.ChalkColors
import com.aydanailroom.app.util.formatShortDate
import java.io.File

@Composable
fun GalleryScreen(
    items: List<GalleryItemEntity>,
    filesDirectory: File,
    latinDigits: Boolean,
    onAdd: () -> Unit,
    onToggleFavorite: (GalleryItemEntity) -> Unit,
    onDelete: (GalleryItemEntity) -> Unit,
) {
    var selected by remember { mutableStateOf<GalleryItemEntity?>(null) }
    var confirmDelete by remember { mutableStateOf<GalleryItemEntity?>(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(R.string.gallery_title), style = MaterialTheme.typography.headlineSmall, color = ChalkColors.Chalk)
                Text(stringResource(R.string.gallery_pick_hint), style = MaterialTheme.typography.bodySmall, color = ChalkColors.ChalkDim)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.PhotoCamera, contentDescription = stringResource(R.string.action_add_photo), tint = ChalkColors.Pink)
            }
        }
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ChalkEmptyState(stringResource(R.string.gallery_empty), doodle = "✧")
                    ChalkButton(stringResource(R.string.action_add_photo), onAdd, Modifier.fillMaxWidth())
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    val imageFile = remember(item.photoPath, filesDirectory) {
                        File(item.photoPath).let { if (it.isAbsolute) it else File(filesDirectory, item.photoPath) }
                    }
                    ChalkCard(modifier = Modifier.rotate(if (item.id % 2L == 0L) -0.8f else 0.8f), contentPadding = 8.dp) {
                        Box {
                            AsyncImage(
                                model = imageFile,
                                contentDescription = stringResource(R.string.gallery_show_to_client),
                                modifier = Modifier.fillMaxWidth().height(176.dp).rotate(if (item.id % 2L == 0L) 0.4f else -0.4f),
                                contentScale = ContentScale.Crop,
                            )
                            IconButton(
                                onClick = { onToggleFavorite(item) },
                                modifier = Modifier.align(Alignment.TopEnd),
                            ) {
                                Icon(
                                    if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = stringResource(R.string.action_favorite),
                                    tint = if (item.isFavorite) ChalkColors.Pink else ChalkColors.Chalk,
                                )
                            }
                            IconButton(
                                onClick = { selected = item },
                                modifier = Modifier.align(Alignment.BottomStart),
                            ) {
                                Icon(Icons.Rounded.Fullscreen, contentDescription = stringResource(R.string.gallery_show_to_client), tint = ChalkColors.Chalk)
                            }
                        }
                        Text(
                            item.title.ifBlank { formatShortDate(LocalContext.current, item.createdAt, latinDigits) },
                            modifier = Modifier.padding(top = 8.dp),
                            color = ChalkColors.Chalk,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }

    selected?.let { item ->
        val imageFile = File(item.photoPath).let { if (it.isAbsolute) it else File(filesDirectory, item.photoPath) }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(item.title.ifBlank { stringResource(R.string.gallery_title) }) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = stringResource(R.string.gallery_show_to_client),
                        modifier = Modifier.fillMaxWidth().height(360.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Text(formatShortDate(LocalContext.current, item.createdAt, latinDigits), color = ChalkColors.ChalkDim, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { onToggleFavorite(item); selected = item.copy(isFavorite = !item.isFavorite) }) {
                    Icon(if (item.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, contentDescription = null, tint = ChalkColors.Pink)
                    Text(stringResource(R.string.action_favorite))
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { confirmDelete = item; selected = null }) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = ChalkColors.Coral)
                        Text(stringResource(R.string.action_delete), color = ChalkColors.Coral)
                    }
                    TextButton(onClick = { selected = null }) { Text(stringResource(R.string.action_close)) }
                }
            },
            containerColor = ChalkColors.BoardDeep,
            titleContentColor = ChalkColors.Chalk,
            textContentColor = ChalkColors.Chalk,
        )
    }
    confirmDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.gallery_delete_confirm)) },
            confirmButton = { TextButton(onClick = { onDelete(item); confirmDelete = null }) { Text(stringResource(R.string.action_delete)) } },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.action_cancel)) } },
            containerColor = ChalkColors.BoardDeep,
            titleContentColor = ChalkColors.Chalk,
            textContentColor = ChalkColors.Chalk,
        )
    }
}

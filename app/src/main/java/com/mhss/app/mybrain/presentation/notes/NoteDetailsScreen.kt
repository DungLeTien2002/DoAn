package com.mhss.app.mybrain.presentation.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.mhss.app.mybrain.R
import com.mhss.app.mybrain.domain.model.Note
import com.mhss.app.mybrain.domain.model.NoteFolder
import com.mhss.app.mybrain.presentation.util.Screen
import com.mhss.app.mybrain.ui.theme.Orange
import com.mhss.app.mybrain.util.date.formatDateDependingOnDay
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun NoteDetailsScreen(
    navController: NavHostController,
    noteId: Int,
    folderId: Int,
    viewModel: NotesViewModel = hiltViewModel()
) {
    LaunchedEffect(true) {
        if (noteId != -1) viewModel.onEvent(NoteEvent.GetNote(noteId))
        if (folderId != -1) viewModel.onEvent(NoteEvent.GetFolder(folderId))
    }
    val state = viewModel.notesUiState
    val scaffoldState = rememberScaffoldState()
    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var openFolderDialog by rememberSaveable { mutableStateOf(false) }

    var title by rememberSaveable { mutableStateOf(state.note?.title ?: "") }
    var content by rememberSaveable { mutableStateOf(state.note?.content ?: "") }
    var pinned by rememberSaveable { mutableStateOf(state.note?.pinned ?: false) }
    val readingMode = state.readingMode
    var folder: NoteFolder? by remember { mutableStateOf(state.folder) }
    val lastModified by remember(state.note) {
        derivedStateOf { state.note?.updatedDate?.formatDateDependingOnDay() }
    }
    val wordCountString by remember {
        derivedStateOf { content.split(" ").size.toString() }
    }


    LaunchedEffect(state.note) {
        if (state.note != null) {
            title = state.note.title
            content = state.note.content
            pinned = state.note.pinned
            folder = state.folder
        }
    }
    LaunchedEffect(state) {
        if (state.navigateUp) {
            openDeleteDialog = false
            navController.popBackStack(route = Screen.NotesScreen.route, inclusive = false)
        }
        if (state.error != null) {
            scaffoldState.snackbarHostState.showSnackbar(
                state.error
            )
            viewModel.onEvent(NoteEvent.ErrorDisplayed)
        }
        if (state.folder != folder) folder = state.folder
    }
    IconButton(onClick = {
        addOrUpdateNote(
            Note(
                title = title,
                content = content,
                pinned = pinned,
                folderId = folder?.id
            ),
            state.note,
            onNotChanged = {
                navController.popBackStack(
                    route = Screen.NotesScreen.route,
                    inclusive = false
                )
            },
            onUpdate = {
                if (state.note != null) {
                    viewModel.onEvent(
                        NoteEvent.UpdateNote(
                            state.note.copy(
                                title = title,
                                content = content,
                                folderId = folder?.id
                            )
                        )
                    )
                } else {
                    viewModel.onEvent(
                        NoteEvent.AddNote(
                            Note(
                                title = title,
                                content = content,
                                pinned = pinned,
                                folderId = folder?.id
                            )
                        )
                    )
                }
                navController.popBackStack(
                    route = Screen.NotesScreen.route,
                    inclusive = false
                )
            }
        )
    }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_save), // Biểu tượng lưu
            contentDescription = stringResource(R.string.save)
        )
    }
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    // Tiêu đề và các thành phần khác
                },
                actions = {
                    // Các nút khác như xóa, ghim, đọc...
                    IconButton(onClick = {
                        addOrUpdateNote(
                            Note(
                                title = title,
                                content = content,
                                pinned = pinned,
                                folderId = folder?.id
                            ),
                            state.note,
                            onNotChanged = {
                                navController.popBackStack(
                                    route = Screen.NotesScreen.route,
                                    inclusive = false
                                )
                            },
                            onUpdate = {
                                if (state.note != null) {
                                    viewModel.onEvent(
                                        NoteEvent.UpdateNote(
                                            state.note.copy(
                                                title = title,
                                                content = content,
                                                folderId = folder?.id
                                            )
                                        )
                                    )
                                } else {
                                    viewModel.onEvent(
                                        NoteEvent.AddNote(
                                            Note(
                                                title = title,
                                                content = content,
                                                pinned = pinned,
                                                folderId = folder?.id
                                            )
                                        )
                                    )
                                }
                                navController.popBackStack(
                                    route = Screen.NotesScreen.route,
                                    inclusive = false
                                )
                            }
                        )
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_save), // Biểu tượng lưu
                            contentDescription = stringResource(R.string.save)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        },

        ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(text = stringResource(R.string.title)) },
                shape = RoundedCornerShape(15.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            if (readingMode)
                MarkdownText(
                    markdown = content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(8.dp),
                    linkColor = Color.Blue,
                    style = MaterialTheme.typography.body1.copy(
                        color = MaterialTheme.colors.onBackground
                    )
                )
            else
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(text = stringResource(R.string.note_content))
                    },
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lastModified ?: "",
                    style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                )
                Text(
                    text = wordCountString,
                    style = MaterialTheme.typography.caption.copy(color = Color.Gray)
                )
            }
        }
        if (openDeleteDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_note_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_note_confirmation_message,
                            state.note?.title!!
                        )
                    )
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            viewModel.onEvent(NoteEvent.DeleteNote(state.note!!))
                        },
                    ) {
                        Text(stringResource(R.string.delete_note), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        shape = RoundedCornerShape(25.dp),
                        onClick = {
                            openDeleteDialog = false
                        }) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                }
            )
        if (openFolderDialog)
            AlertDialog(
                shape = RoundedCornerShape(25.dp),
                onDismissRequest = { openFolderDialog = false },
                title = { Text(stringResource(R.string.change_folder)) },
                text = {
                    FlowRow {
                        Row(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                .clickable {
                                    folder = null
                                    openFolderDialog = false
                                }
                                .background(if (folder == null) MaterialTheme.colors.onBackground else Color.Transparent),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.none),
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.body1,
                                color = if (folder == null) MaterialTheme.colors.background else MaterialTheme.colors.onBackground
                            )
                        }
                        state.folders.forEach {
                            Row(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(25.dp))
                                    .clickable {
                                        folder = it
                                        openFolderDialog = false
                                    }
                                    .background(if (folder?.id == it.id) MaterialTheme.colors.onBackground else Color.Transparent),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_folder),
                                    stringResource(R.string.folders),
                                    modifier = Modifier.padding(
                                        start = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                    tint = if (folder?.id == it.id) MaterialTheme.colors.background else MaterialTheme.colors.onBackground
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = it.name,
                                    modifier = Modifier.padding(
                                        end = 8.dp,
                                        top = 8.dp,
                                        bottom = 8.dp
                                    ),
                                    style = MaterialTheme.typography.body1,
                                    color = if (folder?.id == it.id) MaterialTheme.colors.background else MaterialTheme.colors.onBackground
                                )
                            }
                        }
                    }
                },
                buttons = {}
            )
    }
}

private fun addOrUpdateNote(
    newNote: Note,
    note: Note? = null,
    onNotChanged: () -> Unit = {},
    onUpdate: (Note) -> Unit,
) {
    if (note != null) {
        if (noteChanged(newNote, note))
            onUpdate(note)
        else
            onNotChanged()
    } else {
        onUpdate(newNote)
    }
}

private fun noteChanged(
    note: Note,
    newNote: Note
): Boolean {
    return note.title != newNote.title ||
            note.content != newNote.content ||
            note.folderId != newNote.folderId
}
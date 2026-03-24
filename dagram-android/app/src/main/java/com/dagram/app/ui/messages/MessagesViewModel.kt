package com.dagram.app.ui.messages

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.Message
import com.dagram.app.data.repository.ChatRepository
import com.dagram.app.data.repository.Result
import com.dagram.app.utils.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MessagesUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val error: String? = null,
    val isSending: Boolean = false,
    val isTyping: Boolean = false
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wsManager: WebSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: Int = savedStateHandle.get<Int>("chatId") ?: 0

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String? = null

    init {
        loadMessages()
        wsManager.joinChat(chatId)
        observeWebSocket()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = chatRepository.getMessages(chatId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, messages = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun onMessageTextChange(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(messageText = "", isSending = true)

        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(chatId, text)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messageText = text,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun sendMediaMessage(uri: Uri, type: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true, error = null)
            when (val result = chatRepository.uploadAndSendMedia(chatId, uri, type)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    fun startRecording(context: Context) {
        try {
            val outputDir = context.cacheDir
            val outputFile = File.createTempFile("voice_", ".aac", outputDir)
            audioFilePath = outputFile.absolutePath

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = "فشل في بدء التسجيل")
            mediaRecorder = null
            audioFilePath = null
        }
    }

    fun stopRecordingAndSend() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val filePath = audioFilePath ?: return
            audioFilePath = null

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSending = true, error = null)
                when (val result = chatRepository.sendAudioMessage(chatId, filePath)) {
                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            messages = _uiState.value.messages + result.data
                        )
                    }
                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            error = result.message
                        )
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            mediaRecorder = null
            _uiState.value = _uiState.value.copy(error = "فشل في إيقاف التسجيل")
        }
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null
        audioFilePath?.let { File(it).delete() }
        audioFilePath = null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            wsManager.events.collect { event ->
                when (event.type) {
                    "new_message" -> {
                        val msg = event.message ?: return@collect
                        if (msg.chatId == chatId) {
                            val current = _uiState.value.messages
                            if (!current.any { it.id == msg.id }) {
                                _uiState.value = _uiState.value.copy(
                                    messages = current + msg
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager.leaveChat(chatId)
        cancelRecording()
    }
}

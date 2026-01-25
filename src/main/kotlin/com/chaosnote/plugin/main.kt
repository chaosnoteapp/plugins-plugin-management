package com.chaosnote.plugin

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.chaosnote.api.block.BlockHandle

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Chaosnote Plugin Store") {
        MaterialTheme {
            PluginManagementPlugin().Render(FakeBlockHandle(""))
        }
    }
}

class FakeBlockHandle(override val payload: String) : BlockHandle {
    override val id: String = "plugin_management"
    override fun update(value: String) {
    }
}
// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.overlay.editor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OverlayLayout(
    val elements: MutableList<OverlayElement> = mutableListOf()
)

@Serializable
sealed class OverlayElement {
    abstract val id: String
    abstract val type: ElementType
    abstract var x: Int
    abstract var y: Int
    abstract var scale: Float

    @Serializable
    @SerialName("button")
    data class Button(
        override val id: String,
        override var x: Int,
        override var y: Int,
        override var scale: Float = 1.0f,
        // When mappingType == "control", controlId must be provided
        val mappingType: MappingType = MappingType.CONTROL,
        val controlId: Int? = null,
        // When mappingType == "action", actionKey must be provided
        val actionKey: String? = null,
        // Optional appearance key, e.g., "wiimote_a", "wiimote_b", "generic"
        val appearance: String? = null
    ) : OverlayElement() {
        override val type: ElementType = ElementType.BUTTON
    }

    @Serializable
    @SerialName("dpad")
    data class DPad(
        override val id: String,
        override var x: Int,
        override var y: Int,
        override var scale: Float = 1.0f,
        val upControlId: Int,
        val downControlId: Int,
        val leftControlId: Int,
        val rightControlId: Int
    ) : OverlayElement() {
        override val type: ElementType = ElementType.DPAD
    }

    @Serializable
    @SerialName("joystick")
    data class Joystick(
        override val id: String,
        override var x: Int,
        override var y: Int,
        override var scale: Float = 1.0f,
        val xControlId: Int,
        val yControlId: Int
    ) : OverlayElement() {
        override val type: ElementType = ElementType.JOYSTICK
    }
}

@Serializable
enum class ElementType { BUTTON, DPAD, JOYSTICK }

@Serializable
enum class MappingType { CONTROL, ACTION }

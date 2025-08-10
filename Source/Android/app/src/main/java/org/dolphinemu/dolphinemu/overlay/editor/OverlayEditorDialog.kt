// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.overlay.editor

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dolphinemu.dolphinemu.R
import org.dolphinemu.dolphinemu.features.input.model.InputOverrider.ControlId
import java.util.UUID

class OverlayEditorDialog : DialogFragment() {
    interface Listener {
    fun onAddElement(element: OverlayElement, gameSpecific: Boolean)
        fun onSaveLayout(gameSpecific: Boolean)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_overlay_editor, null)
    val typeSpinner: Spinner = view.findViewById(R.id.type_spinner)
    val buttonControlSpinner: Spinner = view.findViewById(R.id.button_control_spinner)
    val scopeSwitch: CheckBox = view.findViewById(R.id.scope_switch)
    val mappingSpinner: Spinner = view.findViewById(R.id.mapping_spinner)
    val labelType: TextView = view.findViewById(R.id.label_type)
    val labelMapping: TextView = view.findViewById(R.id.label_mapping)
    val labelMappingType: TextView = view.findViewById(R.id.label_mapping_type)

        val types = listOf("Button", "D-Pad", "Joystick")
    typeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

        val wiimoteButtons = listOf(
            Pair("Wiimote A", ControlId.WIIMOTE_A_BUTTON),
            Pair("Wiimote B", ControlId.WIIMOTE_B_BUTTON),
            Pair("Wiimote 1", ControlId.WIIMOTE_ONE_BUTTON),
            Pair("Wiimote 2", ControlId.WIIMOTE_TWO_BUTTON),
            Pair("Plus", ControlId.WIIMOTE_PLUS_BUTTON),
            Pair("Minus", ControlId.WIIMOTE_MINUS_BUTTON),
            Pair("Home", ControlId.WIIMOTE_HOME_BUTTON)
        )
    val controlLabels = wiimoteButtons.map { it.first }
    val controlAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controlLabels)
    buttonControlSpinner.adapter = controlAdapter

    // Mapping choices: control vs special actions
    val mappings = listOf(
        "Control",
        "Action: Toggle Sideways/Upright",
        "Action: Toggle IR Recenter",
        "Action: Cycle IR Mode"
    )
    mappingSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappings)

        // Default scope: if not checked, save as global instead of doing nothing
        scopeSwitch.isChecked = true

        // Hide irrelevant fields for D-Pad/Joystick; show only for Button
        fun refreshVisibility() {
            val isButton = typeSpinner.selectedItemPosition == 0
            val vis = if (isButton) View.VISIBLE else View.GONE
            labelMapping.visibility = vis
            buttonControlSpinner.visibility = vis
            labelMappingType.visibility = vis
            mappingSpinner.visibility = vis
        }
        refreshVisibility()
        typeSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshVisibility()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

        return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.overlay_editor_title)
        .setView(view)
            .setPositiveButton(R.string.add) { _, _ ->
        when (typeSpinner.selectedItemPosition) {
                    0 -> { // Button
            val idx = buttonControlSpinner.selectedItemPosition
                        val controlId = wiimoteButtons[idx].second
                        val selectedMapIndex = mappingSpinner.selectedItemPosition
                        val isAction = selectedMapIndex != 0
                        val actionKey = when (selectedMapIndex) {
                            1 -> "toggle_sideways"
                            2 -> "toggle_ir_recenter"
                            3 -> "cycle_ir_mode"
                            else -> null
                        }
                        // Infer an appearance from the chosen control so the icon isn't always 'A'
                        val appearanceKey = when (controlId) {
                            ControlId.WIIMOTE_B_BUTTON -> "wiimote_b"
                            ControlId.WIIMOTE_ONE_BUTTON -> "wiimote_one"
                            ControlId.WIIMOTE_TWO_BUTTON -> "wiimote_two"
                            ControlId.WIIMOTE_PLUS_BUTTON -> "wiimote_plus"
                            ControlId.WIIMOTE_MINUS_BUTTON -> "wiimote_minus"
                            ControlId.WIIMOTE_HOME_BUTTON -> "wiimote_home"
                            else -> null
                        }
                        val element = OverlayElement.Button(
                            id = UUID.randomUUID().toString(),
                            x = 100, y = 100, scale = 1.0f,
                            controlId = if (!isAction) controlId else null,
                            mappingType = if (isAction) MappingType.ACTION else MappingType.CONTROL,
                            actionKey = actionKey,
                            appearance = appearanceKey
                        )
                        (activity as? Listener)?.onAddElement(element, scopeSwitch.isChecked)
                    }
                    1 -> { // D-Pad
                        val element = OverlayElement.DPad(
                            id = UUID.randomUUID().toString(),
                            x = 100, y = 100, scale = 1.0f,
                            upControlId = ControlId.WIIMOTE_DPAD_UP,
                            downControlId = ControlId.WIIMOTE_DPAD_DOWN,
                            leftControlId = ControlId.WIIMOTE_DPAD_LEFT,
                            rightControlId = ControlId.WIIMOTE_DPAD_RIGHT
                        )
                        (activity as? Listener)?.onAddElement(element, scopeSwitch.isChecked)
                    }
                    2 -> { // Joystick (default to Classic left stick; future: add assignment)
                        val element = OverlayElement.Joystick(
                            id = UUID.randomUUID().toString(),
                            x = 100, y = 100, scale = 1.0f,
                            xControlId = ControlId.CLASSIC_LEFT_STICK_X,
                            yControlId = ControlId.CLASSIC_LEFT_STICK_Y
                        )
                        (activity as? Listener)?.onAddElement(element, scopeSwitch.isChecked)
                    }
                }
            }
            .setNeutralButton(R.string.save) { _, _ ->
        (activity as? Listener)?.onSaveLayout(scopeSwitch.isChecked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }
}

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
    val labelMapping: TextView = view.findViewById(R.id.label_mapping)
    val labelMappingType: TextView = view.findViewById(R.id.label_mapping_type)
    val labelIrAccel: TextView = view.findViewById(R.id.label_ir_accel)
    val irAccelSpinner: Spinner = view.findViewById(R.id.ir_accel_spinner)
    val labelIrSpeed: TextView = view.findViewById(R.id.label_ir_speed)
    val irSpeedSpinner: Spinner = view.findViewById(R.id.ir_speed_spinner)

    val types = listOf("Button", "IR Pointer to Stick")
    typeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)

        val wiimoteButtons = listOf(
            // Core buttons; strip the word "Wiimote" and use symbols for plus/minus
            "A" to ControlId.WIIMOTE_A_BUTTON,
            "B" to ControlId.WIIMOTE_B_BUTTON,
            "1" to ControlId.WIIMOTE_ONE_BUTTON,
            "2" to ControlId.WIIMOTE_TWO_BUTTON,
            "+" to ControlId.WIIMOTE_PLUS_BUTTON,
            "-" to ControlId.WIIMOTE_MINUS_BUTTON,
            "Home" to ControlId.WIIMOTE_HOME_BUTTON
        )
    val controlLabels = wiimoteButtons.map { it.first }
    val controlAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, controlLabels)
    buttonControlSpinner.adapter = controlAdapter

    // Mapping choices: control vs special actions (includes Motion Simulation placeholders)
    val mappings = listOf(
        "Control",
        "Action: Toggle Sideways/Upright",
        "Action: Save State (Quick)",
        "Action: Load State (Quick)",
        "Action: Recenter IR",
        "Action: Cycle Quick Save Slot",
        // Unified Wiimote shake action
        "Action: Wiimote Shake"
    )
    mappingSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, mappings)

    // Keep original captions; spacing handled in layout padding for alignment

        // Default scope: if not checked, save as global instead of doing nothing
        scopeSwitch.isChecked = true

        // Hide irrelevant fields; show mapping controls only for Button
        fun refreshVisibility() {
            val isButton = typeSpinner.selectedItemPosition == 0
            val showBtn = if (isButton) View.VISIBLE else View.GONE
            labelMapping.visibility = showBtn
            buttonControlSpinner.visibility = showBtn
            labelMappingType.visibility = showBtn
            mappingSpinner.visibility = showBtn

            val isIrPointer = typeSpinner.selectedItemPosition == 1
            val showIr = if (isIrPointer) View.VISIBLE else View.GONE
            labelIrAccel.visibility = showIr
            irAccelSpinner.visibility = showIr
            labelIrSpeed.visibility = showIr
            irSpeedSpinner.visibility = showIr
        }
    // Populate IR accel/speed spinners with 5 meaningful values
    val accelValues = listOf(1.5f, 2.0f, 3.0f, 4.0f, 5.0f)
    val speedValues = listOf(0.8f, 1.0f, 1.5f, 2.0f, 2.5f)
    irAccelSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, accelValues.map { "x$it" })
    irSpeedSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, speedValues.map { "x$it" })
    // Preselect from saved prefs
    val prefs = requireContext().getSharedPreferences("OverlayPrefs", 0)
    val savedAccel = prefs.getFloat("OverlayIrJoyAccel", 3.0f)
    val savedSpeed = prefs.getFloat("OverlayIrJoyMaxVel", 1.5f)
    irAccelSpinner.setSelection(accelValues.indexOfFirst { it == savedAccel }.coerceAtLeast(0))
    irSpeedSpinner.setSelection(speedValues.indexOfFirst { it == savedSpeed }.coerceAtLeast(0))
    // Persist immediately when changed so runtime ticker picks it up
    irAccelSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
            prefs.edit().putFloat("OverlayIrJoyAccel", accelValues[position]).apply()
        }
        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }
    irSpeedSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
            prefs.edit().putFloat("OverlayIrJoyMaxVel", speedValues[position]).apply()
        }
        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
    }
        refreshVisibility()
        typeSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshVisibility()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })

    // Build dialog with only a single Save action on the right (acts like the previous Add)
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.overlay_editor_title)
        .setView(view)
    .setPositiveButton(R.string.save) { _, _ ->
            when (typeSpinner.selectedItemPosition) {
                0 -> { // Button
                    val idx = buttonControlSpinner.selectedItemPosition
                    val controlId = wiimoteButtons[idx].second
                    val selectedMapIndex = mappingSpinner.selectedItemPosition
                    val isAction = selectedMapIndex != 0
                    val actionKey = when (selectedMapIndex) {
                        1 -> "toggle_sideways"
                        2 -> "save_state_quick"
                        3 -> "load_state_quick"
                        4 -> "recenter_ir"
                        5 -> "cycle_quick_saveslot"
                        6 -> "wiimote_shake"
                        else -> null
                    }
                    // Infer an appearance from the chosen control so the icon isn't always 'A'
                    val appearanceKey = when (controlId) {
                        ControlId.WIIMOTE_A_BUTTON -> "wiimote_a"
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
                    1 -> { // IR Pointer to Stick
                    val element = OverlayElement.Joystick(
                        id = UUID.randomUUID().toString(),
                        x = 100, y = 100, scale = 1.0f,
                        xControlId = ControlId.WIIMOTE_IR_X,
                        yControlId = ControlId.WIIMOTE_IR_Y
                    )
                    (activity as? Listener)?.onAddElement(element, scopeSwitch.isChecked)
                }
            }
            // Persist current layout to the chosen scope as part of Save
            (activity as? Listener)?.onSaveLayout(scopeSwitch.isChecked)
        }
        .create()
    }
}

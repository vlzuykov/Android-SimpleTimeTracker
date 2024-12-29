package com.example.util.simpletimetracker.core.dialog

import com.example.util.simpletimetracker.domain.record.model.Range

interface CustomRangeSelectionDialogListener {

    fun onCustomRangeSelected(range: Range)
}
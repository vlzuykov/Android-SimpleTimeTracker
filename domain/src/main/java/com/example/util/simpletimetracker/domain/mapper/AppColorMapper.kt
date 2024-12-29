package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.color.model.AppColor

interface AppColorMapper {

    fun mapToColorInt(color: AppColor): Int

    fun mapToHsv(colorInt: Int): FloatArray
}
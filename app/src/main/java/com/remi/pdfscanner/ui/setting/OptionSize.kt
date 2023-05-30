package com.remi.pdfscanner.ui.setting

enum class OptionSize(val ratio: Float){
    A3(297f/420),
    A4(210f/297),
    A5(148f/210),
    B4(250f/353),
    B5(176f/250),
    LETTER(0f),
    LEGAL(0f),
    EXECUTIVE(0f),
    BUSINESS_CARD(0f)
}

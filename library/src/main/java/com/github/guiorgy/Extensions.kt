package com.github.guiorgy

fun TwowaySeekBar.setMinProgressStaysProportion(minValue: Double) {
    val normalizedProgress = this.normalizedValue
    this.min = minValue
    this.normalizedValue = normalizedProgress
}

fun TwowaySeekBar.setMinProgressFollowsAbsolute(minValue: Double) {
    val progress = this.value + minValue - this.min
    this.min = minValue
    this.value = progress
}

fun TwowaySeekBar.setMaxProgressStaysProportion(maxValue: Double) {
    val normalizedProgress = this.normalizedValue
    this.max = maxValue
    this.normalizedValue = normalizedProgress
}

fun TwowaySeekBar.setMaxProgressFollowsAbsolute(maxValue: Double) {
    val progress = this.value + maxValue - this.max
    this.max = min
    this.value = progress
}
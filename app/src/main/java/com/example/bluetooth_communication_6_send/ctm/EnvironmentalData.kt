package com.example.dogactivityplanner.ctm

data class EnvironmentalData(
    val Tdb: Double = 1.39,       // 室温
    val Ta: Double = Tdb,         // 大気温度（室温と同じ値をデフォルトとして使用）
    val RH: Double = 62.4,       // 相対湿度
    val SR: Double = 800.0,      // 太陽放射
    val WS: Double = 1.1,        // 風速
    val PB: Double = 101325.0,   // 気圧

    val Tfur: Double = Tdb,      // todo 毛皮の温度（気温と同じ値をデフォルトとして使用）
    val hin: Double = RH,        // 吸入空気の湿度（相対湿度と同じ値をデフォルトとして使用）
    val hex: Double = RH + 10,   // todo 排出空気の湿度（相対湿度+10をデフォルトとして使用）
    val wet: Double = 2.1,       // todo 皮膚の濡れ具合？
    val esw: Double = 0.1        // todo 汗による熱損失？

)
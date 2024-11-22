data class DogData(
    val DTM: Double = 1.0,            // 時間間隔(1分)
    val TTCR: Double = 38.3,         // 初期深部温度(38.3℃)

    val fur_thick: Double = 10.0,        // 毛皮の厚さ(10mm)
    val ipcl: Double = 0.45,            // 毛皮の水蒸気透過性(0.45)
    val alpha: Double = 0.05,          // 体重に対する皮膚の重量(0.05)
    val abf: Double = 0.8,             // 毛皮の放射吸収性(0.8)
    val CB: Double = 1.163,            // コア血液熱(1.163)
    val CMIN: Double = 5.28,           // コア-皮膚コンダクタンス(5.28)
    val Cc: Double = 0.97,             // コアの比熱(0.97)
    val Lnth: Double=100.0,          // 体長
    val Wt: Double=30.0,             // 体重
    val MET: Double=7.0,             // mets
    val Bfat: Double=15.0,           // 体脂肪率
    val PantVent1: Double = TTCR + 0.1,   // パンティング開始温度(TTCR+0.1℃)
    val PantVent2: Double = TTCR + 1.0,   // パンティング増加温度(TTCR+1.0℃)
    val TTCRshiv: Double = TTCR - 0.8,    // シバリング開始温度(TTCR-0.8℃)
    val TTSK:Double=TTCR-1.0,              // todo 皮膚温度(要検討)

    val maxTTCR: Double = 40.0        // 最大深部温度(40℃)

)

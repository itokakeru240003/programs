package com.example.dogactivityplanner.ctm

import DogData
import android.util.Log
import kotlin.math.exp
import kotlin.reflect.KMutableProperty


class CTM(dogData: DogData):Cloneable {
    //todo Tdb,SR,Ta,RH,PB,Lnth,Wt,furthick,TTCR,TTCRshiv,Bfat,MET,WS,Tfur,abf,TTSK,ipcl,Psk,alpha,Wv,hex,hin,TTCRs,CMIN,CB,Cc,DTM,wet,esw

    //環境条件
    //周囲温度
    var Ta: Double = 0.0

    //相対湿度
    var RH: Double = 0.0

    //日射量
    var SR: Double = 0.0

    //平均放射温度
    var MRT: Double = 0.0

    //乾球温度
    var Tdb: Double = 0.0

    //放射温度差
    var dTr: Double = 0.0

    //飽和蒸気圧
    var PsT: Double = 0.0
    var Tdp: Double = 0.0
    var Pa: Double = 0.0
    var PB: Double = 0.0
    var Lnth: Double = 0.0
    var Wt: Double = 0.0
    var Wa: Double = 0.0
    var BSA: Double = 0.0
    var BSAfur: Double = 0.0
    var fur_thick: Double = 0.0
    var Ffur: Double = 0.0
    var Shiver: Double = 0.0
    var TTCR: Double = 0.0
    var TTCRshiv: Double = 0.0
    var Bfat: Double = 0.0
    var BasalM: Double = 0.0
    var M: Double = 0.0
    var MET: Double = 0.0
    var Rf: Double = 0.0
    var Rf_clo: Double = 0.0
    var CHCA: Double = 0.0
    var CHCV: Double = 0.0
    var WS: Double = 0.0
    var hcod: Double = 0.0
    var qf_conv: Double = 0.0
    var QF_conv: Double = 0.0
    var Tfur: Double = 0.0
    var hrod: Double = 0.0
    var abf: Double = 0.0
    var HROD: Double = 0.0
    var qf_rad: Double = 0.0
    var QF_rad: Double = 0.0
    var Qf_dry: Double = 0.0
    var Rfur: Double = 0.0
    var TTSK: Double = 0.0
    var Rbound: Double = 0.0
    var Rd: Double = 0.0
    var RdT: Double = 0.0
    var Rpf: Double = 0.0
    var ipcl: Double = 0.0
    var hpd: Double = 0.0
    var Rpbound: Double = 0.0
    var Rp: Double = 0.0
    var qf_vap: Double = 0.0
    var QF_vap: Double = 0.0
    var Psk: Double = 0.0
    var Qf: Double = 0.0
    var Skbf: Double = 0.0
    var SKBF: Double = 0.0
    var alpha: Double = 0.0
    var Va_res: Double = 0.0
    var effwt: Double = 0.0
    var Win: Double = 0.0
    var Pin: Double = 0.0
    var Spv: Double = 0.0
    var mres: Double = 0.0
    var qres_dry: Double = 0.0
    var qres_vap: Double = 0.0
    var Wv: Double = 0.0022
    var PantVent: Double = 0.0
    var PantVent1: Double = 0.0
    var PantVent2: Double = 0.0
    var qkc: Double = 0.0
    var CMIN: Double = 0.0
    var CB: Double = 0.0
    var Wc: Double = 0.0
    var TCCR: Double = 0.0
    var Cc: Double = 0.0
    var HSCR: Double = 0.0
    var DTM: Double = 0.0
    var emax: Double = 0.0
    var edif: Double = 0.0
    var wet: Double = 0.0
    var esw: Double = 0.0
    var Tood: Double = 0.0
    var qsk_dry: Double = 0.0
    var qsk_vap: Double = 0.0
    var HSSK: Double = 0.0

    var maxTTCR: Double = 0.0
    var Ti: Double = 0.0
    var Tex: Double = 0.0
    var Pex: Double = 0.0
    var PsTex: Double = 0.0
    var hi: Double = 0.0
    var he: Double = 0.0
    var RHi: Double = 0.0
    var RHex: Double = 0.0
    var SAHi: Double = 0.0
    var SAHex: Double = 0.0
    var AHi: Double = 0.0
    var AHex: Double = 0.0
    var AirDensityi: Double = 0.0
    var AirDensityex: Double = 0.0
    var wi: Double = 0.0
    var wex: Double = 0.0


    init {
        this.DTM = dogData.DTM
        this.TTCR = dogData.TTCR
        this.TTSK = dogData.TTSK
        this.PantVent1 = dogData.PantVent1
        this.PantVent2 = dogData.PantVent2
        this.TTCRshiv = dogData.TTCRshiv
        this.fur_thick = dogData.fur_thick
        this.ipcl = dogData.ipcl
        this.alpha = dogData.alpha
        this.abf = dogData.abf
        this.CB = dogData.CB
        this.CMIN = dogData.CMIN
        this.Cc = dogData.Cc
        this.Lnth = dogData.Lnth
        this.Wt = dogData.Wt
        this.MET = dogData.MET
        this.Bfat = dogData.Bfat
        this.maxTTCR = dogData.maxTTCR
    }

    public override fun clone(): CTM {
        return super.clone() as CTM
    }

    //コンストラクタに移行済み
    fun setDefaultValues() {
        //todo 任意の値を設定できるように
        DTM = 1.0 //時間間隔(1分)
        TTCR = 38.3 //初期深部温度(38.3℃)
        PantVent1 = TTCR + 0.1 //パンティング開始温度(TTCR+0.1℃)
        PantVent2 = TTCR + 1.0 //パンティング増加温度(TTCR+1.0℃)
        TTCRshiv = TTCR - 0.8 //シバリング開始温度(TTCR-0.8℃)
        fur_thick = 10.0 //毛皮の厚さ(10mm)
        ipcl = 0.45 //毛皮の水蒸気透過性(0.45)
        alpha = 0.05 //体重に対する皮膚の重量(0.05)
        abf = 0.8 //毛皮の放射吸収性(0.8)
        CB = 1.163 //コア血液熱(1.163)
        CMIN = 5.28 //コア-皮膚コンダクタンス(5.28)
        Cc = 0.97 //コアの比熱(0.97)

        Lnth = 100.0 //体長
        Wt = 30.0 //体重
        MET = 7.0//mets
        Bfat = 15.0 //体脂肪率


    }

    //移行済み
    fun setEnvironmentalValue() {
        Tdb = 28.4
        Ta = Tdb
        RH = 62.4
        SR = 800.0
        WS = 1.1
        PB = 100000.0


        Tfur = Tdb
        wet = 0.1
        esw = 0.0


    }

    fun setEnvironmentalValue(envData: EnvironmentalData) {
        Tdb = envData.Tdb
        Ta = envData.Ta
        RH = envData.RH
        SR = envData.SR
        WS = envData.WS
        PB = envData.PB

        Tfur = envData.Tfur
        wet = envData.wet
        esw = envData.esw

        this.initializeModel()
    }

    fun calculateNextTemperature() {
        calculatePhysiologicalParameter()
        calculateHeatTransferFromFur()
        calculateRespiratoryHeatLoss()
        calculateHeatTransferFromCoreToSkin()

    }

    fun calculateNextTemperature(envData: EnvironmentalData) {
        setEnvironmentalValue(envData)
        calculatePhysiologicalParameter()
        calculateHeatTransferFromFur()
        calculateRespiratoryHeatLoss()
        calculateHeatTransferFromCoreToSkin()
    }

    fun initializeModel() {

        MRT = Math.pow(Math.pow(Tdb + 273.15, 4.0) + SR / (2 * 0.000000567), 0.25) - 273.15

        dTr = MRT - Tdb
        //T→Ta
        PsT = 133.32 * exp(18.6686 - (4030.183 / (Ta + 235)))
        //??
        Pa = RH * PsT / 100
        //Tdp = (1 / 0.06) * ln(Pa / 5.138)
        Tdp = 237.3 * Math.log10(6.1078 * 100 / Pa) / (Math.log10(Pa / (6.1078 * 100)) - 7.5)
        Wa = 0.622 * Pa / (PB - Pa)
        BSA = 2.268 * Lnth * Math.pow(Wt * 1000, 0.367) / 10000
        val r = Lnth / (2.0 * 100)
        val a = r//+(fur_thick/1000)
        BSAfur = 4 * Math.PI * Math.pow(
            (Math.pow(2 * a * a, 1.6075) + Math.pow(r, 1.6075)) / 3,
            1 / 1.6075
        )
        BSAfur = BSA
        Ffur = BSAfur / BSA

    }

    private fun calculatePhysiologicalParameter() {
        if (TTCR < TTCRshiv) {
            Shiver = 156 * (TTCRshiv - TTCR) / Math.pow(Bfat, 0.5)
        }
        BasalM = 3.5 * Math.pow(Wt, 0.75)
        M = MET * BasalM + Shiver

        Rf = 0.155 * 3.19 * fur_thick / 25.4
        Rf_clo = 3.19 * fur_thick / 25.4
    }

    private fun calculateHeatTransferFromFur() {
        CHCA = 5.56 * Math.pow(MET - 0.85, 0.39)
        CHCV = 8.6 * Math.pow(WS, 0.53)
        CHCV = if (CHCV <= 3.0) 3.0 else CHCV
        hcod = if (CHCV >= CHCA) CHCV else CHCA
        qf_conv = Ffur * hcod * (Tfur - MRT)
        QF_conv = qf_conv * BSA

        hrod =
            abf * 4 * 0.725 * 5.67 * Math.pow(10.0, -8.0) * Math.pow(((Tfur + MRT) / 2) + 273, 3.0)
        HROD = hrod * BSA
        qf_rad = Ffur * hrod * (Tfur - MRT)
        QF_rad = qf_rad * BSA

        Qf_dry = QF_conv + QF_rad

        Rfur = (hcod * Ta + hrod * MRT + TTSK / (BSAfur * Rf)) / (hcod + hrod + 1 / (BSAfur * Rf))
        Rbound = 1 / ((hcod + hrod) * Rfur)
        Rd = Rbound + Rf
        RdT = Rd / BSA

        val LR = 2.2
        Rpf = Rf / (LR * ipcl)
        hpd = LR * hcod
        Rpbound = 1 / (hpd * Ffur)
        Rp = Rpf + Rpbound
//todo copilotにかかせた
        Psk = Pa + 0.5 * Wv * 1000

        qf_vap = (Psk - Pa) / Rp
        QF_vap = qf_vap * BSA

        Qf = QF_vap + Qf_dry


    }

    private fun calculateRespiratoryHeatLoss() {
        Skbf = 4.6009 + 9.1753 * MET - 4.5601 * Math.pow(MET, 2.0) + 0.8558 * Math.pow(
            MET,
            3.0
        ) - 0.0749 * Math.pow(MET, 4.0) + 0.0031 * Math.pow(MET, 5.0) - 0.00005 * Math.pow(MET, 6.0)
        SKBF = Skbf * 0.6 * alpha * Wt / BSA
        Va_res = 7.2564 * MET + 0.8057
        effwt = Math.pow(Wt, 0.75)

        //todo 修正
        Pin = Pa

        Win = 0.662 * Pin / (PB - Pin)
        Spv = 0.287 * (Ta + 273.15) * 2.6078 * Win / (PB * 0.133 / 1000)
        mres = Va_res * effwt / (1000 * Spv * BSA)

        Ti = Ta
        Tex = Ti + 0.82 * (TTCR - Ti)
        PsTex = 133.32 * exp(18.6686 - (4030.183 / (Ti + 235)))
        Pex = Pin + 0.84 * (PsTex - Pin)

        RHi = RH
        RHex = 100 * Pex / PsTex
        SAHi = 0.622 * Pin / (PB - Pin)
        SAHex = 0.622 * Pex / (PB - Pex)
        AHi = RHi * 0.01 * SAHi
        AHex = RHex * 0.01 * SAHex
        AirDensityi = PB * 0.0289 / (8.314 * (Ti + 273.15))
        AirDensityex = PB * 0.0289 / (8.314 * (Tex + 273.15))
        wi = AHi / AirDensityi
        wex = AHex / AirDensityex



        hi = 0.24 * (Ti - TTCR) + 0.444 * wi * (Ti - TTCR) + 1098.6 * wi
        he = 0.24 * (Tex - TTCR) + 0.444 * wex * (Tex - TTCR) + 1098.6 * wex








        qres_dry = mres * (he - hi) * 2326 / 3600
        qres_vap = Wv * (he - hi) * 0.278

        if (TTCR > PantVent1)
            PantVent = if (TTCR > PantVent2) {
                1.5 * Wt * (0.00137 * Wt + 0.4457)
            } else {
                Wt * (0.00137 * Wt + 0.4457)
            }
    }

    private fun calculateHeatTransferFromCoreToSkin() {
        qkc = (CMIN + CB * Skbf) * (TTCR - TTSK)
        Wc = (1 - alpha) * Wt
        TCCR = Cc * Wc
        //??
        HSCR = M - qkc - (qres_dry + qres_vap)

        TTCR = TTCR + HSCR * BSA * DTM / (TCCR * 60)

        emax = (Psk - Pa) / Rp
        edif = 0.06 * (1 - wet) * emax
        Tood = (hcod * Ta + hrod * MRT) / (hcod + hrod)
        qsk_dry = (TTSK - Tood) / Rd
        qsk_vap = BasalM * (esw + edif)
        HSSK = qkc - qsk_dry - qsk_vap

        val TCSK = Cc * alpha * Wt
        TTSK = TTSK + HSSK * BSA * DTM / (TCSK * 60)

    }
}




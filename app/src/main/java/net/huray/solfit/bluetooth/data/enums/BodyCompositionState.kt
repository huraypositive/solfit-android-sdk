package net.huray.solfit.bluetooth.data.enums

enum class BodyCompositionState(sourceId:Int){
    START(20),
    FAIL(21),
    SUCCESS(22),
    UNKNOWN(-1);

    companion object {
        fun getBodyCompositionState(sourceId: Int) =
            when(sourceId){
                20 -> START
                21 -> FAIL
                22 -> SUCCESS
                else -> UNKNOWN
            }

    }
}
package net.huray.solfit.bluetooth.data.enums

enum class WeightState(sourceId: Int) {
    // GET WEIGHT STATE
    START(0),
    WAITING(1),
    SUCCESS(2),
    UNKNOWN(-1);

    companion object{
        fun getWeightState(sourceId: Int) =
            when(sourceId){
                0 -> START
                1 -> WAITING
                2 -> SUCCESS
                else -> UNKNOWN
            }
    }
}
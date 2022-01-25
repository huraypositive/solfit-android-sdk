package net.huray.solfit.bluetooth.data.enums

enum class ConnectState(sourceId: Int) {
    DISCONNECTED(0),
    CONNECTED(1),
    SERVICE_DISCOVERED(2),
    INDICATION_SUCCESS(3),
    CONNECTING(4),
    TIME_OUT(5),
    ERROR(6),
    UNKNOWN(-1);

    companion object{
        fun getConnectState(sourceId: Int) =
            when(sourceId) {
                0 -> DISCONNECTED
                1 -> CONNECTED
                2 -> SERVICE_DISCOVERED
                3 -> INDICATION_SUCCESS
                4 -> CONNECTING
                5 -> TIME_OUT
                6 -> ERROR
                else -> UNKNOWN
            }

    }

}
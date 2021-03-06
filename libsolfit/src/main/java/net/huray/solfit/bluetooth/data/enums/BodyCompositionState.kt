package net.huray.solfit.bluetooth.data.enums

enum class BodyCompositionState(val sourceId: Int) {
    START(20),
    FAIL(21),
    SUCCESS(22),
    FAIL_USER_INFO_NOT_INITIALIZED(23),
    UNKNOWN(-1);

    companion object {
        fun getBodyCompositionState(sourceId: Int) =
            when (sourceId) {
                20 -> START
                21 -> FAIL
                22 -> SUCCESS
                23 -> FAIL_USER_INFO_NOT_INITIALIZED
                else -> UNKNOWN
            }

    }
}

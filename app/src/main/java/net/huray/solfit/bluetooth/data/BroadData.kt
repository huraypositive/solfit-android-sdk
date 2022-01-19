package net.huray.solfit.bluetooth.data

import aicare.net.cn.iweightlibrary.entity.BroadData

class BroadData: BroadData() {

    override fun getName(): String {
        return super.getName()
    }

    override fun getRssi(): Int {
        return super.getRssi()
    }

    override fun getSpecificData(): ByteArray {
        return super.getSpecificData()
    }

    override fun getDeviceType(): Int {
        return super.getDeviceType()
    }

    override fun getAddress(): String {
        return super.getAddress()
    }
}
package net.huray.solfit.bluetooth.data

import cn.net.aicare.MoreFatData

class MoreFatData: MoreFatData() {
    override fun getStandardWeight(): Double {
        return super.getStandardWeight()
    }

    override fun getControlWeight(): Double {
        return super.getControlWeight()
    }

    override fun getFat(): Double {
        return super.getFat()
    }

    override fun getRemoveFatWeight(): Double {
        return super.getRemoveFatWeight()
    }

    override fun getMuscleMass(): Double {
        return super.getMuscleMass()
    }

    override fun getProtein(): Double {
        return super.getProtein()
    }

    override fun getFatLevel(): FatLevel {
        return super.getFatLevel()
    }
}
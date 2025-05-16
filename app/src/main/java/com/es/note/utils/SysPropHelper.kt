package com.es.note.utils

import android.annotation.SuppressLint;
import java.lang.reflect.Method

@SuppressLint("PrivateApi", "DiscouragedPrivateApi")
object SysPropHelper {
    private val TAG = "SysPropHelper"
    private val EINK_MODE = "sys.eink.mode"
    private val HWC_COMMIT_DISABLED = "persist.sys.hwc_commit_disabled"

    private var getMethod: Method
    private var setMethod: Method

    init {
        val sysPropClz = Class.forName("android.os.SystemProperties")
        getMethod = sysPropClz.getDeclaredMethod("get", String::class.java)
        setMethod = sysPropClz.getDeclaredMethod("set", String::class.java, String::class.java)
    }

    fun setHwcCommitDisabled(disabled: Boolean) {
        try {
            setMethod.invoke(null, HWC_COMMIT_DISABLED, if (disabled) "1" else "0")
        } catch (e: RuntimeException) {
            LogUtil.d(TAG, "setHwcCommitDisabled($disabled) failed", e)
        }
    }
}
package com.jksalcedo.datasimswitcher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.IBinder
import android.telephony.SubscriptionInfo
import com.topjohnwu.superuser.ipc.RootService

class SimRootService : RootService() {

    companion object {
        const val SHELL_PACKAGE = "com.android.shell"
        const val SUBSCRIPTION_INTERFACE = "com.android.internal.telephony.ISub"
        const val TELEPHONY_INTERFACE = "com.android.internal.telephony.ITelephony"
    }

    var currentName: String = ""

    override fun onBind(intent: Intent): IBinder {
        return SimSwitcherBinder()
    }

    private inner class SimSwitcherBinder : IRootSimService.Stub() {

        @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
        override fun switchDataSim() {
            try {
                val iSubService = getSystemServiceInterface("isub", SUBSCRIPTION_INTERFACE)

                //  Get the current active data SIM ID
                val currentSubId = invokeMethod(iSubService, SUBSCRIPTION_INTERFACE, "getDefaultDataSubId") as Int

                // Get list of all inserted/active SIMs
                val activeSims = getActiveSubIds(iSubService)
                if (activeSims.size < 2) return // Nothing to switch to

                // Calculate the next SIM ID
                val currentIndex = activeSims.indexOfFirst { it.subscriptionId == currentSubId }
                val nextIndex = if (currentIndex != -1) (currentIndex + 1) % activeSims.size else 0
                val targetSim = activeSims[nextIndex]
                val targetSubId = targetSim.subscriptionId

                currentName = targetSim.displayName?.toString()
                    ?: targetSim.carrierName?.toString()
                            ?: "SIM ${targetSim.simSlotIndex + 1}"

                // Execute the switch
                invokeMethod(
                    iSubService,
                    SUBSCRIPTION_INTERFACE,
                    "setDefaultDataSubId",
                    arrayOf(Int::class.java),
                    targetSubId
                )

                // Turn on data for the newly selected SIM
                setMobileDataEnabled(targetSubId)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getDisplayName(): String {
            return currentName
        }
    }

    // Helper Functions

    @SuppressLint("PrivateApi")
    private fun getSystemServiceInterface(serviceName: String, interfaceName: String): Any {
        val serviceManagerClass = Class.forName("android.os.ServiceManager")
        val binder = serviceManagerClass.getMethod("getService", String::class.java)
            .invoke(null, serviceName) as IBinder

        val stubClass = Class.forName("$interfaceName\$Stub")
        return stubClass.getMethod("asInterface", IBinder::class.java).invoke(null, binder)!!
    }

    private fun getActiveSubIds(isubService: Any): List<SubscriptionInfo> {
        val activeSims = mutableListOf<SubscriptionInfo>()

        val value = try {
            invokeMethod(isubService, SUBSCRIPTION_INTERFACE, "getActiveSubscriptionInfoList",
                arrayOf(String::class.java, String::class.java, Boolean::class.java), SHELL_PACKAGE, null, true)
        } catch (_: NoSuchMethodException) {
            try {
                invokeMethod(isubService, SUBSCRIPTION_INTERFACE, "getActiveSubscriptionInfoList",
                    arrayOf(String::class.java, String::class.java), SHELL_PACKAGE, null)
            } catch (_: NoSuchMethodException) {
                invokeMethod(isubService, SUBSCRIPTION_INTERFACE, "getActiveSubscriptionInfoList",
                    arrayOf(String::class.java), SHELL_PACKAGE)
            }
        }

        if (value is List<*>) {
            // Sort by slot index to ensure consistent toggling
            val sortedList = value.filterIsInstance<SubscriptionInfo>().sortedBy { it.simSlotIndex }
            activeSims.addAll(sortedList)
        }
        return activeSims
    }

    private fun setMobileDataEnabled(subId: Int) {
        val phoneService = getSystemServiceInterface("phone", TELEPHONY_INTERFACE)

        // Same as Telephony.DATA_ENABLED_REASON_USER
        // Hardcoded to prevent API level warning
        val reasonUser = 0

        try {
            invokeMethod(phoneService, TELEPHONY_INTERFACE, "setDataEnabledForReason",
                arrayOf(Int::class.java, Int::class.java, Boolean::class.java, String::class.java),
                subId, reasonUser, true, null)
        } catch (_: NoSuchMethodException) {
            try {
                invokeMethod(phoneService, TELEPHONY_INTERFACE, "setDataEnabledForReason",
                    arrayOf(Int::class.java, Int::class.java, Boolean::class.java),
                    subId, reasonUser, true)
            } catch (_: NoSuchMethodException) {
                try {
                    invokeMethod(phoneService, TELEPHONY_INTERFACE, "setUserDataEnabled",
                        arrayOf(Int::class.java, Boolean::class.java), subId, true)
                } catch (_: NoSuchMethodException) {
                    invokeMethod(phoneService, TELEPHONY_INTERFACE, "setDataEnabled",
                        arrayOf(Int::class.java, Boolean::class.java), subId, true)
                }
            }
        }
    }

    private fun invokeMethod(
        targetInstance: Any,
        interfaceName: String,
        methodName: String,
        paramTypes: Array<Class<*>> = emptyArray(),
        vararg args: Any?
    ): Any? {
        // reflect against the Interface class string
        val method = Class.forName(interfaceName).getMethod(methodName, *paramTypes)
        return method.invoke(targetInstance, *args)
    }
}
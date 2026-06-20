package com.jksalcedo.datasimswitcher

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService

class DataSimTileService : TileService() {

    override fun onClick() {
        super.onClick()

        if (!Shell.getShell().isRoot) return

        val intent = Intent(this, SimRootService::class.java)

        RootService.bind(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val rootService = IRootSimService.Stub.asInterface(service)

                // Tell the Root service to figure out the next SIM and switch it
                rootService.switchDataSim()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this@DataSimTileService.qsTile.apply {
                        subtitle = rootService.getDisplayName().toString()
                        updateTile()
                    }
                }

                RootService.unbind(this)
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        })
    }
}
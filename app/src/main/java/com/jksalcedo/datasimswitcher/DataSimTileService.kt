package com.jksalcedo.datasimswitcher

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.*

class DataSimTileService : TileService() {

    private var rootService: IRootSimService? = null
    // Scope tied to the TileService lifecycle
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            rootService = IRootSimService.Stub.asInterface(service)
            updateTileUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            rootService = null
        }
    }

    // Bind when the user pulls down the QS panel
    override fun onStartListening() {
        super.onStartListening()
        
        // Ensure global shell is ready
        if (Shell.getShell().isRoot) {
            val intent = Intent(this, SimRootService::class.java)
            RootService.bind(intent, connection)
        }
    }

    // Unbind when the QS panel is closed
    override fun onStopListening() {
        super.onStopListening()
        if (rootService != null) {
            RootService.unbind(connection)
            rootService = null
        }
        // Cancel any pending switch operations if the panel closes
        serviceScope.coroutineContext.cancelChildren()
    }

    override fun onClick() {
        super.onClick()
        val service = rootService ?: return

        // Prevent spam-clicking by disabling the tile during the switch
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()

        serviceScope.launch {
            // Suspend and wait for the IPC call on a background thread
            val newSimName = withContext(Dispatchers.IO) {
                service.switchDataSim() 
            }
            
            // Update UI on the main thread based on success or failure
            if (newSimName != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = newSimName
                }
                qsTile.state = Tile.STATE_ACTIVE
            } else {
                // visual feedback that the reflection failed
                qsTile.state = Tile.STATE_INACTIVE 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    qsTile.subtitle = "Switch Failed"
                }
            }
            
            qsTile.updateTile()
        }
    }

    private fun updateTileUI() {
        val service = rootService ?: return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = service.displayName
        }
        
        qsTile.state = Tile.STATE_ACTIVE
        qsTile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
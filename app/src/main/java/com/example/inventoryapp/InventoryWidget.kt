package com.example.inventoryapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class InventoryWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences("InventarioPrefs", Context.MODE_PRIVATE)
        val mostrarSaldo = prefs.getBoolean("mostrarSaldo", true)
        val total = prefs.getFloat("totalInventario", 12345.67f)

        val views = RemoteViews(context.packageName, R.layout.inventory_widget)

        // 👁 Mostrar u ocultar saldo
        if (mostrarSaldo) {
            views.setTextViewText(R.id.txtSaldo, "$%.2f".format(total))
            views.setImageViewResource(R.id.btnToggle, R.drawable.abierto)
        } else {
            views.setTextViewText(R.id.txtSaldo, "$ ****")
            views.setImageViewResource(R.id.btnToggle, R.drawable.cerrado)
        }

        // 🔁 Acción del botón del ojo
        val toggleIntent = Intent(context, InventoryWidget::class.java).apply {
            action = "TOGGLE_SALDO"
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId, // 🔹 usar ID único evita cacheo del intent
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnToggle, togglePendingIntent)

        // ⚙️ Acción para abrir EditProductActivity
        val gestionarIntent = Intent(context, EditProductActivity::class.java)
        val gestionarPendingIntent = PendingIntent.getActivity(
            context,
            1, // distinto del anterior
            gestionarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.imgGestionar, gestionarPendingIntent)
        views.setOnClickPendingIntent(R.id.txtGestionar, gestionarPendingIntent)

        // ✅ No ponemos acción en el fondo
        // Así el fondo no abrirá nada por error

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "TOGGLE_SALDO") {
            val prefs = context.getSharedPreferences("InventarioPrefs", Context.MODE_PRIVATE)
            val mostrarActual = prefs.getBoolean("mostrarSaldo", true)
            prefs.edit().putBoolean("mostrarSaldo", !mostrarActual).apply()

            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, InventoryWidget::class.java))
            onUpdate(context, manager, ids)
        }
    }
}

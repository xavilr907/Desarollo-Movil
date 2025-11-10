package com.example.inventoryapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class InventoryWidget : AppWidgetProvider() {

    // Lista de productos: Pair<precio, cantidad>
    private val productos = listOf(
        Pair(12000.0, 5),
        Pair(25000.0, 8),
        Pair(18000.0, 6)
    )

    // Función para calcular el total
    private fun calcularTotal(): Double {
        return productos.sumOf { it.first * it.second }
    }

    // Guardar el estado del ojo
    private fun guardarEstado(context: Context, mostrar: Boolean) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("mostrarSaldo", mostrar).apply()
    }

    // Leer el estado guardado del ojo
    private fun obtenerEstado(context: Context): Boolean {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("mostrarSaldo", false)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "com.example.inventoryapp.TOGGLE_SALDO") {
            val estadoActual = obtenerEstado(context)
            val nuevoEstado = !estadoActual
            guardarEstado(context, nuevoEstado)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, InventoryWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val total = calcularTotal()
        val mostrarSaldo = obtenerEstado(context)
        val views = RemoteViews(context.packageName, R.layout.inventory_widget)

        // Mostrar u ocultar saldo
        val saldoTexto = if (mostrarSaldo) {
            "$ ${"%,.2f".format(total)}"
        } else "$ ****"
        views.setTextViewText(R.id.txtSaldo, saldoTexto)

        // Alternar icono ojo
        val icono = if (mostrarSaldo) R.drawable.cerrado else R.drawable.ic_eye_open
        views.setImageViewResource(R.id.btnToggle, icono)

        // PendingIntent para alternar saldo
        val toggleIntent = Intent(context, InventoryWidget::class.java).apply {
            action = "com.example.inventoryapp.TOGGLE_SALDO"
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnToggle, togglePendingIntent)

        // Ícono naranja inferior (decorativo o clic si quieres)
        val manageIntent = Intent(context, InventoryWidget::class.java).apply {
            action = "com.example.inventoryapp.MANAGE_INVENTORY"
        }
        val managePendingIntent = PendingIntent.getBroadcast(
            context, appWidgetId, manageIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.imgGestionar, managePendingIntent)

        // Actualiza el widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

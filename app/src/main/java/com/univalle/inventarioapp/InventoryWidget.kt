package com.univalle.inventarioapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

class InventoryWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_TOGGLE_VISIBILITY =
            "com.univalle.inventarioapp.action.TOGGLE_WIDGET_VISIBILITY"

        private const val PREFS_NAME = "inventory_widget_prefs"
        private const val KEY_IS_HIDDEN = "is_hidden"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_VISIBILITY -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val current = prefs.getBoolean(KEY_IS_HIDDEN, true)
                prefs.edit().putBoolean(KEY_IS_HIDDEN, !current).apply()

                val manager = AppWidgetManager.getInstance(context)
                val thisWidget = ComponentName(context, InventoryWidget::class.java)
                val ids = manager.getAppWidgetIds(thisWidget)
                onUpdate(context, manager, ids)
            }
        }
    }

    private fun updateSingleWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.inventory_widget)

        // ---------------------------
        // INTENTO: Toggle del ojo
        // ---------------------------
        val toggleIntent = Intent(context, InventoryWidget::class.java).apply {
            action = ACTION_TOGGLE_VISIBILITY
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btnToggle, togglePendingIntent)

        // ---------------------------
        // INTENTO: Abrir MainActivity
        // ---------------------------
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.imgGestionar, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.txtGestionar, openAppPendingIntent)

        // ---------------------------
        // OJO ABIERTO / CERRADO
        // ---------------------------
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isHidden = prefs.getBoolean(KEY_IS_HIDDEN, true)

        if (isHidden) {
            // OJO CERRADO → ocultamos saldo
            views.setTextViewText(R.id.txtSaldo, "$ ****")
            views.setImageViewResource(R.id.btnToggle, R.drawable.cerrado)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return
        }

        // OJO ABIERTO → mostramos saldo real
        views.setImageViewResource(R.id.btnToggle, R.drawable.abierto)

        // ---------------------------
        // LEER TOTAL DESDE FIRESTORE
        // ---------------------------

        // Ajusta "products" si tu colección se llama distinto
        FirebaseFirestore.getInstance()
            .collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                var totalCents = 0L

                for (doc in snapshot.documents) {
                    // Campos tal como los guardas en Firestore
                    val priceCents = doc.getLong("priceCents") ?: 0L
                    val quantity = doc.getLong("quantity") ?: 0L

                    totalCents += priceCents * quantity
                }

                // Si guardas centavos (100 = $1,00)
                val totalPesos = totalCents / 100.0

                val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                val formatted = formatter.format(totalPesos)

                views.setTextViewText(R.id.txtSaldo, formatted)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            .addOnFailureListener {
                // En caso de error de red / firestore, no rompemos el widget
                views.setTextViewText(R.id.txtSaldo, "$ ****")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
    }
}

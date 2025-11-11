package com.example.inventoryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

class EditProductActivity : AppCompatActivity() {

    private lateinit var editTextId: EditText
    private lateinit var editTextNombre: EditText
    private lateinit var editTextPrecio: EditText
    private lateinit var editTextCantidad: EditText
    private lateinit var btnEditar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        // 🔹 Referencias a los elementos del layout
        editTextId = findViewById(R.id.editTextId)
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextPrecio = findViewById(R.id.editTextPrecio)
        editTextCantidad = findViewById(R.id.editTextCantidad)
        btnEditar = findViewById(R.id.btnEditar)

        // 🔹 Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar_edit)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // 🔹 Datos simulados (como si vinieran del intent o base de datos)
        val idProducto = 1
        val nombre = "Camiseta"
        val precio = 25000.0
        val cantidad = 15

        // Mostrar los datos simulados
        editTextId.setText(idProducto.toString())
        editTextNombre.setText(nombre)
        editTextPrecio.setText(precio.toString())
        editTextCantidad.setText(cantidad.toString())

        // 🔹 Activar/desactivar el botón según campos válidos
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnEditar.isEnabled = camposValidos()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        editTextNombre.addTextChangedListener(watcher)
        editTextPrecio.addTextChangedListener(watcher)
        editTextCantidad.addTextChangedListener(watcher)

        // 🔹 Acción del botón
        btnEditar.setOnClickListener {
            if (camposValidos()) {
                val nuevoNombre = editTextNombre.text.toString()
                val nuevoPrecio = editTextPrecio.text.toString().toDoubleOrNull()
                val nuevaCantidad = editTextCantidad.text.toString().toIntOrNull()

                Toast.makeText(
                    this,
                    "✅ Producto editado: $nuevoNombre ($nuevoPrecio x $nuevaCantidad)",
                    Toast.LENGTH_LONG
                ).show()

                finish() // Cierra la pantalla simulando que se actualizó
            } else {
                Toast.makeText(this, "⚠️ Completa todos los campos antes de editar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun camposValidos(): Boolean {
        val nombre = editTextNombre.text.toString().trim()
        val precio = editTextPrecio.text.toString().trim()
        val cantidad = editTextCantidad.text.toString().trim()
        return nombre.isNotEmpty() && precio.isNotEmpty() && cantidad.isNotEmpty()
    }
}

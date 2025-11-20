package com.univalle.inventarioapp.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.univalle.inventarioapp.EditProductActivity
import com.univalle.inventarioapp.R
import com.univalle.inventarioapp.data.local.AppDatabase
import com.univalle.inventarioapp.databinding.FragmentProductDetailBinding

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var vm: ProductDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ---------- Toolbar: icono y acción "atrás" ----------
        binding.toolbarDetail.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_white)

        binding.toolbarDetail.setNavigationOnClickListener {
            // Simplemente volver al fragment anterior (Home en tu flujo)
            findNavController().popBackStack()
        }

        // ---------- DB y ViewModel ----------
        val db = AppDatabase.getInstance(requireContext())

        val productCode = arguments?.getString("productCode") ?: run {
            // si no hay código, salimos
            findNavController().popBackStack()
            return
        }

        val factory = ProductDetailViewModelFactory(db.productDao(), productCode)
        vm = ViewModelProvider(this, factory)[ProductDetailViewModel::class.java]

        // ---------- Observers ----------
        vm.product.observe(viewLifecycleOwner) { p ->
            if (p == null) return@observe
            binding.tvProductName.text = p.name
            binding.tvPrice.text = formatCurrency(p.priceCents)
            binding.tvQuantity.text = p.quantity.toString()
        }

        vm.totalFormatted.observe(viewLifecycleOwner) { t ->
            binding.tvTotal.text = t
        }

        vm.navigateBack.observe(viewLifecycleOwner) { goBack ->
            if (goBack == true) {
                // Después de eliminar, simplemente volvemos atrás
                findNavController().popBackStack()
            }
        }

        vm.error.observe(viewLifecycleOwner) { err ->
            err?.let {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage(it)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // ---------- Botón Eliminar ----------
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Sí") { _, _ ->
                    vm.deleteProduct()
                }
                .show()
        }

        // ---------- FAB Editar -> abre EditProductActivity ----------
        binding.fabEdit.setOnClickListener {
            val context = requireContext()
            val intent = Intent(context, EditProductActivity::class.java).apply {
                putExtra("EXTRA_CODE", productCode)
            }
            startActivity(intent)
        }
    }

    private fun formatCurrency(cents: Long): String {
        val units = cents / 100.0
        return java.text.NumberFormat
            .getCurrencyInstance(java.util.Locale.getDefault())
            .format(units)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

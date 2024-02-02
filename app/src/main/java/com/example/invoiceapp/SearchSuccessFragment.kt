package com.example.invoiceapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.invoiceapp.databinding.FragmentSearchBindingImpl
import com.example.invoiceapp.databinding.FragmentSearchFailBinding
import com.example.invoiceapp.databinding.FragmentSearchSuccessBinding


class SearchSuccessFragment(private val invoice: Invoice, private val prize: String) : Fragment() {

    private var _binding: FragmentSearchSuccessBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // 初始化
        _binding = FragmentSearchSuccessBinding.inflate(inflater, container, false)

        // 返回按鈕
        binding.btnBackToList.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, SearchFragment())
            transaction.commit()
        }

        // 將中獎發票資料設置於 TextView 中
        binding.searchSuccessTitle.text = "中獎金額 \$" + prize
        binding.searchSuccessInvoiceNumber.text = invoice.invoiceNumber
        binding.searchSuccessDate.text = String.format("民國 %d 年 %02d 月 %02d 日",
            invoice.year,
            invoice.month,
            invoice.day)
        binding.searchSuccessStoreName.text = invoice.storeName
        binding.searchSuccessAmount.text = String.format("消費金額 \$ %d", invoice.amount)
        binding.searchSuccessDetails.text = invoice.details

        return binding.root
    }
}
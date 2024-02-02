package com.example.invoiceapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.invoiceapp.databinding.FragmentListBinding


class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: InvoiceDatabaseHelper
    private lateinit var invoicesAdapter: InvoicesAdapter
    private var currentYear = 113
    private var currentMonth = 2
    private lateinit var yearMonthTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 初始化
        _binding = FragmentListBinding.inflate(inflater, container, false)
        db = InvoiceDatabaseHelper(requireContext())
        yearMonthTextView = binding.yearMonthTextView

        // 依照年分與月份更新列表
        updateYearMonthTextView()

        // 減時按鈕
        binding.btnDecrease.setOnClickListener {
            decreaseMonth()
        }

        // 加時按鈕
        binding.btnIncrease.setOnClickListener {
            increaseMonth()
        }

        // 查看日期列表
        binding.btnDateList.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, DateListFragment())
            transaction.commit()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        invoicesAdapter.refreshData(db.getInvoicesByYearAndMonth(currentYear, currentMonth))
    }

    // 更新 RecyclerView 列表
    private fun setInvoiceAdapter(){
        val list: List<Invoice> = db.getInvoicesByYearAndMonth(currentYear, currentMonth)
        invoicesAdapter = InvoicesAdapter(list, requireContext())

        invoicesAdapter.onItemClick = {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, EditInvoiceFragment(it.id))
            transaction.commit()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = invoicesAdapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager(requireContext()).orientation
            )
        )

        binding.amountSumTextView.text = "$ " + list.sumOf { it.amount }.toString()
        binding.countSumTextView.text = list.size.toString() + "張"
    }

    private fun updateYearMonthTextView() {

        setInvoiceAdapter()

        val formattedMonth = String.format("%02d", currentMonth)
        yearMonthTextView.text = "民國 $currentYear 年 $formattedMonth 月"
    }

    // 減時
    private fun decreaseMonth() {
        if (currentMonth > 1) {
            currentMonth--
        } else {
            currentMonth = 12
            currentYear--
        }
        updateYearMonthTextView()
    }

    // 加時
    private fun increaseMonth() {
        if (currentYear == 113 && currentMonth == 2){
            return
        }

        if (currentMonth < 12) {
            currentMonth++
        } else {
            currentMonth = 1
            currentYear++
        }
        updateYearMonthTextView()
    }
}
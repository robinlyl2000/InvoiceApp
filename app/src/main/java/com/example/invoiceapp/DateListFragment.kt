package com.example.invoiceapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.invoiceapp.databinding.FragmentDateListBinding
import com.example.invoiceapp.databinding.FragmentListBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date


class DateListFragment : Fragment() {

    private var _binding: FragmentDateListBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: InvoiceDatabaseHelper
    private lateinit var invoicesAdapter: InvoicesAdapter
    private var currentYear = 113
    private var currentMonth = 2
    private var currentDay = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 初始化
        _binding = FragmentDateListBinding.inflate(inflater, container, false)
        db = InvoiceDatabaseHelper(requireContext())

        // 設定日期選擇器
        setPicker()

        // 設定 RecyclerView
        setInvoiceAdapter()

        // 返回按鈕
        binding.btnBackToList.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, ListFragment())
            transaction.commit()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // 重新整理 RecyclerView
        invoicesAdapter.refreshData(db.getInvoicesByYearAndMonthAndDay(currentYear, currentMonth,currentDay))
    }

    private fun setInvoiceAdapter(){
        // 取得符合條件之發票物件列表
        val list: List<Invoice> = db.getInvoicesByYearAndMonthAndDay(currentYear, currentMonth,currentDay)
        invoicesAdapter = InvoicesAdapter(list, requireContext())

        // 設定點擊事件
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

        // 更新相關 TextView
        binding.amountSumTextView.text = "$ " + list.sumOf { it.amount }.toString()
        binding.countSumTextView.text = list.size.toString() + "張"
    }

    // 設定日期選擇器
    private fun setPicker() {
        // 年分選擇器之限制
        binding.numberPickerYear.apply {
            minValue = 90
            maxValue = 113
            value = currentYear
            wrapSelectorWheel = false

            // 避免使用者選擇出錯誤格式的日期
            setOnValueChangedListener { _, oldValue, newValue ->
                if (!isValidDate(newValue, binding.numberPickerMonth.value, binding.numberPickerDay.value)) {
                    value = oldValue
                } else {
                    updateCurrentDate(newValue, binding.numberPickerMonth.value, binding.numberPickerDay.value)
                }
            }
        }

        // 月份選擇器之限制
        binding.numberPickerMonth.apply {
            minValue = 1
            maxValue = 12
            value = currentMonth

            // 避免使用者選擇出錯誤格式的日期
            setOnValueChangedListener { _, oldValue, newValue ->
                if (!isValidDate(binding.numberPickerYear.value, newValue, binding.numberPickerDay.value)) {
                    value = oldValue
                } else {
                    updateCurrentDate(binding.numberPickerYear.value, newValue, binding.numberPickerDay.value)
                }
            }
        }

        // 日期選擇器之限制
        binding.numberPickerDay.apply {
            minValue = 1
            maxValue = 31
            value = currentDay

            // 避免使用者選擇出錯誤格式的日期
            setOnValueChangedListener { _, oldValue, newValue ->
                if (!isValidDate(binding.numberPickerYear.value, binding.numberPickerMonth.value, newValue)) {
                    value = oldValue
                } else {
                    updateCurrentDate(binding.numberPickerYear.value, binding.numberPickerMonth.value, newValue)
                }
            }
        }
    }

    // 判斷日期是否符合格式
    private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        val text = (year + 1911).toString() + "-" + String.format("%02d", month) + "-" + String.format("%02d", day)
        val dataFormat = SimpleDateFormat("yyyy-MM-dd")
        dataFormat.isLenient = false
        return try{
            val parseDate: Date? = dataFormat.parse(text)
            println(parseDate)
            true
        } catch (e: ParseException) {
            false
        }
    }

    // 更新當前區域變數
    private fun updateCurrentDate(year: Int, month: Int, day: Int) {
        currentYear = year
        currentMonth = month
        currentDay = day

        setInvoiceAdapter()
    }

}
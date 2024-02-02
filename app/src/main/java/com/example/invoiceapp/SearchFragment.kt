package com.example.invoiceapp

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.invoiceapp.databinding.FragmentSearchBinding


class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var currentYear = 113
    private lateinit var db: InvoiceDatabaseHelper
    private lateinit var stringList: Array<String?>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 初始化
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        db = InvoiceDatabaseHelper(requireContext())

        stringList = Array(6) { index ->
            Pair(index * 2 + 1, index * 2 + 2)
        }.map { (first, second) ->
            "$first 月 ~ $second 月"
        }.toTypedArray()

        // 設置日期選擇器
        setPicker()

        // 返回按鈕
        binding.btnBackToList.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, ListFragment())
            transaction.commit()
        }

        // 對獎按鈕
        binding.btnSearch.setOnClickListener {

            // 確認輸入之頭獎發票號碼是否符合格式
            val validationMessage = validateInvoiceNumber(binding.invoiceNumberEdittextNum.text.toString().trim())

            if (validationMessage != null){
                AlertDialog.Builder(requireContext())
                    .setTitle("輸入格式錯誤")
                    .setMessage(validationMessage)
                    .setPositiveButton("確定") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else {
                // 抓取符合中獎資格之發票
                val res = db.searchInvoiceByWinningNum(
                    binding.numberPickerYear.value,
                    stringList[binding.numberPickerMonth.value],
                    binding.invoiceNumberEdittextNum.text.toString().trim()
                )
                db.close()

                if (res.size == 0){ // 若發現無發票中獎
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentContainerView, SearchFailFragment())
                    transaction.commit()
                } else{ // 若發現有發票中獎
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentContainerView, SearchSuccessFragment(res[0].first, res[0].second))
                    transaction.commit()
                }

            }
        }

        return binding.root
    }

    // 設置日期選擇器
    private fun setPicker() {
        binding.numberPickerYear.apply {
            minValue = 90
            maxValue = 113
            value = currentYear
            wrapSelectorWheel = false
        }

        binding.numberPickerMonth.apply {
            minValue = 0
            maxValue = 5
            displayedValues = stringList
            value = 0
        }
    }

    // 確認是否符合發票號碼格式
    private fun validateInvoiceNumber(num: String): String? {
        val regex_num = Regex("^[0-9]{8}\$")
        if (! regex_num.matches(num)){
            return "發票號碼格式錯誤，請輸入正確的發票號碼!(首碼須為2個大寫英文字元，數字部分需剛好為8個數字)"
        }
        return null
    }


}
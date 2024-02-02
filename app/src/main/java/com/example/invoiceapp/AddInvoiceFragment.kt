package com.example.invoiceapp

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.invoiceapp.databinding.FragmentAddInvoiceBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date


class AddInvoiceFragment : Fragment() {

    private var _binding: FragmentAddInvoiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: InvoiceDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 初始化
        _binding = FragmentAddInvoiceBinding.inflate(inflater, container, false)
        db = InvoiceDatabaseHelper(requireContext())

        // 返回按鈕
        binding.btnScannerInput.setOnClickListener{
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, ScannerFragment())
            transaction.commit()
        }

        // 確定新增按鈕
        binding.btnSubmitSaving.setOnClickListener {

            // 針對不同的 EditText View，進行不同的判斷
            val validationMessage = validateAllEditTexts(
                Pair(
                    (binding.dateYearEdittext.text.toString().trim().toInt() + 1911).toString()
                            + "-" + binding.dateMonthEdittext.text.toString().trim()
                            + "-" + binding.dateDayEdittext.text.toString().trim(),
                    this::validateDate
                ), // 確認日期格式
                Pair(
                    binding.invoiceNumberEdittextEn.text.toString().trim()
                            + "-"
                            + binding.invoiceNumberEdittextNum.text.toString().trim(),
                    this::validateInvoiceNumber
                ) // 確認發票號碼格式
            )

            if (validationMessage != null) { // 若發現格式錯誤，則提醒使用者
                AlertDialog.Builder(requireContext())
                    .setTitle("輸入格式錯誤")
                    .setMessage(validationMessage)
                    .setPositiveButton("確定") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else { // 若未發現格式錯誤，則再次和使用者確認是否新增資料
                AlertDialog.Builder(requireContext())
                    .setTitle("是否新增此發票數據?")
                    .setPositiveButton("確定"){ _, _ ->

                        // 抓取當前 EditText View 的所有值
                        val invoiceNumber = binding.invoiceNumberEdittextEn.text.toString() + "-" + binding.invoiceNumberEdittextNum.text.toString()
                        val year = binding.dateYearEdittext.text.toString().toInt()
                        val month = binding.dateMonthEdittext.text.toString().toInt()
                        val day = binding.dateDayEdittext.text.toString().toInt()
                        val storeName = binding.storeNameEdittext.text.toString()
                        val amount = binding.amountEdittext.text.toString().toInt()
                        val details = binding.detailsEdittext.text.toString()

                        // insert 到 DB 中
                        val invoice = Invoice(0, invoiceNumber, year, month, day, storeName, amount, details)
                        db.insertInvoice(invoice)

                        backToListFragment()

                        Toast.makeText(requireContext(), "發票數據成功儲存", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("取消", null)
                    .show()
            }
        }
        return binding.root
    }

    // 返回列表頁面
    private fun backToListFragment(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, ListFragment())
        transaction.commit()
        val activityBinding = (requireActivity() as MainActivity).binding
        activityBinding.btnList.performClick()
    }

    // 針對不同的 EditText View，以及其對應的判斷條件，辨別是否有格式錯誤發生
    private fun validateAllEditTexts(vararg editTextPairs: Pair<String, (String) -> String?>): String? {
        for ((text, validationFunction) in editTextPairs) {
            val validationMessage = validationFunction.invoke(text)
            if (validationMessage != null) {
                return validationMessage
            }
        }
        return null
    }

    // 確認日期格式是否正確
    private fun validateDate(text: String): String? {

        val year = text.split("-")[0].toInt()
        if (year !in 0..2024){
            return "發票日期格式錯誤，請輸入正確的日期!(民國年份/月份/日期)"
        }

        val dataFormat = SimpleDateFormat("YYYY-MM-dd")
        dataFormat.isLenient = false
        return try{
            val parseDate: Date? = dataFormat.parse(text)
            println(parseDate)
            null
        } catch (e: ParseException) {
            "發票日期格式錯誤，請輸入正確的日期!(民國年份/月份/日期)"
        }
    }

    // 確認發票號碼格式是否正確
    private fun validateInvoiceNumber(text: String): String? {
        val code = text.split("-")
        val regex_en = Regex("^[A-Z]{2}$")
        val regex_num = Regex("^[0-9]{8}\$")
        if (! regex_en.matches(code[0]) || ! regex_num.matches(code[1])){
            return "發票號碼格式錯誤，請輸入正確的發票號碼!(首碼須為2個大寫英文字元，數字部分需剛好為8個數字)"
        }
        return null
    }
}
package com.example.invoiceapp

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.invoiceapp.databinding.FragmentEditInvoiceBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date


class EditInvoiceFragment(private val invoiceId: Int) : Fragment() {

    private var _binding: FragmentEditInvoiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: InvoiceDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化
        _binding = FragmentEditInvoiceBinding.inflate(inflater, container, false)
        db = InvoiceDatabaseHelper(requireContext())

        if (invoiceId == -1) {
            backToListFragment()
        }

        // 返回按鈕
        binding.btnBackToList.setOnClickListener{
            backToListFragment()
        }

        // 取消按鈕
        binding.btnCancel.setOnClickListener {
            backToListFragment()
        }

        // 刪除按鈕
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("是否刪除此發票資料?")
                .setPositiveButton("確定"){ _, _ ->
                    // 依照 ID 刪除對應資料
                    db.deleteInvoice(invoiceId)
                    backToListFragment()
                    Toast.makeText(requireContext(), "發票數據成功刪除", Toast.LENGTH_SHORT).show()
                }
                .setNeutralButton("取消", null)
                .show()
        }

        // 更新按鈕
        binding.btnUpdate.setOnClickListener {

            // 檢查各項 EditText View 格式是否正確
            val validationMessage = validateAllEditTexts(
                Pair(
                    (binding.dateYearEdittext.text.toString().trim().toInt() + 1911).toString()
                            + "-" + binding.dateMonthEdittext.text.toString().trim()
                            + "-" + binding.dateDayEdittext.text.toString().trim(),
                    this::validateDate
                ),
                Pair(
                    binding.invoiceNumberEdittextEn.text.toString().trim()
                            + "-"
                            + binding.invoiceNumberEdittextNum.text.toString().trim(),
                    this::validateInvoiceNumber
                )
            )

            if (validationMessage != null) { // 如果格式有誤
                AlertDialog.Builder(requireContext())
                    .setTitle("輸入格式錯誤")
                    .setMessage(validationMessage)
                    .setPositiveButton("確定") { dialog, _ -> dialog.dismiss() }
                    .show()
            } else { // 如果格式皆正確
                AlertDialog.Builder(requireContext())
                    .setTitle("是否儲存此變更?")
                    .setPositiveButton("確定"){ _, _ ->
                        // 抓取當前值
                        val invoiceNumber = binding.invoiceNumberEdittextEn.text.toString() + "-" + binding.invoiceNumberEdittextNum.text.toString()
                        val year = binding.dateYearEdittext.text.toString().toInt()
                        val month = binding.dateMonthEdittext.text.toString().toInt()
                        val day = binding.dateDayEdittext.text.toString().toInt()
                        val storeName = binding.storeNameEdittext.text.toString()
                        val amount = binding.amountEdittext.text.toString().toInt()
                        val details = binding.detailsEdittext.text.toString()

                        // 更新至 DB 中
                        val invoice = Invoice(invoiceId, invoiceNumber, year, month, day, storeName, amount, details)
                        db.updateInvoice(invoice)

                        backToListFragment()

                        Toast.makeText(requireContext(), "發票數據成功修改", Toast.LENGTH_SHORT).show()
                    }
                    .setNeutralButton("取消", null)
                    .show()
            }
        }

        // 依照 ID 抓取對應的發票資料
        val invoice = db.getInvoiceByID(invoiceId)

        // 填至 EditText View 中
        binding.amountEdittext.setText(invoice.amount.toString())
        binding.storeNameEdittext.setText(invoice.storeName)
        binding.dateYearEdittext.setText(invoice.year.toString())
        binding.dateMonthEdittext.setText(invoice.month.toString())
        binding.dateDayEdittext.setText(invoice.day.toString())
        val code = invoice.invoiceNumber.split("-")
        binding.invoiceNumberEdittextEn.setText(code[0])
        binding.invoiceNumberEdittextNum.setText(code[1])
        binding.detailsEdittext.setText(invoice.details)

        return binding.root
    }

    // 返回列表
    private fun backToListFragment(){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, ListFragment())
        transaction.commit()
    }

    // 判斷各 EditText VIew 是否符合各自的格式
    private fun validateAllEditTexts(vararg editTextPairs: Pair<String, (String) -> String?>): String? {
        for ((text, validationFunction) in editTextPairs) {
            val validationMessage = validationFunction.invoke(text)
            if (validationMessage != null) {
                return validationMessage
            }
        }
        return null
    }


    // 判斷是否符合日期格式
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

    // 判斷是否符合發票號碼格式
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
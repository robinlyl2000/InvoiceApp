package com.example.invoiceapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.invoiceapp.databinding.MainactivityBinding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    public lateinit var binding: MainactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.mainactivity);

        binding.btnList.performClick()
        replaceFragment(ListFragment())

        // 可分別前往四種主要 Fragment
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked){
                when (checkedId){
                    R.id.btnList ->  replaceFragment(ListFragment())
                    R.id.btnScanner -> replaceFragment(ScannerFragment())
                    R.id.btnMember -> println("會員頁面")
                    R.id.btnSearch -> replaceFragment(SearchFragment())
                }
            }
        }
    }

    fun replaceFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainerView, fragment)
        transaction.commit()
    }
}
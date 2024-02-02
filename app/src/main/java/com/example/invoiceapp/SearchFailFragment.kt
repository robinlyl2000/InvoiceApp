package com.example.invoiceapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.invoiceapp.databinding.FragmentSearchFailBinding

class SearchFailFragment : Fragment() {

    private var _binding: FragmentSearchFailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSearchFailBinding.inflate(inflater, container, false)

        binding.btnBackToList.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, SearchFragment())
            transaction.commit()
        }

        return binding.root
    }

}
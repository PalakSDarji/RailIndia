package com.palak.railindia.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.palak.railindia.databinding.HomeFragmentBinding
import com.palak.railindia.di.DateSDF
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var binding : HomeFragmentBinding
    private val viewModel by activityViewModels<HomeViewModel>()

    @Inject
    @DateSDF
    lateinit var dateSdf : SimpleDateFormat

    var cal = Calendar.getInstance()

    val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthOfYear)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        setDate()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = HomeFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.downloadComponentData()

        binding.layoutDate.sdf = dateSdf
        binding.homeViewModel = viewModel
        viewModel.componentLiveData.observe(viewLifecycleOwner){
            println(it.toString())
        }

        binding.layoutDate.tilDate.editText?.setOnClickListener {
            DatePickerDialog(requireContext(),
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnAddData.setOnClickListener {

        }
    }

    private fun setDate() {
        viewModel.selectedDate = cal.time
        binding.layoutDate.tilDate.editText?.setText(dateSdf.format(viewModel.selectedDate))
    }
}
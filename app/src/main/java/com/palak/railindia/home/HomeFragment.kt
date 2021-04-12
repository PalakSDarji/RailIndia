package com.palak.railindia.home

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.palak.railindia.R
import com.palak.railindia.databinding.HomeFragmentBinding
import com.palak.railindia.di.DateSDF
import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import com.palak.railindia.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var adapter: ComponentAdapter
    private lateinit var binding: HomeFragmentBinding
    private val viewModel by activityViewModels<HomeViewModel>()

    private val entry = Entry()

    @Inject
    @DateSDF
    lateinit var dateSdf: SimpleDateFormat

    var cal = Calendar.getInstance()

    val dateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            setDate()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.downloadComponentData()

        binding.layoutDate.sdf = dateSdf
        binding.homeViewModel = viewModel
        binding.showList = false

        binding.layoutDate.tilDate.editText?.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnContinue.setOnClickListener {

            val date = binding.layoutDate.tilDate.editText?.text.toString().trim()
            if(date.isEmpty()){
                Snackbar.make(binding.container,"Enter date!",Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            it.visibility = View.GONE
            binding.showList = true
            hideKeyboard()
            initList()
        }

        binding.btnAddData.setOnClickListener {
            //Save to database.
            hideKeyboard()
            Timber.d("Entry : $entry")
            viewModel.saveEntry(entry)
        }
    }

    private fun initList(){
        lifecycleScope.launchWhenCreated {

            viewModel.componentLiveData.map {
                it.map {
                    val ce = ComponentEntry()
                    ce.componentId = it.id
                    ce.component = it
                    ce
                }
            }.distinctUntilChanged().collect { componentEntryList ->

                entry.componentEntry = componentEntryList

                Timber.d("printing flow $componentEntryList")

                binding.rvComponentData.layoutManager = LinearLayoutManager(context)

                val bogieNumberText = binding.layoutBogie.tilBogieNumber.editText?.text.toString()

                var noOfBogie = 1;

                if (!bogieNumberText.isEmpty()) {
                    noOfBogie = bogieNumberText.toInt()
                }

                adapter = ComponentAdapter(noOfBogie, onPassSave = { data, pos ->
                    componentEntryList.get(pos).pass = data
                }, onFailSave = { data, pos ->
                    componentEntryList.get(pos).fail = data
                })

                binding.rvComponentData.adapter = adapter
                binding.rvComponentData.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )

                binding.rvComponentData.isNestedScrollingEnabled = false
                adapter.submitList(componentEntryList)
            }
        }
    }

    private fun setDate() {

        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        viewModel.selectedDate = cal.time
        binding.layoutDate.tilDate.editText?.setText(dateSdf.format(viewModel.selectedDate))

        entry.date = Date(cal.time.time)
    }
}
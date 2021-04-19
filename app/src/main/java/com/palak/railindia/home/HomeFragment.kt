package com.palak.railindia.home

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.RuntimeException
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private var askForBackPress: Boolean = false
    private lateinit var adapter: ComponentAdapter
    private lateinit var binding: HomeFragmentBinding
    private val viewModel by activityViewModels<HomeViewModel>()

    private lateinit var entry: Entry
    private var noOfBogie: Int = 1

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

        initViews()

        viewModel.downloadComponentData()

        binding.layoutDate.sdf = dateSdf
        binding.homeViewModel = viewModel

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

            hideKeyboard()
            val date = binding.layoutDate.tilDate.editText?.text.toString().trim()
            if (date.isEmpty()) {
                Snackbar.make(binding.container, "Enter date!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val boggieNumber = binding.layoutBogie.tilBogieNumber.editText?.text.toString()
            if (boggieNumber.isNotEmpty() && boggieNumber.toInt() > 100) {
                Snackbar.make(
                    binding.container,
                    "You can not select more than 100 boggie!",
                    Snackbar.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            binding.layoutDate.tilDate.isEnabled = false

            if (binding.layoutBogie.tilBogieNumber.editText?.text.toString().isEmpty()) {
                binding.layoutBogie.tilBogieNumber.editText?.setText("1")
            }

            binding.layoutBogie.tilBogieNumber.isEnabled = false
            binding.showList = true
            initList()

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (askForBackPress) {
                            showBackConfirmationDialog()
                        } else {
                            isEnabled = false
                            requireActivity().onBackPressed()
                        }
                    }

                })
        }

        binding.btnAddData.setOnClickListener {
            //Save to database.
            hideKeyboard()
            Timber.d("Entry : $entry")

            if (validate()) {

                lifecycleScope.launch {

                    try {
                        entry.qty = noOfBogie
                        viewModel.saveEntry(entry)
                        Snackbar.make(
                            binding.container,
                            "Data Added Successfully!",
                            Snackbar.LENGTH_LONG
                        ).show()
                        initViews()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Snackbar.make(
                            binding.container,
                            "Something went wrong!",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        binding.btnViewData.setOnClickListener {
            Snackbar.make(binding.container, "Feature pending!", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showBackConfirmationDialog() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.go_back_ask).setTitle(R.string.are_you_sure)
            .setPositiveButton(R.string.exit) { dialog, id ->
                requireActivity().finish()
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun initViews() {

        entry = Entry()
        noOfBogie = 1
        binding.showList = false

        binding.layoutDate.tilDate.editText?.setText("")
        binding.layoutDate.tilDate.isEnabled = true

        binding.layoutBogie.tilBogieNumber.editText?.setText("")
        binding.layoutBogie.tilBogieNumber.isEnabled = true
    }

    private fun validate(): Boolean {

        entry.componentEntry.let {
            it?.forEach { compEntry ->
                val qty = compEntry.component?.qty
                val max = noOfBogie * qty!!
                if (compEntry.pass + compEntry.fail != max) {
                    Snackbar.make(
                        binding.container,
                        "Please check ${compEntry.component!!.name}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    return false
                }
            }
        }

        return true
    }

    private fun initList() {

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



                if (!bogieNumberText.isEmpty()) {
                    noOfBogie = bogieNumberText.toInt()
                }

                adapter = ComponentAdapter(
                    noOfBogie,
                    componentEntryList.size,
                    onPassSave = { editText, data, pos ->

                        val componentEntry = componentEntryList[pos]
                        //val qty = componentEntry.component?.qty
                        //val max = noOfBogie * qty!!

                        componentEntry.pass = data

                        /*if(data <= max){
                            componentEntry.fail = max - data
                            if(!binding.rvComponentData.isComputingLayout){
                                adapter.notifyItemChanged(pos)
                                editText.setSelection(data.toString().length)
                            }
                        }*/

                    }) { editText, data, pos ->

                    val componentEntry = componentEntryList[pos]
                    //val qty = componentEntry.component?.qty
                    //val max = noOfBogie * qty!!

                    componentEntry.fail = data

                    /*if(data <= max){
                        componentEntry.pass = max - data
                        if(!binding.rvComponentData.isComputingLayout) {
                            adapter.notifyItemChanged(pos)
                            editText.setSelection(data.toString().length)
                        }
                    }*/
                }

                binding.rvComponentData.adapter = adapter
                binding.rvComponentData.addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL
                    )
                )

                binding.rvComponentData.isNestedScrollingEnabled = false
                adapter.submitList(componentEntryList)

                askForBackPress = true
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
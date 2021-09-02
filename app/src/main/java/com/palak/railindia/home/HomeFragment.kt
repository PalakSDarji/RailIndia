package com.palak.railindia.home

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kal.rackmonthpicker.RackMonthPicker
import com.palak.railindia.MainActivity
import com.palak.railindia.R
import com.palak.railindia.databinding.HomeFragmentBinding
import com.palak.railindia.di.DateMonthSDF
import com.palak.railindia.di.DateSDF
import com.palak.railindia.di.MonthSDF
import com.palak.railindia.di.TimeStampSDF
import com.palak.railindia.model.ComponentEntry
import com.palak.railindia.model.Entry
import com.palak.railindia.repo.ComponentRepo
import com.palak.railindia.utils.HomeViewStatus
import com.palak.railindia.utils.Utils
import com.palak.railindia.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    private var askForBackPress: Boolean = false
    private lateinit var adapter: ComponentAdapter
    private lateinit var binding: HomeFragmentBinding
    private val viewModel by activityViewModels<HomeViewModel>()

    @Inject
    lateinit var componentRepo: ComponentRepo

    private lateinit var entry: Entry
    private var noOfBogie: Int = 1

    @Inject
    @DateSDF
    lateinit var dateSdf: SimpleDateFormat

    @Inject
    @MonthSDF
    lateinit var monthSdf: SimpleDateFormat

    @Inject
    @DateMonthSDF
    lateinit var dateMonthSdf: SimpleDateFormat

    @Inject
    @TimeStampSDF
    lateinit var timeStampSdf: SimpleDateFormat

    var cal = Calendar.getInstance()

    val isUnsupportedDevice by lazy { Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP }

    var isInSearchMode: Boolean = false

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
        viewModel.setHomeViewStatus(HomeViewStatus.Empty)

        lifecycleScope.launchWhenCreated {
            viewModel.homeViewStatusFlow.collect {
                when(it){
                    HomeViewStatus.Empty -> {
                        askForBackPress = false
                        binding.hideActionButtons = false
                        binding.showList = false
                        binding.isSearching = false
                        binding.llExport.visibility = View.VISIBLE
                    }
                    HomeViewStatus.ShowList -> {
                        askForBackPress = true
                        binding.hideActionButtons = true
                        binding.showList = true
                        binding.isSearching = false
                        binding.llExport.visibility = View.GONE
                    }
                    HomeViewStatus.Searching -> {
                        binding.hideActionButtons = true
                        binding.showList = false
                        binding.isSearching = true
                        binding.llExport.visibility = View.GONE
                    }

                }
            }
        }

        viewModel.downloadComponentData()

        binding.layoutDate.sdf = dateSdf
        binding.homeViewModel = viewModel

        binding.layoutDate.tilDate.editText?.setOnClickListener {
            showDialogPicker()
        }

        binding.btnContinue.setOnClickListener {

            if(!validateDateBoggieInput()) return@setOnClickListener

            lifecycleScope.launch {

                viewModel.setHomeViewStatus(HomeViewStatus.Searching)

                val dateFormatted = dateSdf.format(viewModel.selectedDate!!)
                viewModel.searchByDate(dateFormatted).collect { result ->

                    when {
                        result.isSuccess -> {

                            val foundEntry = result.getOrNull()
                            foundEntry?.let { e ->
                                Timber.d("time to load entry :: $e")
                                isInSearchMode = true
                                entry = foundEntry
                                noOfBogie = foundEntry.qty
                                loadView()
                            }

                            /*//If returned success means date is available. dont let user enter.
                            Snackbar.make(
                                binding.container,
                                context?.getString(R.string.date_available) + dateFormatted,
                                Snackbar.LENGTH_LONG
                            ).show()

                            viewModel.setHomeViewStatus(HomeViewStatus.Empty)*/
                        }
                        result.isFailure -> {

                            loadView()
                        }
                    }

                    isInSearchMode = false
                }
            }
        }

        binding.btnAddData.setOnClickListener {
            //Save to database.
            hideKeyboard()
            Timber.d("Entry : $entry")

            if (validate()) {

                if(!Utils.isOnline(requireContext())){
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setMessage(R.string.no_internet)
                        .setPositiveButton(R.string.ok) { dialog, id ->
                            dialog.dismiss()
                        }
                    // Create the AlertDialog object and return it
                    builder.create().show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {

                    try {
                        entry.qty = noOfBogie
                        viewModel.syncEntryToServer(entry)
                        Snackbar.make(
                            binding.container,
                            "Data Added Successfully!",
                            Snackbar.LENGTH_LONG
                        ).show()
                        initViews()
                        viewModel.setHomeViewStatus(HomeViewStatus.Empty)
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

        binding.llExport.setOnClickListener {

            hideKeyboard()
            Timber.d("Export Clicked")

            (requireActivity() as MainActivity).downloadExcel{
                showMonthPicker()
            }
        }
    }

    private fun searchMonth(time: java.util.Date) {

        viewModel.setHomeViewStatus(HomeViewStatus.Searching)
        lifecycleScope.launch {
            val date = monthSdf.format(time)
            viewModel.searchByMonth(date).collect { result ->
                when {
                    result.isSuccess -> {
                        val list = result.getOrNull()
                        list?.let {
                            entryList ->
                            val listOfComponents= componentRepo.fetchAllFromDb().first()
                            Utils.createSpreadSheet(requireContext(),
                                date, dateMonthSdf,timeStampSdf, entryList, isUnsupportedDevice, listOfComponents)
                        }

                    }
                    result.isFailure -> {
                        Snackbar.make(
                            binding.container,
                            "${result.exceptionOrNull()?.message}",
                            Snackbar.LENGTH_LONG
                        ).show()

                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
                viewModel.setHomeViewStatus(HomeViewStatus.Empty)
            }
        }
    }

    private fun validateDateBoggieInput() : Boolean{

        hideKeyboard()
        val date = binding.layoutDate.tilDate.editText?.text.toString().trim()
        if (date.isEmpty()) {
            Snackbar.make(binding.container, "Enter date!", Snackbar.LENGTH_LONG).show()
            return false
        }

        val boggieNumber = binding.layoutBogie.tilBogieNumber.editText?.text.toString()
        if (boggieNumber.isNotEmpty() && boggieNumber.toInt() > 100) {
            Snackbar.make(
                binding.container,
                "You can not select more than 100 boggie!",
                Snackbar.LENGTH_LONG
            ).show()
            return false
        }

        return true
    }

    private fun loadView() {

        binding.layoutDate.tilDate.isEnabled = false

        if (binding.layoutBogie.tilBogieNumber.editText?.text.toString().isEmpty()) {
            binding.layoutBogie.tilBogieNumber.editText?.setText(""+noOfBogie)
        }

        binding.layoutBogie.tilBogieNumber.isEnabled = false

        viewModel.setHomeViewStatus(HomeViewStatus.ShowList)
        //binding.showList = true
        //setActionButtons(false)
        initList()
        setSubmitButton()

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

    private fun setSubmitButton() {

        if(isInSearchMode){
            binding.btnAddData.text = getString(R.string.update_data)
        }
        else{
            binding.btnAddData.text = getString(R.string.add_data)
        }
    }

    private fun showMonthPicker(){

        RackMonthPicker(requireActivity()).setLocale(Locale.ENGLISH)
            .setPositiveButton { month, startDate, endDate, year, monthLabel ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR,year)
                cal.set(Calendar.MONTH, month-1)
                searchMonth(cal.time)
            }.setNegativeButton {
                it.dismiss()
            }.show()
    }

    private fun showDialogPicker() {
        DatePickerDialog(
            requireContext(),
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showBackConfirmationDialog() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.go_back_ask).setTitle(R.string.are_you_sure)
            .setPositiveButton(R.string.exit) { dialog, id ->

                initViews()
                viewModel.setHomeViewStatus(HomeViewStatus.Empty)
                //requireActivity().finish()
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
        // Create the AlertDialog object and return it
        builder.create().show()
    }

    private fun initViews() {

        entry = Entry()
        entry.id = UUID.randomUUID().toString()
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

        if (isInSearchMode) {
            entry.componentEntry?.let { initAdapter(it) }
        } else {
            lifecycleScope.launchWhenCreated {

                viewModel.componentLiveData.map {
                    it.map {
                        val ce = ComponentEntry()
                        ce.id = UUID.randomUUID().toString()
                        ce.componentId = it.id
                        ce.component = it
                        ce
                    }
                }.distinctUntilChanged().collect { componentEntryList ->

                    entry.componentEntry = componentEntryList

                    Timber.d("printing flow $componentEntryList")

                    val bogieNumberText =
                        binding.layoutBogie.tilBogieNumber.editText?.text.toString()

                    if (!bogieNumberText.isEmpty()) {
                        noOfBogie = bogieNumberText.toInt()
                    }

                    initAdapter(componentEntryList)


                }
            }
        }
    }

    private fun initAdapter(componentEntryList: List<ComponentEntry>) {

        adapter = ComponentAdapter(
            noOfBogie,
            isInSearchMode,
            componentEntryList.size,
            onPassSave = { editText, data, pos ->

                val componentEntry = componentEntryList[pos]
                componentEntry.pass = data

            }, onFailSave = { editText, data, pos ->

                val componentEntry = componentEntryList[pos]
                componentEntry.fail = data

            })

        binding.rvComponentData.layoutManager = LinearLayoutManager(context)
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

    private fun setDate() {

        cal.set(Calendar.HOUR, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        viewModel.selectedDate = cal.time

        val dateFormated = dateSdf.format(viewModel.selectedDate)
        binding.layoutDate.tilDate.editText?.setText(dateFormated)

        entry.date = Date(cal.time.time)
        entry.month = monthSdf.format(entry.date)

        if (isInSearchMode) {

            //Call API to check if data is available. if so, then loadView()

            lifecycleScope.launch {

                viewModel.setHomeViewStatus(HomeViewStatus.Searching)
                viewModel.searchByDate(dateFormated).collect { result ->

                    when {
                        result.isSuccess -> {

                            val foundEntry = result.getOrNull()
                            foundEntry?.let { e ->
                                Timber.d("time to load entry :: $e")

                                entry = foundEntry
                                noOfBogie = foundEntry.qty
                                loadView()
                            }
                        }
                        result.isFailure -> {
                            Snackbar.make(
                                binding.container,
                                "${result.exceptionOrNull()?.message}",
                                Snackbar.LENGTH_LONG
                            ).show()

                            result.exceptionOrNull()?.printStackTrace()
                            viewModel.setHomeViewStatus(HomeViewStatus.Empty)
                            initViews()
                        }
                    }


                    isInSearchMode = false
                }
            }


        }
    }
}
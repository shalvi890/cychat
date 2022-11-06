/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cioinfotech.cychat.features.home.notice

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.databinding.FragmentCreateEventBinding
import com.cioinfotech.cychat.features.cycore.viewmodel.CyCoreViewModel
import com.cioinfotech.cychat.features.home.notice.model.EventModel
import org.matrix.android.sdk.internal.network.NetworkConstants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateEventFragment : VectorBaseFragment<FragmentCreateEventBinding>() {

    private lateinit var selectedDate: Date
    private lateinit var cyCoreViewModel: CyCoreViewModel
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentCreateEventBinding.inflate(inflater, container, false)
    private val myCalendar: Calendar = Calendar.getInstance()
    private var startDateSelected = false
//    private var timeZones = mutableListOf<Timezone>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as NoticeBoardActivity).setToolbarTitle(getString(R.string.create_event))
        cyCoreViewModel = (requireActivity() as NoticeBoardActivity).cyCoreViewModel
//        views.rbLiveEvent.setOnCheckedChangeListener { _, isChecked ->
//            views.etEventLink.isVisible = !isChecked
//            views.etVenue.isVisible = isChecked
//        }
        views.rbOnlineEvent.setOnCheckedChangeListener { _, isChecked ->
            views.tlEventLink.isVisible = isChecked
            views.tlVenue.isVisible = !isChecked
        }
        views.etStartDateAndTime.setOnClickListener {
            startDateSelected = true
            val datePicker = DatePickerDialog(
                    requireContext(), dateListener, myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
            )
            datePicker.setTitle("Select Start Date")
            datePicker.datePicker.minDate = Date().time
            datePicker.datePicker.touchables[0].performClick()
            datePicker.show()
        }

        views.etEndDateAndTime.setOnClickListener {
            startDateSelected = false
            val datePicker = DatePickerDialog(
                    requireContext(), dateListener, myCalendar[Calendar.YEAR],
                    myCalendar[Calendar.MONTH],
                    myCalendar[Calendar.DAY_OF_MONTH]
            )
            datePicker.setTitle("Select End Date")
            datePicker.datePicker.touchables[0].performClick()
            datePicker.datePicker.minDate = selectedDate.time
            datePicker.show()
        }
        views.btnCreateEvent.setOnClickListener {
            val eventType = if (views.rbLiveEvent.isChecked) NetworkConstants.EVENT_LIVE else NetworkConstants.EVENT_ONLINE
            val startDate = views.etStartDateAndTime.text.toString()
            val endDate = views.etEndDateAndTime.text.toString()
            val venue = if (views.rbLiveEvent.isChecked) views.etVenue.text.toString() else views.etEventLink.text.toString()
//            val timeZone = views.etTimeZone.text.toString()

             if (startDate.isEmpty()) {
                views.etStartDateAndTime.error = "Please, select Start Date & Time"
            }else if (endDate.isEmpty()) {
                views.etEndDateAndTime.error = "Please, select End Date & Time"
//            else if (timeZone.isEmpty())
//                views.etTimeZone.error = "Please, select Timezone"
            }else if (eventType == NetworkConstants.EVENT_LIVE && venue.isEmpty()) {
            views.etVenue.error = "Please, enter Event Venue"
        }
        else if (eventType == NetworkConstants.EVENT_ONLINE && venue.isEmpty()) {
            views.etEventLink.error = "Please, enter Event Link"
        } else {
                cyCoreViewModel.eventLiveData.postValue(EventModel(startDate, endDate, venue, "Asia/Kolkata", eventType))
                requireFragmentManager().popBackStack()
            }
        }

//        views.etTimeZone.setOnClickListener {
//            OrgListFragment.getInstance(object : OrgListAdapter.ItemClickListener {
//                override fun onClick(name: String) {
//                    var org: Timezone? = null
//                    for (tempOrg in timeZones)
//                        if (name.contains(tempOrg.tz_name)) {
//                            org = tempOrg
//                            break
//                        }
//                    views.etTimeZone.setText(org?.tz_name)
//                }
//            },
//                    timeZones.map { org -> org.tz_name + org.std_offset }.toMutableList(),
//                    getString(R.string.select_timezone)).show(parentFragmentManager, "")
//        }

        cyCoreViewModel.eventLiveData.observe(viewLifecycleOwner) {
            it?.let { event ->
                views.etStartDateAndTime.setText(event.eventStart)
                views.etEndDateAndTime.setText(event.eventEnd)
//                views.etTimeZone.setText(event.eventTzName)
                views.etVenue.setText(event.eventVenue)
                views.rbLiveEvent.isChecked = it.eventType == NetworkConstants.EVENT_LIVE
                views.rbOnlineEvent.isChecked = it.eventType == NetworkConstants.EVENT_ONLINE
            }
        }

//        cyCoreViewModel.getTimeZones().subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(handleNextPostList())
    }

//    private fun handleNextPostList(): SingleObserver<TimezoneParent> {
//        return object : SingleObserver<TimezoneParent> {
//
//            override fun onSuccess(t: TimezoneParent) {
//                timeZones = t.data
//            }
//
//            override fun onSubscribe(d: Disposable) {}
//
//            override fun onError(e: Throwable) {
//                Timber.log(1, e)
//            }
//        }
//    }

    private var dateListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
        myCalendar.set(Calendar.YEAR, year)
        myCalendar.set(Calendar.MONTH, monthOfYear)
        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val currentTime = Calendar.getInstance()
        val mTimePicker = TimePickerDialog(
                requireContext(), { _, selectedHour, selectedMinute ->
            var dateText = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(myCalendar.time)
            val newHour = if (selectedHour > 9) "$selectedHour" else "0$selectedHour"
            dateText += if (selectedMinute > 9)
                " $newHour:$selectedMinute:00"
            else
                " $newHour:0$selectedMinute:00"
            if (startDateSelected) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                selectedDate = dateFormat.parse(dateFormat.format(myCalendar.time))!!
                views.etStartDateAndTime.setText(dateText)
                views.etStartDateAndTime.error = null
            } else {
                views.etEndDateAndTime.setText(dateText)
                views.etEndDateAndTime.error = null
            }
        }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), true
        )

        mTimePicker.setTitle(if (startDateSelected) "Select Start Time" else "Select End Time")
        mTimePicker.show()
    }
}

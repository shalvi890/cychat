package com.cioinfotech.cychat.features.plugins.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cioinfotech.cychat.databinding.ItemChoiceBinding

class ChoiceAdapter(
//        private val isMultiChoice: Boolean = false,
        private val optionList: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<ChoiceAdapter.ChoiceViewHolder>() {
    //    private var answerList: MutableList<String> = mutableListOf()
    private var singleAnswer = ""
    lateinit var checkClickListener: CheckClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChoiceViewHolder(
            ItemChoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = optionList.size

    override fun onBindViewHolder(holder: ChoiceViewHolder, position: Int) {
        val itemBinding = holder.itemBinding
//        if (isMultiChoice) {
//            itemBinding.checkbox.isVisible = true
//            itemBinding.checkbox.text = optionList[position]
//            itemBinding.checkbox.setOnCheckedChangeListener(null)
//            itemBinding.checkbox.isChecked = answerList.contains(optionList[position])
//            itemBinding.checkbox.setOnCheckedChangeListener { _, isChecked ->
//                checkClickListener.onChecked(optionList[position], isChecked)
//            }
//        } else {
        itemBinding.radioButton.isVisible = true
        itemBinding.radioButton.text = optionList[position]
        itemBinding.radioButton.setOnCheckedChangeListener(null)
        itemBinding.radioButton.isChecked = optionList[position] == singleAnswer
        itemBinding.radioButton.setOnCheckedChangeListener { _, isChecked ->
            checkClickListener.onChecked(optionList[position], isChecked)
        }
//        }
    }

//    fun updateData(list: MutableList<String>) {
//        answerList = list
//        notifyDataSetChanged()
//    }

    fun updateData(answer: String) {
        singleAnswer = answer
        notifyDataSetChanged()
    }

    inner class ChoiceViewHolder(val itemBinding: ItemChoiceBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

    interface CheckClickListener {
        fun onChecked(choice: String, isChecked: Boolean)
    }
}

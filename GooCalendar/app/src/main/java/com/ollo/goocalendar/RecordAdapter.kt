package com.ollo.goocalendar

import android.view.LayoutInflater
import android.view.ScrollCaptureCallback
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordAdapter : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {
    private var recordList: ArrayList<RecordModel> = ArrayList()
    private var onClickBtnSave:((RecordModel) -> Unit)? = null
    private var onClickBtnEdit:((RecordModel)-> Unit)? = null

    fun addRecords(items: ArrayList<RecordModel>) {
        this.recordList = items
        notifyDataSetChanged()
    }

    fun setOnClickBtnSave(callback: (RecordModel) -> Unit) {
        this.onClickBtnSave = callback
    }

    fun setOnClickBtnEdit(callback: (RecordModel) -> Unit) {
        this.onClickBtnEdit = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecordViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
    )

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = recordList[position]
        holder.bindView(record)
        holder.btnSave.setOnClickListener { onClickBtnSave?.invoke(record) }
        holder.btnEdit.setOnClickListener { onClickBtnEdit?.invoke(record) }
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var id = view.findViewById<TextView>(R.id.tvId)
        private var dienstNummer = view.findViewById<TextView>(R.id.tvDienstNummer)
        private var time = view.findViewById<TextView>(R.id.tvTime)
        private var location = view.findViewById<TextView>(R.id.tvLocation)
        private var lohnstunden = view.findViewById<TextView>(R.id.tvLohnstunden)
        private var date = view.findViewById<TextView>(R.id.tvDate)
        var btnSave = view.findViewById<Button>(R.id.btnSave)
        var btnEdit = view.findViewById<Button>(R.id.btnEdit)

        fun bindView(record: RecordModel) {
            id.text = record.id.toString()
            dienstNummer.text = record.dienstNummer
            time.text = record.time
            location.text = record.location
            lohnstunden.text = record.lohnstunden
            date.text = record.date
        }
    }
}
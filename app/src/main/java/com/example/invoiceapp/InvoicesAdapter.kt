package com.example.invoiceapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InvoicesAdapter(
    private var invoices: List<Invoice>,
    context: Context
) : RecyclerView.Adapter<InvoicesAdapter.InvoiceViewHolder>() {

    var onItemClick: ((Invoice) -> Unit)? = null

    inner class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val storeNameTextView: TextView = itemView.findViewById(R.id.storeNameTextView)
        val invoiceNumberTextView: TextView = itemView.findViewById(R.id.invoiceNumberTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.invoice_item, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun getItemCount(): Int = invoices.size

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.dateTextView.text =
            String.format("%02d/%02d", invoice.month, invoice.day)
        holder.storeNameTextView.text = invoice.storeName
        holder.invoiceNumberTextView.text = invoice.invoiceNumber
        holder.amountTextView.text = "$" + invoice.amount.toString()

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(invoice)
        }
    }

    fun refreshData(newInvoices: List<Invoice>){
        invoices = newInvoices
        notifyDataSetChanged()
    }
}
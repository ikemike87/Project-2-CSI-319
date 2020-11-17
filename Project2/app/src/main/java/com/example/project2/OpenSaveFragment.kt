package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val EXTRA_ALIVE_LIST = "Alive_List"
private const val TAG = "saveFragment"

class OpenSaveFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var saveList: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_open_save, container, false)

        recycler = view.findViewById(R.id.save_recycler)

        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = SaveAdapter()

        saveList = requireContext().fileList()

        return view
    }

    private inner class SaveViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.access_save_button)

        fun bind(text: String) {
            button.text = text
        }

        init {
            button.setOnClickListener {
                val intent = Intent(activity, MainActivity::class.java)
                //intent.putExtra(EXTRA_ALIVE_LIST, getSaveData(button.text))
                startActivity(intent)
            }
        }
    }

    private inner class SaveAdapter : RecyclerView.Adapter<SaveViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaveViewHolder {
            val view = layoutInflater.inflate(R.layout.list_item_save, parent, false)
            return SaveViewHolder(view)
        }

        override fun onBindViewHolder(holder: SaveViewHolder, position: Int) {
            holder.bind(saveList[position])
        }

        override fun getItemCount(): Int {
            // count is amount of saves
            return saveList.count()
        }
    }

    /*
    private fun getSaveData(fileName: String): IntArray {
        var aliveList: IntArray
        var aliveListString: String

        activity?.openFileInput(fileName).use { stream ->
            val text = stream?.bufferedReader().use {
                it?.readText()

            }
            text?.drop(1)
            text?.dropLast(1)
            if (text != null) {
                aliveListString = text
            }
        }

        aliveList = aliveListString.to

        return aliveList
    }
    */
}
package com.example.project2

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.parseColor
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "grid_fragment"
private const val EXTRA_ALIVE_LIST = "Alive_List"
private const val EXTRA_SAVE = "Open_Saves"

class GridFragment : Fragment() {

    private lateinit var resetImageButton: ImageButton
    private lateinit var startImageButton: ImageButton
    private lateinit var stopImageButton: ImageButton
    private lateinit var cloneButton: Button
    private lateinit var saveImageButton: ImageButton
    private lateinit var openButton: Button
    private lateinit var deadColorSpinner: Spinner
    private lateinit var aliveColorSpinner: Spinner
    private lateinit var recycler: RecyclerView
    private lateinit var gridViewModel: GridViewModel

    // class for alive color spinner changes color of alive cells
    private inner class AliveColorSpinner : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            gridViewModel.aliveColor = parent.getItemAtPosition(pos).toString()
            gridViewModel.gameState = false
            for ( position in gridViewModel.gridAliveList ) {
                recycler.adapter?.notifyItemChanged(position)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    // class for dead color spinner changes color of dead cells
    private inner class DeadColorSpinner : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
            gridViewModel.deadColor = parent.getItemAtPosition(pos).toString()
            gridViewModel.gameState = false
            val drawable = ResourcesCompat.getDrawable(resources, R.drawable.grid_button_border, null) as GradientDrawable
            drawable.setColor(parseColor(gridViewModel.deadColor))
            recycler.adapter?.notifyDataSetChanged()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grid, container, false)

        recycler = view.findViewById(R.id.grid_recycler)
        resetImageButton = view.findViewById(R.id.reset_image_button)
        startImageButton = view.findViewById(R.id.start_image_button)
        stopImageButton = view.findViewById(R.id.stop_image_button)
        cloneButton = view.findViewById(R.id.clone_button)
        saveImageButton = view.findViewById(R.id.save_button)
        openButton = view.findViewById(R.id.open_button)
        deadColorSpinner = view.findViewById(R.id.dead_color_spinner)
        aliveColorSpinner = view.findViewById(R.id.alive_color_spinner)

        //https://developer.android.com/guide/topics/ui/controls/spinner
        ArrayAdapter.createFromResource(
            requireContext(), R.array.color_array_alive,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            aliveColorSpinner.adapter = adapter
            aliveColorSpinner.onItemSelectedListener = AliveColorSpinner()
        }

        ArrayAdapter.createFromResource(
            requireContext(), R.array.color_array_dead,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            deadColorSpinner.adapter = adapter
            deadColorSpinner.onItemSelectedListener = DeadColorSpinner()
        }

        recycler.layoutManager = GridLayoutManager(context, 20)
        recycler.adapter = GridAdapter()

        gridViewModel = ViewModelProvider(this).get(GridViewModel::class.java)

        val aliveList = arguments?.getIntArray(EXTRA_ALIVE_LIST)

        if (aliveList != null) {
            gridViewModel.gridAliveList = aliveList.toMutableList()
            for (position in gridViewModel.gridAliveList) {
                gridViewModel.gridItemList[position].isAlive = true
                recycler.adapter?.notifyItemChanged(position)
            }
        }

        // make all alive slots dead
        resetImageButton.setOnClickListener {
            gridViewModel.gameState = false
            gridViewModel.resetGrid()
            for (position in gridViewModel.gridAliveList) {
                recycler.adapter?.notifyItemChanged(position)
            }
            gridViewModel.gridAliveList.clear()
        }

        // set gameState to true
        // start game
        startImageButton.setOnClickListener {
            gridViewModel.gameState = true
            playGame()
        }

        // stop game
        // set gameState to false
        stopImageButton.setOnClickListener { gridViewModel.gameState = false }

        // clone activity
        cloneButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(EXTRA_ALIVE_LIST, gridViewModel.gridAliveList.toIntArray())
            startActivity(intent)
            Toast.makeText(activity, "Cloned" ,Toast.LENGTH_SHORT).show()
        }

        saveImageButton.setOnClickListener {
            saveGrid()
            Toast.makeText(activity, "Saved" ,Toast.LENGTH_SHORT).show()
        }

        openButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(EXTRA_SAVE, "Save")
            startActivity(intent)
        }

        return view
    }

    private inner class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: Button = view.findViewById(R.id.grid_button)
        private var gridPosition = 0

        fun bind(p: Int) {
            gridPosition = p
        }

        init {
            button.setOnClickListener {
                setIsAlive(gridPosition)
                recycler.adapter?.notifyItemChanged(gridPosition)
            }
        }
    }

    private inner class GridAdapter : RecyclerView.Adapter<GridViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            val view = layoutInflater.inflate(R.layout.list_item_grid_slot, parent, false)
            return GridViewHolder(view)
        }

        override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
            holder.bind(position)

            val animation: Animator = AnimatorInflater.loadAnimator(requireContext(), R.animator.grid_pulse_animation)

            if (gridViewModel.gridItemList[position].isAlive) {
                //https://stackoverflow.com/questions/18033260/set-background-color-android
                holder.button.setBackgroundColor(parseColor(gridViewModel.aliveColor))
                animation.apply {
                    setTarget(holder.button)
                    start()
                }

            } else {
                holder.button.setBackgroundResource(R.drawable.grid_button_border)
                animation.apply {
                    setTarget(holder.button)
                    cancel()
                    end()
                }
            }
        }

        // 20x20 grid will always have 400 slots
        override fun getItemCount(): Int {
            return 400
        }
    }

    private fun saveGrid() {
        val fileCount = requireContext().fileList().count()
        val aliveListString = gridViewModel.gridAliveList.toString().toByteArray()
        activity?.openFileOutput("Save ${fileCount + 1}", Context.MODE_PRIVATE).use {
            it?.write(aliveListString)
        }
    }

    private fun setIsAlive(position: Int) {
        if (gridViewModel.gridItemList[position].isAlive) {
            gridViewModel.gridItemList[position].isAlive = false
            gridViewModel.gridAliveList.remove(position)
        }
        else {
            gridViewModel.gridItemList[position].isAlive = true
            gridViewModel.gridAliveList.add(position)
        }
    }

    private fun playGame() {
        //https://stackoverflow.com/questions/61023968/how-to-solve-handler-deprecated
        if (gridViewModel.gameState) {
            Handler(Looper.getMainLooper()).postDelayed ({
                playOneGameCycle()
                playGame()
            }, 1000)
        }
    }

    private fun playOneGameCycle() {
        // clear previous generation data
        gridViewModel.deadNeighbors.clear()
        gridViewModel.newAliveState.clear()
        gridViewModel.newNeighborState.clear()

        // go through alive slots
        // get neighbors
        // go through neighbors
        // if alive add to count
        // otherwise add to deadNeighbors set
        // if count is 2 or 3 set new state to alive
        // otherwise set to dead
        for (position in gridViewModel.gridAliveList) {
            var aliveCount = 0
            gridViewModel.getNeighbors(position)

            for (neighbor in gridViewModel.neighborList) {
                if (gridViewModel.gridItemList[neighbor].isAlive) {
                    aliveCount++
                }
                else {
                    gridViewModel.deadNeighbors.add(neighbor)
                }
            }

            if (aliveCount == 2 || aliveCount == 3) gridViewModel.newAliveState.add(true)
            else gridViewModel.newAliveState.add(false)
        }

        // go through deadNeighbor set
        // get neighbors of the dead neighbors
        // go through the neighbors of the dead neighbors
        // if alive add to count
        // if count is 3 set new state to alive
        // otherwise set to dead
        for (position in gridViewModel.deadNeighbors) {
            var aliveCount = 0
            gridViewModel.getNeighbors(position)

            for (neighbor in gridViewModel.neighborList) {
                if (gridViewModel.gridItemList[neighbor].isAlive) {
                    aliveCount++
                }
            }

            if (aliveCount == 3 ) gridViewModel.newNeighborState.add(true)
            else gridViewModel.newNeighborState.add(false)
        }

        // save oldGridAliveList
        // clear grid
        // clear alive list
        val oldGridAliveList = gridViewModel.gridAliveList.toMutableList()
        gridViewModel.resetGrid()
        gridViewModel.gridAliveList.clear()

        // set old alive slots to new states
        // add new alive slots to alive list
        for ((i, position) in oldGridAliveList.withIndex()) {
            gridViewModel.gridItemList[position].isAlive = gridViewModel.newAliveState[i]
            if (gridViewModel.newAliveState[i]) {
                gridViewModel.gridAliveList.add(position)
            }
        }

        // set dead neighbor slots to new states
        // add new alive slots to alive list
        for ((i, position) in gridViewModel.deadNeighbors.withIndex()) {
            gridViewModel.gridItemList[position].isAlive = gridViewModel.newNeighborState[i]
            if (gridViewModel.newNeighborState[i]) {
                gridViewModel.gridAliveList.add(position)
            }
        }

        // update ui
        for (position in oldGridAliveList) {
            recycler.adapter?.notifyItemChanged(position)
        }
        for (position in gridViewModel.deadNeighbors) {
            recycler.adapter?.notifyItemChanged(position)
        }
    }

    companion object {
        fun newInstance(aliveList: IntArray?): GridFragment {
            val args = Bundle().apply {
                putIntArray(EXTRA_ALIVE_LIST, aliveList)
            }
            return GridFragment().apply {
                arguments = args
            }
        }
    }
}

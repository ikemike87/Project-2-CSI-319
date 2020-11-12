package com.example.project2

import android.graphics.Color.parseColor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.android.awaitFrame

private const val TAG = "gridfragment"

class GridFragment : Fragment() {

    private lateinit var resetButton: ImageButton
    private lateinit var startButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var recycler: RecyclerView
    private lateinit var gridViewModel: GridViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_grid, container, false)

        recycler = view.findViewById(R.id.grid_recycler)
        resetButton = view.findViewById(R.id.reset_button)
        startButton = view.findViewById(R.id.start_button)
        stopButton = view.findViewById(R.id.stop_button)

        recycler.layoutManager = GridLayoutManager(context, 20)
        recycler.adapter = GridAdapter()

        gridViewModel = ViewModelProvider(this).get(GridViewModel::class.java)

        // make all alive slots dead
        resetButton.setOnClickListener {
            gridViewModel.resetGrid()
            for (position in gridViewModel.gridAliveList) {
                recycler.adapter?.notifyItemChanged(position)
            }
        }
        startButton.setOnClickListener {
                playOneGameCycle()
        }
        stopButton.setOnClickListener {}

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

            if (gridViewModel.gridItemList[position].isAlive) {
                //https://stackoverflow.com/questions/18033260/set-background-color-android
                holder.button.setBackgroundColor(parseColor("#000000"))
            } else {
                //https://stackoverflow.com/questions/7815689/how-do-you-obtain-a-drawable-object-from-a-resource-id-in-android-package
                holder.button.setBackgroundResource(R.drawable.grid_button_border)
            }

        }

        // 20x20 grid will always have 400 slots
        override fun getItemCount(): Int {
            return 400
        }
    }

    private fun setIsAlive(position: Int) {
        if (gridViewModel.gridItemList[position].isAlive) {
            gridViewModel.gridItemList[position].isAlive = false
            gridViewModel.gridAliveList.remove(position)
        } else {
            gridViewModel.gridItemList[position].isAlive = true
            gridViewModel.gridAliveList.add(position)
        }
    }

    private fun playOneGameCycle() {
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
        for ( position in oldGridAliveList) {
            recycler.adapter?.notifyItemChanged(position)
        }
        for ( position in gridViewModel.deadNeighbors) {
            recycler.adapter?.notifyItemChanged(position)
        }
    }
}

package com.example.project2

import android.content.Context
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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "grid_fragment"
private const val ARG_ALIVE_LIST = "Alive_List"

class GridFragment : Fragment() {

    private lateinit var resetImageButton: ImageButton
    private lateinit var startImageButton: ImageButton
    private lateinit var stopImageButton: ImageButton
    private lateinit var cloneButton: Button
    private lateinit var recycler: RecyclerView
    private lateinit var gridViewModel: GridViewModel
    private var aliveList: ArrayList<Int>? = null
    private var gameState = false

    interface Callbacks {
        fun onCloneButtonClicked(aliveList: ArrayList<Int>?)
    }

    private var callbacks: Callbacks? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aliveList = arguments?.getIntegerArrayList(ARG_ALIVE_LIST)
        //https://proandroiddev.com/backpress-handling-in-android-fragments-the-old-and-the-new-method-c41d775fb776
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                gameState = false
                gridViewModel.gridAliveList = gridViewModel.oldAliveList
                for (position in gridViewModel.gridAliveList) {
                    gridViewModel.gridItemList[position].isAlive = true
                    recycler.adapter?.notifyItemChanged(position)
                }
                isEnabled = false
                activity?.onBackPressed()

            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
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

        recycler.layoutManager = GridLayoutManager(context, 20)
        recycler.adapter = GridAdapter()

        gridViewModel = ViewModelProvider(this).get(GridViewModel::class.java)

        if (aliveList != null)
        {
            gridViewModel.oldAliveList = gridViewModel.gridAliveList
            gridViewModel.gridAliveList = aliveList as ArrayList<Int>
            for (position in gridViewModel.gridAliveList) {
                gridViewModel.gridItemList[position].isAlive = true
                recycler.adapter?.notifyItemChanged(position)
            }
        }

        // make all alive slots dead
        resetImageButton.setOnClickListener {
            Log.d(TAG, "reset button pressed")
            gameState = false
            gridViewModel.resetGrid()
            for (position in gridViewModel.gridAliveList) {
                recycler.adapter?.notifyItemChanged(position)
            }
        }

        // set gameState to true
        // start game
        startImageButton.setOnClickListener {
            gameState = true
            playGame()
        }

        // stop game
        // set gameState to false
        stopImageButton.setOnClickListener { gameState = false }

        // clone activity
        cloneButton.setOnClickListener{
            callbacks?.onCloneButtonClicked(gridViewModel.gridAliveList)
            Toast.makeText(activity, "Cloned" ,Toast.LENGTH_SHORT).show()
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

            if (gridViewModel.gridItemList[position].isAlive) {
                //https://stackoverflow.com/questions/18033260/set-background-color-android
                holder.button.setBackgroundColor(parseColor("#000000"))
            } else {
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
        }
        else {
            gridViewModel.gridItemList[position].isAlive = true
            gridViewModel.gridAliveList.add(position)
        }
    }

    private fun playGame() {
        //https://stackoverflow.com/questions/61023968/how-to-solve-handler-deprecated
        if (gameState) {
            Handler(Looper.getMainLooper()).postDelayed ({
                playOneGameCycle()
                playGame()
            }, 100)
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

    companion object {
        fun newInstance(aliveList: ArrayList<Int>?): GridFragment {
            val args = Bundle().apply {
                putIntegerArrayList(ARG_ALIVE_LIST, aliveList)
            }
            return GridFragment().apply {
                arguments = args
            }
        }
    }
}

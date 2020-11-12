package com.example.project2

import androidx.lifecycle.ViewModel

class GridViewModel : ViewModel() {
    var gridItemList = mutableListOf<GridSlot>()
    var gridAliveList = mutableListOf<Int>()
    var newAliveState = mutableListOf<Boolean>()
    var newNeighborState = mutableListOf<Boolean>()
    var deadNeighbors = mutableSetOf<Int>()
    var neighborList = mutableListOf<Int>()

    init {
        for ( i in 0..399 ) {
            gridItemList.add(GridSlot(i, false))
        }
    }

    fun resetGrid() {
        for ( position in gridAliveList ) {
            gridItemList[position].isAlive = false
        }
    }

    fun getNeighbors(position: Int){
        neighborList.clear()

        when  {
            position == 0 -> {
                neighborList.add(position + 1)  // grid slot to the right of selected grid slot
                neighborList.add(position + 19)  // grid slot to the left of selected grid slot
                neighborList.add(position + 20) // grid slot below the selected grid slot
                neighborList.add(position + 21) // grid slot below and right of the selected grid slot
                neighborList.add(position + 39) // grid slot below and left of the selected grid slot
                neighborList.add(position + 381) // grid slot above and right of the selected grid slot
                neighborList.add(position + 380) // grid slot above and of the selected grid slot
                neighborList.add(position + 399) // grid slot above and left of the selected grid slot
            }
            position == 19 -> {
                neighborList.add(position - 19)  // grid slot to the right of selected grid slot
                neighborList.add(position - 1)  // grid slot to the left of selected grid slot
                neighborList.add(position + 20) // grid slot below the selected grid slot
                neighborList.add(position + 1) // grid slot below and right of the selected grid slot
                neighborList.add(position + 19) // grid slot below and left of the selected grid slot
                neighborList.add(position + 361) // grid slot above and right of the selected grid slot
                neighborList.add(position + 380) // grid slot above and of the selected grid slot
                neighborList.add(position + 379) // grid slot above and left of the selected grid slot
            }
            position == 380 -> {
                neighborList.add(position + 1)  // grid slot to the right of selected grid slot
                neighborList.add(position + 19)  // grid slot to the left of selected grid slot
                neighborList.add(position - 380) // grid slot below the selected grid slot
                neighborList.add(position - 379) // grid slot below and right of the selected grid slot
                neighborList.add(position - 361) // grid slot below and left of the selected grid slot
                neighborList.add(position - 19) // grid slot above and right of the selected grid slot
                neighborList.add(position - 20) // grid slot above and of the selected grid slot
                neighborList.add(position - 1) // grid slot above and left of the selected grid slot
            }
            position == 399 -> {
                neighborList.add(position - 19)  // grid slot to the right of selected grid slot
                neighborList.add(position - 1)  // grid slot to the left of selected grid slot
                neighborList.add(position - 380) // grid slot below the selected grid slot
                neighborList.add(position - 399) // grid slot below and right of the selected grid slot
                neighborList.add(position - 381) // grid slot below and left of the selected grid slot
                neighborList.add(position - 39) // grid slot above and right of the selected grid slot
                neighborList.add(position - 20) // grid slot above and of the selected grid slot
                neighborList.add(position - 21) // grid slot above and left of the selected grid slot
            }
            // top edge no corners
            position < 20 && position != 0 && position != 19 -> {
               neighborList.add(position + 1)  // grid slot to the right of selected grid slot
               neighborList.add(position - 1)  // grid slot to the left of selected grid slot
               neighborList.add(position + 20) // grid slot below the selected grid slot
               neighborList.add(position + 21) // grid slot below and right of the selected grid slot
               neighborList.add(position + 19) // grid slot below and left of the selected grid slot
               neighborList.add(position + 381) // grid slot above and right of the selected grid slot
               neighborList.add(position + 380) // grid slot above and of the selected grid slot
               neighborList.add(position + 379) // grid slot above and left of the selected grid slot
            }
            //left edge no corners
            position % 20 == 0 && position != 0 && position != 380 -> {
                neighborList.add(position + 1)  // grid slot to the right of selected grid slot
                neighborList.add(position + 19)  // grid slot to the left of selected grid slot
                neighborList.add(position + 20) // grid slot below the selected grid slot
                neighborList.add(position + 21) // grid slot below and right of the selected grid slot
                neighborList.add(position + 39) // grid slot below and left of the selected grid slot
                neighborList.add(position - 19) // grid slot above and right of the selected grid slot
                neighborList.add(position - 20) // grid slot above and of the selected grid slot
                neighborList.add(position - 1) // grid slot above and left of the selected grid slot
            }
            // right edge no corners
            position % 20 == 19 && position != 19 && position != 399 -> {
                neighborList.add(position - 19)  // grid slot to the right of selected grid slot
                neighborList.add(position - 1)  // grid slot to the left of selected grid slot
                neighborList.add(position + 20) // grid slot below the selected grid slot
                neighborList.add(position + 1) // grid slot below and right of the selected grid slot
                neighborList.add(position + 19) // grid slot below and left of the selected grid slot
                neighborList.add(position - 39) // grid slot above and right of the selected grid slot
                neighborList.add(position - 20) // grid slot above and of the selected grid slot
                neighborList.add(position - 21) // grid slot above and left of the selected grid slot
            }
            // bottom edge
            position >= 381 && position != 380 && position != 399 -> {
                neighborList.add(position + 1)  // grid slot to the right of selected grid slot
                neighborList.add(position - 1)  // grid slot to the left of selected grid slot
                neighborList.add(position - 380) // grid slot below the selected grid slot
                neighborList.add(position - 379) // grid slot below and right of the selected grid slot
                neighborList.add(position - 381) // grid slot below and left of the selected grid slot
                neighborList.add(position - 19) // grid slot above and right of the selected grid slot
                neighborList.add(position - 20) // grid slot above and of the selected grid slot
                neighborList.add(position - 21) // grid slot above and left of the selected grid slot
            }
            // all others
            else ->  {
                neighborList.add(position + 1)  // grid slot to the right of selected grid slot
                neighborList.add(position - 1)  // grid slot to the left of selected grid slot
                neighborList.add(position + 20) // grid slot below the selected grid slot
                neighborList.add(position + 21) // grid slot below and right of the selected grid slot
                neighborList.add(position + 19) // grid slot below and left of the selected grid slot
                neighborList.add(position - 19) // grid slot above and right of the selected grid slot
                neighborList.add(position - 20) // grid slot above and of the selected grid slot
                neighborList.add(position - 21) // grid slot above and left of the selected grid slot
            }
        }
    }
}
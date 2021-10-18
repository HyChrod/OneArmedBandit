/**
 * Class created by Florian Scheunert on 15.10.2021
 * all rights reserved
 */

package de.FScheunert.OneArmedBandit

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import kotlin.random.Random

/**
 * All icons used in this application are from the following source:
 * https://www.flaticon.com/
 */
class MainActivity : AppCompatActivity() {

    // Changes the amount of fruits to randomly pick from.
    // Lower numbers higher the chance of a win.
    // Max value = 6
    private val CFG_AMOUNT_OF_FRUITS = 3
    // Controls how many repetitions the spin will move.
    // Higher numbers equals to a longer spin-time
    // Min: 5, Default: 15
    private val CFG_AMOUNT_OF_REPETITIONS = 15

    // Array containing the ressource id of all fruit images
    private val mipmapIds: IntArray = intArrayOf(
        R.mipmap.fruit_apple_foreground,
        R.mipmap.fruit_lemon_foreground,
        R.mipmap.fruit_orange_foreground,
        R.mipmap.fruit_strawberry_foreground,
        R.mipmap.fruit_watermelon_foreground,
        R.mipmap.fruit_bananas_foreground)

    // Resource IDs of the win and lose icons
    private val mipmapResultWin = R.mipmap.result_win_foreground
    private val mipmapResultLose = R.mipmap.result_lose_foreground

    // Two dimensional array to save the ids from the previous rotation
    // Is needed to get the "shuffle-animation" right
    private var oldMipMapIDs = Array(2) {
        intArrayOf(R.mipmap.fruit_apple_foreground,R.mipmap.fruit_apple_foreground,R.mipmap.fruit_apple_foreground)
        intArrayOf(R.mipmap.fruit_apple_foreground,R.mipmap.fruit_apple_foreground,R.mipmap.fruit_apple_foreground)
    }

    // Variables to keep track of statistics
    private var spins = 0.0
    private var wins = 0.0

    // Identifies if there is already a spin running to prevent the
    // spin button from being spam-clicked
    private var currentlySpinning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate has been called!")
        setContentView(R.layout.activity_main)
        initialize()
    }

    private fun initialize() {
        findViewById<Button>(R.id.btn_spin).setOnClickListener {
            // If there is not already a spin running, start a new one
            if (!isSpinning()) {
                toggleSpinning()
                toggleResultVisibility(false)
                updateStatistics(false) { spins++ }
                spin(0)
            }
        }
    }

    // Recursive method which will call itself as long as defined in CFG_AMOUNT_OF_REPETITIONS
    // Handles the "Spin-Animation" and basically the main logic of the app
    private fun spin(repetition: Int) {
        // Create a list of 3 random values in the range of CFG_AMOUNT_OF_FRUITS
        val randomValues = List(3) { Random.nextInt(CFG_AMOUNT_OF_FRUITS) }

        // To make the different rows of fruits stop moving at different times
        // the first row stops getting updated at a third of the repetitions and the
        // second row at two third of the repetitions
        if(repetition < getSpinRepetitions()) updateColumnOfFruits(0, randomValues)
        if(repetition < getSpinRepetitions()*2) updateColumnOfFruits(1, randomValues)
        updateColumnOfFruits(2, randomValues)

        // As long as we have not reached the amount of desired repetitions, the method recalls
        // itself after a slight delay of 250ms
        if(repetition < getSpinRepetitions()*3) {
            Handler().postDelayed({
                spin(repetition+1)
                                  }, 250)
        } else {
            // At the end, enable the spin button again and check the results
            toggleSpinning()
            checkResults()
        }

    }

    // This method compares the results of the last round and checks for a potential win
    private fun checkResults() {
        val imageView = findViewById<ImageView>(R.id.result_imageView)
        log("Final combination of icons is ${oldMipMapIDs[0][0]} & ${oldMipMapIDs[0][1]} & ${oldMipMapIDs[0][2]}")
        // if the values of the middle row are identical we have identified a win
        if(oldMipMapIDs[0][0] == oldMipMapIDs[0][1] && oldMipMapIDs[0][1] == oldMipMapIDs[0][2]) {
            // Update the statistics and increase the wins
            updateStatistics(true) { wins++ }
            // Display the winning picture
            imageView.setImageResource(mipmapResultWin)
            log("Game won!")
        } else {
            // Update the statistics but do not increase wins
            updateStatistics(true) {}
            // Display the losing picture
            imageView.setImageResource(mipmapResultLose)
            log("Game loss!")
        }
        // Make the result imageview visable
        toggleResultVisibility(imageView,true)
    }

    // This method rotates one column of fruits to the next set of pictures
    private fun updateColumnOfFruits(rowId: Int, randomValues: List<Int>) {
        getViewByName<ImageView>("spin_icon_bottom_" + (rowId+1)).setImageResource(oldMipMapIDs[0][rowId])
        oldMipMapIDs[0][rowId] = oldMipMapIDs[1][rowId]
        getViewByName<ImageView>("spin_icon_" + (rowId+1)).setImageResource(oldMipMapIDs[1][rowId])
        oldMipMapIDs[1][rowId] = mipmapIds[randomValues[rowId]]
        getViewByName<ImageView>("spin_icon_top_" + (rowId+1)).setImageResource(oldMipMapIDs[1][rowId])
        log("ID for middle icon in row $rowId is now ${oldMipMapIDs[1][rowId]}!")
    }

    // Updates all views displaying the statistics
    // updateLooses: Boolean to decide wether the view for the loses should be update or not
    // function: in-line function to change different values more easily as seen in line 83 or 168
    private fun updateStatistics(updateLoses: Boolean, function: () -> (Unit)) {
        function()
        findViewById<TextView>(R.id.txt_total_spins).text = spins.toInt().toString()
        findViewById<TextView>(R.id.txt_wins).text = wins.toInt().toString()
        if(updateLoses) findViewById<TextView>(R.id.txt_loses).text = (spins-wins).toInt().toString()
        val winLoseRatio = ((wins/spins)*100).toInt().toString() + "%"
        findViewById<TextView>(R.id.txt_wr_ratio).text = winLoseRatio
    }

    // Basically handles if the spin button is able to being pressed or not
    private fun toggleSpinning() {
        currentlySpinning = !currentlySpinning
        val toApply = if (isSpinning()) Color.RED else Color.BLUE
        findViewById<Button>(R.id.btn_spin).setBackgroundColor(toApply)
    }

    // Changes the visibility of a given imageview to hide it during the spins
    private fun toggleResultVisibility(view: ImageView, visible: Boolean) {
        if(visible) view.visibility = View.VISIBLE
        else view.visibility = View.INVISIBLE
    }

    // Overloaded method to be able to call this method without having the view as a parameter
    private fun toggleResultVisibility(visible: Boolean) {
        toggleResultVisibility(findViewById(R.id.result_imageView), visible)
    }

    // Returns the corresponding id of a view by its given name.
    // This method comes from an user on a stackoverflow thread:
    // https://stackoverflow.com/questions/8438943/how-to-find-view-from-string-instead-of-r-id
    private fun getIDByName(name: String): Int {
        return resources.getIdentifier(name, "id", packageName)
    }

    // Generic method to receive a view by its corresponding name
    private fun <T : View?> getViewByName(name: String): T {
        return findViewById<T>(getIDByName(name))
    }

    // Basic getter to check if theres currently a spin running
    private fun isSpinning(): Boolean {
        return currentlySpinning
    }

    // Returns a third of the on top defined amount of repetitions
    private fun getSpinRepetitions(): Int {
        return CFG_AMOUNT_OF_REPETITIONS/3
    }

    // Logs a given message
    private fun log(msg: String) {
        Log.d("[LOG]", msg)
    }

}
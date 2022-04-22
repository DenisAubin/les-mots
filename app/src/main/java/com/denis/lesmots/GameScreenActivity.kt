package com.denis.lesmots

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.denis.lesmots.databinding.ActivityGameScreenBinding
import java.io.InputStream
import java.util.*
import kotlin.random.Random

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameScreenBinding
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    private val lineList = mutableListOf<String>()
    private lateinit var randomWord: String

    private var rowPointer = 0
    private var colPointer = 0

    private val ID_DEF_TYPE: String = "id"
    private val NOT_IN_DICTIONARY_MESSAGE = "Pas dans le dictionnaire"

    private lateinit var activeCharBackground: Drawable
    private lateinit var defaultCharBackground: Drawable
    private lateinit var greenCharBackground: Drawable
    private lateinit var orangeCharBackground: Drawable
    private lateinit var blackCharBackground: Drawable

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = binding.titleText
        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = binding.linearLayout

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //binding.buttonenter.setOnTouchListener(delayHideTouchListener)

        val inputStream: InputStream = resources.openRawResource(R.raw.dict)
        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it) } }

        randomWord = lineList[Random.nextInt(0, lineList.size - 1)]
        binding.randomWord.text = randomWord

        activeCharBackground = resources.getDrawable(R.drawable.char_background, theme)
        defaultCharBackground = resources.getDrawable(R.drawable.char_background, theme)
        greenCharBackground = resources.getDrawable(R.drawable.green_char_background, theme)
        orangeCharBackground = resources.getDrawable(R.drawable.orange_char_background, theme)
        blackCharBackground = resources.getDrawable(R.drawable.black_char_background, theme)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }


    /**
     * Gets the active tile in the current game.
     * @return the Active tile as a TextView
     */
    private fun getActiveTile(): TextView {
        val row = binding.wordTable[rowPointer] as TableRow
        return row[colPointer] as TextView
    }

    /**
     * Gets the tile specific to the row and column parameters
     * @param colIndex is the index of the column
     * @param rowIndex is the index of the row
     * @return the specific tile as a TextView
     */
    private fun getSpecificTile(colIndex: Int, rowIndex: Int): TextView {
        val row = binding.wordTable[rowIndex] as TableRow
        return row[colIndex] as TextView
    }

    /**
     * Gets the tile juste before the active one
     * @return the Previous tile as a TextView
     */
    private fun getPreviousTile(): TextView {
        if (rowPointer == 0 && colPointer == 0) {
            return getActiveTile()
        }
        if (colPointer == 0) {
            val row = binding.wordTable[rowPointer - 1] as TableRow
            return row[4] as TextView
        }
        val row = binding.wordTable[rowPointer] as TableRow
        return row[colPointer - 1] as TextView
    }

    /**
     * Gets the tile juste after the active one
     * @return the Next tile as a TextView
     */
    private fun getNextTile(): TextView {
        if (rowPointer == 5 && colPointer == 4) {
            return getActiveTile()
        }
        if (colPointer == 4) {
            val row = binding.wordTable[rowPointer + 1] as TableRow
            return row[0] as TextView
        }
        val row = binding.wordTable[rowPointer] as TableRow
        return row[colPointer + 1] as TextView
    }

    /**
     * Reacts to a click on one of the letters of the keyboard
     * @param view the view that's been clicked on
     */
    fun onKeyClick(view: View) {
        if (colPointer < 5) {
            val b = view as TextView
            val case = getActiveTile()
            val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.letter_change)
            case.startAnimation(animation)
            case.text = b.text
            colPointer++
            updateActiveTile()
        }
    }

    /**
     * Reacts to a click on enter key of the keyboard
     * @param view the view that's been clicked on
     */
    fun onEnterClick(view: View) {
        val word = getWord()
        if (isInDict(word)) {
            if (word == randomWord) {
                val toast = Toast.makeText(applicationContext, "Bien joué!", Toast.LENGTH_SHORT)
                toast.show()
                colorRow(word)
                Handler(Looper.getMainLooper()).postDelayed({
                    newGame()
                }, 2000)
            } else {
                if (rowPointer < 5) {
                    colorRow(word)
                    rowPointer++
                    colPointer = 0
                } else {
                    colorRow(word)
                    val toast = Toast.makeText(applicationContext, "Perdu!", Toast.LENGTH_SHORT)
                    toast.show()
                    binding.randomWord.visibility = View.VISIBLE
                    Handler(Looper.getMainLooper()).postDelayed({
                        newGame()
                    }, 3000)
                }
            }
        }
    }

    /**
     * Reacts to a click on return key of the keyboard
     * @param view the view that's been clicked on
     */
    fun onReturnClick(view: View) {
        if (colPointer > 0) {
            colPointer--
            val case = getActiveTile()
            val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.letter_change)
            case.startAnimation(animation)
            case.text = ""
            updateActiveTile(false)
        }
    }

    /**
     * Updates the background of the tiles when the active tile changes
     * @param isNotReturnKey boolean in case the return key is used
     */
    private fun updateActiveTile(isNotReturnKey: Boolean = true) {
        if (isNotReturnKey) {
            if (colPointer < 5 && rowPointer < 6) {
                getActiveTile().background = activeCharBackground
            }
            getPreviousTile().background = defaultCharBackground
        } else {
            if (colPointer < 5 && rowPointer < 6) {
                getNextTile().background = defaultCharBackground
            }
            getActiveTile().background = activeCharBackground
        }
    }

    /**
     * Creates a string from the active row
     * @return the word on the active row
     */
    private fun getWord(): String {
        var word = ""
        val row = binding.wordTable[rowPointer] as TableRow
        for (colIndex: Int in 0..4) {
            val col = row[colIndex] as TextView
            word += col.text
        }
        return word.lowercase(Locale.getDefault())
    }

    /**
     * Checks whether the parameter string is in the dictionary
     * And shows a Toast with an animation if not
     * @param word the word to be checked
     * @return boolean true if the word is in the dict
     */
    private fun isInDict(word: String): Boolean {
        for (el: String in lineList) {
            if (el == word) {
                return true
            }
        }
        val toast =
            Toast.makeText(applicationContext, NOT_IN_DICTIONARY_MESSAGE, Toast.LENGTH_SHORT)
        toast.show()
        val row = binding.wordTable[rowPointer] as TableRow
        val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.shake_error)
        row.startAnimation(animation)
        return false
    }

    /**
     * Resets the UI and changes the random word in order to play a new game
     */
    private fun newGame() {
        randomWord = lineList[Random.nextInt(0, lineList.size - 1)]
        binding.randomWord.visibility = View.GONE
        binding.randomWord.text = randomWord
        colPointer = 0
        rowPointer = 0
        for (colIndex: Int in 0..4) {
            for (rowIndex: Int in 0..5) {
                val row = binding.wordTable[rowIndex] as TableRow
                val case = row[colIndex] as TextView
                case.text = ""
                case.background = defaultCharBackground
            }
        }
        getActiveTile().background = activeCharBackground
        for (rowIndex: Int in 0..2) {
            val row = binding.keyboard[rowIndex] as TableRow
            for (colIndex: Int in 0 until (row.childCount)) {
                val key = row[colIndex] as TextView
                key.background = defaultCharBackground
            }
        }
    }

    /**
     * Colors the tiles from the active row when the word is in the dictionary
     * @param word the word from the row that's colored
     */
    private fun colorRow(word: String) {
        for (charIndex: Int in 0..4) {
            val specificTile = getSpecificTile(charIndex, rowPointer)
            val animation: Animation = AnimationUtils.loadAnimation(this, R.anim.letter_change)
            val delay: Long = 200 * (java.lang.Long.parseLong(charIndex.toString()))
            animation.startOffset = delay
            specificTile.startAnimation(animation)
            val keyName = "button" + word[charIndex].lowercase()
            if (randomWord.contains(word[charIndex])) {
                if (word[charIndex] == randomWord[charIndex]) {
                    colorLetter(specificTile, greenCharBackground, delay)
                    val keyId = resources.getIdentifier(
                        keyName,
                        ID_DEF_TYPE, packageName
                    )
                    colorLetter(findViewById(keyId), greenCharBackground, delay)
                } else {
                    if (canBeOrange(word, charIndex)) {
                        colorLetter(specificTile, orangeCharBackground, delay)
                        val keyId = resources.getIdentifier(
                            keyName,
                            ID_DEF_TYPE, packageName
                        )
                        if (findViewById<TextView>(keyId).background?.constantState?.equals(
                                greenCharBackground.constantState
                            ) == false
                        ) {
                            colorLetter(findViewById(keyId), orangeCharBackground, delay)
                        }
                    } else {
                        colorLetter(specificTile, defaultCharBackground, delay)
                        val keyId = resources.getIdentifier(
                            keyName,
                            ID_DEF_TYPE, packageName
                        )
                        colorLetter(findViewById(keyId), blackCharBackground, delay)
                    }
                }
            } else {
                colorLetter(specificTile, defaultCharBackground, delay)
                val keyId = resources.getIdentifier(
                    keyName,
                    ID_DEF_TYPE, packageName
                )
                if (findViewById<TextView>(keyId).background.constantState?.equals(
                        defaultCharBackground.constantState
                    ) == true
                ) {
                    colorLetter(findViewById(keyId), blackCharBackground, delay)
                }
            }
        }
    }

    /**
     * Colors the tile with with background and animation sync
     * @param tile the tile that's being colored
     * @param background the drawable to use to color the tile
     * @param delay the delay after which the color gets applied
     */
    private fun colorLetter(tile: TextView, background: Drawable, delay: Long) {
        Handler(Looper.getMainLooper()).postDelayed({
            tile.background = background
        }, delay)
    }

    /**
     * Checks whether the tile be colored in orange
     * Ensures the behaviour with orange and green coloring is correct
     * @param word the to check
     * @param charIndex the index at which we want to color
     * @return the boolean indicating if the tile can be colored orange
     */
    private fun canBeOrange(word: String, charIndex: Int): Boolean {
        val checkedChar = word[charIndex]
        var count = 0
        for (i: Int in 0..4) {
            if (randomWord[i] == checkedChar) {
                count++
            }
            if (word[i] == checkedChar && i < charIndex) {
                count--
            }
            if (word[i] == checkedChar && randomWord[i] == checkedChar && i > charIndex) {
                count--
            }
        }
        return count > 0
    }
}
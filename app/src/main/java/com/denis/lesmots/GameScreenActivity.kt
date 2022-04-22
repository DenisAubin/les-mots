package com.denis.lesmots

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.get
import com.denis.lesmots.databinding.ActivityGameScreenBinding
import java.io.InputStream
import java.util.*
import kotlin.random.Random

class GameScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameScreenBinding
    private lateinit var fullscreenContent: TextView

    private val lineList = mutableListOf<String>()
    private lateinit var targetWord: String

    private var rowPointer = 0
    private var colPointer = 0

    private val ID_DEF_TYPE: String = "id"
    private val NOT_IN_DICTIONARY_MESSAGE = "Pas dans le dictionnaire"

    private lateinit var activeCharBackground: Drawable
    private lateinit var defaultCharBackground: Drawable
    private lateinit var greenCharBackground: Drawable
    private lateinit var orangeCharBackground: Drawable
    private lateinit var blackCharBackground: Drawable

    private fun hideSystemBars() {
        supportActionBar?.hide()
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemBars()
        super.onCreate(savedInstanceState)

        binding = ActivityGameScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fullscreenContent = binding.titleText

        val inputStream: InputStream = resources.openRawResource(R.raw.dict)
        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it) } }

        val dataFromMenu = intent.getStringExtra(Intent.EXTRA_TEXT)



        targetWord = getTargetWord(dataFromMenu.orEmpty())
        binding.randomWord.text = targetWord

        activeCharBackground = resources.getDrawable(R.drawable.char_background, theme)
        defaultCharBackground = resources.getDrawable(R.drawable.char_background, theme)
        greenCharBackground = resources.getDrawable(R.drawable.green_char_background, theme)
        orangeCharBackground = resources.getDrawable(R.drawable.orange_char_background, theme)
        blackCharBackground = resources.getDrawable(R.drawable.black_char_background, theme)
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
            if (word == targetWord) {
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
        var msg : String = getString(R.string.not_in_dict)
        if (word.length < 5){
            msg=getString(R.string.too_short)
        }
        val toast =
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT)
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
        targetWord = getTargetWord("")
        binding.randomWord.visibility = View.GONE
        binding.randomWord.text = targetWord
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
     * Gets a target word depending on the used mode
     *
     * @param dataFromMenu the data received from the menu indicating the mode
     * @return the word the player has to find
     */
    private fun getTargetWord(dataFromMenu : String) : String{
        return when("Daily" == dataFromMenu){
            true -> "denis"
            false -> lineList[Random.nextInt(0, lineList.size - 1)]
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
            if (targetWord.contains(word[charIndex])) {
                if (word[charIndex] == targetWord[charIndex]) {
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
            if (targetWord[i] == checkedChar) {
                count++
            }
            if (word[i] == checkedChar && i < charIndex) {
                count--
            }
            if (word[i] == checkedChar && targetWord[i] == checkedChar && i > charIndex) {
                count--
            }
        }
        return count > 0
    }
}
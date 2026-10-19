package com.jokellojewels.game.ui

import androidx.annotation.DrawableRes
import com.jokellojewels.game.R

/**
 * The single place where art is wired in. All files live in res/drawable-nodpi with FIXED names
 * (produced from the art pack by tools/import_art.py):
 *
 *   bg_main.jpg (opaque curtain stage)   logo.png
 *   mascot_a.png (greeting jester)  mascot_b.png (jester with emerald)  mascot_c.png (bowing jester)
 *   btn_menu.png (ornate jester-scroll plate)   btn_plate.png (simple gold-rimmed plate)
 *   plate_round.png (level bubbles)   icon_back.png   icon_pause.png   panel_question.png
 *   item_01.png .. item_12.png (match icons)
 *   deco_hat / deco_mask / deco_book / deco_hourglass / deco_scepter / deco_ticket (.png)
 *
 * To drop in new art, overwrite the files with the same names — no code change needed.
 * If a plate's proportions change, update the matching *_ASPECT constant (width / height).
 */
object Assets {
    @DrawableRes val BG_MAIN = R.drawable.bg_main
    @DrawableRes val LOGO = R.drawable.logo
    @DrawableRes val MASCOT_A = R.drawable.mascot_a
    @DrawableRes val MASCOT_B = R.drawable.mascot_b
    @DrawableRes val MASCOT_C = R.drawable.mascot_c
    @DrawableRes val BTN_MENU = R.drawable.btn_menu
    @DrawableRes val BTN_PLATE = R.drawable.btn_plate
    @DrawableRes val PLATE_ROUND = R.drawable.plate_round
    @DrawableRes val ICON_BACK = R.drawable.icon_back
    @DrawableRes val ICON_PAUSE = R.drawable.icon_pause
    @DrawableRes val PANEL_QUESTION = R.drawable.panel_question

    @DrawableRes val DECO_HAT = R.drawable.deco_hat
    @DrawableRes val DECO_MASK = R.drawable.deco_mask
    @DrawableRes val DECO_BOOK = R.drawable.deco_book
    @DrawableRes val DECO_HOURGLASS = R.drawable.deco_hourglass
    @DrawableRes val DECO_SCEPTER = R.drawable.deco_scepter
    @DrawableRes val DECO_TICKET = R.drawable.deco_ticket

    /** Width / height of the plates (fixed-shape art is never stretched out of proportion). */
    const val BTN_MENU_ASPECT = 4.348f
    const val BTN_PLATE_ASPECT = 5.2f
    const val PANEL_QUESTION_ASPECT = 1.923f

    val items: List<Int> = listOf(
        R.drawable.item_01, R.drawable.item_02, R.drawable.item_03, R.drawable.item_04,
        R.drawable.item_05, R.drawable.item_06, R.drawable.item_07, R.drawable.item_08,
        R.drawable.item_09, R.drawable.item_10, R.drawable.item_11, R.drawable.item_12,
    )

    /** Text alternatives for the 12 match icons. */
    val itemNames: List<String> = listOf(
        "Ruby rhombus", "Emerald", "Amethyst", "Star brooch",
        "Jester bell", "Jester goblet", "Gift box", "Treasure chest",
        "Golden lyre", "Royal key", "Emerald potion", "Royal fan",
    )

    const val ITEM_RUBY = 0
    const val ITEM_EMERALD = 1
    const val ITEM_AMETHYST = 2
    const val ITEM_STAR = 3
    const val ITEM_BELL = 4
    const val ITEM_GOBLET = 5
    const val ITEM_GIFT = 6
    const val ITEM_CHEST = 7

    @DrawableRes
    fun item(index: Int): Int = items[index.mod(items.size)]

    fun itemName(index: Int): String = itemNames[index.mod(itemNames.size)]
}

package com.aidandavisdev.aidandavis.prioritylist

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Aidan Davis on 22/12/2017.
 */

class Constants {

    object Intents {
        val PARAM_ITEM = "PARAM_ITEM"
    }

    object DateFormats {
        val fullDate = SimpleDateFormat("h:mm a | EEE, dd-MM-yyyy", Locale.getDefault())
    }
}

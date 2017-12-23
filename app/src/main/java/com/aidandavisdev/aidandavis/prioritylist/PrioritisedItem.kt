package com.aidandavisdev.aidandavis.prioritylist

import java.io.Serializable
import java.util.*

/**
 * Created by Aidan Davis on 9/12/2017.
 */

class PrioritisedItem(val id: String,
                      var name: String,
                      var description: String,
                      var startDate: Date?,
                      var endDate: Date?,
                      var importance: Int,
                      var effort: Int,
                      var ticked: Boolean) : Serializable {

    init {
        // check that effort and importance are within expected range
        // would be interesting to have these limits as a remote config
        if (importance > 10 || importance < 1) throw Exception()
        if (effort > 10 || effort < 1) throw Exception()
        // if start date and no end date, throw exception
        if (startDate != null && endDate == null) throw Exception()
        // if start date equal to or after end date, throw exception
        if (startDate != null && endDate != null) {
            if (startDate!!.time >= endDate!!.time) throw Exception()
        }
    }

    //returns: 1 =< urgency <= 10
    private fun getUrgency(): Double {
        if (startDate == null || endDate == null) return 1.0

        var urgency = ((Calendar.getInstance().timeInMillis - startDate!!.time) / (endDate!!.time - startDate!!.time)) * 10.0

        if (urgency < 1.0) urgency = 1.0
        return urgency
    }

    // returns the priority score of this object
    // places importance, urgency and effort in it's own dimension from origin.
    // larger distance from origin would be done first
    fun getScore(): Double {
        val revEffort = 11 - effort
        return Math.sqrt(Math.pow(importance.toDouble(), 2.0) +
                Math.pow(getUrgency(), 2.0) +
                Math.pow(revEffort.toDouble(), 2.0))
    }
}
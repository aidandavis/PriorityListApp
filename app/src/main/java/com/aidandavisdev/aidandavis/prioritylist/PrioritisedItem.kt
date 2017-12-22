package com.aidandavisdev.aidandavis.prioritylist

import java.io.Serializable

/**
 * Created by Aidan Davis on 9/12/2017.
 */

class PrioritisedItem(val id: String,
                      var name: String,
                      var description: String,
                      var importance: Int,
                      var effort: Int,
                      var ticked: Boolean) : Serializable {

    // would be interesting to have these limits as a remoteconfig
    init {
        if (importance > 10 || importance < 1) {
            throw Exception()
        }
        if (effort > 10 || effort < 1) {
            throw Exception()
        }
    }

    // returns the priority score of this object
    // places importance, urgency and effort in it's own dimension from origin.
    // larger distance from origin would be done first
    fun getScore(): Double {
        val revEffort = 11 - effort
        return Math.sqrt(Math.pow(importance.toDouble(), 2.0) +
//                Math.pow(urgency.toDouble(), 2.0) +
                Math.pow(revEffort.toDouble(), 2.0))
    }
}
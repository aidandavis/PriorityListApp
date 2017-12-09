package com.aidandavisdev.aidandavis.prioritylist

import java.util.*

/**
 * Created by Aidan Davis on 9/12/2017.
 */

class PrioritisedItem(val id: String, var name: String, var details: String, val dateCreated: Date, var importance: Int, var urgency: Int, var effort: Int) {

    // would be interesting to have these limits as a remoteconfig
    init {
        if (importance > 5 || importance < 1) {
            throw Exception()
        }
        if (urgency > 5 || urgency < 1) {
            throw Exception()
        }
        if (effort > 5 || effort < 1) {
            throw Exception()
        }
    }

    // returns the priority score of this object
    // places importance, urgency and effort in it's own dimension from origin.
    // larger distance from origin would be done first
    fun getScore(): Double {
        val revEffort = 6 - effort
        return Math.sqrt(Math.pow(importance.toDouble(), 2.0) + Math.pow(urgency.toDouble(), 2.0) + Math.pow(revEffort.toDouble(), 2.0))
    }
}
package com.devs.mdmanager

import android.content.Context
import com.google.android.gms.maps.GoogleMap

/**
 * Created by ${Deven} on 2019-08-27.
 */



class MDMBuilder(val context: Context) {

    /**
     * A field that is marked as lateinit must be initialized prior to any access.
     * An exception will be thrown if the invoker has not called the appropriate builder method.
     */
    private lateinit var googleMap: GoogleMap

    fun withMap(googleMap: GoogleMap) = apply { this.googleMap = googleMap }
    fun build() = MapDrawingManager(googleMap, context)

}
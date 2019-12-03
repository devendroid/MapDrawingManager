package com.devs.mdmanager

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil

/**
 * Created by ${Deven} on 2019-08-22.
 */
data class MapDrawingManager(
    val googleMap: GoogleMap,
    val context: Context,
    var removeListener: OnShapeRemoveListener? = null,
    var drawListener: OnShapeDrawListener? = null,
    var markerSize: Int = DEFAULT_MARKER_SIZE,
    var markerColor: Int = DEFAULT_MARKER_COLOR,
    var badgeColor: Int = DEFAULT_BADGE_COLOR,
    var badgeTextColor: Int = DEFAULT_BADGE_TEXT_COLOR,
    var editable: Boolean = DEFAULT_EDITABLE,
    var strokWidth: Float = DEFAULT_STROKE_WIDTH,
    var fillColor: Int = DEFAULT_FILL_COLOR,
    var strokColor: Int = DEFAULT_STROKE_COLOR,
    private var _shapeType: ShapeType = DEFAULT_SHAPE_TYPE)
    :
    GoogleMap.OnMapClickListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener {

    var shapeType: ShapeType
        get() = _shapeType
        set(value) {
            /* execute setter logic */
            _shapeType = value

            // Conditions to remove any uncompleted shape during shape type change
            if( !(isLastPolygonDone && isLastPolylineDone) ) {
                currentPolyline?.remove()
                currentPolyline = null
                latLngListCurrent.clear()
                currentMarkers.forEach { it.remove() }
                currentMarkers.clear()
                // Make its true because on option change there are no uncompeted shapes
                isLastPolygonDone = true
                isLastPolylineDone = true
            }
        }

    companion object {
        private val TAG = MapDrawingManager::class.java.simpleName
        private const val DEFAULT_BADGE_COLOR = Color.WHITE
        private const val DEFAULT_BADGE_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_MARKER_COLOR = Color.RED
        private const val DEFAULT_MARKER_SIZE = 12 // in dp
        private const val DEFAULT_STROKE_WIDTH = 7f
        private val DEFAULT_STROKE_COLOR = Color.rgb(120, 161, 46)
        private val DEFAULT_FILL_COLOR = Color.argb(90, 120, 161, 46)
        private val DEFAULT_SHAPE_TYPE = ShapeType.POLYGON
        private const val DEFAULT_EDITABLE = true
        private const val DEFAULT_CIRCLE_RADIUS = 64.0
        private val POLYLINE_STROKE_COLOR = Color.rgb(218, 179, 32)
        private val CIRCLE_STROKE_COLOR = Color.rgb(42, 177, 155)
        private val CIRCLE_FILL_COLOR = Color.argb(90, 42, 177, 155)

    }

    // Don't forgot to reset all "Temp lists" on clear and complete any shape
    private val latLngListCurrent = ArrayList<LatLng>()
    private val latLngListTemp = ArrayList<LatLng>()
    private val helperMarkersList = ArrayList<Marker>()

    private var isRemoveMode = false
    private var isLastPolygonDone = true
    private var isLastPolylineDone = true
    private var currentEditingShape: ShapeType = ShapeType.POLYGON

    private var polygonCount = 0
    private var polylineCount = 0
    private var circleCount = 0
    private var markerCount = 0

    // Triple can contain <All Dots, Badge, Shape>
    public var polygonList = ArrayList<Triple<ArrayList<Marker>, Marker, Polygon>>()
        private set
    public var polylineList = ArrayList<Triple<ArrayList<Marker>,Marker, Polyline>>()
        private set
    public var circleList = ArrayList<Triple<List<Marker>, Marker, Circle>>()
        private set
    public var markerList = ArrayList<Marker>()
        private set

    // Don't forgot to reset "shapeID" on clear and complete any shape
    private var shapeID = System.currentTimeMillis()
    // Don't forgot to reset "current elements" on clear and complete any shape
    private val currentMarkers = ArrayList<Marker>()
    private var tempPolyline: Polyline? = null
    private var currentPolyline: Polyline? = null
    private var currentPolygon: Polygon? = null
    private var currentCircle: Circle? = null
    private var currentMarker: Marker? = null
    private var currentShapeIndex: Int = -1
    /**
     * current marker's count for reference.
     */
    private var currentMarkerIndex: Int = -1

    init {
        googleMap.setOnMapClickListener(this)
        googleMap.setOnMarkerClickListener(this)
        googleMap.setOnMarkerDragListener(this)
    }

    override fun onMapClick(latLng: LatLng) {
        //To prevent drawing any shape if removeMode is enable
        if(isRemoveMode) return

        when (shapeType) {
            ShapeType.POLYGON -> {
                if (isLastPolygonDone) {
                    resetPolygon()
                    isLastPolygonDone = false
                }

                if (latLngListCurrent.size >= 1) {
                    // Get extra point between two points
                    val bc = LatLngBounds.Builder()
                    bc.include(latLngListCurrent.last())
                    bc.include(latLng)
                    addMarkerOnMap(bc.build().center)
                    latLngListCurrent.add(bc.build().center)
                }

                addMarkerOnMap(latLng)
                latLngListCurrent.add(latLng)

                if (latLngListCurrent.size <= 1) {
                    // Initiate a polyline
                    val polylineOptions = PolylineOptions()
                    polylineOptions.addAll(latLngListCurrent)
                    currentPolyline = googleMap.addPolyline(polylineOptions)
                    currentPolyline?.width = strokWidth
                    currentPolyline?.color = strokColor
                } else {
                    // Update initiated polyline
                    // To connect all three points A-B-C
                    // for A-B
                    updatePolyline(
                        // latLngListCurrent[latLngListCurrent.size.minus(3)],
                        latLngListCurrent[latLngListCurrent.size.minus(2)]
                    )

                    // for B-C
                    updatePolyline(
                        // latLngListCurrent[latLngListCurrent.size.minus(2)],
                        latLngListCurrent.last()
                    )
                }
            }
            ShapeType.POLYLINE -> {
                if (isLastPolylineDone) {
                    resetPolyline()
                    isLastPolylineDone = false
                }

                if (latLngListCurrent.size >= 1) {
                    // Get extra point between two points
                    val bc = LatLngBounds.Builder()
                    bc.include(latLngListCurrent.last())
                    bc.include(latLng)
                    addMarkerOnMap(bc.build().center)
                    latLngListCurrent.add(bc.build().center)
                }

                addMarkerOnMap(latLng)
                latLngListCurrent.add(latLng)

                if (latLngListCurrent.size <= 1) {
                    val polylineOptions = PolylineOptions()
                    polylineOptions.addAll(latLngListCurrent)
                    currentPolyline = googleMap.addPolyline(polylineOptions)
                    currentPolyline?.width = 7f
                    currentPolyline?.color = POLYLINE_STROKE_COLOR
                } else {
                    // Update initiated polyline
                    // To connect all three points A-B-C
                    // for A-B
                    updatePolyline(
                        // latLngListCurrent[latLngListCurrent.size.minus(3)],
                        latLngListCurrent[latLngListCurrent.size.minus(2)]
                    )

                    // for B-C
                    updatePolyline(
                        // latLngListCurrent[latLngListCurrent.size.minus(2)],
                        latLngListCurrent.last()
                    )
                }

            }
            ShapeType.CIRCLE -> {
                resetCircle()
                drawCircle(latLng)
            }
            ShapeType.POINT -> {
                resetMarker()
                drawMarker(latLng)
            }
        }
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        if(isRemoveMode) {
            handleRemovalProcess(marker)
            //  default info window should not appear for ShapeType.POINT
            return (shapeType == ShapeType.POINT)
        }

        when (shapeType) {
            ShapeType.POLYGON -> {
                if (!latLngListCurrent.isNullOrEmpty()) {
                    if (latLngListCurrent.first().equals(marker.position)) {
                        // Get extra point between two points
                        val bc = LatLngBounds.Builder()
                        bc.include(latLngListCurrent.last())
                        bc.include(marker.position)
                        addMarkerOnMap(bc.build().center)
                        latLngListCurrent.add(bc.build().center)

                        // Send index -1 if you want to draw a new fresh polygon
                        isLastPolygonDone = drawPolygon(shapeID.toString(), -1
                            , latLngListCurrent)
                        if (isLastPolygonDone) {
                            currentPolyline?.remove()
                            resetPolygon()
                        }
                    }
                }
            }

            ShapeType.POLYLINE -> {
                if (!latLngListCurrent.isNullOrEmpty()) {
                    if (latLngListCurrent.last().equals(marker.position)) {
                        // Send index -1 if you want to draw a new fresh polyline
                        isLastPolylineDone = drawPolyline(shapeID.toString(), -1
                            , latLngListCurrent)
                        if (isLastPolylineDone) {
                            resetPolygon()
                        }
                    }
                }
            }
            ShapeType.CIRCLE -> {}
            ShapeType.POINT -> return true
        }
        return false
    }

    /**
     * marker.tag = markerId#shapeId#shapeType
     * where:
     * markerId - eg - 0,1,2..[Index of a marker(point/latLng) inside a shape]
     * shapeId - eg - timestamp in milliseconds [Different for each shape]
     * shapeType - eg - enum of ShapeType [Different for each type of shape]
     * @param marker
     */
    override fun onMarkerDragStart(marker: Marker) {
        val markerTagParts = marker.tag.toString().split("#")
        currentMarkerIndex = markerTagParts[0].toInt()
        val shapeID = markerTagParts[1]
        currentEditingShape = ShapeType.valueOf(markerTagParts[2])

        when(currentEditingShape) {
            ShapeType.POLYGON -> {
                currentShapeIndex = polygonList.indexOfFirst { it.third.tag == shapeID }// -1 if not found
                if(currentShapeIndex >= 0){
                    // Edit completed polygon
                    currentPolygon = polygonList[currentShapeIndex].third
                    latLngListTemp.clear()
                    latLngListTemp.addAll(polygonList[currentShapeIndex].third.points.dropLast(1))
                }
                else {
                    // Edit uncompleted polygon (that is actually a polyline)
                    currentPolygon = null
                    latLngListCurrent.clear()
                    latLngListCurrent.addAll(currentPolyline!!.points)
                }
            }

            ShapeType.POLYLINE -> {
                currentShapeIndex = polylineList.indexOfFirst { it.third.tag == shapeID }// -1 if not found
                if(currentShapeIndex >= 0){
                    // Edit completed polyline
                    tempPolyline = polylineList[currentShapeIndex].third
                    latLngListTemp.clear()
                    latLngListTemp.addAll(polylineList[currentShapeIndex].third.points)
                }
                else {
                    // Edit uncompleted polyline
                    tempPolyline = null
                    latLngListCurrent.clear()
                    latLngListCurrent.addAll(currentPolyline!!.points)
                }
            }

            ShapeType.CIRCLE -> {
                currentShapeIndex = circleList.indexOfFirst { it.third.tag == shapeID }// -1 if not found
                if(currentShapeIndex >= 0){
                    // Edit completed circle only
                    currentCircle = circleList[currentShapeIndex].third
                    latLngListTemp.clear()
                    latLngListTemp.add(circleList[currentShapeIndex].third.center)
                    latLngListTemp.add(Utils.getDestinationPoint(
                        circleList[currentShapeIndex].third.center, DEFAULT_CIRCLE_RADIUS)  )
                }
            }
            ShapeType.POINT -> {
                currentShapeIndex = markerList.indexOfFirst { it.tag == shapeID }
                if(currentShapeIndex >= 0){
                    // Edit completed polygon
                    currentMarker = markerList[currentShapeIndex]
                    latLngListTemp.clear()
                    latLngListTemp.add(markerList[currentShapeIndex].position)
                }
            }
        }
        // Log.i(TAG,"==currentMarkerId/Index $currentMarkerIndex /currentShapeIndex $currentShapeIndex /shapeID $shapeID")
    }

    override fun onMarkerDrag(marker: Marker) {
        when (currentEditingShape) {
            ShapeType.POLYGON -> {
                currentPolygon?.let {
                    // Edit - Completed polygon
                    latLngListTemp.set(currentMarkerIndex, marker.position)
                    val shapeId = it.tag.toString()
                    it.remove()
                    drawPolygon(shapeId, currentShapeIndex, latLngListTemp)
                }
                    ?: run{
                        //Edit - incomplete polygon (that is actually a polyline )
                        latLngListCurrent.set(currentMarkerIndex, marker.position)
                        val points = currentPolyline?.points
                        points?.set(currentMarkerIndex, marker.position)
                        currentPolyline?.points = points
                    }
            }

            ShapeType.POLYLINE -> {
                tempPolyline?.let {
                    // Edit - Completed polyline
                    latLngListTemp.set(currentMarkerIndex, marker.position)
                    val points = tempPolyline?.points
                    points?.set(currentMarkerIndex, marker.position)
                    tempPolyline?.points = points
                    drawPolyline(it.tag.toString(), currentShapeIndex, latLngListTemp)
                }
                    ?: run {
                        // Edit - unCompleted polyline
                        latLngListCurrent.set(currentMarkerIndex, marker.position)
                        val points = currentPolyline?.points
                        points?.set(currentMarkerIndex, marker.position)
                        currentPolyline?.points = points
                    }
            }
            ShapeType.CIRCLE ->  {
                // Edit - Completed circle only
                latLngListTemp.set(currentMarkerIndex, marker.position)
                updateCircle(marker)
            }
            ShapeType.POINT -> currentMarker?.position = marker.position
        }
    }

    override fun onMarkerDragEnd(marker: Marker) {
        //Log.i(TAG,"== onMarkerDragEnd ${marker.position}")
        when (currentEditingShape) {
            ShapeType.POLYGON -> {
                currentPolygon?.let {
                    // Edit completed polygon
                    latLngListTemp.set(currentMarkerIndex, marker.position)
                    // Update marker position in marker list
                    val updatedM =  polygonList[currentShapeIndex].first[currentMarkerIndex]
                    updatedM.position = marker.position
                    polygonList[currentShapeIndex].first.set(currentMarkerIndex,updatedM)
                    // Final attempt of draw polygon and update in polygonlist
                    val shapeid = it.tag.toString()
                    it.remove()
                    drawPolygon(shapeid, currentShapeIndex, latLngListTemp)

                }
                    ?: run {
                        // Edit uncompleted polygon (that is actually a polyline)
                        latLngListCurrent.set(currentMarkerIndex, marker.position)
                    }
            }
            ShapeType.POLYLINE -> {
                tempPolyline?.let {
                    // Edit completed polygon
                    latLngListTemp.set(currentMarkerIndex, marker.position)
                    // Update marker position in marker list
                    val updatedM =  polylineList[currentShapeIndex].first[currentMarkerIndex]
                    updatedM.position = marker.position
                    polylineList[currentShapeIndex].first.set(currentMarkerIndex,updatedM)
                    // Final attempt of draw polyline and update in polylinelist
                    val points = tempPolyline?.points
                    points?.set(currentMarkerIndex, marker.position)
                    tempPolyline?.points = points
                    drawPolyline(it.tag.toString(), currentShapeIndex, latLngListTemp)

                }
                    ?: run {
                        // Edit uncompleted polygon (that is actually a polyline)
                        latLngListCurrent.set(currentMarkerIndex, marker.position)
                    }
            }
            ShapeType.CIRCLE -> {
                latLngListTemp.set(currentMarkerIndex, marker.position)
                val updatedNumBadge = updateCircle(marker)
                // Update complete Triple in circleList
                currentCircle?.let{
                    val list = ArrayList<Marker>()
                    list.addAll(circleList[currentShapeIndex].first)
                    circleList.set(currentShapeIndex, Triple(list, updatedNumBadge, it))
                }
            }
            ShapeType.POINT -> {
                currentMarker?.let {
                    markerList.set(currentShapeIndex, marker)
                    val tagParts = marker.tag.toString().split("#")
                    drawListener?.onShapeUpdated(currentEditingShape, tagParts[1])
                }
            }
        }
    }

    fun enableRemoveMode() {

        if(!isRemoveMode) {
            // Show cross icon in place of last marker in the marker list of polygonList
            polygonList.forEach {
                it.first.last().setIcon(Utils.getInstance()?.bitmapDescriptorFromVector(
                    context, R.drawable.ic_cancel_24dp))
                isRemoveMode = true
            }

            // Show cross icon in place of last marker in the marker list of polylineList
            polylineList.forEach {
                it.first.last().setIcon(Utils.getInstance()?.bitmapDescriptorFromVector(
                    context,R.drawable.ic_cancel_24dp))
                isRemoveMode = true
            }

            // Show cross icon in place of last marker in the marker list of circleList
            circleList.forEach {
                it.first.last().setIcon(Utils.getInstance()?.bitmapDescriptorFromVector(
                    context,R.drawable.ic_cancel_24dp))
                isRemoveMode = true
            }
            // Show cross icon below each Marker image
            markerList.forEach {
                val markerOptions = MarkerOptions().position(it.position)
                markerOptions.icon(Utils.getInstance()?.bitmapDescriptorFromVector(
                    context,R.drawable.ic_cancel_24dp))
                markerOptions.anchor(.5f, .5f)
                val marker = googleMap.addMarker(markerOptions)
                // Id formatted as point index in list # shape id # shape type
                marker?.tag = it.tag.toString() + "#helper"
                helperMarkersList.add(marker)
                isRemoveMode = true
            }
        }
        else {
            cancelRemoveMode()
        }

        if(isRemoveMode) removeListener?.onShapeRemoveModeEnabled(isRemoveMode)
    }

    fun cancelRemoveMode(){
        isRemoveMode = false
        polygonList.forEach {
            it.first.last().setIcon(Utils.getDotIcon(markerSize, markerColor))
        }
        polylineList.forEach {
            it.first.last().setIcon(Utils.getDotIcon(markerSize, markerColor))
        }

        circleList.forEach {
            it.first.last().setIcon(Utils.getDotIcon(markerSize, markerColor))
        }
        helperMarkersList.forEach {
            it.remove()
        }
        helperMarkersList.clear()
    }

    fun removeShape(shapeType: ShapeType, shapeIndex: Int) {
        when(shapeType) {
            ShapeType.POLYGON -> {
                // 1. Remove All node markers
                polygonList[shapeIndex].first.forEach { it.remove() }
                // 2. Remove Num Badge marker
                polygonList[shapeIndex].second.remove()
                // 3. Remove actual shape
                polygonList[shapeIndex].third.remove()
                // 4. Remove Triple from list
                polygonList.removeAt(shapeIndex)
            }
            ShapeType.POLYLINE -> {
                // 1. Remove All node markers
                polylineList[shapeIndex].first.forEach { it.remove() }
                // 2. Remove Num Badge marker
                polylineList[shapeIndex].second.remove()
                // 3. Remove actual shape
                polylineList[shapeIndex].third.remove()
                // 4. Remove Triple from list
                polylineList.removeAt(shapeIndex)
            }
            ShapeType.CIRCLE -> {
                // 1. Remove All node markers
                circleList[shapeIndex].first.forEach { it.remove() }
                // 2. Remove Num Badge marker
                circleList[shapeIndex].second.remove()
                // 3. Remove actual shape
                circleList[shapeIndex].third.remove()
                // 4. Remove Triple from list
                circleList.removeAt(shapeIndex)
            }
            ShapeType.POINT -> {
                // - remove main marker
                markerList[shapeIndex].remove()
                // - remove helper marker
                helperMarkersList[shapeIndex].remove()
                // - remove main marker from list
                markerList.removeAt(shapeIndex)
                // - remove helper marker from list
                helperMarkersList.removeAt(shapeIndex)
            }
        }
        removeListener?.onShapeRemoveAfter(true)
    }

    private fun handleRemovalProcess(marker: Marker){

        // To check user clicked on a node markers or not
        if(!marker.tag.toString().contains("#")) return

        val markerTagParts = marker.tag.toString().split("#")
        val shapeID = markerTagParts[1]
        val shapeType = ShapeType.valueOf(markerTagParts[2])
        var shapeIndex = -1
        var shapeBadgeNum = 0

        when(shapeType) {
            ShapeType.POLYGON -> {
                // Condition to find clicked marker is belongs to polygon list and is really crossed marker
                shapeIndex = polygonList.indexOfFirst { it.third.tag == shapeID } // -1 if not found
                if (shapeIndex >= 0 &&
                    marker.position.equals(polygonList[shapeIndex].first.last().position)) {
                    shapeBadgeNum = polygonList[shapeIndex].second.tag as Int
                }
            }
            ShapeType.POLYLINE -> {
                // Condition to find clicked marker is belongs to polyline list and is really crossed marker
                shapeIndex = polylineList.indexOfFirst { it.third.tag == shapeID } // -1 if not found
                if (shapeIndex >= 0 &&
                    marker.position.equals(polylineList[shapeIndex].first.last().position)) {
                    shapeBadgeNum = polylineList[shapeIndex].second.tag as Int
                }
            }
            ShapeType.CIRCLE -> {
                // Condition to find clicked marker is belongs to circle list and is really crossed marker
                shapeIndex = circleList.indexOfFirst { it.third.tag == shapeID } // -1 if not found
                if (shapeIndex >= 0 &&
                    marker.position.equals(circleList[shapeIndex].first.last().position)) {
                    shapeBadgeNum = circleList[shapeIndex].second.tag as Int
                }
            }
            ShapeType.POINT -> {
                if (markerTagParts.size > 3 && markerTagParts[3].equals("helper")) {
                    for ((index, element) in markerList.withIndex()) {
                        val tagParts = element.tag.toString().split("#")
                        if (tagParts[1] == shapeID) {
                            shapeIndex = index
                            break
                        }
                    }
                    if (shapeIndex >= 0 &&
                        marker.position.equals(markerList[shapeIndex].position)) {
                        shapeBadgeNum = markerTagParts[0].toInt()
                    }
                }
            }
        }

        // If shapeBadgeNum value > 0 means user clicked on crossed marker to delete shape
        if(shapeBadgeNum > 0) {
            removeListener?.onShapeRemoveBefore(shapeType, shapeIndex, shapeBadgeNum)
                ?: removeShape(shapeType, shapeIndex)
        }
    }

    /**
     * @param count The number that you want to show on badge icon
     * @param latLng Position on that badge will show on map
     * @param type For which type of shape you want to show this badge
     * @param value Calculated size/length of shape in feet or sq feet
     * @return Added marker
     */
    private fun addNumBadgeMarkerOnMap(count: Int, latLng: LatLng, type: ShapeType
                                       , value: Double): Marker {
        val markerOption = MarkerOptions()
        markerOption.position(latLng)
        markerOption.anchor(0.5f, 0.5f)
        markerOption.icon(Utils.getTextIcon(count.toString(), badgeColor, badgeTextColor))

        when(type){
            ShapeType.POLYGON -> {
                markerOption.title(String.format("%.2f sq. feet", value))
                markerOption.snippet("Size")
            }
            ShapeType.POLYLINE -> {
                markerOption.title(String.format("%.2f feet", value))
                markerOption.snippet("Length")
            }
            ShapeType.CIRCLE -> {
                markerOption.title(String.format("%.2f sq. feet", value))
                markerOption.snippet("Size")
            }
            ShapeType.POINT -> {
                markerOption.anchor(0.5f, 1.0f)
                markerOption.icon(Utils.getInstance()?.getMarkerIcon(context
                    , markerCount.toString()))
                markerOption.title(count.toString())
                markerOption.snippet("Count")
            }
        }

        return googleMap.addMarker(markerOption).also{ it.tag = count }
    }

    private fun addMarkerOnMap(latLng: LatLng): Marker {
        val markerOptions = MarkerOptions().position(latLng)
        markerOptions.icon(Utils.getDotIcon(markerSize, markerColor))
        markerOptions.anchor(.5f, .5f)
        val marker = googleMap.addMarker(markerOptions)
        marker?.isDraggable = editable
        // Id formatted as point index in list # shape id # shape type
        marker?.tag = latLngListCurrent.size.toString() + "#" + shapeID+"#"+shapeType
        currentMarkers.add(marker)
        return marker
    }

    /**
     * This method are using for both:
     * 1) Draw a new polygon with new polygonID or
     * 2) Update a old polygon with old polygonID
     * @param polygonID its a current time millies
     * @param indexForEdit can be 0 or greater for draw new fresh polygon, or lesser from 0 to update
     * any old polygon in polygonList
     * @return true if job done otherwise false
     */
    private fun drawPolygon(polygonID: String, indexForEdit: Int, latLngList: List<LatLng>): Boolean {
        if (latLngList.size >= 3) {
            val polygonOptions = PolygonOptions()
            polygonOptions.addAll(latLngList)
            polygonOptions.strokeColor(strokColor)
            polygonOptions.strokeWidth(7f)
            polygonOptions.fillColor(fillColor)
            currentPolygon = googleMap.addPolygon(polygonOptions)
            currentPolygon?.tag = polygonID
            currentPolygon?.let {

                // Calculate polygon area size
                val areaInSqFt = SphericalUtil.computeArea(latLngList).squareMeterToFeet()

                // Get polygon center
                val builder = LatLngBounds.Builder()
                latLngList.forEach{builder.include(it)}
                val bounds = builder.build()

                if (indexForEdit < 0) { // ADD
                    // Need to add because method calling for add polygon
                    // Add Num Badge
                    val badgeMarker = addNumBadgeMarkerOnMap(++polygonCount,
                        bounds.center, ShapeType.POLYGON, areaInSqFt)
                    val list = ArrayList<Marker>()
                    list.addAll(currentMarkers)
                    polygonList.add(Triple(list, badgeMarker, it))
                }
                else { // EDIT
                    // Need to update because method calling for update polygon
                    val badgeMarker = polygonList.get(indexForEdit).second
                    badgeMarker.position = bounds.center
                    badgeMarker.title = String.format("%.2f sq. feet", areaInSqFt)

                    val list = ArrayList<Marker>()
                    list.addAll(polygonList[currentShapeIndex].first)
                    polygonList.set(indexForEdit, Triple(list, badgeMarker, it))
                }
            }

            drawListener?.onShapeCompleted(shapeType, polygonID)

            return true
            //val areaInSqFt =  SphericalUtil.computeArea(latLngListCurrent) * 10.764
            //tv_size.text = String.format("Size: %.2f sq. feet", areaInSqFt)
        }
        return false
    }

    private fun drawPolyline(polylineID: String, indexForEdit: Int, latLngList: List<LatLng>): Boolean {
        if (latLngList.size >= 3) {

            // Calculate polyline area size
            val lengthInFt = SphericalUtil.computeLength(latLngList).meterToFeet()
            // Get a point between last and second last point of polyline for for number badge
            val builder = LatLngBounds.Builder()
            builder.include(latLngList.last())
            builder.include(latLngList[latLngList.size - 2])
            val bounds = builder.build()

            if (indexForEdit < 0) { // ADD
            currentPolyline?.let {
                //Log.i(TAG, "==== saved shapeid $polylineID")
                it.tag = polylineID
                    // Add Num Badge
                    val badgeMarker = addNumBadgeMarkerOnMap(++polylineCount,
                        bounds.center, ShapeType.POLYLINE, lengthInFt)
                    val list = ArrayList<Marker>()
                    list.addAll(currentMarkers)
                    polylineList.add(Triple(list,badgeMarker, it))
                }
            }
            else { // EDIT
                tempPolyline?.let {
                    val badgeMarker = polylineList.get(indexForEdit).second
                    badgeMarker.position = bounds.center
                    badgeMarker.title = String.format("%.2f feet", lengthInFt)
                    val list = ArrayList<Marker>()
                    list.addAll(polylineList[currentShapeIndex].first)
                    polylineList.set(indexForEdit, Triple(list, badgeMarker, it))
                }
            }

            drawListener?.onShapeCompleted(shapeType, polylineID)
            return true
        }
        return false
    }

    private fun updatePolyline( endLatLng: LatLng) {
        //Log.i(TAG, "== updatePolyline ${latLngListCurrent.first()}")
        val points = currentPolyline?.points
        points?.add(endLatLng)
        currentPolyline?.points = points

//        if(!latLngListCurrent.isNullOrEmpty()) {
//            val lengthInSqFt = SphericalUtil.computeLength(pointsPolyline) * 3.281
//            tv_size.text = String.format("Length: %.2f feet", lengthInSqFt)
//        }
//        drawListener?.onShapeCompleted(shapeType)
    }

    private fun drawCircle(latLngCenter : LatLng){
        // Center point
        addMarkerOnMap(latLngCenter)
        latLngListCurrent.add(latLngCenter)

        // Perimeter points
        val perimeterPoint = Utils.getDestinationPoint(latLngCenter,DEFAULT_CIRCLE_RADIUS)
        addMarkerOnMap(perimeterPoint)
        latLngListCurrent.add(perimeterPoint)

        val circleOptions = CircleOptions()
        circleOptions.center(latLngCenter)
        circleOptions.strokeColor(CIRCLE_STROKE_COLOR)
        circleOptions.fillColor(CIRCLE_FILL_COLOR)
        circleOptions.strokeWidth(Constants.CIRCLE_STROKE_WIDTH.toFloat())
        circleOptions.radius(DEFAULT_CIRCLE_RADIUS)

        // Get a point between two points for number badge
        val builder = LatLngBounds.Builder()
        builder.include(latLngCenter)
        builder.include(perimeterPoint)
        val bounds = builder.build()

        // Add Num Badge
        val areaInSqFt =  (Math.PI * Math.sqrt(circleOptions.radius) ).squareMeterToFeet()
        val badgeMarker = addNumBadgeMarkerOnMap(++circleCount,
            bounds.center, ShapeType.CIRCLE, areaInSqFt)

        currentCircle = googleMap.addCircle(circleOptions)
        currentCircle?.tag = shapeID.toString()

        currentCircle?.let {
            val list = ArrayList<Marker>()
            list.addAll(currentMarkers)
            circleList.add(Triple(list, badgeMarker,it))
        }

        drawListener?.onShapeCompleted(shapeType, shapeID.toString())
    }

    private fun updateCircle(marker : Marker): Marker{

        val numBadge = circleList[currentShapeIndex].second

        if(currentMarkerIndex==0){
            // Drag Circle
            currentCircle?.center = marker.position

            currentCircle?.let {
                // Update Perimeter
                val perimeter = Utils.getDestinationPoint(marker.position, it.radius)
                // Update Perimeter point position
                circleList[currentShapeIndex].first.last().position = perimeter

                // Update num badge
                val builder = LatLngBounds.Builder()
                builder.include(marker.position)
                builder.include(perimeter)
                numBadge.position = builder.build().center
            }
        }
        else {
            currentCircle?.let {
                // Resize Circle
                it.radius = SphericalUtil.computeDistanceBetween(it.center, marker.position)

                currentCircle?.let {
                    // Update Perimeter
                    val perimeter = Utils.getDestinationPoint(it.center,it.radius)
                    // Update Perimeter point position
                    marker.position = perimeter

                    // Update num badge
                    val builder = LatLngBounds.Builder()
                    builder.include(it.center)
                    builder.include(perimeter)
                    numBadge.position = builder.build().center

                    // Recalculate area
                    val areaInSqFt =  (Math.PI * Math.sqrt(it.radius) ).squareMeterToFeet()
                    numBadge.title = String.format("%.2f sq. feet", areaInSqFt)
                }
            }
        }

        drawListener?.onShapeUpdated(shapeType, currentCircle?.tag.toString())

        return numBadge
    }

    /**
     * Sets two Marker's on latLng,
     * Main marker with custom vector image and count of Main Markers, which is draggable.
     *
     * Another marker with transparent color image that to be used for removing marker.
     *
     * @param latLng LatLng to be used for marker position.
     */
    private fun drawMarker(latLng : LatLng){
        val imageMarker = addNumBadgeMarkerOnMap(++markerCount, latLng, ShapeType.POINT, 0.0)
        imageMarker.isDraggable = editable
        latLngListCurrent.add(latLng)
        // Id formatted as point index in list # shape id # shape type
        imageMarker.tag = "$markerCount#$shapeID#$shapeType"

        markerList.add(imageMarker)

        drawListener?.onShapeCompleted(shapeType, shapeID.toString())
    }

    private fun resetPolygon() {
        latLngListCurrent.clear()
        shapeID = System.currentTimeMillis()
        currentPolygon = null
        currentMarkers.clear()
    }

    private fun resetPolyline() {
        latLngListCurrent.clear()
        shapeID = System.currentTimeMillis()
        currentPolyline = null
        currentMarkers.clear()
    }

    private fun resetCircle(){
        latLngListCurrent.clear()
        shapeID = System.currentTimeMillis()
        currentCircle = null
        currentMarkers.clear()
    }

    private fun resetMarker() {
        latLngListCurrent.clear()
        shapeID = System.currentTimeMillis()
        currentMarker = null
        currentMarkers.clear()
    }

    fun clearMap() {
        googleMap.clear()

        polygonList.clear()
        polylineList.clear()
        circleList.clear()
        markerList.clear()

        latLngListCurrent.clear()
        latLngListTemp.clear()

        shapeID = System.currentTimeMillis()

        currentPolygon = null
        currentPolyline = null
        currentCircle = null

        currentMarkers.clear()

        polygonCount = 0
        polylineCount = 0
        circleCount = 0
        markerCount = 0
        isRemoveMode = false

        removeListener?.onAllShapeRemove()
    }
    
}
package com.devs.mapdrawingmanager

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.devs.mdmanager.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.frag_map.*

/**
 * Created by ${Deven} on 31/7/19.
 */
class FragMap : Fragment(), View.OnClickListener, OnShapeRemoveListener
    , OnShapeDrawListener {

    companion object {
        private val TAG = FragMap::class.java.simpleName
    }

    private var parentView: View? = null
    private var actionMode: ActionMode? = null
    private var googleMap: GoogleMap? = null
    private var mapDrawingManager: MapDrawingManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?): View? {

        parentView = inflater.inflate(R.layout.frag_map, container, false)

        return parentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)

        iv_polygon.setOnClickListener(this)
        iv_polyline.setOnClickListener(this)
        iv_circle.setOnClickListener(this)
        iv_pin.setOnClickListener(this)

        btn_clear.setOnClickListener {
            mapDrawingManager?.clearMap()
            tv_size.text = ""
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_frag) as SupportMapFragment
        mapFragment.getMapAsync { gm ->
            googleMap = gm
            googleMap?.getUiSettings()?.isMapToolbarEnabled = false
            googleMap?.getUiSettings()?.isMyLocationButtonEnabled = true
            //googleMap?.getUiSettings()?.isZoomControlsEnabled = true
           // googleMap?.setMapType(GoogleMap.MAP_TYPE_SATELLITE)
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(12.9756, 77.5354), 16f)
            )

            activity?.let {
                mapDrawingManager = MDMBuilder(it.baseContext).withMap(gm).build()
                mapDrawingManager?.removeListener = this
                mapDrawingManager?.drawListener = this

                onClick(iv_polygon)
            }
        }
    }

    override fun onClick(view: View?) {
        iv_polygon.setColorFilter(getResources().getColor(R.color.semi_white))
        iv_polyline.setColorFilter(getResources().getColor(R.color.semi_white))
        iv_circle.setColorFilter(getResources().getColor(R.color.semi_white))
        iv_pin.setColorFilter(getResources().getColor(R.color.semi_white))
        (view as ImageView).setColorFilter(getResources().getColor(R.color.colorAccent))

        when (view.id) {
            R.id.iv_polygon -> {
                mapDrawingManager?.shapeType = ShapeType.POLYGON
                //mapDrawingManager?.strokColor = AnimatorPolygon.POLY_STROKE_COLOR
                //mapDrawingManager?.fillColor = AnimatorPolygon.POLY_FILL_COLOR
                updateShapeSizeTotal(ShapeType.POLYGON)
            }
            R.id.iv_polyline -> {
                mapDrawingManager?.shapeType = ShapeType.POLYLINE
                //mapDrawingManager?.strokColor = AnimatorPolyline.POLYLINE_STROKE_COLOR
                updateShapeSizeTotal(ShapeType.POLYLINE)
            }
            R.id.iv_circle -> {
                mapDrawingManager?.shapeType = ShapeType.CIRCLE
                updateShapeSizeTotal(ShapeType.CIRCLE)
            }
            R.id.iv_pin -> {
                mapDrawingManager?.shapeType = ShapeType.POINT
                updateShapeSizeTotal(ShapeType.POINT)
            }
        }
    }

    override fun onShapeRemoveModeEnabled(removeModeEnable: Boolean) {
        activity?.log("remove mode enabled $removeModeEnable")
        // Show Actionbar

        activity?.startActionMode(object : ActionMode.Callback {

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return true
            }

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                actionMode = mode
                mode?.title = "Remove Shape"
                mode?.subtitle = "Click on cross to remove any shape"
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                mapDrawingManager?.cancelRemoveMode()
            }
        })
    }

    override fun onShapeRemoveBefore(shapeType: ShapeType, shapeIndex: Int, shapeCount: Int) {
        mapDrawingManager?.removeShape(shapeType, shapeIndex)
        updateShapeSizeTotal(shapeType)

        // Show confirm dialog
//        val builder = activity?.let { AlertDialog.Builder(it) }
//        builder?.setTitle("Want to remove this shape?")
//        builder?.setMessage("$shapeType number $shapeCount will be removed from Map.")
//        builder?.setCancelable(false)
//        builder?.setPositiveButton("Remove",
//            DialogInterface.OnClickListener { dialog, which ->
//                mapDrawingManager?.removeShape(shapeType, shapeIndex)
//                updateShapeSizeTotal(shapeType)
//            })
//        builder?.setNegativeButton("Cancel",
//            DialogInterface.OnClickListener { dialog, which ->
//            })
//        builder?.show()
    }

    override fun onShapeRemoveAfter(deleted: Boolean) {
        activity?.log("shape deleted $deleted")
    }

    override fun onAllShapeRemove() {
        activity?.log("onAllShapeRemove")
    }

    override fun onShapeCompleted(shapeType: ShapeType, shapeId: String) {
        activity?.log("onShapeCompleted")
        updateShapeSizeTotal(shapeType)
    }

    override fun onShapeUpdated(shapeType: ShapeType, shapeId: String) {
        //activity?.log("onShapeUpdated")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        when (item.itemId) {
            R.id.action_remove -> mapDrawingManager?.enableRemoveMode()
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateShapeSizeTotal(shapeType: ShapeType) {
        var total = 0f

        when(shapeType) {
            ShapeType.POLYGON ->
                mapDrawingManager?.polygonList?.let {
                   it.forEach {
                       val titleParts = it.second.title.toString().split(" ")
                       total += titleParts[0].toFloat()
                   }
                   tv_size.text= String.format("%.2f sq. feet", total)
                }
            ShapeType.POLYLINE ->
                mapDrawingManager?.polylineList?.let {
                    it.forEach {
                        val titleParts = it.second.title.toString().split(" ")
                        total += titleParts[0].toFloat()
                    }
                    tv_size.text= String.format("%.2f feet", total)
                }

            ShapeType.CIRCLE ->
                mapDrawingManager?.circleList?.let {
                    it.forEach {
                        val titleParts = it.second.title.toString().split(" ")
                        total += titleParts[0].toFloat()
                    }
                    tv_size.text= String.format("%.2f sq. feet", total)
                }

            ShapeType.POINT ->
                mapDrawingManager?.markerList?.let {
                    it.size
                    tv_size.text= String.format("%d units", it.size)
                }
        }

    }
}
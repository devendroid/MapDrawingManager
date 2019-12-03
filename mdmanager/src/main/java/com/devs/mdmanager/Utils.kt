package com.devs.mdmanager

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng

/**
 * Created by ${Deven} on 2019-08-27.
 */
class Utils private constructor() {

    companion object {
        private var singleton: Utils? = null

        /* Thread safe approach with better performance */
        fun getInstance(): Utils? {
            if (singleton == null) {
                synchronized(Utils::class.java) { singleton = Utils() }
            }
            return singleton
        }

        fun getZoomLevel(circle: Circle?): Int {
            var zoomLevel = 11
            if (circle != null) {
                val radius = circle.radius + circle.radius / 2
                val scale = radius / 500
                zoomLevel = (16 - Math.log(scale) / Math.log(2.0)).toInt()
            }
            return zoomLevel
        }

        fun getTextIcon(text: String, badgeColor: Int, badgeTextColor: Int): BitmapDescriptor {
            val TEXT_SIZE = 12f.spToPx()
            val CIRCLE_HEIGHT = 28.dpToPx().toInt()
            val CIRCLE_WIDTH = 28.dpToPx().toInt()
            val CIRCLE_STROKE_WIDTH = 1.dpToPx()

            val textPaint = Paint()
            textPaint.textSize = TEXT_SIZE
            textPaint.color = badgeTextColor
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val circlePaint = Paint()
            circlePaint.color = badgeColor
            circlePaint.strokeWidth = CIRCLE_STROKE_WIDTH
            circlePaint.style = Paint.Style.FILL_AND_STROKE

            val textWidth = textPaint.measureText(text)
            val textHeight = textPaint.textSize

            val image = Bitmap.createBitmap(CIRCLE_WIDTH, CIRCLE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            canvas.translate(0f, CIRCLE_HEIGHT.toFloat())

            //canvas.drawColor(Color.LTGRAY);
            canvas.drawCircle((CIRCLE_WIDTH / 2).toFloat(), (-CIRCLE_HEIGHT / 2).toFloat(),
                (CIRCLE_WIDTH / 2 - CIRCLE_STROKE_WIDTH), circlePaint)

            circlePaint.color = badgeTextColor
            circlePaint.style = Paint.Style.STROKE
            circlePaint.isAntiAlias = true

            canvas.drawCircle((CIRCLE_WIDTH / 2).toFloat(), (-CIRCLE_HEIGHT / 2).toFloat(),
                (CIRCLE_WIDTH / 2 - CIRCLE_STROKE_WIDTH), circlePaint
            )

            canvas.drawText(text, CIRCLE_WIDTH / 2 - textWidth / 2,
                -(CIRCLE_HEIGHT / 2 - textHeight / 2) - CIRCLE_STROKE_WIDTH, textPaint)

            return BitmapDescriptorFactory.fromBitmap(image)

            //return image;
        }

        fun getDotIcon(markerSize: Int, markerColor: Int): BitmapDescriptor {
            val CIRCLE_HEIGHT = markerSize.dpToPx().toInt()
            val CIRCLE_WIDTH = markerSize.dpToPx().toInt()

            val circlePaint = Paint()
            circlePaint.color = markerColor
            circlePaint.style = Paint.Style.FILL

            val image = Bitmap.createBitmap(CIRCLE_WIDTH, CIRCLE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            canvas.translate(0f, CIRCLE_HEIGHT.toFloat())

            //canvas.drawColor(Color.LTGRAY);
            canvas.drawCircle(
                (CIRCLE_WIDTH / 2).toFloat(),
                (-CIRCLE_HEIGHT / 2).toFloat(),
                (CIRCLE_WIDTH / 2).toFloat(),
                circlePaint
            )
            return BitmapDescriptorFactory.fromBitmap(image)
        }

        fun getDestinationPoint(startLoc: LatLng, distance: Double): LatLng {
            val bearing = 1.6 // create a right side point
            val newLocation = Location("newLocation")

            val radius = 6371000.0 // earth's mean radius in m
            val lat1 = Math.toRadians(startLoc.latitude)
            val lng1 = Math.toRadians(startLoc.longitude)
            val lat2 = Math.asin(
                Math.sin(lat1) * Math.cos(distance / radius) + Math.cos(lat1)
                        * Math.sin(distance / radius) * Math.cos(bearing)
            )
            var lng2 = lng1 + Math.atan2(
                Math.sin(bearing) * Math.sin(distance / radius) * Math.cos(lat1),
                Math.cos(distance / radius) - Math.sin(lat1) * Math.sin(lat2)
            )
            lng2 = (lng2 + Math.PI) % (2 * Math.PI) - Math.PI

            // normalize to -180...+180
            if (lat2 == 0.0 || lng2 == 0.0) {
                newLocation.latitude = 0.0
                newLocation.longitude = 0.0
            } else {
                newLocation.latitude = Math.toDegrees(lat2)
                newLocation.longitude = Math.toDegrees(lng2)
            }
            return LatLng(newLocation.latitude, newLocation.longitude)
        }
    }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    fun getMarkerIcon(context: Context, text: String): BitmapDescriptor? {
        val textSize = 18f.spToPx()

        val textPaint = Paint()
        textPaint.textSize = textSize
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val textWidth = textPaint.measureText(text)
        val textHeight = textPaint.textSize

        return ContextCompat.getDrawable(context, R.drawable.ic_marker)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight
                , Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            draw(canvas)
            canvas.drawText(text, intrinsicWidth / 2 - textWidth / 2,
                (intrinsicHeight / 2).toFloat(), textPaint)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

}

// extension functions
fun Int.dpToPx() : Float = this * Resources.getSystem().displayMetrics.density
fun Float.pxToDp() : Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Float.spToPx() : Float = this * Resources.getSystem().displayMetrics.scaledDensity
fun Double.squareMeterToFeet() : Double = this * 10.764
fun Double.meterToFeet() : Double = this * 3.281
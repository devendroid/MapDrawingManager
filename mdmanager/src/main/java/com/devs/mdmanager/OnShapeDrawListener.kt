package com.devs.mdmanager

/**
 * Created by Deven on 2019-09-10.
 */
interface OnShapeDrawListener {

    fun onShapeCompleted(shapeType: ShapeType, shapeId: String)

    fun onShapeUpdated(shapeType: ShapeType, shapeId: String)

}
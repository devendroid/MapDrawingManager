package com.devs.mdmanager

/**
 * Created by Deven on 2019-09-10.
 */
interface OnShapeRemoveListener {

    fun onShapeRemoveModeEnabled(removeModeEnable: Boolean)

    fun onShapeRemoveBefore(shapeType: ShapeType, shapeIndex: Int, shapeCount: Int)

    fun onShapeRemoveAfter(deleted: Boolean)

    /**
     * to be called when all shapes cleared from Map (i.e. on Clear Button click)
     */
    fun onAllShapeRemove()

}
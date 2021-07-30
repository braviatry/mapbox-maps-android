package com.mapbox.maps.extension.style.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class NinePatchBitmap(
  val bitmap: Bitmap,
  val ninePatchDescription: NinePatchDescription,
)

data class NinePatchDescription(
  val leftPadding: Int,
  val rightPadding: Int,
  val topPadding: Int,
  val bottomPadding: Int,
  val xCoordinates: List<Int>,
  val yCoordinates: List<Int>,
)

fun Context.parseNinePatchResource(@DrawableRes res: Int): NinePatchBitmap {
  val bitmap = BitmapFactory.decodeResource(resources, res,)

  val ninePatchBuffer = bitmap.ninePatchChunk
  checkNotNull(ninePatchBuffer) {
    "bitmap.ninePatchChunk is null. Is drawable a 9 patch?"
  }

  return NinePatchBitmap(
    bitmap,
    parseNinePatchBuffer(ninePatchBuffer)
  )
}

private fun parseNinePatchBuffer(ninePatchBuffer: ByteArray): NinePatchDescription {
  val buffer = ByteBuffer.wrap(ninePatchBuffer).order(ByteOrder.nativeOrder())

  buffer.get()
  val xCoordinatesNumber = buffer.get() // second byte - number of x coordinates
  val yCoordinatesNumber = buffer.get() // third byte - number of y coordinates
  buffer.get()

  buffer.int
  buffer.int

  val leftPadding = buffer.int
  val rightPadding = buffer.int
  val topPadding = buffer.int
  val bottomPadding = buffer.int

  buffer.int

  val xCoordinates = mutableListOf<Int>()
  for (i in (0 until xCoordinatesNumber)) {
    xCoordinates.add(buffer.int)
  }
  val yCoordinates = mutableListOf<Int>()
  for (i in (0 until yCoordinatesNumber)) {
    yCoordinates.add(buffer.int)
  }

  return NinePatchDescription(
    leftPadding = leftPadding,
    rightPadding = rightPadding,
    topPadding = topPadding,
    bottomPadding = bottomPadding,
    xCoordinates = xCoordinates,
    yCoordinates = yCoordinates,
  )
}
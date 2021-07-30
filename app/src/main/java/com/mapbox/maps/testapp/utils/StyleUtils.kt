package com.mapbox.maps.testapp.utils

import android.graphics.Bitmap
import com.mapbox.maps.Image
import com.mapbox.maps.ImageContent
import com.mapbox.maps.ImageStretches
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.utils.NinePatchBitmap
import java.nio.ByteBuffer

fun Bitmap.toImage(): Image {
  val byteBuffer = ByteBuffer.allocate(byteCount)
  copyPixelsToBuffer(byteBuffer)
  return Image(width, height, byteBuffer.array())
}

fun Style.addNinePatchImage(
  imageId: String,
  ninePatchBitmap: NinePatchBitmap,
) {
  this.addStyleImage(
    imageId,
    scale = pixelRatio,
    image = ninePatchBitmap.bitmap.toImage(),
    sdf = false,
    stretchX = ninePatchBitmap.ninePatchDescription
      .xCoordinates
      .map { it.toFloat() }
      .zipWithNext(::ImageStretches),
    stretchY = ninePatchBitmap.ninePatchDescription
      .yCoordinates
      .map { it.toFloat() }
      .zipWithNext(::ImageStretches),
    content = ImageContent(
      0f + ninePatchBitmap.ninePatchDescription.leftPadding.toFloat(),
      0f + ninePatchBitmap.ninePatchDescription.topPadding.toFloat(),
      ninePatchBitmap.bitmap.width.toFloat() - ninePatchBitmap.ninePatchDescription.rightPadding.toFloat(),
      ninePatchBitmap.bitmap.height.toFloat() - ninePatchBitmap.ninePatchDescription.bottomPadding.toFloat(),
    ),
  )
}
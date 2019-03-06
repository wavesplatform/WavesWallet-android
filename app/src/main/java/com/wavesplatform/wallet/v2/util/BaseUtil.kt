package com.wavesplatform.wallet.v2.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.text.TextUtils
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.*

/**
 * Created by anonymous on 07.03.18.
 */
class BaseUtil {
    companion object {
        fun createMultipartFromFile(picture: File?, key: String = "photo"): MultipartBody.Part? {
            if (picture != null) {
                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), checkOrientation(picture))
                return MultipartBody.Part.createFormData(key, picture.name, requestFile)
            } else {
                return null
            }
        }

        fun createPartFromString(descriptionString: String?): RequestBody? {
            if (descriptionString == null) return null
            return RequestBody.create(
                    okhttp3.MultipartBody.FORM, descriptionString)
        }

        fun checkOrientation(image: File): File {
            var image = image
            try {
                val exifInterface = ExifInterface(image.absolutePath)
                val exifOrientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION)
                val exifOrientationInt = if (TextUtils.isEmpty(exifOrientation)) 1 else Integer.parseInt(exifOrientation)

                if (exifOrientationInt != ExifInterface.ORIENTATION_NORMAL) {
                    var angle = 0f
                    when (exifOrientationInt) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> angle = 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> angle = 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> angle = 270f
                    }
                    if (angle > 0) {
                        image = saveBitmap(rotateBitmap(getBitmap(image.absolutePath), angle), image.absolutePath)
                        Log.i("my", "ROTATEd:" + angle)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return image
        }

        fun rotateBitmap(src: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            if (angle > 0) {
                if (src.isRecycled) {
                    return src
                }
                var rotated: Bitmap? = null
                try {
                    rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
                } catch (e: OutOfMemoryError) {
                    e.printStackTrace()
                    System.gc()
                    try {
                        rotated = Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
                    } catch (e2: OutOfMemoryError) {
                        e2.printStackTrace()
                        return null
                    }
                }

                if (src != rotated) {
                    src.recycle()
                }
                return rotated
            }
            return src
        }

        fun getBitmap(path: String): Bitmap {
            val imagefile = File(path)
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(imagefile)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            return BitmapFactory.decodeStream(fis)
        }

        fun saveBitmap(bitmap: Bitmap?, path: String): File {
            val f = File(path)
            try {
                f.createNewFile()
                val bos = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 0, bos)
                val bitmapdata = bos.toByteArray()
                val fos = FileOutputStream(f)
                fos.write(bitmapdata)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
            }

            return f
        }
    }
}
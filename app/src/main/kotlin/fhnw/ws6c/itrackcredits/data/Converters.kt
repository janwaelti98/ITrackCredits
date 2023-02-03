package fhnw.ws6c.itrackcredits.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.time.LocalDate

@ProvidedTypeConverter
class Converters {
    @TypeConverter
    fun stringToJSONObject(string: String?): JSONObject? {
        return string?.let { JSONObject(string) }
    }

    @TypeConverter
    fun jsonObjectToString(jsonObject: JSONObject?): String? {
        return jsonObject?.let { jsonObject.toString() }
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray?): Bitmap? {
        return bytes?.let {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap?): ByteArray? {
        return bitmap?.let {
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
            bos.toByteArray()
        }
    }

    @TypeConverter
    fun toDate(dateString: String?): LocalDate? {
        return if (dateString == null) {
            null
        } else {
            LocalDate.parse(dateString)
        }
    }

    @TypeConverter
    fun toDateString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toColor(long: Long?): Color? {
        return long?.let { Color(long) }
    }

}
package io.github.ackeecz.retroceptor

import android.content.SharedPreferences

/**
 * Mocked [SharedPreferences] used for testing.
 */
class MockedSharedPreferences : SharedPreferences, SharedPreferences.Editor {

    private var values = mutableMapOf<String, Any?>()
    private val tempValues = mutableMapOf<String, Any?>()
    override fun edit(): SharedPreferences.Editor {
        return this
    }

    override fun contains(key: String): Boolean {
        return values.containsKey(key)
    }

    override fun getAll(): Map<String, *> {
        return HashMap(values)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return if (values.containsKey(key)) {
            (values[key] as Boolean)
        } else defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return if (values.containsKey(key)) {
            (values[key] as Float).toFloat()
        } else defValue
    }

    override fun getInt(key: String, defValue: Int): Int {
        return if (values.containsKey(key)) {
            (values[key] as Int).toInt()
        } else defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return if (values.containsKey(key)) {
            (values[key] as Long).toLong()
        } else defValue
    }

    override fun getString(key: String, defValue: String?): String? {
        return if (values.containsKey(key)) values[key] as String? else defValue
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        return if (values.containsKey(key)) {
            values[key] as Set<String>?
        } else defValues
    }

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        throw UnsupportedOperationException()
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        throw UnsupportedOperationException()
    }

    override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
        tempValues[key] = java.lang.Boolean.valueOf(value)
        return this
    }

    override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
        tempValues[key] = value
        return this
    }

    override fun putInt(key: String, value: Int): SharedPreferences.Editor {
        tempValues[key] = value
        return this
    }

    override fun putLong(key: String, value: Long): SharedPreferences.Editor {
        tempValues[key] = value
        return this
    }

    override fun putString(key: String, value: String?): SharedPreferences.Editor {
        tempValues[key] = value
        return this
    }

    override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
        tempValues[key] = values
        return this
    }

    override fun remove(key: String): SharedPreferences.Editor {
        tempValues.remove(key)
        return this
    }

    override fun clear(): SharedPreferences.Editor {
        tempValues.clear()
        return this
    }

    override fun commit(): Boolean {
        values = tempValues.toMutableMap()
        return true
    }

    override fun apply() {
        commit()
    }
}

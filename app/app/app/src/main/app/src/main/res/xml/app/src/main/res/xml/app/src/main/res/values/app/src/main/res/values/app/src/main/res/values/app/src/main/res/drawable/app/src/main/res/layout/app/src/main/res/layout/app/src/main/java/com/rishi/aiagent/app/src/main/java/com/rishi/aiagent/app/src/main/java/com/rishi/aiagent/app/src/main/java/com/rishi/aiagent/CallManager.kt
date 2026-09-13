package com.rishi.aiagent

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class CallManager(private val context: Context) {

    private val aliasMap = mapOf(
        "papa" to listOf("daddy", "dad", "papa", "pappa", "father"),
        "daddy" to listOf("daddy", "dad", "papa", "pappa", "father"),
        "mummy" to listOf("mummy", "mom", "mother", "mummyji"),
        "mom" to listOf("mummy", "mom", "mother", "mummyji")
    )

    fun makeCall(targetName: String): CallResult {
        val targets = aliasMap[targetName.lowercase()] ?: listOf(targetName.lowercase())
        val matchedContacts = queryContacts(targets)

        if (matchedContacts.isEmpty()) {
            return CallResult.NotFound
        }

        if (matchedContacts.size > 1) {
            val names = matchedContacts.map { it.name }.distinct()
            if (names.size > 1) {
                return CallResult.MultipleMatches(names)
            }
        }

        val chosen = matchedContacts.first()
        return executeCall(chosen.number)
    }

    private fun queryContacts(aliases: List<String>): List<ContactData> {
        val results = mutableListOf<ContactData>()
        val resolver: ContentResolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        ) ?: return results

        cursor.use { c ->
            val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (c.moveToNext()) {
                val name = c.getString(nameIdx) ?: ""
                val num = c.getString(numIdx) ?: ""
                val lowerName = name.lowercase()

                if (aliases.any { lowerName.contains(it) }) {
                    results.add(ContactData(name, num))
                }
            }
        }
        return results
    }

    private fun executeCall(phoneNumber: String): CallResult {
        val cleanedNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val uri = Uri.parse("tel:$cleanedNumber")

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        return try {
            val intent = if (hasPermission) {
                Intent(Intent.ACTION_CALL, uri)
            } else {
                Intent(Intent.ACTION_DIAL, uri)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            CallResult.Success(cleanedNumber)
        } catch (_: Exception) {
            CallResult.Failure
        }
    }

    sealed class CallResult {
        data class Success(val number: String) : CallResult()
        data class MultipleMatches(val names: List<String>) : CallResult()
        object NotFound : CallResult()
        object Failure : CallResult()
    }

    private data class ContactData(val name: String, val number: String)
}

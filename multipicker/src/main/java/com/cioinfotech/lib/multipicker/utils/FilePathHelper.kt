/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cioinfotech.lib.multipicker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.min

object FilePathHelper {
    fun getRealPath(context: Context?, fileUri: Uri?): Uri? {
        return getRealPathFromURIAPI19(context!!, fileUri!!)
    }

    @SuppressLint("NewApi")
    private fun getRealPathFromURIAPI19(context: Context, uri: Uri): Uri? {
        //val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider

            if ("content".equals(uri.scheme, ignoreCase = true)) {
                // Return the remote address
                return getDriveFilePath(uri, context)
            } else {
                // ExternalStorageProvider
                when {

//                    isExternalStorageDocument(uri) -> {
//                        val docId = DocumentsContract.getDocumentId(uri)
//                        val split = docId.split(":".toRegex()).toTypedArray()
//                        val type = split[0]
//                        // This is for checking Main Memory
//                        return if ("primary".equals(type, ignoreCase = true)) {
//                            if (split.size > 1) {
//                                context.getExternalFilesDir(null)?.absolutePath + "/" + split[1]
//                            } else {
//                                context.getExternalFilesDir(null).toString() + "/"
//                            }
//                            // This is for checking SD Card
//                        } else {
//                            "storage" + "/" + docId.replace(":", "/")
//                        }
//                    }
//                    isDownloadsDocument(uri) -> {
//                        val fileName = getFilePath(context, uri)
//                        if (fileName != null) {
//                            return Environment.getExternalStorageDirectory()
//                                    .toString() + "/Download/" + fileName
//                            //return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + fileName
//                        }
//                        var id = DocumentsContract.getDocumentId(uri)
//                        if (id.startsWith("raw:")) {
//                            id = id.replaceFirst("raw:".toRegex(), "")
//                            val file = File(id)
//                            if (file.exists()) return id
//                        }
//                        val contentUri = ContentUris.withAppendedId(
//                                Uri.parse("content://downloads/public_downloads"),
//                                java.lang.Long.valueOf(id)
//                        )
//                        return getDataColumn(context, contentUri, null, null)
//                    }
                    isMediaDocument(uri) -> {
                        val docId = DocumentsContract.getDocumentId(uri)
                        val split = docId.split(":".toRegex()).toTypedArray()
                        val type = split[0]
                        var contentUri: Uri? = null
                        when (type) {
                            "image" -> {
                                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            }
                            "video" -> {
                                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            }
                            "audio" -> {
                                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                        }
                        val selection = "_id=?"
                        val selectionArgs = arrayOf(
                                split[1]
                        )
                        return contentUri//getDataColumn(context, contentUri, selection, selectionArgs)
                    }

                }
            }

 /*       else if ("content".equals(uri.scheme, ignoreCase = true)) {

            return uri
//            if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
//                    context,
//                    uri,
//                    null,
//                    null
//            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri //uri.path
        }*/
        return null
    }

    private fun getDataColumn(
            context: Context, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
                column
        )
        try {
            cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getFilePath(context: Context?, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        try {
            cursor = context?.contentResolver?.query(uri!!, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    @SuppressLint("Recycle")
    private fun getDriveFilePath(
            uri: Uri,
            context: Context
    ): Uri? {
        val returnCursor =
                context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        //val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        //val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, name)
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available()!!

            //int bufferSize = 1024;
            val bufferSize = min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
        }
        return file.toUri()
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }
}

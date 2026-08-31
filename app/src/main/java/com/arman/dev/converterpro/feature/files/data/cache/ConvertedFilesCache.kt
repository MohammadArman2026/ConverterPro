package com.arman.dev.converterpro.feature.files.data.cache

import java.io.File
import kotlin.concurrent.thread

/**
 * In-memory snapshot backed by a JSON file so the Files list survives process death.
 */
class ConvertedFilesCache(private val cacheFile: File) {

    private val lock = Any()
    private var memory: List<CachedConvertedFile>? = null

    fun peek(): List<CachedConvertedFile> = synchronized(lock) {
        memory ?: readDisk().also { memory = it }
    }

    /**
     * In-memory rows only. Never touches disk, so the Files screen can seed UI on the
     * main thread without hitching the navigation animation.
     */
    fun snapshot(): List<CachedConvertedFile> = synchronized(lock) {
        memory.orEmpty()
    }

    /**
     * Reads the JSON file on a background thread so [snapshot] is populated before the
     * user opens Files.
     */
    fun warmAsync() {
        thread(name = "converted-files-cache-warm", isDaemon = true) {
            peek()
        }
    }

    fun replaceAll(files: List<CachedConvertedFile>) {
        synchronized(lock) {
            memory = files
            writeDisk(files)
        }
    }

    fun upsert(file: CachedConvertedFile) {
        synchronized(lock) {
            val current = (memory ?: readDisk()).toMutableList()
            val index = current.indexOfFirst { it.id == file.id }
            if (index >= 0) {
                current[index] = file
            } else {
                current.add(0, file)
            }
            memory = current
            writeDisk(current)
        }
    }

    fun remove(id: Long) {
        synchronized(lock) {
            val current = (memory ?: readDisk()).filterNot { it.id == id }
            memory = current
            writeDisk(current)
        }
    }

    private fun readDisk(): List<CachedConvertedFile> {
        if (!cacheFile.exists()) return emptyList()
        return try {
            CachedConvertedFilesJson.decode(cacheFile.readText())
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeDisk(files: List<CachedConvertedFile>) {
        cacheFile.parentFile?.mkdirs()
        cacheFile.writeText(CachedConvertedFilesJson.encode(files))
    }
}

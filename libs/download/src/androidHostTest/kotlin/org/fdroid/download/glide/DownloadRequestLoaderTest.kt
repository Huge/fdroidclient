package org.fdroid.download.glide

import kotlin.test.Test
import kotlin.test.assertEquals
import org.fdroid.IndexFile
import org.fdroid.download.DownloadRequest
import org.fdroid.download.Mirror

internal class DownloadRequestLoaderTest {

  private val indexFileWithHash =
    object : IndexFile {
      override val name = "/icons/app.png"
      override val sha256 = "784973023c9e3f32a750f3bc566bb13ee3f46b3811c2f269e2d11b47f07d1dab"
      override val size = 1024L
      override val ipfsCidV1 = null

      override fun serialize() = ""
    }

  private val indexFileWithoutHash =
    object : IndexFile {
      override val name = "/icons/app.png"
      override val sha256 = null
      override val size = 1024L
      override val ipfsCidV1 = null

      override fun serialize() = ""
    }

  @Test
  fun testGetKeyWithSha256() {
    val request = DownloadRequest(indexFileWithHash, emptyList())
    assertEquals(indexFileWithHash.sha256, request.getKey())
  }

  @Test
  fun testGetKeyWithoutSha256WithMirrors() {
    val mirror = Mirror("https://example.org/repo")
    val request = DownloadRequest(indexFileWithoutHash, listOf(mirror))
    assertEquals("https://example.org/repo/icons/app.png", request.getKey())
  }

  @Test
  fun testGetKeyWithoutSha256EmptyMirrors() {
    val request = DownloadRequest(indexFileWithoutHash, emptyList())
    // Should not throw IndexOutOfBoundsException when mirrors list is empty
    assertEquals(indexFileWithoutHash.name, request.getKey())
  }
}

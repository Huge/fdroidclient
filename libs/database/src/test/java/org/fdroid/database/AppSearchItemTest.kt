package org.fdroid.database

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test

internal class AppSearchItemTest {

  private fun createMatchInfo(numPhrases: Int, numColumns: Int, hits: List<Int>): ByteArray {
    val buffer = ByteBuffer.allocate(4 * (2 + hits.size)).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putInt(numPhrases)
    buffer.putInt(numColumns)
    hits.forEach { buffer.putInt(it) }
    return buffer.array()
  }

  @Test
  fun testEmptyMatchInfoDoesNotThrow() {
    val item =
      AppSearchItem(
        repoId = 1L,
        packageName = "org.example",
        lastUpdated = System.currentTimeMillis(),
        matchInfo = ByteArray(0),
      )
    assertNotNull(item)
    assertEquals(0.0, item.score)
  }

  @Test
  fun testShortMatchInfoDoesNotThrow() {
    val item =
      AppSearchItem(
        repoId = 1L,
        packageName = "org.example",
        lastUpdated = System.currentTimeMillis(),
        matchInfo = ByteArray(4), // only 1 integer
      )
    assertNotNull(item)
    assertEquals(0.0, item.score)
  }

  @Test
  fun testTruncatedMatchInfoDoesNotThrow() {
    // numPhrases = 2, numColumns = 6, but only provide 3 hit ints instead of 2 * 6 * 3 = 36
    val matchInfo = createMatchInfo(2, 6, listOf(1, 1, 1))
    val item =
      AppSearchItem(
        repoId = 1L,
        packageName = "org.example",
        lastUpdated = System.currentTimeMillis(),
        matchInfo = matchInfo,
      )
    assertNotNull(item)
  }

  @Test
  fun testUnknownColumnIndexDoesNotThrow() {
    // numColumns = 8, column 6 has hits, but weights only defines 1..5
    val hits = IntArray(1 * 8 * 3) { 0 }
    hits[3 * 6] = 1 // column 6 has 1 hit
    val matchInfo = createMatchInfo(1, 8, hits.toList())
    val item =
      AppSearchItem(
        repoId = 1L,
        packageName = "org.example",
        lastUpdated = System.currentTimeMillis(),
        matchInfo = matchInfo,
      )
    assertNotNull(item)
  }

  @Test
  fun testValidMatchInfoCalculatesScore() {
    // 1 phrase, 6 columns, column 1 ("name", weight 100) has 1 hit
    val hits = IntArray(1 * 6 * 3) { 0 }
    hits[3 * 1] = 1
    val matchInfo = createMatchInfo(1, 6, hits.toList())
    val now = System.currentTimeMillis()
    val item =
      AppSearchItem(
        repoId = 1L,
        packageName = "org.example",
        lastUpdated = now,
        matchInfo = matchInfo,
      )
    assertEquals(100.0, item.score)
  }
}

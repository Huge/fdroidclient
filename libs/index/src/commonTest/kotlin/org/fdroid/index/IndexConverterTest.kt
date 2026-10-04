package org.fdroid.index

import com.goncalossilva.resources.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import org.fdroid.index.IndexParser.parseV1
import org.fdroid.index.v2.IndexV2
import org.fdroid.test.TestDataEmptyV2
import org.fdroid.test.TestDataMaxV2
import org.fdroid.test.TestDataMidV2
import org.fdroid.test.TestDataMinV2
import org.fdroid.test.TestUtils.sorted
import org.fdroid.test.v1compat

internal class IndexConverterTest {

  @Test
  fun testEmpty() {
    testConversation("index-empty-v1.json", TestDataEmptyV2.index.v1compat())
  }

  @Test
  fun testMin() {
    testConversation("index-min-v1.json", TestDataMinV2.index.v1compat())
  }

  @Test
  fun testMid() {
    testConversation("index-mid-v1.json", TestDataMidV2.indexCompat)
  }

  @Test
  fun testMax() {
    testConversation("index-max-v1.json", TestDataMaxV2.indexCompat)
  }

  private fun testConversation(file: String, expectedIndex: IndexV2) {
    val indexV1Res = Resource(file)
    val indexV1Str = indexV1Res.readText()
    val indexV1 = parseV1(indexV1Str)

    val v2 = IndexConverter().toIndexV2(indexV1)

    assertEquals(expectedIndex.sorted(), v2.sorted())
  }

  @Test
  fun testAppWithEmptyVersionsList() {
    val indexV1 =
      org.fdroid.index.v1.IndexV1(
        repo =
          org.fdroid.index.v1.RepoV1(
            timestamp = 42L,
            version = 42,
            name = "Test Repo",
            icon = "icon.png",
            address = "https://example.com",
            description = "Test description",
          ),
        apps =
          listOf(org.fdroid.index.v1.AppV1(packageName = "org.example.app", license = "GPL-3.0")),
        packages = mapOf("org.example.app" to emptyList()),
      )

    val v2 = IndexConverter().toIndexV2(indexV1)
    val pkg = v2.packages["org.example.app"]
    assertEquals(emptyMap(), pkg?.versions)
  }
}

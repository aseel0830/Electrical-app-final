package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.util.SummaryFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("أصيل المليكي", appName)
  }

  @Test
  fun `verify summary formatting with client only and per-box colored wire lines`() {
    val project = ProjectEntity(name = "عمارة الشروق بالرياض", clientName = "أبو فهد")
    val itemsByStage = mapOf(
      "تأسيس السقف" to listOf(
        ItemEntity(stageName = "تأسيس السقف", name = "خراطيم سقف 16مم", quantity = 5, unit = "لفة")
      ),
      "الأسلاك" to listOf(
        ItemEntity(
          stageName = "الأسلاك",
          name = "سلك 2.5 مم",
          quantityMode = "TRIPLE_COLOR",
          q1 = 3, q2 = 2, q3 = 1,
          c1 = "أحمر", c2 = "أزرق", c3 = "أخضر مخطط بأصفر",
          unit = "لفة"
        )
      )
    )

    val summary = SummaryFormatter.generateFormattedSummary(
      project = project,
      technicianName = "أصيل المليكي",
      itemsByStage = itemsByStage
    )

    // Verify project name is omitted and only client name is included
    assertFalse(summary.contains("عمارة الشروق بالرياض"))
    assertTrue(summary.contains("العميل: أبو فهد"))

    // Verify stage and separator
    assertTrue(summary.contains("[تأسيس السقف]"))
    assertTrue(summary.contains("- خراطيم سقف 16مم : 5-لفة"))
    assertTrue(summary.contains("..........."))

    // Verify colored items format
    assertTrue(summary.contains("- سلك 2.5 مم :"))
    assertTrue(summary.contains("-3-لفة-أحمر"))
    assertTrue(summary.contains("-2-لفة-أزرق"))
    assertTrue(summary.contains("-1-لفة-أخضر مخطط بأصفر"))

    // Verify signature
    assertTrue(summary.contains("تحياتي، فني تمديدات الكهرباء المنزلية [أصيل المليكي]"))
  }

  @Test
  fun `verify custom technician name signature`() {
    val project = ProjectEntity(name = "مشروع 1", clientName = "سالم الدوسري")
    val summary = SummaryFormatter.generateFormattedSummary(
      project = project,
      technicianName = "مهندس علي",
      itemsByStage = emptyMap()
    )
    assertTrue(summary.contains("العميل: سالم الدوسري"))
    assertTrue(summary.contains("تحياتي، فني تمديدات الكهرباء المنزلية [مهندس علي]"))
  }
}

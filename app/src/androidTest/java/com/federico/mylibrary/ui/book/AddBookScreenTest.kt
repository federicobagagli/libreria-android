package com.federico.mylibrary.ui.book

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.federico.mylibrary.test.TestActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4


@RunWith(AndroidJUnit4::class)
class AddBookScreenTest {


    @get:Rule(order = 0)
    val activityRule = ActivityScenarioRule(TestActivity::class.java)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testClickGalleria() {
        composeTestRule.onRoot().printToLog("FULL_HIERARCHY")
        composeTestRule.onAllNodes(hasTestTag("galleryButton")).printToLog("DEBUG_TAG")

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasTestTag("galleryButton")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("galleryButton").performClick()
    }



    @Test
    fun testClickFotocamera() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(hasTestTag("takePhotoButton")).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag("takePhotoButton").performClick()
    }
}

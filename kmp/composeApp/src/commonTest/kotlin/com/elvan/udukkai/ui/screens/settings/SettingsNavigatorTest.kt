package com.elvan.udukkai.ui.screens.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsNavigatorTest {
    @Test fun backReturnsThroughNestedPageThenCompanyBeforeExiting() {
        val navigation = SettingsNavigator(SettingsRoute.Merchant)
        navigation.selectRoot(SettingsRoute.Account)
        navigation.push(SettingsRoute.LinkedAccounts)
        assertTrue(navigation.popOrReturnTo(SettingsRoute.Merchant))
        assertEquals(SettingsRoute.Account, navigation.state.route)
        assertTrue(navigation.popOrReturnTo(SettingsRoute.Merchant))
        assertEquals(SettingsRoute.Merchant, navigation.state.route)
        assertFalse(navigation.popOrReturnTo(SettingsRoute.Merchant))
    }
    @Test fun passwordFlowsUseStandalonePages() {
        assertTrue(SettingsRoute.ChangePassword.opensStandalonePage)
        assertTrue(SettingsRoute.CreatePassword.opensStandalonePage)
        assertFalse(SettingsRoute.Account.opensStandalonePage)
        assertFalse(SettingsRoute.LinkedAccounts.opensStandalonePage)
    }
    @Test fun nestedPagesPopWithinTheDetailPane() {
        val navigator = SettingsNavigator(SettingsRoute.About)
        navigator.push(SettingsRoute.AboutApp)
        val incoming = navigator.state.index
        assertTrue(navigator.canGoBack)
        assertTrue(navigator.pop())
        assertEquals(SettingsRoute.About, navigator.state.route)
        assertTrue(navigator.state.index < incoming)
        assertFalse(navigator.pop())
    }

    @Test fun selectingACategoryResetsOnlyItsOwnHistoryAndSlidesForward() {
        val detail = SettingsNavigator(SettingsRoute.Merchant)
        val otherPane = SettingsNavigator(SettingsRoute.Hub)
        detail.push(SettingsRoute.ManageProfiles)
        val previous = detail.state.index
        detail.selectRoot(SettingsRoute.Bank)
        assertEquals(SettingsRoute.Bank, detail.state.route)
        assertTrue(detail.state.index > previous)
        assertFalse(detail.canGoBack)
        assertEquals(SettingsRoute.Hub, otherPane.state.route)
    }

    @Test fun repeatTapsDoNotDuplicateTheCurrentPage() {
        val navigator = SettingsNavigator(SettingsRoute.Account)
        navigator.push(SettingsRoute.LinkedAccounts)
        navigator.push(SettingsRoute.LinkedAccounts)
        assertTrue(navigator.pop())
        assertEquals(SettingsRoute.Account, navigator.state.route)
        assertFalse(navigator.canGoBack)
    }
}

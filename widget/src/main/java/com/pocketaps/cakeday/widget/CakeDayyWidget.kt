package com.pocketaps.cakeday.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pocketaps.cakeday.widget.di.CakeDayyWidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class CakeDayyWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            CakeDayyWidgetEntryPoint::class.java,
        )
        val getUpcomingBirthdays = entryPoint.getUpcomingBirthdaysUseCase()
        val uiState = runCatching {
            getUpcomingBirthdays().first().toWidgetUiState()
        }.getOrElse { WidgetUiState.Error }
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val strings = WidgetStrings(
            title = context.getString(R.string.widget_title),
            empty = context.getString(R.string.widget_empty),
            error = context.getString(R.string.widget_error),
        )

        provideContent {
            GlanceTheme(colors = CakeDayyGlanceColors) {
                CakeDayyWidgetContent(uiState = uiState, launchIntent = launchIntent, strings = strings)
            }
        }
    }

    companion object {
        val SMALL = DpSize(WIDGET_SMALL_SIZE_DP.dp, WIDGET_SMALL_SIZE_DP.dp)
        val LARGE = DpSize(WIDGET_LARGE_WIDTH_DP.dp, WIDGET_LARGE_HEIGHT_DP.dp)
    }
}

private const val WIDGET_SMALL_SIZE_DP = 110
private const val WIDGET_LARGE_WIDTH_DP = 250
private const val WIDGET_LARGE_HEIGHT_DP = 180
private const val VISIBLE_ITEMS_SMALL = 3
private const val VISIBLE_ITEMS_LARGE = 5
private const val HEADER_FONT_SIZE_SP = 14
private const val ROW_FONT_SIZE_SP = 13
private const val LABEL_FONT_SIZE_SP = 12
private const val CONTENT_PADDING_DP = 12
private const val ROW_VERTICAL_PADDING_DP = 4
private const val HEADER_SPACER_HEIGHT_DP = 8

private data class WidgetStrings(val title: String, val empty: String, val error: String)

@Composable
private fun CakeDayyWidgetContent(
    uiState: WidgetUiState,
    launchIntent: Intent?,
    strings: WidgetStrings
) {
    val openApp = launchIntent?.let { actionStartActivity(it) }
    val visibleCount = if (LocalSize.current.width < CakeDayyWidget.LARGE.width) {
        VISIBLE_ITEMS_SMALL
    } else {
        VISIBLE_ITEMS_LARGE
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(CONTENT_PADDING_DP.dp),
    ) {
        Text(
            text = strings.title,
            style = headerTextStyle(GlanceTheme.colors.onBackground),
            modifier = GlanceModifier.fillMaxWidth().clickableIfPresent(openApp),
        )
        Spacer(modifier = GlanceModifier.height(HEADER_SPACER_HEIGHT_DP.dp))
        when (uiState) {
            WidgetUiState.Empty -> WidgetMessage(strings.empty, openApp)
            WidgetUiState.Error -> WidgetMessage(strings.error, openApp)
            is WidgetUiState.Content -> WidgetBirthdayList(uiState.items.take(visibleCount), openApp)
        }
    }
}

@Composable
private fun WidgetMessage(text: String, openApp: Action?) {
    Box(
        modifier = GlanceModifier.fillMaxSize().clickableIfPresent(openApp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = rowTextStyle(GlanceTheme.colors.onSurfaceVariant))
    }
}

@Composable
private fun WidgetBirthdayList(items: List<WidgetBirthdayItem>, openApp: Action?) {
    LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
        items(items) { item ->
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = ROW_VERTICAL_PADDING_DP.dp)
                    .clickableIfPresent(openApp),
            ) {
                Text(
                    text = item.name,
                    style = rowTextStyle(GlanceTheme.colors.onBackground),
                    modifier = GlanceModifier.defaultWeight(),
                )
                Text(item.daysUntilLabel, style = labelTextStyle(GlanceTheme.colors.primary))
            }
        }
    }
}

private fun headerTextStyle(color: ColorProvider) =
    TextStyle(color = color, fontWeight = FontWeight.Bold, fontSize = HEADER_FONT_SIZE_SP.sp)

private fun rowTextStyle(color: ColorProvider) = TextStyle(color = color, fontSize = ROW_FONT_SIZE_SP.sp)

private fun labelTextStyle(color: ColorProvider) = TextStyle(color = color, fontSize = LABEL_FONT_SIZE_SP.sp)

private fun GlanceModifier.clickableIfPresent(action: Action?): GlanceModifier =
    if (action != null) clickable(action) else this

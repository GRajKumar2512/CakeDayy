package com.pocketaps.cakeday.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.pocketaps.cakeday.core.model.UpcomingBirthday
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject

class BirthdayNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun notify(upcomingBirthday: UpcomingBirthday) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        ensureChannel()

        val targetYear = LocalDate.now().plusDays(upcomingBirthday.daysUntilNext.toLong()).year
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_cake)
            .setContentTitle(titleFor(upcomingBirthday))
            .setContentText(textFor(upcomingBirthday))
            .apply { launchAppPendingIntent(upcomingBirthday.person.id)?.let { setContentIntent(it) } }
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            notificationId(upcomingBirthday.person.id, targetYear),
            notification,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService<NotificationManager>()?.createNotificationChannel(channel)
    }

    private fun launchAppPendingIntent(personId: Long): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(
            context,
            personId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun titleFor(upcomingBirthday: UpcomingBirthday): String = when (upcomingBirthday.daysUntilNext) {
        0 -> "${upcomingBirthday.person.name}'s birthday is today!"
        1 -> "${upcomingBirthday.person.name}'s birthday is tomorrow"
        else -> "${upcomingBirthday.person.name}'s birthday is coming up"
    }

    private fun textFor(upcomingBirthday: UpcomingBirthday): String {
        val age = upcomingBirthday.nextAge
        return when {
            upcomingBirthday.daysUntilNext == 0 && age != null -> "Turning $age today"
            upcomingBirthday.daysUntilNext == 0 -> "Today"
            age != null -> "In ${upcomingBirthday.daysUntilNext} days, turning $age"
            else -> "In ${upcomingBirthday.daysUntilNext} days"
        }
    }

    private fun notificationId(personId: Long, targetYear: Int): Int =
        "$personId-$targetYear".hashCode()

    private companion object {
        const val CHANNEL_ID = "birthday_reminders"
        const val CHANNEL_NAME = "Birthday reminders"
    }
}

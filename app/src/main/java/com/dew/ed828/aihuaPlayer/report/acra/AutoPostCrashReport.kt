package com.dew.ed828.aihuaPlayer.report.acra

import android.content.Context
import com.dew.ed828.aihuaPlayer.about.R
import com.dew.ed828.aihuaPlayer.report.activity.ErrorActivity
import com.dew.ed828.aihuaPlayer.report.model.ErrorInfo
import com.dew.ed828.aihuaPlayer.report.model.UserAction
import org.acra.collector.CrashReportData
import org.acra.config.ACRAConfiguration
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

/**
 *
 * Created by Edward on 12/3/2018.
 *
 * RepostSender is a simple interface for defining various crash report senders.
 *
 * You can reuse {@link HttpSender} to send reports to your custom server-side report
 * collection script even if you expect (or prefer) specific names for each
 * report field as {@link HttpSender#send(Context, CrashReportData)}
 * can take a {@code Map<ReportField, String>} as an input to convert each field name to
 * your preferred POST parameter name.
 */

class AcraReportSender : ReportSender {
    override fun send(context: Context, report: CrashReportData) {
        ErrorActivity.reportError(context, report,
            ErrorInfo.make(
                UserAction.UI_ERROR, "none",
                "App crash, UI failure", R.string.app_ui_crash))
    }
}

/**
 *
 * Factory for creating and configuring a {@link ReportSender} instance.
 * Implementations must have a no argument constructor.
 *
 * Each configured ReportSenderFactory is created within the {@link SenderService}
 * and is used to construct and configure a single {@link ReportSender}.
 *
 */
class AcraReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: ACRAConfiguration): ReportSender {
        return AcraReportSender()
    }
}
package com.webtoapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.webtoapp.core.i18n.Strings
import com.webtoapp.core.playstore.PlayPolicyChecker
import com.webtoapp.core.playstore.aab.AabExportException
import com.webtoapp.core.playstore.aab.AabExporter
import com.webtoapp.core.playstore.aab.AabExportCoordinator
import com.webtoapp.core.playstore.aab.FailureStage
import com.webtoapp.data.model.WebApp
import com.webtoapp.ui.design.WtaButton
import com.webtoapp.ui.design.WtaButtonVariant
import com.webtoapp.ui.design.WtaCard
import com.webtoapp.ui.design.WtaCardTone
import com.webtoapp.ui.design.WtaScreen
import com.webtoapp.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayStoreScreen(
    onBack: () -> Unit,
    initialAppId: Long? = null,
    autoStartExport: Boolean = false,
    viewModel: MainViewModel = koinViewModel()
) {
    val webApps by viewModel.webApps.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val coordinator = remember(context) {
        AabExportCoordinator(context.applicationContext)
    }

    var selectedAppId by remember { mutableStateOf(initialAppId) }
    var report by remember { mutableStateOf<PlayPolicyChecker.Report?>(null) }
    var exportState by remember { mutableStateOf<ExportState>(ExportState.Idle) }
    var policyExpanded by rememberSaveable { mutableStateOf(false) }
    var exportJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    LaunchedEffect(webApps) {
        if (selectedAppId == null && webApps.isNotEmpty()) {
            selectedAppId = if (initialAppId != null && webApps.any { it.id == initialAppId }) {
                initialAppId
            } else {
                webApps.first().id
            }
        }

        if (selectedAppId != null && webApps.none { it.id == selectedAppId }) {
            selectedAppId = webApps.firstOrNull()?.id
            report = null
        }
    }

    val selectedApp = webApps.firstOrNull { it.id == selectedAppId }

    fun runExport(app: WebApp) {
        if (exportState is ExportState.Running) return
        exportState = ExportState.Running(AabExporter.Stage.STARTING, 0)
        exportJob = scope.launch {
            exportState = withContext(Dispatchers.IO) {
                try {
                    val result = coordinator.export(app) { stage, pct ->
                        exportState = ExportState.Running(stage, pct)
                    }
                    ExportState.Success(result.signedAab.absolutePath)
                } catch (_: kotlinx.coroutines.CancellationException) {
                    ExportState.Cancelled
                } catch (e: AabExportException) {
                    ExportState.Failed(
                        failureStage = e.failureStage,
                        technicalDetails = e.message ?: e.javaClass.simpleName,
                        throwable = e
                    )
                } catch (e: Exception) {
                    ExportState.Failed(
                        failureStage = FailureStage.UNKNOWN,
                        technicalDetails = e.message ?: e.javaClass.simpleName,
                        throwable = e
                    )
                }
            }
        }
    }

    fun cancelExport() {
        exportJob?.cancel()
        exportJob = null
        exportState = ExportState.Cancelled
    }

    LaunchedEffect(autoStartExport, selectedApp?.id) {
        if (autoStartExport && selectedApp != null && exportState is ExportState.Idle) {
            runExport(selectedApp)
        }
    }

    WtaScreen(
        title = Strings.playStoreTitle,
        subtitle = Strings.playStoreSubtitle,
        onBack = onBack,
        snackbarHostState = snackbarHostState
    ) { _ ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                AppSelectionCard(
                    apps = webApps,
                    selectedAppId = selectedAppId,
                    onSelectApp = { id ->
                        selectedAppId = id
                        report = null
                        if (exportState !is ExportState.Running) {
                            exportState = ExportState.Idle
                        }
                    }
                )
            }

            if (selectedApp != null) {
                item {
                    WtaButton(
                        onClick = { runExport(selectedApp) },
                        text = if (exportState is ExportState.Running) {
                            Strings.playStoreExportRunning
                        } else {
                            Strings.playStoreExportAabButton
                        },
                        variant = WtaButtonVariant.Tonal,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = exportState !is ExportState.Running,
                        leadingIcon = Icons.Outlined.PlayCircleOutline
                    )
                }

                if (exportState !is ExportState.Idle) {
                    item {
                        ExportStateCard(
                            state = exportState,
                            onShare = { path ->
                                shareAab(context, java.io.File(path), snackbarHostState, scope)
                            },
                            onCancel = { cancelExport() }
                        )
                    }
                }

                item {
                    PolicyAdviceCard(
                        expanded = policyExpanded,
                        onToggle = {
                            policyExpanded = !policyExpanded
                            if (policyExpanded) {
                                report = PlayPolicyChecker.check(selectedApp)
                            }
                        },
                        report = report,
                        webApp = selectedApp
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSelectionCard(
    apps: List<WebApp>,
    selectedAppId: Long?,
    onSelectApp: (Long) -> Unit
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.PlayCircleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = Strings.playStoreSelectApp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (apps.isEmpty()) {
                Text(
                    text = Strings.playStoreNoAppsHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    apps.forEach { app ->
                        AppRow(
                            app = app,
                            isSelected = app.id == selectedAppId,
                            onClick = { onSelectApp(app.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: WebApp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val container = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val onContainer = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = onContainer
                )
                if (app.url.isNotBlank()) {
                    Text(
                        text = app.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
            if (isSelected) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PolicyAdviceCard(
    expanded: Boolean,
    onToggle: () -> Unit,
    report: PlayPolicyChecker.Report?,
    webApp: WebApp
) {
    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                onClick = onToggle,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Policy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.playStoreAdviceTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = Strings.playStoreAdviceSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val rpt = report ?: PlayPolicyChecker.check(webApp)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportSummaryCard(report = rpt)
                    rpt.violations.forEach { violation ->
                        ViolationCard(violation = violation)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportSummaryCard(report: PlayPolicyChecker.Report) {
    val summary = when {
        report.isClean -> ReportSummary(
            icon = Icons.Outlined.CheckCircle,
            tone = WtaCardTone.Highlighted,
            headlineColor = Color(0xFF2E7D32),
            headline = Strings.playStoreReportClean
        )
        report.blockerCount > 0 -> ReportSummary(
            icon = Icons.Outlined.Block,
            tone = WtaCardTone.Critical,
            headlineColor = MaterialTheme.colorScheme.error,
            headline = String.format(Strings.playStoreReportBlocked, report.blockerCount)
        )
        else -> ReportSummary(
            icon = Icons.Outlined.Warning,
            tone = WtaCardTone.Highlighted,
            headlineColor = Color(0xFFE65100),
            headline = String.format(Strings.playStoreReportWarning, report.warningCount)
        )
    }

    WtaCard(tone = summary.tone) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    summary.icon,
                    contentDescription = null,
                    tint = summary.headlineColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.playStoreSummaryTitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = summary.headline,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = summary.headlineColor
                    )
                }
            }

            if (report.isClean) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Strings.playStoreReportCleanDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (report.blockerCount > 0) {
                        SeverityBadge(
                            count = report.blockerCount,
                            label = Strings.playStoreSeverityBlocker,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (report.warningCount > 0) {
                        SeverityBadge(
                            count = report.warningCount,
                            label = Strings.playStoreSeverityWarning,
                            color = Color(0xFFE65100)
                        )
                    }
                    if (report.infoCount > 0) {
                        SeverityBadge(
                            count = report.infoCount,
                            label = Strings.playStoreSeverityInfo,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = count.toString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ViolationCard(violation: PlayPolicyChecker.Violation) {
    val resolved = remember(violation) {
        PlayPolicyChecker.resolveViolation(violation)
    }
    val severityColor = when (resolved.severity) {
        PlayPolicyChecker.Severity.BLOCKER -> MaterialTheme.colorScheme.error
        PlayPolicyChecker.Severity.WARNING -> Color(0xFFE65100)
        PlayPolicyChecker.Severity.INFO -> MaterialTheme.colorScheme.primary
    }
    val severityLabel = when (resolved.severity) {
        PlayPolicyChecker.Severity.BLOCKER -> Strings.playStoreSeverityBlocker
        PlayPolicyChecker.Severity.WARNING -> Strings.playStoreSeverityWarning
        PlayPolicyChecker.Severity.INFO -> Strings.playStoreSeverityInfo
    }
    val severityIcon = when (resolved.severity) {
        PlayPolicyChecker.Severity.BLOCKER -> Icons.Outlined.Block
        PlayPolicyChecker.Severity.WARNING -> Icons.Outlined.Warning
        PlayPolicyChecker.Severity.INFO -> Icons.Outlined.Info
    }

    WtaCard(tone = WtaCardTone.Surface) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = severityColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            severityIcon,
                            contentDescription = null,
                            tint = severityColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = severityColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resolved.featurePath,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = Strings.playStorePolicyAreaLabel + ": ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = resolved.policyArea,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = Strings.playStoreFixHintLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resolved.fixHint,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private data class ReportSummary(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tone: WtaCardTone,
    val headlineColor: Color,
    val headline: String
)

internal sealed interface ExportState {
    data object Idle : ExportState

    data class Running(
        val stage: AabExporter.Stage,
        val percent: Int
    ) : ExportState

    data class Success(val aabPath: String) : ExportState

    data object Cancelled : ExportState

    data class Failed(
        val failureStage: FailureStage,
        val technicalDetails: String,
        val throwable: Throwable? = null
    ) : ExportState
}

@Composable
private fun ExportStateCard(
    state: ExportState,
    onShare: (path: String) -> Unit,
    onCancel: () -> Unit
) {
    when (state) {
        is ExportState.Idle -> Unit

        is ExportState.Running -> {
            WtaCard(tone = WtaCardTone.Highlighted) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = Strings.playStoreExportRunning,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stageLabel(state.stage)} (${state.percent}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { state.percent / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.material3.TextButton(
                        onClick = onCancel,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = Strings.btnCancel,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        is ExportState.Cancelled -> {
            WtaCard(tone = WtaCardTone.Surface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.playStoreExportCancelled,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        is ExportState.Success -> {
            WtaCard(tone = WtaCardTone.Highlighted) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.playStoreExportSuccess,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.playStoreExportSuccessDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.aabPath,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.playStoreExportPreUploadHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    WtaButton(
                        onClick = { onShare(state.aabPath) },
                        text = Strings.playStoreExportShare,
                        variant = WtaButtonVariant.Tonal,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        is ExportState.Failed -> {
            WtaCard(tone = WtaCardTone.Critical) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = failureStageLabel(state.failureStage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (state.technicalDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Strings.playStoreExportTechnicalDetails,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.technicalDetails,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    val report = remember(state.failureStage, state.technicalDetails, state.throwable) {
                        com.webtoapp.ui.components.buildErrorReport(
                            scope = "AAB export",
                            message = state.technicalDetails,
                            throwable = state.throwable,
                            contextLines = listOf("failureStage=${state.failureStage.name}")
                        )
                    }
                    com.webtoapp.ui.components.WtaErrorDetailsSection(
                        report = report,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun shareAab(
    context: android.content.Context,
    aab: java.io.File,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            aab
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_SUBJECT, aab.name)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = android.content.Intent.createChooser(intent, Strings.playStoreExportShare)
            .apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(chooser)
    } catch (e: Exception) {
        scope.launch {
            snackbarHostState.showSnackbar(e.message ?: e.javaClass.simpleName)
        }
    }
}

private fun stageLabel(stage: AabExporter.Stage): String {
    return when (stage) {
        AabExporter.Stage.STARTING ->
            Strings.playStoreExportStageStarting
        AabExporter.Stage.BUILDING_APK ->
            Strings.playStoreExportStageBuildingApk
        AabExporter.Stage.ASSEMBLING ->
            Strings.playStoreExportStageAssembling
        AabExporter.Stage.ASSEMBLED ->
            Strings.playStoreExportStageAssembled
        AabExporter.Stage.SIGNING ->
            Strings.playStoreExportStageSigning
        AabExporter.Stage.SIGNED ->
            Strings.playStoreExportStageSigned
    }
}

private fun failureStageLabel(stage: FailureStage): String {
    return when (stage) {
        FailureStage.BUILD_APK ->
            Strings.playStoreExportFailureBuildApk
        FailureStage.ASSEMBLE ->
            Strings.playStoreExportFailureAssemble
        FailureStage.SIGN ->
            Strings.playStoreExportFailureSign
        FailureStage.UNKNOWN ->
            Strings.playStoreExportFailureUnknown
    }
}

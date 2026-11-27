package de.mcaffen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import de.mcaffen.app.network.EventItemDto
import de.mcaffen.app.network.EventsDto
import de.mcaffen.app.network.FaqEntryDto
import de.mcaffen.app.network.PartnerDto
import de.mcaffen.app.network.RuleDto
import de.mcaffen.app.network.TeamMemberDto
import de.mcaffen.app.ui.LocalStrings

/** Alles, was der Info-Tab aus der API zeigt. */
internal data class InfoContent(
    val events: EventsDto = EventsDto(),
    val faq: List<FaqEntryDto> = emptyList(),
    val rules: List<RuleDto> = emptyList(),
    val team: List<TeamMemberDto> = emptyList(),
    val partners: List<PartnerDto> = emptyList(),
) {
    val isEmpty: Boolean
        get() = events.upcoming.isEmpty() && events.weekly.isEmpty() &&
            faq.isEmpty() && rules.isEmpty() && team.isEmpty() && partners.isEmpty()
}

private enum class InfoSection(val labelKey: String) {
    EVENTS("info.section.events"),
    FAQ("info.section.faq"),
    RULES("info.section.rules"),
    TEAM("info.section.team"),
}

/**
 * Info-Tab: Events, FAQ, Regeln und Team samt Partnern
 * (`/api/events`, `/faq`, `/regeln`, `/team`, `/partner`).
 */
@Composable
internal fun InfoScreen(
    content: InfoContent,
    loading: Boolean,
    error: String?,
    onReload: () -> Unit,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    var section by rememberSaveable { mutableStateOf(InfoSection.EVENTS) }

    if (loading && content.isEmpty) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    if (content.isEmpty) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("ℹ️", style = MaterialTheme.typography.headlineLarge)
                Text(
                    error ?: strings["info.empty"],
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    strings["news.reload"],
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onReload() },
                )
            }
        }
        return
    }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoSection.entries.forEach { entry ->
                FilterChip(
                    selected = entry == section,
                    onClick = { section = entry },
                    label = { Text(strings[entry.labelKey]) },
                )
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (section) {
                InfoSection.EVENTS -> eventItems(content.events)
                InfoSection.FAQ -> faqItems(content.faq)
                InfoSection.RULES -> ruleItems(content.rules)
                InfoSection.TEAM -> teamItems(content.team, content.partners, onOpen)
            }
        }
    }
}

@Composable
private fun InfoCard(modifier: Modifier = Modifier, body: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) { body() }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun EmptySection() {
    Text(
        LocalStrings.current["info.sectionEmpty"],
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
}

private fun LazyListScope.eventItems(events: EventsDto) {
    if (events.upcoming.isEmpty() && events.weekly.isEmpty()) {
        item("events-empty") { EmptySection() }
        return
    }
    if (events.upcoming.isNotEmpty()) {
        item("events-upcoming") { SectionLabel(LocalStrings.current["info.upcoming"]) }
        itemsIndexed(events.upcoming, key = { index, _ -> "up-$index" }) { _, event -> EventCard(event) }
    }
    if (events.weekly.isNotEmpty()) {
        item("events-weekly") { SectionLabel(LocalStrings.current["info.weekly"]) }
        itemsIndexed(events.weekly, key = { index, _ -> "wk-$index" }) { _, event -> EventCard(event) }
    }
}

@Composable
private fun EventCard(event: EventItemDto) {
    InfoCard {
        Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        val whenText = listOfNotNull(event.weekday, event.date, event.time)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (whenText.isNotEmpty()) {
            Text(whenText, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        event.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun LazyListScope.faqItems(faq: List<FaqEntryDto>) {
    if (faq.isEmpty()) {
        item("faq-empty") { EmptySection() }
        return
    }
    itemsIndexed(faq, key = { index, _ -> "faq-$index" }) { _, entry -> FaqCard(entry) }
}

@Composable
private fun FaqCard(entry: FaqEntryDto) {
    var open by rememberSaveable(entry.question) { mutableStateOf(false) }
    InfoCard(Modifier.clickable { open = !open }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entry.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Text(if (open) "−" else "+", style = MaterialTheme.typography.titleMedium)
        }
        if (open) {
            Text(
                entry.answer,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.ruleItems(rules: List<RuleDto>) {
    if (rules.isEmpty()) {
        item("rules-empty") { EmptySection() }
        return
    }
    var lastCategory: String? = null
    rules.forEachIndexed { index, rule ->
        val category = rule.category?.takeIf { it.isNotBlank() }
        if (category != null && category != lastCategory) {
            item("rule-cat-$index") { SectionLabel(category) }
        }
        lastCategory = category ?: lastCategory
        item("rule-$index") {
            InfoCard {
                Text(rule.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    rule.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun LazyListScope.teamItems(
    team: List<TeamMemberDto>,
    partners: List<PartnerDto>,
    onOpen: (String) -> Unit,
) {
    if (team.isEmpty() && partners.isEmpty()) {
        item("team-empty") { EmptySection() }
        return
    }
    itemsIndexed(team, key = { index, _ -> "team-$index" }) { _, member -> TeamCard(member) }
    if (partners.isNotEmpty()) {
        item("partners-label") { SectionLabel(LocalStrings.current["info.partners"]) }
        itemsIndexed(partners, key = { index, _ -> "partner-$index" }) { _, partner ->
            PartnerCard(partner, onOpen)
        }
    }
}

@Composable
private fun Avatar(url: String?, fallback: String) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isNullOrBlank()) {
            Text(
                fallback.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TeamCard(member: TeamMemberDto) {
    InfoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(member.avatar, member.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(member.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                member.role?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        member.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PartnerCard(partner: PartnerDto, onOpen: (String) -> Unit) {
    val url = partner.url?.takeIf { it.startsWith("https://") }
    InfoCard(if (url != null) Modifier.clickable { onOpen(url) } else Modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(partner.logo, partner.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(partner.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                partner.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

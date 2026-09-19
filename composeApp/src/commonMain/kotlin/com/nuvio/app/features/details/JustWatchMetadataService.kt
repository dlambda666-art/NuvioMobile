package com.nuvio.app.features.details

import com.nuvio.app.features.addons.buildAddonResourceUrl
import com.nuvio.app.features.addons.fetchAddonResponseText
import kotlinx.coroutines.withTimeoutOrNull

internal object JustWatchMetadataService {
    private const val MANIFEST_URL = "https://lambda666-justwatch-dates.hf.space/manifest.json"
    private const val TIMEOUT_MS = 3_500L

    suspend fun enrich(
        meta: MetaDetails,
        fallbackItemId: String,
        type: String,
    ): MetaDetails {
        if (!type.equals("movie", ignoreCase = true)) return meta

        val id = fallbackItemId
            .takeIf { it.startsWith("tmdb:", ignoreCase = true) || it.startsWith("tt", ignoreCase = true) }
            ?: meta.id.takeIf { it.startsWith("tmdb:", ignoreCase = true) || it.startsWith("tt", ignoreCase = true) }
            ?: return meta

        val jwMeta = withTimeoutOrNull(TIMEOUT_MS) {
            runCatching {
                val url = buildAddonResourceUrl(
                    manifestUrl = MANIFEST_URL,
                    resource = "meta",
                    type = "movie",
                    id = id,
                )
                MetaDetailsParser.parse(fetchAddonResponseText(url))
            }.getOrNull()
        } ?: return meta

        val digitalLink = jwMeta.links.firstOrNull { link ->
            link.name.startsWith("Sortie numérique", ignoreCase = true) &&
                link.name.contains(Regex("\\d{4}-\\d{2}-\\d{2}"))
        }

        val digitalDate = digitalLink
            ?.name
            ?.substringAfter(":")
            ?.trim()
            ?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
            ?: return meta

        val digitalLabel = "Numérique : $digitalDate"
        val releaseInfo = meta.releaseInfo
            ?.takeIf { it.isNotBlank() }
            ?.let { current ->
                if (current.contains(digitalLabel, ignoreCase = true)) current
                else "$current • $digitalLabel"
            }
            ?: digitalLabel

        return meta.copy(
            releaseInfo = releaseInfo,
            links = meta.links + MetaLink(
                name = "Sortie numérique : $digitalDate",
                category = "digital",
                url = digitalLink.url,
            ),
        )
    }
}

package com.nuvio.app.features.home

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object HomeCatalogParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseCatalog(payload: String, maxItems: Int? = null): List<MetaPreview> =
        parseCatalogResponse(payload, maxItems).items

    fun parseCatalogResponse(payload: String, maxItems: Int? = null): ParsedCatalogResponse {
        val root = json.parseToJsonElement(payload).jsonObject
        val metas = root.array("metas")
        val parsedItems = buildList {
            val seenKeys = mutableSetOf<String>()
            for (element in metas) {
                if (maxItems != null && size >= maxItems) break
                val meta = element as? JsonObject ?: continue
                val id = meta.string("id")
                val type = meta.string("type")
                val name = meta.string("name")
                if (id.isNullOrBlank() || type.isNullOrBlank() || name.isNullOrBlank()) continue

                // Stremio catalog responses may carry addon-specific metadata
                // inside the standard meta object. FrenchPulse supports both
                // that form and the legacy flat/nested forms.
                val metaPayload = meta["meta"] as? JsonObject
                val pulse = meta["frenchpulse"] as? JsonObject
                    ?: metaPayload?.get("frenchpulse") as? JsonObject
                val hasFrenchPulseMeta =
                    meta["frenchpulse_meta_version"] != null ||
                        metaPayload?.get("frenchpulse_meta_version") != null

                val status = pulse?.string("status")
                    ?: meta.string("frenchpulse_status")
                    ?: metaPayload?.string("frenchpulse_status")
                    ?: meta.string("status").takeIf { hasFrenchPulseMeta }
                    ?: metaPayload?.string("status").takeIf { hasFrenchPulseMeta }

                val quality = pulse?.string("quality")
                    ?: meta.string("frenchpulse_quality")
                    ?: metaPayload?.string("frenchpulse_quality")
                    ?: meta.string("quality").takeIf { hasFrenchPulseMeta }
                    ?: metaPayload?.string("quality").takeIf { hasFrenchPulseMeta }

                val vf = pulse?.boolean("vf")
                    ?: meta.boolean("frenchpulse_vf")
                    ?: metaPayload?.boolean("frenchpulse_vf")
                    ?: meta.boolean("vf").takeIf { hasFrenchPulseMeta }
                    ?: metaPayload?.boolean("vf").takeIf { hasFrenchPulseMeta }
                    ?: false

                val item = MetaPreview(
                    id = id,
                    type = type,
                    name = name,
                    poster = meta.string("poster"),
                    banner = meta.string("banner") ?: meta.string("background"),
                    logo = meta.string("logo"),
                    posterShape = meta.string("posterShape").toPosterShape(),
                    description = meta.string("description"),
                    releaseInfo = meta.string("releaseInfo"),
                    rawReleaseDate = meta.string("released"),
                    imdbRating = meta.string("imdbRating"),
                    genres = meta.array("genres").mapNotNull { it.jsonPrimitive.contentOrNull?.takeIf(String::isNotBlank) },
                    frenchPulseStatus = status,
                    frenchPulseQuality = quality,
                    frenchPulseVf = vf,
                )
                if (seenKeys.add(item.stableKey())) add(item)
            }
        }
        return ParsedCatalogResponse(parsedItems, metas.size)
    }

    private fun JsonObject.string(name: String): String? = this[name]?.jsonPrimitive?.contentOrNull

    private fun JsonObject.boolean(name: String): Boolean? =
        this[name]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull()

    private fun JsonObject.array(name: String): JsonArray =
        this[name] as? JsonArray ?: JsonArray(emptyList())

    private fun String?.toPosterShape(): PosterShape = when (this?.lowercase()) {
        "square" -> PosterShape.Square
        "landscape" -> PosterShape.Landscape
        else -> PosterShape.Poster
    }
}

data class ParsedCatalogResponse(val items: List<MetaPreview>, val rawItemCount: Int)

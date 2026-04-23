package me.qsx.jergal.dualscreen

import me.qsx.jergal.ui.theme.ThemeColorRole

/**
 * Immutable launcher metadata for a single retro game tile and its top-screen showcase content.
 */
data class RetroGame(
    val id: String,
    val title: String,
    val logo: String,
    val tagline: String,
    val heroSummary: String,
    val genre: String,
    val platformLabel: String,
    val accentRole: ThemeColorRole,
    val secondaryAccentRole: ThemeColorRole,
)

/**
 * Static sample game data used to drive the dual-screen launcher prototype.
 */
object RetroLibrary {
    val games = listOf(
        RetroGame(
            id = "aether-blade",
            title = "Aether Blade",
            logo = "AETHER",
            tagline = "Sky duels, clockwork ruins, and a blade tuned to lightning.",
            heroSummary = "Lead a drifting knight through floating citadels and ruined airships in a high-speed action campaign.",
            genre = "Action RPG",
            platformLabel = "GBA / PSP",
            accentRole = ThemeColorRole.Primary,
            secondaryAccentRole = ThemeColorRole.Secondary,
        ),
        RetroGame(
            id = "neon-circuit",
            title = "Neon Circuit",
            logo = "N//C",
            tagline = "Arcade precision for midnight city runs.",
            heroSummary = "Master impossible turns, boost through rain-lit highways, and climb a dense championship ladder.",
            genre = "Racing",
            platformLabel = "PS1 / DC",
            accentRole = ThemeColorRole.Secondary,
            secondaryAccentRole = ThemeColorRole.PrimaryContainer,
        ),
        RetroGame(
            id = "moonlit-keep",
            title = "Moonlit Keep",
            logo = "MOONLIT",
            tagline = "A gothic map stitched together by hidden passages.",
            heroSummary = "Explore a towering castle, uncover relic routes, and defeat night-bound bosses with layered mobility upgrades.",
            genre = "Metroidvania",
            platformLabel = "SNES / DS",
            accentRole = ThemeColorRole.Tertiary,
            secondaryAccentRole = ThemeColorRole.SecondaryContainer,
        ),
        RetroGame(
            id = "pixel-brigade",
            title = "Pixel Brigade",
            logo = "PXL BRGD",
            tagline = "Tactical squads and crunchy handheld-era sprites.",
            heroSummary = "Advance across ruined districts, chain commander skills, and protect civilians in compact turn-based encounters.",
            genre = "Strategy",
            platformLabel = "GBA / NDS",
            accentRole = ThemeColorRole.SecondaryContainer,
            secondaryAccentRole = ThemeColorRole.Primary,
        ),
        RetroGame(
            id = "star-harbor",
            title = "Star Harbor",
            logo = "STAR HARBOR",
            tagline = "Dock, drift, and smuggle through a pastel frontier.",
            heroSummary = "Trade across orbital ports, meet rival crews, and upgrade your ship around a branching story map.",
            genre = "Adventure",
            platformLabel = "PSP / Vita",
            accentRole = ThemeColorRole.TertiaryContainer,
            secondaryAccentRole = ThemeColorRole.Secondary,
        ),
        RetroGame(
            id = "iron-howl",
            title = "Iron Howl",
            logo = "IRON HOWL",
            tagline = "Boss rush spectacle with steel and thunder.",
            heroSummary = "Climb a mechanical fortress, counter enormous war machines, and unlock alternate combat frames.",
            genre = "Boss Rush",
            platformLabel = "SNES / SAT",
            accentRole = ThemeColorRole.Tertiary,
            secondaryAccentRole = ThemeColorRole.Primary,
        ),
        RetroGame(
            id = "reef-odyssey",
            title = "Reef Odyssey",
            logo = "REEF",
            tagline = "An ocean expedition rendered in jewel-tone pixels.",
            heroSummary = "Dive between coral cities, decode ancient routes, and expand a modular submarine with rare tech.",
            genre = "Exploration",
            platformLabel = "GBC / GBA",
            accentRole = ThemeColorRole.Secondary,
            secondaryAccentRole = ThemeColorRole.PrimaryContainer,
        ),
        RetroGame(
            id = "dust-lancer",
            title = "Dust Lancer",
            logo = "DUST",
            tagline = "A desert run-and-gun with sharp silhouettes.",
            heroSummary = "Dash across scrapyards, intercept convoy bosses, and chain weapon pickups for score-chasing runs.",
            genre = "Run & Gun",
            platformLabel = "ARC / NEO",
            accentRole = ThemeColorRole.Primary,
            secondaryAccentRole = ThemeColorRole.Tertiary,
        ),
    )
}

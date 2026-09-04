package com.example.gridsurge.features.adventure.data

import com.example.gridsurge.R
import com.example.gridsurge.features.adventure.ui.dialogs.RelicSpec

object RelicCatalog {

    val SECTOR_01_RELIC = RelicSpec(
        id = "relic_sec01",
        sectorNumber = 1,
        name = "NEON REACTOR CORE",
        codeName = "CONDUIT // ALPHA",
        description = "High-output turquoise energy regulator extracted from Sector 01.",
        rewardTitle = "CONDUIT OPERATIVE",
        rewardBadgeRes = R.drawable.ic_medal_crystal_silver,
        rewardStars = 100,
        fragmentDrawables = listOf(
            R.drawable.relic_sec01_frag_0,
            R.drawable.relic_sec01_frag_1,
            R.drawable.relic_sec01_frag_2,
            R.drawable.relic_sec01_frag_3,
            R.drawable.relic_sec01_frag_4,
            R.drawable.relic_sec01_frag_5,
            R.drawable.relic_sec01_frag_6,
            R.drawable.relic_sec01_frag_7,
            R.drawable.relic_sec01_frag_8
        )
    )

    val SECTOR_02_RELIC = RelicSpec(
        id = "relic_sec02",
        sectorNumber = 2,
        name = "SOLAR PLASMA ENGINE",
        codeName = "HELIOS // CORE",
        description = "Thermonuclear fusion unit salvaged from the Solar Flare conduit.",
        rewardTitle = "SOLAR STRIDER",
        rewardBadgeRes = R.drawable.ic_medal_star_gold,
        rewardStars = 150,
        fragmentDrawables = listOf(
            R.drawable.relic_sec02_frag_0,
            R.drawable.relic_sec02_frag_1,
            R.drawable.relic_sec02_frag_2,
            R.drawable.relic_sec02_frag_3,
            R.drawable.relic_sec02_frag_4,
            R.drawable.relic_sec02_frag_5,
            R.drawable.relic_sec02_frag_6,
            R.drawable.relic_sec02_frag_7,
            R.drawable.relic_sec02_frag_8
        )
    )

    val SECTOR_03_RELIC = RelicSpec(
        id = "relic_sec03",
        sectorNumber = 3,
        name = "VOID ABYSSAL DRIVE",
        codeName = "PHOENIX // PROTOCOL",
        description = "Advanced gravitational regulator extracted from the Void Abyss.",
        rewardTitle = "VOID ARCHITECT",
        rewardBadgeRes = R.drawable.ic_medal_platinum,
        rewardStars = 200,
        fragmentDrawables = listOf(
            R.drawable.relic_sec03_frag_0,
            R.drawable.relic_sec03_frag_1,
            R.drawable.relic_sec03_frag_2,
            R.drawable.relic_sec03_frag_3,
            R.drawable.relic_sec03_frag_4,
            R.drawable.relic_sec03_frag_5,
            R.drawable.relic_sec03_frag_6,
            R.drawable.relic_sec03_frag_7,
            R.drawable.relic_sec03_frag_8
        )
    )

    val SECTOR_04_RELIC = RelicSpec(
        id = "relic_sec04",
        sectorNumber = 4,
        name = "QUANTUM CIPHER KEY",
        codeName = "SYNTHESIS // CORE",
        description = "Master cryptographic bypass unit retrieved from the Quantum Cipher matrix.",
        rewardTitle = "GRID OVERLORD",
        rewardBadgeRes = R.drawable.ic_rank_crest_gold,
        rewardStars = 300,
        fragmentDrawables = listOf(
            R.drawable.relic_sec04_frag_0,
            R.drawable.relic_sec04_frag_1,
            R.drawable.relic_sec04_frag_2,
            R.drawable.relic_sec04_frag_3,
            R.drawable.relic_sec04_frag_4,
            R.drawable.relic_sec04_frag_5,
            R.drawable.relic_sec04_frag_6,
            R.drawable.relic_sec04_frag_7,
            R.drawable.relic_sec04_frag_8
        )
    )

    val SECTOR_05_RELIC = RelicSpec(
        id = "relic_sec05",
        sectorNumber = 5,
        name = "QUANTUM VOID SINGULARITY",
        codeName = "EVENT // HORIZON",
        description = "Gravitational anomaly core stabilized from the Quantum Singularity.",
        rewardTitle = "EVENT HORIZON ELITE",
        rewardBadgeRes = R.drawable.ic_medal_platinum,
        rewardStars = 500,
        fragmentDrawables = listOf(
            R.drawable.relic_sec05_frag_0,
            R.drawable.relic_sec05_frag_1,
            R.drawable.relic_sec05_frag_2,
            R.drawable.relic_sec05_frag_3,
            R.drawable.relic_sec05_frag_4,
            R.drawable.relic_sec05_frag_5,
            R.drawable.relic_sec05_frag_6,
            R.drawable.relic_sec05_frag_7,
            R.drawable.relic_sec05_frag_8
        )
    )

    fun getRelicForSector(sectorNumber: Int): RelicSpec {
        return when (sectorNumber) {
            1 -> SECTOR_01_RELIC
            2 -> SECTOR_02_RELIC
            3 -> SECTOR_03_RELIC
            4 -> SECTOR_04_RELIC
            5 -> SECTOR_05_RELIC
            else -> SECTOR_01_RELIC
        }
    }
}

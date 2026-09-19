package com.example.gridsurge.career.data

import com.example.gridsurge.career.model.CareerDirective
import com.example.gridsurge.career.model.DirectiveCategory
import com.example.gridsurge.career.model.MilestoneStatus

object CareerCatalog {
    val DIRECTIVES = listOf(
        CareerDirective(
            id = "car_sorties_100",
            title = "CENTURION SORTIES",
            description = "Complete 100 tactical arena deployments.",
            category = DirectiveCategory.COMBAT,
            currentProgress = 0,
            targetProgress = 100,
            rewardStars = 200,
            status = MilestoneStatus.IN_PROGRESS
        ),
        CareerDirective(
            id = "car_score_50k",
            title = "HIGH-VOLTAGE APEX",
            description = "Achieve a single-run score exceeding 50,000 PTS.",
            category = DirectiveCategory.COMBAT,
            currentProgress = 0,
            targetProgress = 50000,
            rewardStars = 500,
            status = MilestoneStatus.IN_PROGRESS
        ),
        CareerDirective(
            id = "car_grid_clears_100",
            title = "PURGE MASTER",
            description = "Perform 100 total full-screen grid decontamination sweeps.",
            category = DirectiveCategory.TACTICAL,
            currentProgress = 0,
            targetProgress = 100,
            rewardStars = 300,
            status = MilestoneStatus.IN_PROGRESS
        ),
        CareerDirective(
            id = "car_combo_10x",
            title = "FEVER OVERLOAD",
            description = "Reach an active 10.0x Overdrive combo streak.",
            category = DirectiveCategory.TACTICAL,
            currentProgress = 0,
            targetProgress = 10,
            rewardStars = 400,
            status = MilestoneStatus.IN_PROGRESS
        )
    )
}

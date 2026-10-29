package io.github.fyy0619cell.deskpet.render

object Renderers {
    fun create(look: Look): PetRenderer = when (look.plan) {
        BodyPlan.QUADRUPED -> QuadrupedRenderer(look)
        BodyPlan.THEROPOD -> TheropodRenderer(look)
        BodyPlan.REPTILE -> ReptileRenderer(look)
        BodyPlan.BIRD -> BirdRenderer(look)
        BodyPlan.INSECT -> InsectRenderer(look)
        BodyPlan.ARTHROPOD -> ArthropodRenderer(look)
        BodyPlan.FISH -> FishRenderer(look)
        BodyPlan.MOLLUSC -> MolluscRenderer(look)
        BodyPlan.AMPHIBIAN -> AmphibianRenderer(look)
        BodyPlan.SERPENT -> SerpentRenderer(look)
    }
}

package io.github.fyy0619cell.deskpet.render

object Renderers {
    fun create(look: Look): PetRenderer = when (look.plan) {
        BodyPlan.QUADRUPED -> QuadrupedRenderer(look)
        BodyPlan.THEROPOD -> TheropodRenderer(look)
        BodyPlan.INSECT -> InsectRenderer(look)
    }
}

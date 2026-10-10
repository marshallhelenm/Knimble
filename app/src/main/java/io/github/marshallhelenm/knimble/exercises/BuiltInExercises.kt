package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier

// Stand-in content until the real exercises are written; it predates the current tier definitions, so some
// entries sit in a different tier than they would now.
// Keys are permanent: they tie each entry to its database row, so renaming one would reset its on/off switch.
object BuiltInExercises {
    data class Entry(val key: String, val tier: Tier, val name: String, val tag: String, val description: String)

    val all: List<Entry> = listOf(
        // Minor (stand-ins: seated).
        Entry(
            "wrist_extensor_stretch", Tier.SMALL, "Wrist extensor stretch", "Backs of the forearms",
            "Hold one arm out in front, palm down. With the other hand, gently bend the hand down at the wrist " +
                "until you feel a stretch along the top of the forearm. Hold for 15–20 seconds, then switch sides."
        ),
        Entry(
            "wrist_flexor_stretch", Tier.SMALL, "Wrist flexor stretch", "Insides of the forearms",
            "Hold one arm out in front, palm up. With the other hand, gently pull the fingers down and back " +
                "until you feel a stretch along the inside of the forearm. Hold for 15–20 seconds, then switch sides."
        ),
        Entry(
            "finger_fans", Tier.SMALL, "Finger fans", "Spread, then relax",
            "Make a soft fist, then open your hand and spread the fingers as wide as they'll comfortably go. " +
                "Hold for a few seconds and relax. Repeat 10 times with both hands."
        ),
        Entry(
            "tendon_glides", Tier.SMALL, "Tendon glides", "Five hand shapes",
            "Move slowly through five shapes: fingers straight, a hook fist (fingertips to the base of the fingers), " +
                "a full fist, a tabletop (knuckles bent at 90° with fingers straight), and a straight fist " +
                "(fingertips to the heel of the hand). Hold each for a few seconds. Repeat 5 times."
        ),
        Entry(
            "thumb_stretch", Tier.SMALL, "Thumb stretch", "For tired grip muscles",
            "Hold your hand out, palm facing sideways. With the other hand, gently ease the thumb back and away " +
                "from the fingers until you feel a stretch at its base. Hold for 15 seconds, then switch hands."
        ),
        Entry(
            "neck_side_tilt", Tier.SMALL, "Neck side tilt", "Ear toward shoulder",
            "Sit tall and let your shoulders drop. Slowly tilt your head to bring one ear toward its shoulder, " +
                "without lifting the shoulder. Hold for 15–20 seconds, breathe, then switch sides."
        ),
        Entry(
            "chin_tucks", Tier.SMALL, "Chin tucks", "Undo the craft hunch",
            "Sit tall and look straight ahead. Glide your chin straight back, making a gentle double chin, " +
                "without tipping your head up or down. Hold for 3–5 seconds, then release. Repeat 10 times."
        ),
        Entry(
            "shoulder_rolls", Tier.SMALL, "Shoulder rolls", "Loosen up the shoulders",
            "Lift both shoulders up toward your ears, roll them back, then let them drop down. " +
                "Make 10 slow circles backward, then 10 forward."
        ),
        Entry(
            "eye_break", Tier.SMALL, "20-20-20 eye break", "Look far away",
            "Look up from your work at something at least 20 feet (6 meters) away, like a window or across the room, " +
                "for at least 20 seconds. Blink a few times on purpose while you do."
        ),
        Entry(
            "seated_twist", Tier.SMALL, "Seated twist", "Gentle turn through the spine",
            "Sit tall with both feet flat. Turn your upper body to one side, resting a hand on the back of the chair " +
                "or your opposite knee. Hold for 15–20 seconds, breathing slowly, then turn to the other side."
        ),

        // Balanced (stand-ins: standing, no floor).
        Entry(
            "doorway_chest_stretch", Tier.MEDIUM, "Doorway chest stretch", "Open up the front",
            "Stand in a doorway with your forearms on the frame, elbows at about shoulder height. " +
                "Step one foot forward until you feel a stretch across your chest. Hold for 20–30 seconds."
        ),
        Entry(
            "standing_back_bend", Tier.MEDIUM, "Standing back bend", "Reverse the lean",
            "Stand with feet hip-width apart and your hands on your lower back. Gently lean back, " +
                "lifting your chest toward the ceiling, only as far as is comfortable. Hold for a few seconds. Repeat 5 times."
        ),
        Entry(
            "standing_side_bend", Tier.MEDIUM, "Standing side bend", "Lengthen your sides",
            "Stand tall and reach one arm overhead. Lean gently to the opposite side until you feel a stretch " +
                "along your side. Hold for 15–20 seconds, then switch."
        ),
        Entry(
            "calf_raises", Tier.MEDIUM, "Calf raises", "Wake up your legs",
            "Stand behind a chair or near a wall for balance. Rise up onto your toes, pause, then lower slowly. Repeat 15 times."
        ),
        Entry(
            "hip_circles", Tier.MEDIUM, "Hip circles", "Loosen stiff hips",
            "Stand with feet hip-width apart and hands on your hips. Make slow, wide circles with your hips, " +
                "10 in one direction and 10 in the other."
        ),
        Entry(
            "standing_quad_stretch", Tier.MEDIUM, "Standing quad stretch", "Fronts of the thighs",
            "Hold onto a chair or wall. Bend one knee and bring that heel toward your seat, holding the ankle " +
                "(or your sock or trouser leg) with your hand. Keep the knees close together. Hold for 20–30 seconds, then switch."
        ),
        Entry(
            "standing_hamstring_stretch", Tier.MEDIUM, "Standing hamstring stretch", "Backs of the legs",
            "Put one heel on a low step or stool with the leg straight. Keeping your back long, hinge forward " +
                "at the hips until you feel a stretch behind the thigh. Hold for 20–30 seconds, then switch."
        ),
        Entry(
            "arm_circles", Tier.MEDIUM, "Arm circles", "Get the blood moving",
            "Stand with arms out to the sides at shoulder height. Make small circles, gradually getting bigger, " +
                "for about 15 seconds. Then reverse direction."
        ),
        Entry(
            "wall_angels", Tier.MEDIUM, "Wall angels", "Shoulders and upper back",
            "Stand with your back against a wall, feet a little way out. Raise your arms into a goalpost shape " +
                "against the wall, then slowly slide them up and back down, keeping them as close to the wall as you can. Repeat 10 times."
        ),
        Entry(
            "walk_a_lap", Tier.MEDIUM, "Walk a lap", "Just move",
            "Get up and walk around your home for a couple of minutes. Fetch a glass of water while you're at it."
        ),

        // Involved (stand-ins: floor or whole body).
        Entry(
            "cat_cow", Tier.LARGE, "Cat-cow", "Move the whole spine",
            "On hands and knees, breathe in as you drop your belly and lift your head and tailbone. " +
                "Breathe out as you round your back toward the ceiling and tuck your chin. Flow between the two 10 times."
        ),
        Entry(
            "childs_pose", Tier.LARGE, "Child's pose", "Rest and lengthen",
            "Kneel, bring your big toes together and your knees apart, then sit back toward your heels " +
                "and walk your hands forward until your forehead rests on the floor. Breathe slowly for 30–60 seconds."
        ),
        Entry(
            "thread_the_needle", Tier.LARGE, "Thread the needle", "Upper back twist",
            "On hands and knees, slide one arm under your body along the floor, palm up, letting that shoulder " +
                "and the side of your head come down to rest. Hold for 20–30 seconds, then switch sides."
        ),
        Entry(
            "downward_dog", Tier.LARGE, "Downward dog", "Full-body stretch",
            "From hands and knees, tuck your toes and lift your hips up and back into an upside-down V. " +
                "Keep a soft bend in the knees if you need to. Hold for 5 slow breaths."
        ),
        Entry(
            "knees_to_chest", Tier.LARGE, "Knees to chest", "Ease the lower back",
            "Lie on your back and hug both knees toward your chest. Rock gently side to side if that feels good. " +
                "Hold for 30 seconds."
        ),
        Entry(
            "lying_twist", Tier.LARGE, "Lying twist", "Release through the spine",
            "Lie on your back with your knees bent and arms out to the sides. Let both knees fall to one side " +
                "while your shoulders stay down. Hold for 20–30 seconds, then switch sides."
        ),
        Entry(
            "figure_four", Tier.LARGE, "Figure four", "Hips and glutes",
            "Lie on your back with knees bent. Cross one ankle over the opposite knee, then pull that bottom thigh " +
                "toward your chest until you feel a stretch in the hip. Hold for 20–30 seconds, then switch."
        ),
        Entry(
            "glute_bridge", Tier.LARGE, "Glute bridge", "Wake up the back of the body",
            "Lie on your back with knees bent and feet flat. Press through your heels to lift your hips until " +
                "your body makes a straight line from shoulders to knees. Pause, then lower slowly. Repeat 10 times."
        ),
        Entry(
            "sphinx_pose", Tier.LARGE, "Sphinx pose", "Gentle backbend",
            "Lie on your front and prop yourself up on your forearms, elbows under your shoulders. " +
                "Let your lower back relax and your chest lift. Hold for 30–60 seconds."
        ),
        Entry(
            "bodyweight_squats", Tier.LARGE, "Squats", "Get the heart going",
            "Stand with feet shoulder-width apart. Sit your hips back and down as if into a chair, keeping your chest up, " +
                "then stand back up. Use a chair behind you as a target if you like. Repeat 10 times."
        )
    )

    fun asExercises(): List<Exercise> = all.map {
        Exercise(
            name = it.name,
            tag = it.tag,
            description = it.description,
            tier = it.tier,
            source = ExerciseSource.BUILT_IN,
            builtInKey = it.key
        )
    }
}

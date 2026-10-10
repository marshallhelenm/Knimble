package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier

// Keys are permanent: they tie each entry to its database row, so renaming one would reset its on/off switch.
// Credited entries are adapted, in our own words, from the pages in docs/EXERCISE_SOURCES.md. Uncredited ones
// are stand-ins until the real exercises are written; they predate the current tier definitions, so some sit
// in a different tier than they would now.
object BuiltInExercises {
    data class Entry(
        val key: String,
        val tier: Tier,
        val name: String,
        val tag: String,
        val description: String,
        val credits: List<Credit> = emptyList()
    )

    private val WEB_MD = Credit("WebMD", "https://www.webmd.com/pain-management/carpal-tunnel/best-exercises-carpal-tunnel")
    private val HOME_PHYSIO = Credit(
        "Home Physiotherapy Exercises",
        "https://homephysiotherapyexercises.com/exercises-for-carpal-tunnel-syndrome/"
    )
    private val WELLNESS_52 = Credit("Wellness52", "https://wellness52.com/neck-and-shoulder-stretches/")
    private val KAISER = Credit(
        "Kaiser Permanente",
        "https://healthy.kaiserpermanente.org/health-wellness/health-encyclopedia/he.neck-exercises.ad1500"
    )

    val all: List<Entry> = listOf(
        // Minor: sourced hand and wrist exercises.
        Entry(
            "wrist_rotations", Tier.SMALL, "Wrist rotations", "Loosen stiff wrists",
            "Keep your forearms still and move only your hands: point them up, then down, then side to side. " +
                "Go slowly. Make 4 rounds.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "wrist_flexor_stretch", Tier.SMALL, "Wrist flexor stretch", "Insides of the forearms",
            "Hold one arm out in front, palm up, with the elbow soft rather than locked. Let the hand drop back " +
                "toward the floor, then use your other hand to ease it a little further until you feel a stretch " +
                "along the inside of the forearm. Hold for 15–30 seconds. Do it 2–4 times on each side.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "wrist_extensor_stretch", Tier.SMALL, "Wrist extensor stretch", "Backs of the forearms",
            "Hold one arm out in front, palm down, with the elbow soft rather than locked. Let the fingers drop " +
                "toward the floor, then use your other hand to ease the hand a little further until you feel a " +
                "stretch along the back of the forearm. Hold for 15–30 seconds. Do it 2–4 times on each side.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "prayer_stretch", Tier.SMALL, "Prayer stretch", "Palms together, wrists open",
            "Press your palms together in front of your chest, just below your chin. Keeping them pressed together, " +
                "slowly lower your hands toward your waist until you feel a moderate stretch in your wrists. " +
                "Hold for up to 30 seconds. Repeat 2–4 times.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "tendon_glides", Tier.SMALL, "Tendon glides", "A slow run of hand shapes",
            "With your elbow bent and your hand up, hold each shape for 3 seconds: fingers straight; a hook " +
                "(bend the top two knuckles, big knuckles straight); a full fist. Then straighten again, bend only at " +
                "the big knuckles so your hand makes an L, and finally fold the fingers down until the tips touch " +
                "your palm. Repeat 5–10 times.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "median_nerve_glide", Tier.SMALL, "Median nerve glide", "Gentle movement for the hand's main nerve",
            "Move through these positions, holding each for 3–7 seconds: a fist with your thumb outside the fingers; " +
                "fingers straight with the thumb alongside; wrist bent back with the fingers still straight; then " +
                "the thumb stretched out to the side. Finish by gently easing the thumb back with your other hand. " +
                "Keep it light. Repeat a few times.",
            listOf(HOME_PHYSIO)
        ),
        Entry(
            "fist_to_stop_sign", Tier.SMALL, "Fist to stop sign", "Close, then open wide",
            "Hold your arms out in front. Make a fist, then open your hand with the fingers pointing up and the " +
                "palm facing out, as if signalling stop. Repeat 5–10 times.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "finger_spreads", Tier.SMALL, "Finger spreads", "Spread, then relax",
            "Spread your fingers as wide as they'll comfortably go, hold for a moment, then let them relax. " +
                "Repeat 4 times.",
            listOf(WEB_MD)
        ),
        Entry(
            "thumb_stretch", Tier.SMALL, "Thumb stretch", "For tired grip muscles",
            "With your other hand, gently ease your thumb backward until you feel a mild stretch at its base. " +
                "Hold briefly, then release. Repeat 4 times on each hand.",
            listOf(WEB_MD)
        ),
        Entry(
            "thumb_touches", Tier.SMALL, "Thumb touches", "Make an O with each finger",
            "Touch the tip of your thumb to the tip of each finger in turn, making an O shape each time. " +
                "Go from your index finger to your little finger and back. Repeat a few times on each hand.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "table_wrist_bends", Tier.SMALL, "Table wrist bends", "Forearm on the table",
            "Rest your forearm on a table with your hand over the edge, palm up. From a relaxed straight position, " +
                "bend your hand up toward you and hold for 5 seconds, come back to straight, then bend it down away " +
                "from you and hold for 5 seconds. Repeat 10 times.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "wrist_lift", Tier.SMALL, "Wrist lift", "Push against your other hand",
            "Rest your forearm on a table, palm down, with your hand at the edge. Lay your other hand across the " +
                "knuckles. Try to lift the bottom hand while the top hand presses it down. Hold for 5 seconds. " +
                "Repeat 10 times on each side.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "hand_squeeze", Tier.SMALL, "Hand squeeze", "Grab a ball of yarn",
            "Squeeze a soft ball of yarn, a pair of rolled-up socks or a soft rubber ball. Hold the squeeze for " +
                "5 seconds, then let go slowly. Repeat 10 times with each hand.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "wrist_curls", Tier.SMALL, "Wrist curls", "Borrow a can from the cupboard",
            "Hold something light, like a can of beans, palm down, with your elbow at your side and your forearm " +
                "level. Slowly let your hand drop toward the floor, then lift it back up to level. " +
                "Repeat 10 times with each hand.",
            listOf(WEB_MD, HOME_PHYSIO)
        ),
        Entry(
            "shake_it_out", Tier.SMALL, "Shake it out", "Loose and floppy",
            "Let your hands hang loosely and shake them out, like flicking water off your fingers, for 10–15 " +
                "seconds. Especially good when your hands feel stiff or numb.",
            listOf(WEB_MD)
        ),

        // Minor: sourced neck and shoulder exercises.
        Entry(
            "neck_side_tilt", Tier.SMALL, "Neck side tilt", "Ear toward shoulder",
            "Sit tall and keep your shoulders down. Tilt your head to bring one ear toward its shoulder, letting " +
                "the weight of your head do the stretching. For a little more, rest the hand on that side on your " +
                "head and add the lightest pressure. Hold for 15–30 seconds, come back to centre, then switch. " +
                "Do it 2–4 times on each side.",
            listOf(WELLNESS_52, KAISER)
        ),
        Entry(
            "diagonal_neck_stretch", Tier.SMALL, "Diagonal neck stretch", "Nose toward your armpit",
            "Turn your head slightly to one side, then tip it down on the diagonal, as if bringing your nose toward " +
                "that armpit. Let your head's weight do the work, or add gentle pressure with the hand on that side. " +
                "Hold for 15–30 seconds. Do it 2–4 times, then switch sides.",
            listOf(WELLNESS_52, KAISER)
        ),
        Entry(
            "chin_to_chest", Tier.SMALL, "Chin to chest", "Lengthen the back of your neck",
            "Sit tall and clasp your hands behind your head. Let them gently draw your head forward so your chin " +
                "moves toward your chest. Stop at a mild stretch. Hold for 10 seconds. Repeat up to 10 times.",
            listOf(WELLNESS_52)
        ),
        Entry(
            "chin_tucks", Tier.SMALL, "Chin tucks", "Undo the craft hunch",
            "Sit tall and look straight ahead. Tuck your chin and slide your head straight back over your shoulders, " +
                "without tipping it up or down. Hold for a count of 6, then relax for up to 10 seconds. " +
                "Repeat 2–4 times.",
            listOf(KAISER)
        ),
        Entry(
            "neck_presses", Tier.SMALL, "Neck presses", "Push without moving",
            "Keep your head still and your eyes forward the whole time. Press your forehead into the heels of your " +
                "hands; then, hands clasped behind your head, press back into them; then press the side of your head " +
                "into one hand, and then the other. Push firmly but nowhere near your hardest, holding each for " +
                "about 6 seconds. Repeat each a few times.",
            listOf(KAISER)
        ),
        Entry(
            "shoulder_rolls", Tier.SMALL, "Shoulder rolls", "Loosen up the shoulders",
            "Draw your shoulders back, up toward your ears, forward and down, in one slow, smooth circle. " +
                "Make 10 circles, then 10 the other way.",
            listOf(WELLNESS_52)
        ),

        // Minor (stand-ins: seated).
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

        // Balanced: sourced shoulder and upper-back stretches.
        Entry(
            "behind_back_neck_stretch", Tier.MEDIUM, "Behind-the-back neck stretch", "Neck and top of the shoulder",
            "Sit tall. Reach one arm behind your back and hold that wrist with your other hand. Gently tilt your ear " +
                "toward the shoulder of the hand doing the holding. Hold for 10 seconds, then switch. " +
                "Repeat up to 10 times on each side.",
            listOf(WELLNESS_52, KAISER)
        ),
        Entry(
            "crossover_shoulder_stretch", Tier.MEDIUM, "Crossover shoulder stretch", "Back of the shoulder",
            "Reach one arm across your body at chest height, then use your other arm to gently draw it a little " +
                "further across. Hold for 10 seconds, then switch. Repeat up to 10 times on each side.",
            listOf(WELLNESS_52)
        ),
        Entry(
            "back_scratch_stretch", Tier.MEDIUM, "Back scratch stretch", "Hands meet behind your back",
            "Reach one hand over your shoulder and down your back, and the other hand up from your lower back. " +
                "Try to bring your fingers together, or clasp them if you can. Hold for 30 seconds, then switch arms. " +
                "Do it 4 times each way. It's normal for one side to be harder than the other.",
            listOf(WELLNESS_52)
        ),
        Entry(
            "chest_and_shoulder_stretch", Tier.MEDIUM, "Chest and shoulder stretch", "Squeeze the shoulder blades",
            "Start with a chin tuck. Raise your arms in a goalpost shape, palms forward and elbows out to the sides. " +
                "Keeping your elbows back and hands up, slowly lower your arms while drawing your shoulder blades " +
                "down and together. Hold for about 6 seconds, then relax. Repeat 8–12 times. Doing it with your back " +
                "against a wall makes it harder.",
            listOf(KAISER)
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
            "walk_a_lap", Tier.MEDIUM, "Walk a lap", "Just move",
            "Get up and walk around your home for a couple of minutes. Fetch a glass of water while you're at it."
        ),

        // Involved: sourced.
        Entry(
            "wall_shoulder_stretch", Tier.LARGE, "Wall shoulder stretch", "Front of the shoulder",
            "Stand beside a wall and put one hand flat on it, arm straight, at about shoulder height. Slowly turn " +
                "your body away from the wall until you feel a stretch across the front of that shoulder, going only " +
                "as far as is comfortable. Hold for 10 seconds, then switch. Repeat up to 10 times on each side.",
            listOf(WELLNESS_52)
        ),
        Entry(
            "behind_back_hand_clasp", Tier.LARGE, "Behind-the-back hand clasp", "Open up the chest",
            "Stand tall, reach both hands behind your back and interlace your fingers. Gently press your hands back " +
                "and away from you. Hold for 30 seconds. Repeat 4 times. If your hands won't meet yet, don't force " +
                "it; it gets easier.",
            listOf(WELLNESS_52)
        ),
        Entry(
            "thread_the_needle", Tier.LARGE, "Thread the needle", "Upper back twist",
            "Kneel on hands and knees, with something soft under your knees. Slide one arm under your body toward " +
                "the other side, palm up, and let that shoulder sink toward the floor. Hold for 30 seconds, then " +
                "switch. Do it 4 times on each side.",
            listOf(WELLNESS_52)
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
            builtInKey = it.key,
            credits = it.credits
        )
    }
}

package io.github.marshallhelenm.knimble.exercises

import io.github.marshallhelenm.knimble.model.BodyArea
import io.github.marshallhelenm.knimble.model.BodyArea.BACK
import io.github.marshallhelenm.knimble.model.BodyArea.EYES
import io.github.marshallhelenm.knimble.model.BodyArea.HANDS_WRISTS
import io.github.marshallhelenm.knimble.model.BodyArea.HIPS_LEGS
import io.github.marshallhelenm.knimble.model.BodyArea.NECK_SHOULDERS
import io.github.marshallhelenm.knimble.model.BodyArea.WHOLE_BODY
import io.github.marshallhelenm.knimble.model.Credit
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier

// Keys are permanent: they tie each entry to its database row, so renaming one would reset its on/off switch.
// Every entry is credited: adapted, in our own words, from the sources in docs/EXERCISE_SOURCES.md.
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
    // From the user's own doctor; no public page, and the doctor isn't named.
    private val DOCTORS_SHEET = Credit("a doctor's exercise sheet")
    private val VISUAL_AIDS = Credit(
        "Visual Aids Centre",
        "https://www.visualaidscentre.com/eye-exercises-to-improve-vision-fast/"
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

        // Minor: eye exercises from a doctor's sheet and Visual Aids Centre.
        Entry(
            "eye_rotations", Tier.SMALL, "Open eye rotations", "Look to every edge",
            "Sit or stand, steady but relaxed. Keeping your head still, look as far to the right as you comfortably " +
                "can and hold for a few seconds, letting your eyes ease a little further without straining. Then do " +
                "the same looking left, up, down, upper right, upper left, lower right and lower left.",
            listOf(DOCTORS_SHEET, VISUAL_AIDS)
        ),
        Entry(
            "eye_head_rotations", Tier.SMALL, "Eye-head rotations", "Keep one thing in focus",
            "Sit or stand relaxed and pick something across the room to look at. Keeping it in focus, slowly move " +
                "your head in circles, then side to side, then on the diagonals.",
            listOf(DOCTORS_SHEET)
        ),
        Entry(
            "head_swings", Tier.SMALL, "Head swings", "Look behind you",
            "Sit or stand with your hands behind your back. Turn your head to the right and try to look behind you. " +
                "Then look to the upper right and the lower right, holding each for a few seconds and letting your " +
                "gaze reach a little further. Repeat on the left. Finish by holding your head straight and looking " +
                "down toward your toes.",
            listOf(DOCTORS_SHEET)
        ),

        Entry(
            "eye_break", Tier.SMALL, "20-20-20 eye break", "Look far away",
            "Look up from your work at something at least 20 feet (6 meters) away, like a window or across the room, " +
                "for at least 20 seconds. Blink a few times on purpose while you do.",
            listOf(VISUAL_AIDS)
        ),
        Entry(
            "palming", Tier.SMALL, "Palming", "Warm, dark rest",
            "Rub your palms together until they feel warm. Close your eyes and cup your palms over them, resting " +
                "lightly without pressing on your eyes. Breathe slowly for 30–60 seconds.",
            listOf(VISUAL_AIDS)
        ),
        Entry(
            "conscious_blinking", Tier.SMALL, "Slow blinks", "Close all the way",
            "Close your eyes fully, pause for 2 seconds, then open them. Do 10 slow, complete blinks. Close work " +
                "makes us blink less, so this is worth doing often.",
            listOf(VISUAL_AIDS)
        ),
        Entry(
            "near_far_focus", Tier.SMALL, "Near and far", "Thumb, then the window",
            "Hold your thumb up about a hand's length (15 cm) from your face and focus on it for 5 seconds. Then " +
                "focus on something about 20 feet (6 meters) away for 5 seconds. Switch back and forth 10 times. " +
                "Keep your glasses on if you wear them.",
            listOf(VISUAL_AIDS)
        ),
        Entry(
            "eye_rolling", Tier.SMALL, "Eye rolls", "Slow, full circles",
            "Look up, then slowly roll your eyes around in a full circle. Do 5 circles one way, then 5 the other. " +
                "Keep it slow and smooth.",
            listOf(VISUAL_AIDS)
        ),
        Entry(
            "figure_eight_tracking", Tier.SMALL, "Figure-eight tracking", "Trace an imaginary eight",
            "Pick a spot about 10 feet (3 meters) away and imagine a large figure eight lying on its side there. " +
                "Trace it slowly with your eyes for 30 seconds, then go the other way for 30 seconds.",
            listOf(VISUAL_AIDS)
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
        )
    )

    // Kept apart from the entries so the whole mapping can be read, and changed, in one place.
    private val AREAS: Map<String, Set<BodyArea>> = mapOf(
        "wrist_rotations" to setOf(HANDS_WRISTS),
        "wrist_flexor_stretch" to setOf(HANDS_WRISTS),
        "wrist_extensor_stretch" to setOf(HANDS_WRISTS),
        "prayer_stretch" to setOf(HANDS_WRISTS),
        "tendon_glides" to setOf(HANDS_WRISTS),
        "median_nerve_glide" to setOf(HANDS_WRISTS),
        "fist_to_stop_sign" to setOf(HANDS_WRISTS),
        "finger_spreads" to setOf(HANDS_WRISTS),
        "thumb_stretch" to setOf(HANDS_WRISTS),
        "thumb_touches" to setOf(HANDS_WRISTS),
        "table_wrist_bends" to setOf(HANDS_WRISTS),
        "wrist_lift" to setOf(HANDS_WRISTS),
        "hand_squeeze" to setOf(HANDS_WRISTS),
        "wrist_curls" to setOf(HANDS_WRISTS),
        "shake_it_out" to setOf(HANDS_WRISTS),

        "neck_side_tilt" to setOf(NECK_SHOULDERS),
        "diagonal_neck_stretch" to setOf(NECK_SHOULDERS),
        "chin_to_chest" to setOf(NECK_SHOULDERS),
        "chin_tucks" to setOf(NECK_SHOULDERS),
        "neck_presses" to setOf(NECK_SHOULDERS),
        "shoulder_rolls" to setOf(NECK_SHOULDERS),
        "behind_back_neck_stretch" to setOf(NECK_SHOULDERS),
        "crossover_shoulder_stretch" to setOf(NECK_SHOULDERS),
        "back_scratch_stretch" to setOf(NECK_SHOULDERS),
        "chest_and_shoulder_stretch" to setOf(NECK_SHOULDERS, BACK),
        "wall_shoulder_stretch" to setOf(NECK_SHOULDERS),
        "behind_back_hand_clasp" to setOf(NECK_SHOULDERS),
        "thread_the_needle" to setOf(NECK_SHOULDERS, BACK),

        "eye_break" to setOf(EYES),
        "eye_rotations" to setOf(EYES),
        "eye_head_rotations" to setOf(EYES, NECK_SHOULDERS),
        "head_swings" to setOf(EYES, NECK_SHOULDERS),
        "palming" to setOf(EYES),
        "conscious_blinking" to setOf(EYES),
        "near_far_focus" to setOf(EYES),
        "eye_rolling" to setOf(EYES),
        "figure_eight_tracking" to setOf(EYES)
    )

    fun areasFor(key: String): Set<BodyArea> = AREAS[key].orEmpty()

    fun asExercises(): List<Exercise> = all.map {
        Exercise(
            name = it.name,
            tag = it.tag,
            description = it.description,
            tier = it.tier,
            source = ExerciseSource.BUILT_IN,
            builtInKey = it.key,
            credits = it.credits,
            areas = areasFor(it.key)
        )
    }
}

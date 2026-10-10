package io.github.marshallhelenm.knimble.timer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.marshallhelenm.knimble.data.local.KnimbleDatabase
import io.github.marshallhelenm.knimble.model.Exercise
import io.github.marshallhelenm.knimble.model.ExerciseSource
import io.github.marshallhelenm.knimble.model.Tier
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class BreakTimerTest {
    private lateinit var db: KnimbleDatabase
    private val alarms = FakeAlarms()
    private val clock = MutableClock(1_000_000_000L)
    private lateinit var timer: BreakTimer
    private var smallIds = listOf<Long>()
    private var largeId = 0L

    @Before
    fun setUp() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            KnimbleDatabase::class.java
        ).build()
        timer = BreakTimer(db, alarms, clock, Random(7))
        val dao = db.exerciseDao()
        fun exercise(name: String, tier: Tier) =
            Exercise(name = name, tag = "t", description = "d", tier = tier, source = ExerciseSource.BUILT_IN)
        smallIds = listOf(dao.insert(exercise("Wrists", Tier.SMALL)), dao.insert(exercise("Neck", Tier.SMALL)))
        largeId = dao.insert(exercise("Cat-cow", Tier.LARGE))
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun start_armsTheEndOfTheTimer() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))

        val expected = clock.millis + 25 * 60_000
        assertEquals(expected, timer.current().endsAt)
        assertEquals(expected, alarms.armedAt)
    }

    @Test
    fun timerEnds_ringsWithAnExerciseFromTheChosenTiers_andArmsARepeatAMinuteLater() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)

        val alert = timer.onAlarm()

        assertNotNull(alert)
        assertTrue(alert!!.exercise!!.id in smallIds)
        val state = timer.current()
        assertTrue(state.ringing)
        assertNull(state.endsAt)
        assertEquals(alert.exercise!!.id, state.breakExerciseId)
        assertEquals(clock.millis + BreakTimer.REPEAT_MILLIS, alarms.armedAt)
    }

    @Test
    fun whileRinging_eachAlarmRepeatsTheSameExercise() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        val first = timer.onAlarm()!!.exercise!!.id

        repeat(3) {
            clock.advanceMinutes(1)
            assertEquals(first, timer.onAlarm()!!.exercise!!.id)
            assertEquals(clock.millis + BreakTimer.REPEAT_MILLIS, alarms.armedAt)
        }
    }

    @Test
    fun stopRinging_endsTheRepeatsButKeepsTheExercise() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        val exerciseId = timer.onAlarm()!!.exercise!!.id

        timer.stopRinging()

        val state = timer.current()
        assertFalse(state.ringing)
        assertEquals(exerciseId, state.breakExerciseId)
        assertNull(alarms.armedAt)
    }

    @Test
    fun alarmBeforeTheTimerEnds_doesNothingAndRearmsTheEnd() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(10)

        assertNull(timer.onAlarm())
        assertFalse(timer.current().ringing)
        assertEquals(clock.millis + 15 * 60_000, alarms.armedAt)
    }

    @Test
    fun alarmAfterTheBreakWasStopped_doesNotRingAgain() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        timer.onAlarm()
        timer.stopRinging()

        assertNull(timer.onAlarm())
        assertNull(alarms.armedAt)
    }

    @Test
    fun nextBreak_avoidsTheLastExercise() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        val first = timer.onAlarm()!!.exercise!!.id
        timer.stopRinging()

        timer.start(25, setOf(Tier.SMALL))
        assertNull(timer.current().breakExerciseId)
        clock.advanceMinutes(25)

        assertNotEquals(first, timer.onAlarm()!!.exercise!!.id)
    }

    @Test
    fun everythingInTheTiersSwitchedOff_stillRingsWithoutAnExercise() = runBlocking {
        db.exerciseDao().setEnabled(largeId, false)
        timer.start(25, setOf(Tier.LARGE))
        clock.advanceMinutes(25)

        val alert = timer.onAlarm()

        assertNotNull(alert)
        assertNull(alert!!.exercise)
        assertTrue(timer.current().ringing)
    }

    @Test
    fun cancel_clearsTheTimerAndAlarm() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))

        timer.cancel()

        assertNull(timer.current().endsAt)
        assertNull(alarms.armedAt)
    }

    @Test
    fun rearm_afterRebootPastTheEnd_firesRightAway() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        alarms.cancel()
        clock.advanceMinutes(40)

        timer.rearm()

        assertEquals(clock.millis, alarms.armedAt)
    }

    @Test
    fun rearm_withTimerStillRunning_armsItsEnd() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        val endsAt = timer.current().endsAt
        alarms.cancel()
        clock.advanceMinutes(5)

        timer.rearm()

        assertEquals(endsAt, alarms.armedAt)
    }

    @Test
    fun start_remembersDurationAndTiersForNextTime() = runBlocking {
        timer.start(45, setOf(Tier.MEDIUM, Tier.LARGE))
        timer.cancel()

        val state = timer.current()
        assertEquals(45, state.durationMinutes)
        assertEquals(setOf(Tier.MEDIUM, Tier.LARGE), state.tiers)
    }

    @Test
    fun timerEnds_putsTheUserOnABreakThatOutlastsTheRinging() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        timer.onAlarm()
        timer.stopRinging()

        assertTrue(timer.current().onBreak)
    }

    @Test
    fun pickAnother_swapsForADifferentExerciseInTheSameTiers() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        val first = timer.onAlarm()!!.exercise!!.id

        timer.pickAnother()

        val state = timer.current()
        assertNotEquals(first, state.breakExerciseId)
        assertTrue(state.breakExerciseId in smallIds)
        assertEquals(state.breakExerciseId, state.lastExerciseId)
    }

    @Test
    fun pickAnother_onlyOneChoice_keepsIt() = runBlocking {
        timer.start(25, setOf(Tier.LARGE))
        clock.advanceMinutes(25)
        timer.onAlarm()

        timer.pickAnother()

        assertEquals(largeId, timer.current().breakExerciseId)
    }

    @Test
    fun finishBreak_endsTheBreakAndAnyRinging() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        val exerciseId = timer.onAlarm()!!.exercise!!.id

        timer.finishBreak()

        val state = timer.current()
        assertFalse(state.onBreak)
        assertFalse(state.ringing)
        assertNull(state.breakExerciseId)
        assertEquals(exerciseId, state.lastExerciseId)
        assertNull(alarms.armedAt)
    }

    @Test
    fun start_duringABreak_endsIt() = runBlocking {
        timer.start(25, setOf(Tier.SMALL))
        clock.advanceMinutes(25)
        timer.onAlarm()

        timer.start(25, setOf(Tier.SMALL))

        val state = timer.current()
        assertFalse(state.onBreak)
        assertFalse(state.ringing)
        assertNull(state.breakExerciseId)
    }
}

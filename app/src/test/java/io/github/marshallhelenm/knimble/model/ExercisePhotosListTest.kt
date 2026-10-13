package io.github.marshallhelenm.knimble.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExercisePhotosListTest {
    private val exercise = Exercise(name = "Shrugs", tag = "", description = "", tier = Tier.SMALL, source = ExerciseSource.USER)

    @Test
    fun noPhotos_isAnEmptyList() {
        assertEquals(emptyList<String>(), exercise.photos)
    }

    @Test
    fun aSinglePhotoFromBeforeMultiplePhotos_readsAsAOnePhotoList() {
        assertEquals(listOf("file:///a.jpg"), exercise.copy(imageUri = "file:///a.jpg").photos)
    }

    @Test
    fun withPhotos_roundTripsInOrder() {
        val photos = listOf("file:///b.jpg", "file:///a.jpg", "file:///c.jpg")

        assertEquals(photos, exercise.withPhotos(photos).photos)
    }

    @Test
    fun withPhotos_empty_clearsTheColumn() {
        assertNull(exercise.copy(imageUri = "file:///a.jpg").withPhotos(emptyList()).imageUri)
    }
}

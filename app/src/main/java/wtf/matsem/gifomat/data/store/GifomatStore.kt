package wtf.matsem.gifomat.data.store

import wtf.matsem.gifomat.data.model.ImageSequence

class GifomatStore {

	private val imgSequences = mutableListOf<ImageSequence>()

	fun getSequences(): List<ImageSequence> {
		return imgSequences
	}

	fun addSequence(sequence: ImageSequence) {
		imgSequences.add(0, sequence)
	}

	fun wipe() {
		imgSequences.clear()
	}
}
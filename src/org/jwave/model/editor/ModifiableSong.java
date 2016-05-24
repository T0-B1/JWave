package org.jwave.model.editor;

import java.util.List;

import org.jwave.model.player.Song;

/**
 * This interface models the concept of a modifiable song, extending the
 * interface of a normal song. A modifiable song is one which can be split
 * into separate pieces, which then may be rearranged in order to create a
 * new song, based on the original.
 * 
 * All "ms" acronyms stand for milliseconds.
 *
 */
public interface ModifiableSong extends Song {
    /**
     * Gets the length of the original, non-modified song.
     *          
     * @return
     * 			the length (in ms) of the original song.
     */	
	int getLength();
	
    /**
     * Gets the length of the modified song.
     *          
     * @return
     * 			the length (in ms) of the modified song.
     */	
	int getModifiedLength();

    /**
     * Pastes the given segment of the modified song into a given position.
     *          
     * @param from
     * 			beginning (in ms) of segment to paste.
     * 
     * @param to
     * 			end (in ms) of segment to paste.
     * 
     * @param at
     * 			position (in ms) where to paste the from-to segment, shifting
     * 			all later segments down by the length of the from-to segment.
     */	
	void pasteSelectionAt(int from, int to, int at);
	
    /**
     * Cuts the given segment of the modified song, deleting it.
     *          
     * @param from
     * 			beginning (in ms) of segment to remove.
     * 
     * @param to
     * 			end (in ms) of segment to remove.
     */	
	void deleteSelection(int from, int to);
	
    /**
     * Returns a list of values that represent the waveform of the currently
     * loaded, and possibly modified, song. This method allows the caller to
     * specify what segment of the song to get the waveform of, and the amount
     * of samples to divide that segment into.
     * 
     * The values returned are returned in groups of 8 values for each sample:
     * 4 values for each audio channel (the left and right channel),
     * of these 2 represent the maximum and minimum amplitude for that sample
     * or group of samples, and 2 represent the root mean square of the sample
     * or group of samples.
     * 
     * All values are normalized in a -1 to 1 range.
     * 
     * @param from
     * 			from what position (in ms) to get the waveform.
     * @param to
     * 			to what position (in ms) to get the waveform.
     * @param samples
     * 			number of sets of values to retrieve for the asked for interval.
     * @return
     * 			a list of numbers representing the waveform of the modified song,
     * 			in sets of 8.
     */
	List<Float> getWaveform(int from, int to, int samples);	
	
    /**
     * Temporary debug method for printing a text representation of all the
     * current cuts and segments of a modified song.
     * 
     */
	void printAllCuts();	
}
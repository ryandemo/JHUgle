/*
Ryan Demo
rdemo1
600.226.01
P2
*/

import java.util.AbstractMap;

/** This class is used to create pairs that can be used as entries
 *  in a map, with a tombstone field for open addressing usage.
 * 
 *  @author CS226 staff, Spring 2016
 *  @param <K> the data type of the key field
 *  @param <V> the data type of the value
 */

public class LPMapEntry<K, V> extends AbstractMap.SimpleEntry<K, V> {

    /** Tombstone indicator; true if entry has been inactivated. **/
    private boolean tombstone;

    /** Create a map entry.
     *  @param k the key of the entry
     *  @param v the value in the entry
     */
    public LPMapEntry(K k, V v) {
        super(k, v);
        if (k == null) {
            this.tombstone = true;
        } else {
            this.tombstone = false;
        }
    }
       
    /** Find out if this entry is a tombstone marker.
     *  @return true if it is, false otherwise
     */
    public boolean isTombstone() {
        return this.tombstone;
    }

    /** Make this entry a tombstone marker if it isn't already.
     *  @throws IllegalStateException if the entry is already a tombstone
     */
    public void makeTombstone() throws IllegalStateException {
        if (!this.tombstone) {
            this.tombstone = true;
        } else {
            throw new IllegalStateException("can't kill a ghost");
        }
    }


    /** Check if two entries are the same.
     *  @param o the entry with which to compare this
     *  @return true if same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        LPMapEntry other = null;
        try {
            other = (LPMapEntry) o;
        } catch (ClassCastException e) {
            return false;
        }
        return super.equals(o) && this.tombstone == other.tombstone;
    }

    /** Create a string representation of the entry.
     *  @return the string
     */
    @Override
    public String toString() {
        if (!this.tombstone) {
            return super.toString();
        } else {
            return "|/=\\|";   // tombstone indicator
        }
    }

    /** Compute and return a hashcode based on both key and value.
     *  @return the hash code value, or 0 if tombstone
     */
    public int hashCode() {
        if (this.tombstone) {
            return 0;
        } else {
            return super.hashCode();
        }
    }

}
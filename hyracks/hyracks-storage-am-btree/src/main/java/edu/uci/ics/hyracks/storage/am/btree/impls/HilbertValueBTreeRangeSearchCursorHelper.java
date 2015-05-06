package edu.uci.ics.hyracks.storage.am.btree.impls;

public class HilbertValueBTreeRangeSearchCursorHelper {
    /*
     * This method gets k-th most significant bit from x and y and concatenate the two bits, first x bit and then y bit.
     * Return the concatenated two bits in a byte.
     * Example: 
     * input: x = 11010 (in binary), y = 10111 (in binary), kThMSB = 2, bitLength = 5
     * output: 00000010 (where 1 and 0 came from the second bit of x and y from msb, respectively. 
     */
    public static int concatenateKthMSBs(long x, long y, int kThMSB /* MostSignificantBit */, int bitLength) {
        int result = 0;
        long mask = 1L << (bitLength - kThMSB);
        if ((mask & x) != 0) {
            result = 2;
        }
        if ((mask & y) != 0) {
            ++result;
        }
        return result;
    }

    /*
     * This method gets 2 bits taken from the value starting from k-th MSB
     * Warning: caller must make sure that kThMSB+1 <= bitLength
     * Example:
     * input: value = 11010 (in binary), kThMSB = 3, bitLength = 5
     * output: 01
     */
    public static int get2BitsFromKthMSB(long value, int kThMSB, int bitLength) {
        int result = 0;
        long mask = 1L << (bitLength - kThMSB - 1);
        if ((mask & value) != 0) {
            result = 1;
        }
        mask <<= 1;
        if ((mask & value) != 0) {
            result += 2;
        }
        return result;
    }
}

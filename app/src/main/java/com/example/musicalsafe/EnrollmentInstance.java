package com.example.musicalsafe;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentInstance {

    //hopefully introduce RNN SNN (peter colab?)

    int clickIndex = 0;
    private static final int allowedRange = 100; //possibale security raising by making dynamic to sec settings of the user?
    private static final float allowedError = 0.75F;
    private String enrollmentLine = null;
    private List<Integer> timeBetweenClicks = new ArrayList<Integer>();

    //called by reading from enrol.txt
    public EnrollmentInstance(String inEnrollmentLine) {
        enrollmentLine = inEnrollmentLine;
    }

    //called by used attempted auth
    public EnrollmentInstance(List<PressRecord> inPressRecords) {
        for (PressRecord pressRecord : inPressRecords)
            timeBetweenClicks.add((int) pressRecord.getElapsedTime());

        enrollmentLine = makeEncryptionKey();
    }

    public void addTimeBetweenClick(int inTimeBetweenClick) {
        timeBetweenClicks.add(inTimeBetweenClick);
    }

    //rule -> reduce num variations
    //current bouts is +/- 100 means 2^n possibilities
    //rue set to reduce computations
    //Rule x < 100 -> alphabet number, modulus
    //Rule x > 100 -> take the 1 and add to previous rule

    char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'}; //this is bad!! redo!!!!!!!

    private String ruleOnUnderMark(int inClickBetween) { //should never be more that three chars in string, see underTime below
        //String alphaPart = String.valueOf(inStringRep.charAt(1) + inStringRep.charAt(2));
        int ones = (inClickBetween / 1) % 10;
        int tens = (inClickBetween / 10) % 10;
        int cVal = tens + ones;

        double rem = (cVal % chars.length);
        return String.valueOf(chars[(int)Math.floor(rem)]); //unnecessary cast
    }

    //derivation of Lucas's range idea (reduce range of possibilities)
    public String makeEncryptionKey() { // check for > 1000? !!
        String retKey = "";

        for (int clickBetweenTime : timeBetweenClicks) {
            int underTime = clickBetweenTime;
            while (underTime > 1000) { //see function heading, need third value
                underTime -= 100; //another security setting modification? (100 arbitrary atm)
            }

            //numRep = String.valueOf(underTime);
            if (clickBetweenTime > allowedRange) {
                int hundreds = (underTime / 100) % 10;
                retKey += String.valueOf(hundreds);
            }

            retKey += ruleOnUnderMark(underTime);
        }

        return retKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        EnrollmentInstance that = (EnrollmentInstance) o;
        String thatEnrollmentLine = that.getEnrollmentLine();
        if (this.enrollmentLine.length() != thatEnrollmentLine.length()) {
            return false;
        }

        int numWrong = 0;
        for (int i = 0; i < this.enrollmentLine.length(); i++) {
            if (this.enrollmentLine.charAt(i) != thatEnrollmentLine.charAt(i))
                numWrong += 1;
        }

        if ((float)numWrong / (float)this.enrollmentLine.length() > allowedError) {
            return false;
        }

        return true;
    }

    public int getNumClicks() {
        return timeBetweenClicks.size();
    }

    public int getNextClick() {
        clickIndex += 1;
        if (clickIndex == timeBetweenClicks.size() - 1)
            return -1;

        return timeBetweenClicks.get(clickIndex - 1);
    }

    public String getEnrollmentLine() {
        if (enrollmentLine == null) {
            enrollmentLine = makeEncryptionKey();
        }

        return enrollmentLine;
    }
}

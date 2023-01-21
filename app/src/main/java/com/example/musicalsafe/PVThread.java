package com.example.musicalsafe;

import java.security.MessageDigest;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class PVThread extends Thread implements Callable {

    private int threadId;
    private int numberOfThreads;
    private String fileHash;
    boolean hashFound = false;
    private EnrollmentInstance testInstance;
    private static final int allowedRange = 100;

    AtomicBoolean foundPasswordValue;

    public PVThread(AtomicBoolean inFoundPasswordValue,
                    EnrollmentInstance inTestInstance, String inHashed,
                    int inThreadId, int inNumberOfThreads) {
        foundPasswordValue = inFoundPasswordValue;
        testInstance = inTestInstance;
        fileHash = inHashed;
        threadId = inThreadId;
        numberOfThreads = inNumberOfThreads;
    }

    //key possiility generation
    //take in enrollment instance
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
    public String makeEncryptionKey(int inClickBetween) { // check for > 1000? !!
        String retKey = "";

        int underTime = inClickBetween;
        while (underTime > 1000) { //see function heading, need third value
            underTime -= 100; //another security setting modification? (100 arbitrary atm)
        }

        //numRep = String.valueOf(underTime);
        if (inClickBetween > allowedRange) {
            int hundreds = (underTime / 100) % 10;
            retKey += String.valueOf(hundreds);
        }

        retKey += ruleOnUnderMark(underTime);
        return retKey;
    }

    private void genPermutations(int inLowerBound, int inHigherBound, String priorChunk, int inIndex) {
        if (hashFound || inIndex < 0)
            return;

        if (!priorChunk.equals("")) {
            if (fileHash.equals(hashKey(priorChunk))) {
                hashFound = true;
                return;
            }
        }

        int ms = testInstance.getNextClick();
        if (ms ==  -1){
            return;
        }

        for (int i = (ms - inLowerBound); i < (ms + inHigherBound); i++) {
            genPermutations(
                    inLowerBound,
                    inHigherBound,
                    priorChunk + makeEncryptionKey(ms),
                    inIndex - 1
            );
        }
    }

    //https://stackoverflow.com/questions/5531455/how-to-hash-some-string-with-sha-256-in-java
    private String hashKey(String inCandidateHash) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(inCandidateHash.getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void run() {
        //will always be in twos, no decimal needed
        int lowerBound = (200 * (threadId / numberOfThreads)) - 100; //separate work for threads
        int higherBound = (200 * ((threadId + 1) / numberOfThreads)) - 100;

        genPermutations(lowerBound, higherBound, "", testInstance.getNumClicks());
    }

    @Override
    public String call(){
        return "found";
    }
}

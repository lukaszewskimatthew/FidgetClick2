package com.example.musicalsafe;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO: Add checks for bad passwords
//rapid clicking, sort, simple beat that kind of thing


public class PressAuthenticator {

    private static final String beginInstance = "/se";
    private static final String endInstance = "/ee";

    Context applicationContext;
    private static final int numberOfThreads = 4;
    private static final String enrolFile = "enrol.txt";
    private List<PressRecord> pressRecords = new ArrayList<>();
    List<EnrollmentInstance> enrollmentInstances = new ArrayList<>();

    public PressAuthenticator(Context inApplicationContext) {
        applicationContext = inApplicationContext;
    }

    //Wait have user input several times -> have better idea
    //Hash the averages -> comp the averages
    //store to a degree of percision, make computaion easte
    //Best idea is a RNN classification implementation
    //Would need data gathering phases
    //Original idea, make hash of tone
    private String makeEnrollmentLine() { //TODO, restart PressAuthenticator per enrollment instance
        //for now we have plain text
//        String storeString = beginInstance + "\n";
//        for (PressRecord pressRecord : pressRecords) {
//            storeString += (String.valueOf(pressRecord.getElapsedTime()) + "\n");
//        }
//
//        return (storeString + endInstance + "\n");

        EnrollmentInstance addInstance = new EnrollmentInstance(pressRecords);
        return  addInstance.makeEncryptionKey() + "\n";
    }

    //need to make line by line recording with tags, modify read method as well
    public boolean saveClickEnrollment() {
        File enrollmentFile = null;
        OutputStreamWriter enrolWriter = null;

        try {
            //File directory = applicationContext.getAssets();
            enrollmentFile = new File(String.valueOf(applicationContext.getFilesDir().toString()), enrolFile);

            Toast.makeText(applicationContext, applicationContext.getFilesDir().toString(), Toast.LENGTH_SHORT).show();
            if (!enrollmentFile.exists()) {
                try {
                    enrollmentFile.createNewFile(); //Error here TODO
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            boolean testingOverwriteFlag = false; //controls append mode
            FileOutputStream fileOutputStream = new FileOutputStream(enrollmentFile, testingOverwriteFlag);
            enrolWriter = new OutputStreamWriter(fileOutputStream);
            enrolWriter.append(makeEnrollmentLine());

        } catch (IOException e) {
            Toast.makeText(applicationContext, "Error opening enrollment write", Toast.LENGTH_SHORT).show();
            return false; // not return false here.

        } finally {
            if (enrollmentFile != null) {
                try {
                    if (enrolWriter != null) {
                        enrolWriter.flush();
                        enrolWriter.close();
                    }

                } catch (IOException e) {
                    //log the exception
                }
            }

            return true;
        }
    }

    public boolean readClickEnrollment() {
        int numLineRead = 0;
        BufferedReader enrolReader = null;

        try {
            enrolReader = new BufferedReader( //not sure if O like this new format?
                    new InputStreamReader(
                            new FileInputStream(
                                    new File(
                                            String.valueOf(applicationContext.getFilesDir().toString()),
                                            enrolFile)
                            )
                    )
            );

            String enrolLine;
            EnrollmentInstance enrollmentInstance = null;

            //parsing enrol.txt file for enrollment instances
            while ((enrolLine = enrolReader.readLine()) != null) {
                numLineRead += 1;
                enrolLine.replace("\n", "").replace("\r", "");

                enrollmentInstance = new EnrollmentInstance(enrolLine);
                enrollmentInstances.add(enrollmentInstance);
//                if (enrolLine.equals(beginInstance)) { //if start instance
//                    enrollmentInstance = new EnrollmentInstance();
//                    continue;
//                }
//
//                if (enrolLine.equals(endInstance)) { //if end instance
//                    enrollmentInstances.add(enrollmentInstance);
//                    enrollmentInstance = null;
//                    continue;
//                }
//
//                if (enrollmentInstance != null) { //inter-instance
//                    enrollmentInstance.addTimeBetweenClick(Integer.valueOf(enrolLine));
//                }
            }

            Toast.makeText(applicationContext, String.valueOf(numLineRead), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(applicationContext, "Error opening enrollment for read", Toast.LENGTH_SHORT).show();
            return false; // not return false here.
        } finally {
            if (enrolReader != null) {
                try {
                    enrolReader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
            return true;
        }
    }

    private boolean threadsAlive(List<PVThread> inPVThreads) {
        for (PVThread pVThread : inPVThreads) {
            if (pVThread.isAlive())
                return true;
        }

        return false;
    }

    public boolean authEnrollmentInstance() {
        if (enrollmentInstances.size() == 0) { //TODO: tell the user there is none.
            //pass for now,
        }

        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(numberOfThreads);
            ExecutorCompletionService<String> completionService =
                    new ExecutorCompletionService<>(executorService);

            AtomicBoolean foundPasswordValue = new AtomicBoolean(false);
            List<PVThread> pvThreads = new ArrayList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                pvThreads.add(
                        new PVThread(
                            foundPasswordValue,
                            enrollmentInstances.get(0),
                            "needs to be string hash",
                            i,
                            numberOfThreads));
                pvThreads.get(i).start();
            }

            while (foundPasswordValue.get() && threadsAlive(pvThreads)) {
                //pass
            }

            String showValue;
            if (foundPasswordValue.get())
                showValue = "Authorized";
            else
                showValue = "Invalid";

            Toast.makeText(applicationContext, showValue, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            //what to do here, exception is too generic
        } finally {
            if (executorService != null)
                executorService.shutdown();
        }

        return false;
    }

    //getters, setters, additional helper methods
    public void addPressRecord(PressRecord inPressRecord) {
        pressRecords.add(inPressRecord);
    }

    public void updateLastPress(long inEndTime) {
        pressRecords.get(pressRecords.size() - 1).endTime(inEndTime);
    }

    public int getNumPresses(){
        return pressRecords.size();
    }

    public long calcAveTimeToClick() {
        long retAverage = 0;

        for (int i = 1; i <= pressRecords.size(); i++) {
            retAverage = ((retAverage + pressRecords.get(i - 1).getElapsedTime()) / i);
        }

        return retAverage;
    }
}

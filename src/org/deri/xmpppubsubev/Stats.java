/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.deri.xmpppubsubev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import org.apache.log4j.Logger;

/**
 *
 * @author duy
 */
public class Stats {

    static Logger logger = Logger.getLogger(Stats.class);

    public long avg(HashSet<Long> set) {
        long sum = 0L;
        long avg = 0L;
        for(long row : set) { sum += row; }
        avg = sum/set.size();
        return avg;
    }

    public void processCSVs(String inputFileName, String numberClients,
            String outputFileName) throws IOException {
//        ArrayList<String> numbers = new ArrayList<String>();
        File file = new File(inputFileName);
        BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
        String line = null;

        String columns[];
        HashSet<Long> tPubs = null;
        HashSet<Long> tMsgs = null;
        HashSet<Long> tSubs = null;
        HashSet<Long> tTotal = null;
        long avgTTotal, avgTPub, avgTSub, avgTMsg, maxTTotal, maxTPub, maxTSub,
                maxTMsg, minTTotal, minTPub, minTSub, minTMsg, sum;
        int nPubs, nSubs, nTriples;
        while((line = bufRdr.readLine()) != null) {
//            StringTokenizer st = new StringTokenizer(line,",");
            columns = line.split(",");
            tPubs.add(Long.parseLong(columns[columns.length -4]));
            tMsgs.add(Long.parseLong(columns[columns.length -3]));
            tSubs.add(Long.parseLong(columns[columns.length -2]));
            tTotal.add(Long.parseLong(columns[columns.length -1]));
        }
        bufRdr.close();
        
        columns = line.split(",");
        nSubs = Integer.parseInt(columns[2]);
        nPubs = Integer.parseInt(columns[3]);
        nTriples = Integer.parseInt(columns[4]);

        avgTTotal = avg(tTotal);
        avgTPub = avg(tPubs);
        avgTSub = avg(tSubs);
        avgTMsg = avg(tMsgs);
        maxTTotal = Collections.max(tTotal);
        maxTPub = Collections.max(tPubs);
        maxTSub = Collections.max(tSubs);
        maxTMsg = Collections.max(tMsgs);
        maxTTotal = Collections.min(tTotal);
        maxTPub = Collections.min(tPubs);
        maxTSub = Collections.min(tSubs);
        maxTMsg = Collections.min(tMsgs);
        logger.info("average: " + avgTTotal);
        FileWriter writer = new FileWriter(outputFileName, true);
        writer.append(numberClients);
        writer.append(',');
        writer.append(Long.toString(avgTTotal));
        writer.append('\n');
        writer.flush();
        writer.close();

    }
}

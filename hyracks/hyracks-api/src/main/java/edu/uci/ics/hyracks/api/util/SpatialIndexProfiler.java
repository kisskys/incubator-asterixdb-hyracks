package edu.uci.ics.hyracks.api.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SpatialIndexProfiler {
    public static final SpatialIndexProfiler INSTANCE = new SpatialIndexProfiler();
    public ExperimentProfiler shbtreeNumOfSearchPerQuery;
    public ExperimentProfiler dhbtreeNumOfSearchPerQuery;
    public ExperimentProfiler rtreeNumOfSearchPerQuery;
    public ExperimentProfiler sifNumOfSearchPerQuery;
    public ExperimentProfiler falsePositivePerQuery;
    public ExperimentProfiler cacheMissPerQuery;
    public ExperimentProfiler pidxSearchTimePerQuery;
    public ExperimentProfiler pidxInsertCount;
    public ExperimentProfiler sidxInsertCount;
    public ExperimentProfiler entityCommitLogCount;

    private SpatialIndexProfiler() {
        if (ExperimentProfiler.PROFILE_MODE) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
            shbtreeNumOfSearchPerQuery = new ExperimentProfiler("shbtreeNumOfSearchPerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            shbtreeNumOfSearchPerQuery.begin();
            dhbtreeNumOfSearchPerQuery = new ExperimentProfiler("dhbtreeNumOfSearchPerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            dhbtreeNumOfSearchPerQuery.begin();
            rtreeNumOfSearchPerQuery = new ExperimentProfiler("rtreeNumOfSearchPerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            rtreeNumOfSearchPerQuery.begin();
            sifNumOfSearchPerQuery = new ExperimentProfiler("sifNumOfSearchPerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            sifNumOfSearchPerQuery.begin();
            
            falsePositivePerQuery = new ExperimentProfiler("falsePositivePerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            falsePositivePerQuery.begin();
            cacheMissPerQuery = new ExperimentProfiler("cacheMissPerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            cacheMissPerQuery.begin();
            pidxSearchTimePerQuery = new ExperimentProfiler("pidxSearchTimePerQuery-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            pidxSearchTimePerQuery.begin();
            pidxInsertCount = new ExperimentProfiler("pidxInsertCount-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            pidxInsertCount.begin();
            sidxInsertCount = new ExperimentProfiler("sidxInsertCount-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            sidxInsertCount.begin();
            entityCommitLogCount = new ExperimentProfiler("entityCommitLogCount-" + sdf.format(Calendar.getInstance().getTime()) + ".txt", 1);
            entityCommitLogCount.begin();
        }
    }
}

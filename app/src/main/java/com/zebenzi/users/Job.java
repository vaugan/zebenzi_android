package com.zebenzi.users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by vaugan.nayagar on 2015/09/06.
 */
public class Job {
    private JSONObject job;

    //TODO: getTotalJobs, getPendingJobs, getCompletedJobs, getCancelledJobs etc.

    // Constructor to convert JSON object into a Java class instance
    public Job(JSONObject object){
        this.job = object;
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<Job> fromJson(JSONArray jsonObjects) {
        ArrayList<Job> jobs = new ArrayList<Job>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                jobs.add(new Job(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jobs;
    }

    public String getWorkerName() {
        String workerName = "";
        try {
            workerName = job.getJSONObject("worker").getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return workerName;
    }

    public String getJobId() throws JSONException {
        return job.getString("jobid");
    }

    public String getJobStatus() throws JSONException {
        return job.getString("jobStatus");
    }
    public String getJobStartDate() throws JSONException {
        return job.getString("jobCreatedDate");
    }
    public String getJobCompletedDate() throws JSONException {
        return job.getString("jobUpdatedDate");
    }
    public String getJobRating() throws JSONException {
        return job.getString("rating");
    }

    public String getWorkerId() throws JSONException {
            return job.getJSONObject("worker").getString("id");
    }

    public String getJobServiceId() throws JSONException {
        return job.getJSONObject("jobService").getString("serviceId");
    }

    public String getJobServiceName(){
        String jobServiceName = "";
        try {
            jobServiceName = job.getJSONObject("jobService").getString("serviceName");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return jobServiceName;
    }
}
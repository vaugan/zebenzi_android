package com.zebenzi.ui;

/**
 * Created by Vaugan.Nayagar on 2015/09/29.
 */
public enum FragmentsLookup {

    ACCOUNT("Account", 1),
    HISTORY("History", 2),
    LOGIN("Login", 3),
    JOB("Job", 4),
    REGISTER("Register", 5),
    SEARCH("Search", 6);


    private final String name;
    private final int id;

        private FragmentsLookup(String name, int id) {
            this.name = name;
           this.id = id;
        }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

}

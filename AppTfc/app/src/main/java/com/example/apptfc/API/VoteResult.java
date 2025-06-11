package com.example.apptfc.API;

public class VoteResult {
    private int inFavor;
    private int against;
    private int total;

    public int getInFavor() {
        return inFavor;
    }

    public void setInFavor(int inFavor) {
        this.inFavor = inFavor;
    }

    public int getAgainst() {
        return against;
    }

    public void setAgainst(int against) {
        this.against = against;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}

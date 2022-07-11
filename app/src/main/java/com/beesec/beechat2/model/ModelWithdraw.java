package com.beesec.beechat2.model;
@SuppressWarnings("ALL")
public class ModelWithdraw {

    String id,amount,way,type;

    public ModelWithdraw() {
    }

    public ModelWithdraw(String id, String amount, String way, String type) {
        this.id = id;
        this.amount = amount;
        this.way = way;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getWay() {
        return way;
    }

    public void setWay(String way) {
        this.way = way;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

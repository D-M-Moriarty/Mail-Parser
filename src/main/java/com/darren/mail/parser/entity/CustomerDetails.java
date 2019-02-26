package com.darren.mail.parser.entity;


public class CustomerDetails {

    private String custNo;
    private String email;

    public CustomerDetails() {}

    public CustomerDetails(String custNo, String email) {
        this.custNo = custNo;
        this.email = email;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
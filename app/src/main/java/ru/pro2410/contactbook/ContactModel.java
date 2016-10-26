package ru.pro2410.contactbook;

import java.util.ArrayList;



public class ContactModel {

    public ArrayList<Integer> groupList = new ArrayList<>();
    public String name;
    public String address;
    public String tel;

    public ContactModel(int idGroupList, String name, String address, String tel) {
        this.groupList.add(idGroupList);
        this.name = name;
        this.address = address;
        this.tel = tel;
    }
}

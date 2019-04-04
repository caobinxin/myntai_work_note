package com.myntai.slightech.myntairobotromupdateservice.tx2.Cloud.Download;

public class ManageDownLoad {
    Tx2AppUpdate tx2AppUpdate;
    static private ManageDownLoad instance ;

    static public ManageDownLoad getInstance() {
        if( null == instance){
            instance = new ManageDownLoad();
        }
        return instance;
    }

    private ManageDownLoad(){

    }

    public void setTx2AppUpdate(Tx2AppUpdate tx2AppUpdate) {
        this.tx2AppUpdate = tx2AppUpdate;
    }
}

package com.myntai.slightech.myntairobotromupdateservice.Firefly3399.download;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.ArrayList;
import java.util.List;

public class AppUpdate implements Parcelable {

    @JSONField(name = "version_name")
    private String versionName;
    @JSONField(name = "has_new_version")
    private boolean hasNewVersion;
    @JSONField(name = "download_url")
    private String downloadUrl;
    @JSONField(name = "release_note")
    private String updateDescription;
    @JSONField(name = "update_firmware_url")
    private List<String> fireflyFirmware = new ArrayList<>();
    @JSONField(name = "full_firmware_url")
    private String fullFirmwareRrl;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    public String getFullFirmwareRrl() {
        return fullFirmwareRrl;
    }
    public void setFullFirmwareRrl(String fullFirmwareRrl) {
        this.fullFirmwareRrl = fullFirmwareRrl;
    }

    public boolean isHasNewVersion() {
        return hasNewVersion;
    }

    public void setHasNewVersion(boolean hasNewVersion) {
        this.hasNewVersion = hasNewVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(String updateDescription) {
        this.updateDescription = updateDescription;
    }

    public List<String> getFireflyFirmware() {
        return fireflyFirmware;
    }

    public void setFireflyFirmware(List<String> fireflyFirmware) {
        this.fireflyFirmware = fireflyFirmware;
    }


    @Override
    public String toString() {
        return "AppUpdate{" +
                "versionName='" + versionName + '\'' +
                ", hasNewVersion=" + hasNewVersion +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", updateDescription='" + updateDescription + '\'' +
                ", fireflyFirmware=" + fireflyFirmware + '\'' +
                ", fireflyFirmware=" + fullFirmwareRrl +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.versionName);
        dest.writeByte(this.hasNewVersion ? (byte) 1 : (byte) 0);
        dest.writeString(this.downloadUrl);
        dest.writeString(this.updateDescription);
        dest.writeString(this.fullFirmwareRrl);
    }

    public AppUpdate() {
    }

    protected AppUpdate(Parcel in) {
        this.versionName = in.readString();
        this.hasNewVersion = in.readByte() != 0;
        this.downloadUrl = in.readString();
        this.updateDescription = in.readString();
        this.fullFirmwareRrl = in.readString();
    }

    public static final Parcelable.Creator<AppUpdate> CREATOR = new Parcelable.Creator<AppUpdate>() {
        @Override
        public AppUpdate createFromParcel(Parcel source) {
            return new AppUpdate(source);
        }

        @Override
        public AppUpdate[] newArray(int size) {
            return new AppUpdate[size];
        }
    };
}

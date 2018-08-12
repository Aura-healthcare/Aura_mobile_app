/**
 * @file SingleChoice
 * @author clecoued <clement.lecouedic@aura.healthcare>
 *
 *
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2018 Aura Healthcare
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 *
 * @section DESCRIPTION
 *
 *
 */
package com.wearablesensor.aura.seizure_report;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

public class SingleChoice implements Parcelable {
    private int mID;
    private String mHeadline;
    private String mDescription;
    private int mButtonIconSelector;
    private String mValue;

    /**
     * @brief constructor
     *
     * @param iHeadline choice headline
     * @param iDescription choice longer description
     * @param iButtonIconSelector choice icon representation (selected/not selected)
     * @param iValue choice value used for data processing
     */
    public SingleChoice(String iHeadline, String iDescription, int iButtonIconSelector, String iValue) {
        mID = View.generateViewId();
        mHeadline = iHeadline;
        mDescription = iDescription;
        mButtonIconSelector = iButtonIconSelector;
        mValue = iValue;
    }

    protected SingleChoice(Parcel in) {
        mID = in.readInt();
        mHeadline = in.readString();
        mDescription = in.readString();
        mButtonIconSelector = in.readInt();
        mValue = in.readString();
    }

    public static final Creator<SingleChoice> CREATOR = new Creator<SingleChoice>() {
        @Override
        public SingleChoice createFromParcel(Parcel in) {
            return new SingleChoice(in);
        }

        @Override
        public SingleChoice[] newArray(int size) {
            return new SingleChoice[size];
        }
    };

    public int getId(){
        return mID;
    }

    public String getHeadline(){
        return mHeadline;
    }

    public String getDescription(){
       return mDescription;
    }

    public int getButtonIconSelector(){
        return mButtonIconSelector;
    }

    public String getValue(){
        return mValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mHeadline);
        dest.writeString(mDescription);
        dest.writeInt(mButtonIconSelector);
        dest.writeString(mValue);
    }
}

/**
 * @file SingleChoiceList
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

import java.util.ArrayList;

public class SingleChoiceList implements Parcelable{
    private ArrayList<SingleChoice> mChoices;

    public SingleChoiceList(){
        mChoices = new ArrayList<>();
    }

    protected SingleChoiceList(Parcel in) {
        mChoices = new ArrayList<>();
        in.readTypedList(mChoices, SingleChoice.CREATOR);
    }

    public static final Creator<SingleChoiceList> CREATOR = new Creator<SingleChoiceList>() {
        @Override
        public SingleChoiceList createFromParcel(Parcel in) {
            return new SingleChoiceList(in);
        }

        @Override
        public SingleChoiceList[] newArray(int size) {
            return new SingleChoiceList[size];
        }
    };

    public void addChoice(SingleChoice iChoice){
        mChoices.add(iChoice);
    }

    public ArrayList<SingleChoice> getList(){
        return mChoices;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mChoices);
    }

    /**
     * @brief get choice from button view id
     *
     * @param iId button view id
     * @return selected choice
     */
    public SingleChoice getChoiceFromId(int iId){
        for(SingleChoice lChoice : mChoices){
            if(lChoice.getId() == iId){
                return lChoice;
            }
        }

        return null;
    }
}

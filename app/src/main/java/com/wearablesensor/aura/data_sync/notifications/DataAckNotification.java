/**
 * @file DataAckNotification
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
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
 * DataAckNotification  class
 *
 */

package com.wearablesensor.aura.data_sync.notifications;

import com.wearablesensor.aura.data_repository.FileStorage;

public class DataAckNotification {

    private String mAckMessage;

    private String mAckStatus;
    private String mFileName;

    public DataAckNotification(String iAckMessage){
        mAckMessage = iAckMessage;

        String[] parsedMessage = mAckMessage.split("\\s:\\s");
        mAckStatus = parsedMessage[1];

        String[] parsedFileName = parsedMessage[0].split("_");
        StringBuilder builder = new StringBuilder(parsedFileName[2]);
        builder.insert(13, ":");
        builder.insert(16, ":");

        mFileName = FileStorage.getCachePhysioFilename(builder.toString());
    }

    public String getStatus(){
        return mAckStatus;
    }

    public String getFileName(){
        return mFileName;
    }
}

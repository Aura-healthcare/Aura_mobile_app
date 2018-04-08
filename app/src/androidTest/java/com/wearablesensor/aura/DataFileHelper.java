/**
 * @file DataFileHelper.java
 * @author  clecoued <clement.lecouedic@aura.healthcare>
 * @version 1.0
 *
 *
 * @section LICENSE
 *
 * Aura Mobile Application
 * Copyright (C) 2017 Aura Healthcare
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
package com.wearablesensor.aura;


import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public class DataFileHelper {

    public DataFileHelper(){

    }

    /**
     * @brief check if a file exists
     *
     * @param iFileName name of the file to be checked
     *
     * @return true if file exists, false otherwise
     */
    public boolean isFileExistAt(String iFileName){
        FileInputStream lFileStream = null;
        try {
            lFileStream =  InstrumentationRegistry.getTargetContext().openFileInput(iFileName);
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * @brief get data file list contains in the private root folder
     *
     * @return
     */
    public File[] getDataFiles(){
        File lrootFolder = InstrumentationRegistry.getTargetContext().getFilesDir();
        File[] lDataFiles = lrootFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return Pattern.matches(".+dat$", pathname.getName());
            }
        });

        return lDataFiles;
    }

    /**
     * @brief clean all data files from local storage
     */

    public void cleanPrivateFiles(){
        File lrootFolder = InstrumentationRegistry.getTargetContext().getFilesDir();
        File[] lDataFiles = getDataFiles();

        // clean all data files
        for(File lFile : lDataFiles){
            InstrumentationRegistry.getTargetContext().deleteFile(lFile.getName());
        }
    }
}
/**
 * @file DataCollectorServiceConstants
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
 */
package com.wearablesensor.aura;

public class DataCollectorServiceConstants {
    public interface ACTION {
        public static String MAIN_ACTION = "com.wearablesensor.aura.action.main";
        public static String STARTFOREGROUND_ACTION = "ccom.wearablesensor.aura.action.startforeground";
        public static String STOPFOREGROUND_ACTION = "com.wearablesensor.aura.action.stopforeground";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
        public static int HEARTBEAT_NOTIFICATION_ID = 401;
    }
}

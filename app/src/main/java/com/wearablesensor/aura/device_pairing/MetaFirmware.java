/**
 * @file MetaFirmware.java
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

package com.wearablesensor.aura.device_pairing;

import java.util.UUID;

public class MetaFirmware {

    public final static UUID META_GATT_SERVICE = UUID.fromString("326A9000-85CB-9195-D9DD-464CFBBAE75A");
    public final static UUID META_GATT_CONFIG_CHARACTERISTIC = UUID.fromString("326A9001-85CB-9195-D9DD-464CFBBAE75A");
    public final static UUID META_GATT_NOTFICATIONS_CHARACTERISTIC = UUID.fromString("326A9006-85CB-9195-D9DD-464CFBBAE75A");
    public static class Acceleration{

        public static byte[] getConfig(Acceleration.OutputDataRate iOutputDataRate, Acceleration.Range iRange){
            byte[] lAccDataConfig= new byte[] {0x28, 0x03};
            lAccDataConfig[0]&= 0xf0;
            lAccDataConfig[0]|= iOutputDataRate.ordinal()+ 1;

            lAccDataConfig[1]&= 0xf0;
            lAccDataConfig[1]|= iRange.bitmask;

            return lAccDataConfig;
        }

        public enum OutputDataRate {

            ODR_0_78125_HZ(0.78125f),
            /* 1.5625 Hz */
            ODR_1_5625_HZ(1.5625f),
            /* 3.125 Hz */
            ODR_3_125_HZ(3.125f),
            /* 6.25 Hz */
            ODR_6_25_HZ(6.25f),
            /* 12.5 Hz */
            ODR_12_5_HZ(12.5f),
            /* 25 Hz */
            ODR_25_HZ(25f),
            /* 50 Hz */
            ODR_50_HZ(50f),
            /* 100 Hz */
            ODR_100_HZ(100f),
            /* 200 Hz */
            ODR_200_HZ(200f),
            /* 400 Hz */
            ODR_400_HZ(400f),
            /* 800 Hz */
            ODR_800_HZ(800f),
            /* 1600 Hz */
            ODR_1600_HZ(1600f);

            /**
             * Frequency represented as a float value
             */
            public final float frequency;

            OutputDataRate(float frequency) {
                this.frequency = frequency;
            }
        }

        public enum Range {
            /** +/-2g */
            AR_2G((byte) 0x3, 16384f, 2f),
            /** +/-4g */
            AR_4G((byte) 0x5, 8192f, 4f),
            /** +/-8g */
            AR_8G((byte) 0x8, 4096, 8f),
            /** +/-16g */
            AR_16G((byte) 0xc, 2048f, 16f);

            public final byte bitmask;
            public final float scale, range;

            Range(byte bitmask, float scale, float range) {
                this.bitmask = bitmask;
                this.scale = scale;
                this.range= range;
            }

        }

        public enum Register{
            POWER_MODE((byte) 0x1),
            DATA_INTERRUPT_ENABLE((byte) 0x2),
            DATA_CONFIG((byte)0x3),
            DATA_INTERRUPT((byte) 0x4),
            DATA_INTERRUPT_CONFIG((byte) 0x5);

            public byte id;

            Register(byte id) {
                this.id = id;
            }
        }

    }

    public static class Gyroscope{

        public static byte[] getConfig(Gyroscope.OutputDataRate iOutputDataRate, Gyroscope.Range iRange, Gyroscope.FilterMode iFilterMode){
            byte[] lGyroDataConfig= new byte[] {(byte) (0x20 | OutputDataRate.ODR_100_HZ.bitmask), Range.FSR_2000.bitmask};

            lGyroDataConfig[1] &= 0xf8;
            lGyroDataConfig[1]|= iRange.bitmask;

            lGyroDataConfig[0] &= 0xf0;
            lGyroDataConfig[0]|= iOutputDataRate.bitmask;

            lGyroDataConfig[0] &= 0xcf;
            lGyroDataConfig[0] |= (iFilterMode.ordinal() << 4);

            return lGyroDataConfig;
        }

        public enum OutputDataRate {
            /** 25Hz */
            ODR_25_HZ,
            /** 50Hz */
            ODR_50_HZ,
            /** 100Hz */
            ODR_100_HZ,
            /** 200Hz */
            ODR_200_HZ,
            /** 400Hz */
            ODR_400_HZ,
            /** 800Hz */
            ODR_800_HZ,
            /** 1600Hz */
            ODR_1600_HZ,
            /** 3200Hz */
            ODR_3200_HZ;

            public final byte bitmask;

            OutputDataRate() {
                this.bitmask= (byte)(ordinal() + 5);
            }
        }
        /**
         * Supported angular rate measurement range
         * @author Eric Tsai
         */
        public enum Range {
            /** +/- 2000 degrees / second */
            FSR_2000(16.4f),
            /** +/- 1000 degrees / second */
            FSR_1000(32.8f),
            /** +/- 500 degrees / second */
            FSR_500(65.6f),
            /** +/- 250 degrees / second */
            FSR_250(131.2f),
            /** +/- 125 degrees / second */
            FSR_125(262.4f);

            public final float scale;
            public final byte bitmask;

            Range(float scale) {
                this.scale= scale;
                this.bitmask= (byte) ordinal();
            }
        }

        public enum FilterMode {
            OSR4,
            OSR2,
            NORMAL
        }

        public enum Register{
            POWER_MODE((byte) 0x1),
            DATA_INTERRUPT_ENABLE((byte) 0x2),
            CONFIG((byte) 0x3),
            DATA((byte) 0x5);

            public byte id;
            Register(byte id) {
                this.id = id;
            }
        }
    }

    public enum Module{
        ACCELERATION((byte) 0x3),
        GYROSCOPE((byte) 0x13);

        public byte id;

        Module(byte id) {
            this.id = id;
        }
    }

}

/**
 * Original Copyright none
 * Original code got at https://github.com/felHR85/AndSTK500
 *
 * Modified Copyright (c) 2016 University of Brescia, Alessandra Flammini, All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package it.unibs.sandroide.flasher.flashers.Arduino.stk500;

public class STK500Constants
{
    public static final int Resp_STK_OK                = 0x10  ;// 16 ' '
    public static final int Resp_STK_FAILED            = 0x11  ;// ' '
    public static final int Resp_STK_UNKNOWN           = 0x12  ;// ' '
    public static final int Resp_STK_NODEVICE          = 0x13  ;// ' '
    public static final int Resp_STK_INSYNC            = 0x14  ;// ' '
    public static final int Resp_STK_NOSYNC            = 0x15  ;// ' '

    public static final int Resp_ADC_CHANNEL_ERROR     = 0x16  ;// ' '
    public static final int Resp_ADC_MEASURE_OK        = 0x17  ;// ' '
    public static final int Resp_PWM_CHANNEL_ERROR     = 0x18  ;// ' '
    public static final int Resp_PWM_ADJUST_OK         = 0x19  ;// ' '

    ;// *****************[ STK Special constants ]***************************

    public static final int Sync_CRC_EOP               = 0x20  ;// 'SPACE'

    ;// *****************[ STK Command constants ]***************************

    public static final int Cmnd_STK_GET_SYNC          = 0x30  ;// ' '
    public static final int Cmnd_STK_GET_SIGN_ON       = 0x31  ;// ' '

    public static final int Cmnd_STK_SET_PARAMETER     = 0x40  ;// ' '
    public static final int Cmnd_STK_GET_PARAMETER     = 0x41  ;// ' '
    public static final int Cmnd_STK_SET_DEVICE        = 0x42  ;// ' '
    public static final int Cmnd_STK_SET_DEVICE_EXT    = 0x45  ;// ' '

    public static final int Cmnd_STK_ENTER_PROGMODE    = 0x50  ;// ' '
    public static final int Cmnd_STK_LEAVE_PROGMODE    = 0x51  ;// ' '
    public static final int Cmnd_STK_CHIP_ERASE        = 0x52  ;// ' '
    public static final int Cmnd_STK_CHECK_AUTOINC     = 0x53  ;// ' '
    public static final int Cmnd_STK_LOAD_ADDRESS      = 0x55  ;// ' '
    public static final int Cmnd_STK_UNIVERSAL         = 0x56  ;// ' '
    public static final int Cmnd_STK_UNIVERSAL_MULTI   = 0x57  ;// ' '

    public static final int Cmnd_STK_PROG_FLASH        = 0x60  ;// ' '
    public static final int Cmnd_STK_PROG_DATA         = 0x61  ;// ' '
    public static final int Cmnd_STK_PROG_FUSE         = 0x62  ;// ' '
    public static final int Cmnd_STK_PROG_LOCK         = 0x63  ;// ' '
    public static final int Cmnd_STK_PROG_PAGE         = 0x64  ;// ' '
    public static final int Cmnd_STK_PROG_FUSE_EXT     = 0x65  ;// ' '

    public static final int Cmnd_STK_READ_FLASH        = 0x70  ;// ' '
    public static final int Cmnd_STK_READ_DATA         = 0x71  ;// ' '
    public static final int Cmnd_STK_READ_FUSE         = 0x72  ;// ' '
    public static final int Cmnd_STK_READ_LOCK         = 0x73  ;// ' '
    public static final int Cmnd_STK_READ_PAGE         = 0x74  ;// ' '
    public static final int Cmnd_STK_READ_SIGN         = 0x75  ;// ' '
    public static final int Cmnd_STK_READ_OSCCAL       = 0x76  ;// ' '
    public static final int Cmnd_STK_READ_FUSE_EXT     = 0x77  ;// ' '
    public static final int Cmnd_STK_READ_OSCCAL_EXT   = 0x78  ;// ' '

    ;// *****************[ STK Parameter constants ]***************************

    public static final int Parm_STK_HW_VER            = 0x80  ;// ' ' - R
    public static final int Parm_STK_SW_MAJOR          = 0x81  ;// ' ' - R
    public static final int Parm_STK_SW_MINOR          = 0x82  ;// ' ' - R
    public static final int Parm_STK_LEDS              = 0x83  ;// ' ' - R/W
    public static final int Parm_STK_VTARGET           = 0x84  ;// ' ' - R/W
    public static final int Parm_STK_VADJUST           = 0x85  ;// ' ' - R/W
    public static final int Parm_STK_OSC_PSCALE        = 0x86  ;// ' ' - R/W
    public static final int Parm_STK_OSC_CMATCH        = 0x87  ;// ' ' - R/W
    public static final int Parm_STK_RESET_DURATION    = 0x88  ;// ' ' - R/W
    public static final int Parm_STK_SCK_DURATION      = 0x89  ;// ' ' - R/W

    public static final int Parm_STK_BUFSIZEL          = 0x90  ;// ' ' - R/W, Range {0..255}
    public static final int Parm_STK_BUFSIZEH          = 0x91  ;// ' ' - R/W, Range {0..255}
    public static final int Parm_STK_DEVICE            = 0x92  ;// ' ' - R/W, Range {0..255}
    public static final int Parm_STK_PROGMODE          = 0x93  ;// ' ' - 'P' or 'S'
    public static final int Parm_STK_PARAMODE          = 0x94  ;// ' ' - TRUE or FALSE
    public static final int Parm_STK_POLLING           = 0x95  ;// ' ' - TRUE or FALSE
    public static final int Parm_STK_SELFTIMED         = 0x96  ;// ' ' - TRUE or FALSE


    ;// *****************[ STK status bit definitions ]***************************

    public static final int Stat_STK_INSYNC            = 0x01  ;// INSYNC status bit, '1' - INSYNC
    public static final int Stat_STK_PROGMODE          = 0x02  ;// Programming mode,  '1' - PROGMODE
    public static final int Stat_STK_STANDALONE        = 0x04  ;// Standalone mode,   '1' - SM mode
    public static final int Stat_STK_RESET             = 0x08  ;// RESET button,      '1' - Pushed
    public static final int Stat_STK_PROGRAM           = 0x10  ;// Program button, '   1' - Pushed
    public static final int Stat_STK_LEDG              = 0x20  ;// Green LED status,  '1' - Lit
    public static final int Stat_STK_LEDR              = 0x40  ;// Red LED status,    '1' - Lit
    public static final int Stat_STK_LEDBLINK          = 0x80  ;// LED blink ON/OFF,  '1' - Blink
}

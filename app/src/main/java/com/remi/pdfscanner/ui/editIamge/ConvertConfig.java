package com.remi.pdfscanner.ui.editIamge;

public class ConvertConfig {
    public static String getConfigBright(int progress){

        //"@adjust brightness 0.5"  range [-1,1] project[-0.5 0.5]
        return " @adjust brightness "+(progress*2/100.0-1);

    }
    public static String getConfigContrast(int progress){
        //"@adjust contrast  0.5" range: intensity > 0 project [0,2]
//        If 0 < intensity < 1, the result's contrast is lower than the origin.
//        If intensity = 1, the result is the same to the origin.
//        if intensity > 1, the result's contrast is higher than the origin.
        if (progress<30){
            return " @adjust contrast "+progress/100.0;
        }else {
            return " @adjust contrast "+progress*2/100.0;
        }

    }
    public static String getConfigSaturation(int progress){
//        Param num: 1 (intensity), range: intensity >= 0.
//
//        If intensity = 0, the result is monochrome.
//                If 0 < intensity < 1, the result's saturation is lower than the origin.
//        If intensity = 1, the result is the same to the origin.
//                If intensity > 1, the result's saturation is higher than the origin.
        if (progress<30){
            return " @adjust saturation "+progress/100.0;
        }else {
            return " @adjust saturation "+progress*2/100.0;
        }
    }
    public static String getConfigExposure(int progress){
//        Param num 1 (intensity), range: [-2, 2].
        return " @adjust exposure "+(progress*4/100.0-2);
    }

    public static String getConfigSharpen(int progress){
//        Param num: 1 (intensity), range: [0, 10].
//
//        If intensity = 0, the result is the same to the origin.
//                If intensity > 0, the result is more sharp than the origin.
//
//                e.g. "@adjust sharpen 4.33 2 "
        return " @adjust sharpen "+progress/10;
    }
    public static String getConfigVignette(int progress){
//        vignette format: "@vignette low range centerX centerY"
//        Note: centerX centerY is not necessary. (Default 0.5)
//
//        e.g. "@vignette 0.1 0.9" , "@vignette 0.1 0.9 0.5 0.5"
        return " @vignette "+(1-progress/100.0)+" "+progress/100.0+" 0.5 0.5";
    }
    public static String getConfigMulti(int bright, int contrast, int saturation){
        return getConfigBright(bright)+getConfigContrast(contrast)+getConfigSaturation(saturation);
//                +getConfigExposure(exposure)+getConfigSharpen(sharpen)+getConfigVignette(vignette);
    }
}
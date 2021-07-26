package org.jeecg.modules.activiti.util;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 图片转换工具
 *
 * @author: dongjb
 * @date: 2021/07/06
 */
public class ImageUtil {

    public static void convertToPng(String svgCode, String pngFilePath) {
        File file = new File(pngFilePath);
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            convertToPng(svgCode, file);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void convertToPng(String svgCode, File filePng) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePng);
            byte[] bytes = svgCode.getBytes(StandardCharsets.UTF_8);
            PNGTranscoder t = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(bytes));
            TranscoderOutput output = new TranscoderOutput(outputStream);
            t.transcode(input, output);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
